# WeChat WebSocket/HTTP Gateway API 文档

## 概述

该网关提供了一个基于普通版 wxauto 库的 WebSocket 和 HTTP API 接口，用于与微信进行自动化交互。

## 服务器配置

- WebSocket 服务器: `ws://localhost:8765`
- HTTP 服务器: `http://localhost:8000`

## 核心功能

### 1. 智能监听管理
- 当所有 WebSocket 客户端断开连接时，自动停止所有消息监听
- 节省系统资源，避免不必要的消息轮询

### 2. 消息回调机制
- 支持为每个聊天添加回调函数
- 实时接收新消息并通过 WebSocket 广播

### 3. 多种文件发送方式
- 本地文件路径
- 文件上传
- URL 下载

## WebSocket API

### 连接
```
ws://localhost:8765
```

### 事件类型

#### 1. 连接确认
```json
{
  "event_type": "connected",
  "message": "WebSocket connection established",
  "timestamp": 1703123456789
}
```

#### 2. 微信消息
```json
{
  "event_type": "wechat_messages",
  "data": [
    {
      "chat_name": "张三",
      "chat_type": "friend",
      "messages": [
        {
          "type": "text",
          "attr": "friend",
          "info": null,
          "id": "msg123",
          "sender": "张三",
          "content": "你好"
        }
      ]
    }
  ],
  "timestamp": 1703123456789
}
```

**消息字段说明：**
- `type`: 消息内容类型 (`text`, `image`, `file`, `voice`, `link` 等)
- `attr`: 消息来源类型 (`system`, `time`, `tickle`, `self`, `friend`, `other`)
- `info`: 附加信息（具体内容取决于消息类型）
- `id`: 消息唯一标识符
- `sender`: 发送者名称
- `content`: 消息内容

#### 3. 心跳
```json
{
  "event_type": "heartbeat",
  "timestamp": 1703123456789
}
```

## HTTP API

### 基础响应格式
```json
{
  "success": true,
  "message": "操作描述",
  "data": {}  // 可选的附加数据
}
```

### 1. 发送文本消息
**POST** `/api/send_text_message`

```json
{
  "to_who": "张三",
  "text_content": "你好，这是一条测试消息"
}
```

### 2. 发送文件（本地路径）
**POST** `/api/send_file_by_path`

```json
{
  "to_who": "张三",
  "filepath": "/path/to/file.jpg"
}
```

### 3. 发送文件（上传）
**POST** `/api/send_file_by_upload`

表单数据：
- `to_who`: 接收人名称
- `file`: 文件数据

### 4. 发送文件（URL下载）
**POST** `/api/send_file_by_url`

```json
{
  "to_who": "张三",
  "file_url": "https://example.com/image.jpg",
  "filename": "image.jpg"  // 可选
}
```

## 监听管理 API

### 1. 添加聊天监听
**POST** `/api/add_listen_chat`

```json
{
  "nickname": "张三"
}
```

成功后，该聊天的新消息将通过 WebSocket 实时推送。

### 2. 移除聊天监听
**POST** `/api/remove_listen_chat`

```json
{
  "nickname": "张三"
}
```

### 3. 停止所有监听
**POST** `/api/stop_all_listening`

停止所有聊天的消息监听。

### 4. 切换聊天窗口
**POST** `/api/chat_with`

```json
{
  "who": "张三"
}
```

将指定聊天窗口带到前台。

## 系统 API

### 健康检查
**GET** `/api/health`

返回系统状态信息：
```json
{
  "status": "healthy",
  "wechat_ready": true,
  "websocket_clients": 2,
  "listening_chats": ["张三", "李四"],
  "temp_dir": "/tmp",
  "max_file_size": "100.0MB"
}
```

## 实现特性

### 1. 自动资源管理
- WebSocket 客户端断开时自动清理监听
- 临时文件自动清理
- 连接状态实时监控

### 2. 错误处理
- 详细的错误日志
- 用户友好的错误信息
- 连接异常自动恢复

### 3. 并发处理
- 支持多个 WebSocket 客户端同时连接
- 线程安全的消息广播
- 异步文件处理

## 使用示例

### Python 客户端示例
```python
import asyncio
import websockets
import json
import requests

# WebSocket 监听消息
async def listen_messages():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        async for message in websocket:
            data = json.loads(message)
            if data["event_type"] == "wechat_messages":
                for chat_data in data["data"]:
                    print(f"收到来自 {chat_data['chat_name']} 的消息:")
                    for msg in chat_data["messages"]:
                        print(f"  {msg['sender']}: {msg['content']}")

# HTTP 发送消息
def send_message(to_who, text):
    url = "http://localhost:8000/api/send_text_message"
    payload = {"to_who": to_who, "text_content": text}
    response = requests.post(url, json=payload)
    return response.json()

# 添加监听
def add_listener(nickname):
    url = "http://localhost:8000/api/add_listen_chat"
    payload = {"nickname": nickname}
    response = requests.post(url, json=payload)
    return response.json()

# 使用示例
if __name__ == "__main__":
    # 添加监听
    result = add_listener("张三")
    print(f"添加监听结果: {result}")
    
    # 发送消息
    result = send_message("张三", "你好！")
    print(f"发送消息结果: {result}")
    
    # 开始监听
    asyncio.run(listen_messages())
```

## 注意事项

1. **依赖管理**: 确保安装了普通版 wxauto 及相关依赖
2. **微信版本**: 建议使用兼容的微信版本
3. **权限设置**: 确保微信有必要的界面操作权限
4. **网络配置**: 如需远程访问，请配置防火墙和网络设置
5. **文件大小**: 上传文件限制为 100MB
6. **监听重连**: WebSocket 重连后需要重新添加监听

## 故障排除

### 常见问题

1. **WeChat instance not ready**: 微信未正确初始化，检查微信是否运行
2. **File not found**: 文件路径不存在，检查文件路径是否正确
3. **Connection refused**: 服务器未启动，检查服务器状态
4. **Permission denied**: 权限不足，检查文件和微信操作权限

### 日志查看
服务器会输出详细的日志信息，包括：
- 连接状态
- 消息发送结果
- 错误详情
- 性能指标

通过日志可以快速定位和解决问题。 