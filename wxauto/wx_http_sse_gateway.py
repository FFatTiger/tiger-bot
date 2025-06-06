# wx_http_sse_gateway.py

import asyncio
import websockets
import json
import logging
import time
import os
import tempfile
import requests
from typing import Set, Optional
from fastapi import FastAPI, HTTPException, File, UploadFile, Form, Request
from pydantic import BaseModel, HttpUrl
from contextlib import asynccontextmanager
import uvicorn
import threading
from Mwxauto.wxauto import WeChat
# --- 配置 ---
WEBSOCKET_HOST = "0.0.0.0"
WEBSOCKET_PORT = 8765
HTTP_HOST = "0.0.0.0"
HTTP_PORT = 8000
LOG_LEVEL = logging.INFO
WECHAT_POLL_INTERVAL = 1  # 秒，检查微信新消息的间隔
TEMP_DIR = tempfile.gettempdir()
MAX_FILE_SIZE = 100 * 1024 * 1024  # 100MB
DOWNLOAD_TIMEOUT = 30


# --- 全局变量 ---
wx_instance = None
websocket_clients: Set[websockets.WebSocketServerProtocol] = set()

# --- 日志设置 ---
logging.basicConfig(level=LOG_LEVEL, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("WXHttpSSEGateway")

# --- Pydantic 模型 ---
class SendTextMessageRequest(BaseModel):
    to_who: str
    text_content: str

class SendFileByPathRequest(BaseModel):
    to_who: str
    filepath: str

class SendFileByUrlRequest(BaseModel):
    to_who: str
    file_url: HttpUrl
    filename: Optional[str] = None  # 可选的文件名，如果不提供则从URL推断

class AddListenChatRequest(BaseModel):
    who: str
    savepic: bool = True
    savevoice: bool = True
    parse_links: bool = True

class ChatWithRequest(BaseModel):
    who: str

class VoiceCallRequest(BaseModel):
    user_id: str

class APIResponse(BaseModel):
    success: bool
    message: str
    data: dict = None


# --- WebSocket 客户端管理 ---
async def register_websocket_client(websocket):
    websocket_clients.add(websocket)
    logger.info(f"New WebSocket client connected: {websocket.remote_address}. Total clients: {len(websocket_clients)}")
    
    # 发送连接确认消息
    await websocket.send(json.dumps({
        "event_type": "connected",
        "message": "WebSocket connection established",
        "timestamp": int(time.time() * 1000)
    }))

async def unregister_websocket_client(websocket):
    if websocket in websocket_clients:
        websocket_clients.remove(websocket)
    logger.info(f"WebSocket client disconnected: {websocket.remote_address}. Total clients: {len(websocket_clients)}")

async def broadcast_to_websocket_clients(message_payload):
    if not websocket_clients:
        return
    
    message_json = json.dumps(message_payload)
    clients_to_remove = []
    
    for client in websocket_clients.copy():
        try:
            await client.send(message_json)
        except websockets.exceptions.ConnectionClosed:
            clients_to_remove.append(client)
        except Exception as e:
            logger.error(f"Error sending message to WebSocket client: {e}")
            clients_to_remove.append(client)
    
    # 移除失效的客户端
    for client in clients_to_remove:
        await unregister_websocket_client(client)

# --- 文件处理辅助函数 ---
async def download_file_from_url(url: str, filename: Optional[str] = None) -> str:
    """从URL下载文件到临时目录，返回文件路径"""
    try:
        logger.info(f"Downloading file from URL: {url}")
        
        # 发送请求下载文件
        response = requests.get(str(url), timeout=DOWNLOAD_TIMEOUT, stream=True)
        response.raise_for_status()
        
        # 确定文件名
        if not filename:
            # 尝试从URL推断文件名
            filename = url.split('/')[-1]
            if '?' in filename:
                filename = filename.split('?')[0]
            if not filename or '.' not in filename:
                # 如果无法推断，使用时间戳作为文件名
                import time
                content_type = response.headers.get('content-type', '')
                ext = ''
                if 'image' in content_type:
                    if 'jpeg' in content_type or 'jpg' in content_type:
                        ext = '.jpg'
                    elif 'png' in content_type:
                        ext = '.png'
                    elif 'gif' in content_type:
                        ext = '.gif'
                    else:
                        ext = '.jpg'  # 默认
                elif 'video' in content_type:
                    ext = '.mp4'
                elif 'audio' in content_type:
                    ext = '.mp3'
                else:
                    ext = '.bin'  # 默认二进制文件
                filename = f"downloaded_{int(int(time.time() * 1000))}{ext}"
        
        # 检查文件大小
        content_length = response.headers.get('content-length')
        if content_length and int(content_length) > MAX_FILE_SIZE:
            raise ValueError(f"File too large: {content_length} bytes (max: {MAX_FILE_SIZE} bytes)")
        
        # 保存到临时文件
        temp_filepath = os.path.join(TEMP_DIR, f"wx_download_{int(int(time.time() * 1000))}_{filename}")
        
        with open(temp_filepath, 'wb') as f:
            downloaded_size = 0
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    downloaded_size += len(chunk)
                    if downloaded_size > MAX_FILE_SIZE:
                        f.close()
                        os.remove(temp_filepath)
                        raise ValueError(f"File too large: {downloaded_size} bytes (max: {MAX_FILE_SIZE} bytes)")
                    f.write(chunk)
        
        logger.info(f"File downloaded successfully: {temp_filepath} ({downloaded_size} bytes)")
        return temp_filepath
        
    except requests.RequestException as e:
        logger.error(f"Error downloading file from URL: {e}")
        raise ValueError(f"Failed to download file: {str(e)}")
    except Exception as e:
        logger.error(f"Error processing downloaded file: {e}")
        raise ValueError(f"Failed to process downloaded file: {str(e)}")

async def save_uploaded_file(file: UploadFile) -> str:
    """保存上传的文件到临时目录，返回文件路径"""
    try:
        logger.info(f"Saving uploaded file: {file.filename} (content-type: {file.content_type})")
        
        # 检查文件大小
        content = await file.read()
        if len(content) > MAX_FILE_SIZE:
            raise ValueError(f"File too large: {len(content)} bytes (max: {MAX_FILE_SIZE} bytes)")
        
        # 保存到临时文件
        safe_filename = file.filename or f"upload_{int(int(time.time() * 1000))}.bin"
        # 移除文件名中的危险字符
        safe_filename = "".join(c for c in safe_filename if c.isalnum() or c in '._-')
        temp_filepath = os.path.join(TEMP_DIR, f"wx_upload_{int(int(time.time() * 1000))}_{safe_filename}")
        
        with open(temp_filepath, 'wb') as f:
            f.write(content)
        
        logger.info(f"File saved successfully: {temp_filepath} ({len(content)} bytes)")
        return temp_filepath
        
    except Exception as e:
        logger.error(f"Error saving uploaded file: {e}")
        raise ValueError(f"Failed to save uploaded file: {str(e)}")

def cleanup_temp_file(filepath: str):
    """清理临时文件"""
    try:
        if os.path.exists(filepath) and filepath.startswith(TEMP_DIR):
            os.remove(filepath)
            logger.info(f"Cleaned up temporary file: {filepath}")
    except Exception as e:
        logger.warning(f"Failed to cleanup temporary file {filepath}: {e}")

# --- 辅助函数：使消息对象可JSON序列化 ---
def _sanitize_msg_obj_for_json(msg_obj):
    """严格只输出 wxauto 官方文档字段，保证 type 字段一定存在，去除多余字段。"""
    official_fields = ["type", "content", "sender", "info", "id", "time", "sender_remark"]
    result = {}

    for field in official_fields:
        value = None
        if hasattr(msg_obj, field):
            value = getattr(msg_obj, field, None)
        elif isinstance(msg_obj, dict):
            value = msg_obj.get(field, None)
        
        if field == "type" and value is None:
            value = getattr(msg_obj, "__dict__", {}).get("type", None)
        if value is not None:
            result[field] = value

    return result

async def _sanitize_raw_messages_dict_for_json(original_dict: dict) -> list:
    processed_list = []
    if not isinstance(original_dict, dict):
        logger.warning(f"Expected a dict from GetListenMessage, got {type(original_dict)}. Returning empty list.")
        return []
        
    for chat_id_obj, msg_list in original_dict.items():
        chat_name_to_use = ""

        if isinstance(chat_id_obj, str):
            chat_name_to_use = chat_id_obj
        else:
            if hasattr(chat_id_obj, 'Name'):
                name_attribute_value = getattr(chat_id_obj, 'Name')
                if isinstance(name_attribute_value, str) and name_attribute_value:
                    chat_name_to_use = name_attribute_value
            
            if not chat_name_to_use:
                str_fallback_value = str(chat_id_obj)
                search_pattern = " for "
                pattern_index = str_fallback_value.rfind(search_pattern)
                
                if pattern_index != -1:
                    name_part = str_fallback_value[pattern_index + len(search_pattern):]
                    if name_part.endswith(">"):
                        name_part = name_part[:-1]
                    if name_part:
                        chat_name_to_use = name_part
                    else:
                        chat_name_to_use = str_fallback_value 
                else:
                    chat_name_to_use = str_fallback_value
            
            if not chat_name_to_use:
                logger.warning(f"Could not determine a non-empty chat_name for chat_id_obj: {chat_id_obj}")

        processed_msg_list = []
        if isinstance(msg_list, list):
            for msg_obj in msg_list:
                processed_msg_list.append(_sanitize_msg_obj_for_json(msg_obj))
        else:
            logger.warning(f"Expected a list of messages for chat '{chat_name_to_use}', got {type(msg_list)}")
            
        processed_list.append({
            "chat_name": chat_name_to_use,
            "messages": processed_msg_list
        })
        
    return processed_list

# --- 微信消息监听循环 ---
async def wechat_listener_loop():
    global wx_instance
    if not wx_instance:
        logger.error("WeChat instance not initialized. Listener loop cannot start.")
        return

    logger.info("Starting WeChat message listener loop...")
    while True:
        try:
            raw_messages_dict_from_wx = wx_instance.GetListenMessage()
            if raw_messages_dict_from_wx:
                logger.info(f"Received message structure: {raw_messages_dict_from_wx}")
                sanitized_dict_to_send = await _sanitize_raw_messages_dict_for_json(raw_messages_dict_from_wx)
                
                if sanitized_dict_to_send:
                    logger.info("Broadcasting messages to WebSocket clients")
                    await broadcast_to_websocket_clients({
                        "event_type": "wechat_messages",
                        "data": sanitized_dict_to_send,
                        "timestamp": int(time.time() * 1000)
                    })
            
            await asyncio.sleep(WECHAT_POLL_INTERVAL)

        except Exception as e:
            logger.error(f"Critical error in WeChat listener loop: {e}", exc_info=True)
            if not wx_instance:
                logger.critical("WeChat instance lost. Stopping listener loop.")
                break
            logger.info("Restarting WeChat listener loop after 5 seconds...")
            await asyncio.sleep(5)

# --- WebSocket 处理器 ---
async def websocket_handler(websocket, path = None):
    await register_websocket_client(websocket)
    try:
        # 发送心跳包保持连接
        async def heartbeat():
            while True:
                try:
                    await asyncio.sleep(30)  # 每30秒发送心跳
                    await websocket.send(json.dumps({
                        "event_type": "heartbeat",
                        "timestamp": int(time.time() * 1000)
                    }))
                except websockets.exceptions.ConnectionClosed:
                    break
                except Exception as e:
                    logger.error(f"Heartbeat error: {e}")
                    break

        heartbeat_task = asyncio.create_task(heartbeat())
        
        # 监听客户端消息（主要用于心跳响应）
        async for message in websocket:
            try:
                data = json.loads(message)
                if data.get("event_type") == "pong":
                    logger.debug("Received pong from client")
                elif data.get("event_type") == "ping":
                    await websocket.send(json.dumps({"event_type": "pong", "timestamp": int(time.time() * 1000)}))
                else:
                    logger.info(f"Received message from WebSocket client: {data}")
            except json.JSONDecodeError:
                logger.warning(f"Received invalid JSON from WebSocket client: {message}")
            except Exception as e:
                logger.error(f"Error processing WebSocket message: {e}")

    except websockets.exceptions.ConnectionClosed:
        logger.info("WebSocket client disconnected")
    except Exception as e:
        logger.error(f"Error in WebSocket handler: {e}")
    finally:
        if 'heartbeat_task' in locals():
            heartbeat_task.cancel()
        await unregister_websocket_client(websocket)

# --- FastAPI 应用初始化 ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时执行
    global wx_instance
    try:
        logger.info("Attempting to initialize WeChat instance...")
        wx_instance = WeChat()
        logger.info("WeChat instance created successfully.")

        # 添加初始监听者
        initial_listeners = []
        try:
            my_name = wx_instance.A_MyIcon.Name
            if my_name:
                initial_listeners.append(my_name)
                logger.info(f"Retrieved own WeChat name: '{my_name}'. Will listen to self for testing.")
        except Exception as e:
            logger.warning(f"Error retrieving own WeChat name: {e}")
        
        if "文件传输助手" not in initial_listeners:
             initial_listeners.append("文件传输助手")

        for listener_name in initial_listeners:
            try:
                logger.info(f"Adding initial listener for: '{listener_name}'")
                wx_instance.AddListenChat(who=listener_name, savepic=True, savevoice=True, parse_links=True)
            except Exception as e:
                logger.error(f"Failed to add initial listener for '{listener_name}': {e}")
        
        # 启动消息监听任务
        listener_task = asyncio.create_task(wechat_listener_loop())
        logger.info("WeChat listener task started.")
        
        yield
        
        # 关闭时执行
        if listener_task and not listener_task.done():
            listener_task.cancel()
            try:
                await listener_task
            except asyncio.CancelledError:
                logger.info("WeChat listener task cancelled.")
        
    except Exception as e:
        logger.critical(f"Fatal error during startup: {e}", exc_info=True)
        raise

app = FastAPI(title="WeChat WebSocket/HTTP Gateway", lifespan=lifespan)

# --- HTTP API 端点 ---
@app.post("/api/send_text_message", response_model=APIResponse)
async def send_text_message(request: SendTextMessageRequest):
    """发送文本消息"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        logger.info(f"Sending text message to '{request.to_who}': '{request.text_content[:50]}...'")
        wx_instance.SendMsg(request.text_content, request.to_who)
        return APIResponse(success=True, message="Text message sent successfully")
    except Exception as e:
        logger.error(f"Error sending text message: {e}")
        raise HTTPException(status_code=500, detail=f"Error sending message: {str(e)}")

@app.post("/api/send_file_by_path", response_model=APIResponse)
async def send_file_by_path(request: SendFileByPathRequest):
    """通过本地文件路径发送文件"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        if not os.path.exists(request.filepath):
            raise HTTPException(status_code=400, detail=f"File not found: {request.filepath}")
        
        logger.info(f"Sending file by path to '{request.to_who}': '{request.filepath}'")
        wx_instance.SendFiles(request.filepath, request.to_who)
        return APIResponse(success=True, message="File sent successfully")
    except Exception as e:
        logger.error(f"Error sending file by path: {e}")
        raise HTTPException(status_code=500, detail=f"Error sending file: {str(e)}")

@app.post("/api/send_file_by_upload", response_model=APIResponse)
async def send_file_by_upload(
    to_who: str = Form(...),
    file: UploadFile = File(...)
):
    """通过上传文件发送文件"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    temp_filepath = None
    try:
        logger.info(f"Sending uploaded file to '{to_who}': '{file.filename}'")
        
        # 保存上传的文件到临时目录
        temp_filepath = await save_uploaded_file(file)
        
        # 发送文件
        wx_instance.SendFiles(temp_filepath, to_who)
        
        return APIResponse(
            success=True, 
            message="Uploaded file sent successfully"
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Error sending uploaded file: {e}")
        raise HTTPException(status_code=500, detail=f"Error sending uploaded file: {str(e)}")
    finally:
        # 清理临时文件
        if temp_filepath:
            cleanup_temp_file(temp_filepath)

@app.post("/api/send_file_by_url", response_model=APIResponse)
async def send_file_by_url(request: SendFileByUrlRequest):
    """通过URL地址下载并发送文件"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    temp_filepath = None
    try:
        logger.info(f"Sending file from URL to '{request.to_who}': '{request.file_url}'")
        
        # 从URL下载文件到临时目录
        temp_filepath = await download_file_from_url(str(request.file_url), request.filename)
        
        # 发送文件
        wx_instance.SendFiles(temp_filepath, request.to_who)
        
        return APIResponse(
            success=True, 
            message="File from URL sent successfully",
            data={
                "url": str(request.file_url), 
                "filename": os.path.basename(temp_filepath),
                "size": os.path.getsize(temp_filepath)
            }
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Error sending file from URL: {e}")
        raise HTTPException(status_code=500, detail=f"Error sending file from URL: {str(e)}")
    finally:
        # 清理临时文件
        if temp_filepath:
            cleanup_temp_file(temp_filepath)

@app.post("/api/add_listen_chat", response_model=APIResponse)
async def add_listen_chat(request: AddListenChatRequest):
    """添加消息监听"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        logger.info(f"Adding listener for: '{request.who}'")
        wx_instance.AddListenChat(
            who=request.who, 
            savepic=request.savepic, 
            savevoice=request.savevoice, 
            parse_links=request.parse_links
        )
        return APIResponse(success=True, message=f"Added listener for '{request.who}'")
    except Exception as e:
        logger.error(f"Error adding listener: {e}")
        raise HTTPException(status_code=500, detail=f"Error adding listener: {str(e)}")

@app.get("/api/get_robot_name", response_model=APIResponse)
async def get_robot_name():
    """获取机器人名称"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        robot_name = wx_instance.A_MyIcon.Name
        logger.info(f"Retrieved robot name: '{robot_name}'")
        return APIResponse(
            success=True, 
            message="Robot name retrieved", 
            data={"robot_name": robot_name}
        )
    except Exception as e:
        logger.error(f"Error getting robot name: {e}")
        raise HTTPException(status_code=500, detail=f"Error getting robot name: {str(e)}")

@app.post("/api/chat_with", response_model=APIResponse)
async def chat_with(request: ChatWithRequest):
    """切换到指定聊天窗口"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        logger.info(f"Switching to chat with: '{request.who}'")
        wx_instance.ChatWith(request.who)
        return APIResponse(success=True, message=f"Chat window for '{request.who}' brought to front")
    except Exception as e:
        logger.error(f"Error switching chat: {e}")
        raise HTTPException(status_code=500, detail=f"Error switching chat: {str(e)}")

@app.post("/api/voice_call", response_model=APIResponse)
async def voice_call(request: VoiceCallRequest):
    """发起语音通话"""
    global wx_instance
    if not wx_instance:
        raise HTTPException(status_code=503, detail="WeChat instance not ready")
    
    try:
        logger.info(f"Initiating voice call with: '{request.user_id}'")
        wx_instance.VoiceCall(request.user_id)
        return APIResponse(success=True, message=f"Initiated voice call with '{request.user_id}'")
    except Exception as e:
        logger.error(f"Error initiating voice call: {e}")
        raise HTTPException(status_code=500, detail=f"Error initiating voice call: {str(e)}")

@app.get("/api/health")
async def health_check():
    """健康检查"""
    global wx_instance
    return {
        "status": "healthy" if wx_instance else "unhealthy",
        "wechat_ready": wx_instance is not None,
        "sse_clients": len(sse_clients),
        "temp_dir": TEMP_DIR,
        "max_file_size": f"{MAX_FILE_SIZE / 1024 / 1024:.1f}MB"
    }

# --- WebSocket 服务器启动函数 ---
async def start_websocket_server():
    """启动WebSocket服务器"""
    logger.info(f"Starting WebSocket server on ws://{WEBSOCKET_HOST}:{WEBSOCKET_PORT}")
    
    # 启动消息监听循环
    listener_task = asyncio.create_task(wechat_listener_loop())
    
    # 启动WebSocket服务器
    async with websockets.serve(websocket_handler, WEBSOCKET_HOST, WEBSOCKET_PORT):
        logger.info("WebSocket server is running...")
        try:
            await asyncio.Future()  # 保持运行
        except asyncio.CancelledError:
            pass
        finally:
            listener_task.cancel()
            try:
                await listener_task
            except asyncio.CancelledError:
                logger.info("WeChat listener task cancelled")

def run_websocket_server():
    """在单独线程中运行WebSocket服务器"""
    asyncio.run(start_websocket_server())

# --- HTTP 服务器启动函数 ---
def start_http_server():
    """启动HTTP服务器"""
    logger.info(f"Starting HTTP server on http://{HTTP_HOST}:{HTTP_PORT}")
    uvicorn.run(
        app,
        host=HTTP_HOST,
        port=HTTP_PORT,
        log_level="info"
    )

# --- 主函数 ---
def main():
    try:
        # 在单独线程中启动WebSocket服务器
        websocket_thread = threading.Thread(target=run_websocket_server, daemon=True)
        websocket_thread.start()
        
        # 在主线程中启动HTTP服务器
        start_http_server()
        
    except KeyboardInterrupt:
        logger.info("Application shutting down...")
    except Exception as e:
        logger.critical(f"Failed to start servers: {e}", exc_info=True)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Application shutting down (KeyboardInterrupt).")
    except Exception as e:
        logger.critical(f"Application failed to run: {e}", exc_info=True)
    finally:
        logger.info("Application exited.")
