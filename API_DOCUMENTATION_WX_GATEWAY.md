# 微信 HTTP/WebSocket 网关 API 文档

## 1. 简介

本文档描述了微信 HTTP/WebSocket 网关的API接口。该网关允许通过HTTP接口与微信进行交互（如发送消息、文件等），并通过WebSocket接收来自微信的实时消息。它是基于 `wxauto` 库实现的微信UI自动化操作的封装。

**主要功能:**
*   通过 HTTP API 发送文本消息、文件（本地路径、上传、URL）。
*   通过 HTTP API 添加消息监听对象、切换聊天窗口、发起语音通话、获取机器人自身微信名。
*   通过 WebSocket 实时接收指定监听对象的微信消息。

**基本架构:**
*   **HTTP 服务器 (FastAPI)**: 提供 RESTful API 用于主动操作微信。
*   **WebSocket 服务器 (websockets)**: 用于向客户端推送实时微信消息。
*   **微信实例 (`wxauto.WeChat`)**: 后台实际操作微信UI的实例。

## 2. 配置

以下是主要的配置项，在 `wx_http_sse_gateway.py` 脚本开头定义：
*   `WEBSOCKET_HOST`: WebSocket 服务器监听的主机地址 (默认: `"0.0.0.0"`)。
*   `WEBSOCKET_PORT`: WebSocket 服务器监听的端口 (默认: `8765`)。
*   `HTTP_HOST`: HTTP 服务器监听的主机地址 (默认: `"0.0.0.0"`)。
*   `HTTP_PORT`: HTTP 服务器监听的端口 (默认: `8000`)。
*   `WECHAT_POLL_INTERVAL`: 检查微信新消息的轮询间隔 (秒, 默认: `1`)。
*   `MAX_FILE_SIZE`: 允许处理的最大文件大小 (默认: `100MB`)。
*   `DOWNLOAD_TIMEOUT`: 从URL下载文件的超时时间 (秒, 默认: `30`)。

## 3. WebSocket API

WebSocket API 用于实时接收来自微信的消息。

### 3.1. 连接

*   **URL**: `ws://{WEBSOCKET_HOST}:{WEBSOCKET_PORT}`
    *   例如: `ws://localhost:8765`

连接成功后，服务器会立即发送一条 `connected` 事件。

### 3.2. 服务器推送事件

所有事件均为 JSON 格式。

#### 3.2.1. `connected` (连接成功)
当 WebSocket 客户端成功连接到服务器时发送。
```json
{
    "event_type": "connected",
    "message": "WebSocket connection established",
    "timestamp": 1678886400.123456 // Unix timestamp
}
```

#### 3.2.2. `heartbeat` (心跳)
服务器会定期（每30秒）发送此事件以保持连接活跃，并帮助客户端检测连接是否断开。
```json
{
    "event_type": "heartbeat",
    "timestamp": 1678886430.123456 // Unix timestamp
}
```
客户端可以选择性地回复一个 `pong` 事件 (见 3.3.2)。

#### 3.2.3. `wechat_messages` (微信消息)
当监听到新的微信消息时，服务器会推送此事件。
```json
{
    "event_type": "wechat_messages",
    "data": [ // 消息列表，每个元素代表一个聊天会话的消息
        {
            "chat_name": "文件传输助手", // 聊天对象名称（好友昵称、群聊名称等）
            "messages": [ // 该聊天对象收到的消息列表
                {
                    "type": "TEXT", // 消息类型 (例如: TEXT, IMAGE, FILE, VOICE, CARD, SHARELINK 等)
                    "content": "你好，这是一条测试消息", // 消息内容
                    "sender": "wxid_xxxxxx", // 发送者的原始ID (可能是自己或联系人)
                    "info": {}, // wxauto原始消息对象中的info字段，可能包含更多细节
                    "id": "msgid_yyyyyyy", // 消息的唯一ID
                    "time": "YYYY-MM-DD HH:MM:SS", // 消息发送时间
                    "sender_remark": "我的备注名" // 发送者的备注名 (如果是联系人且有备注)
                },
                // ... 可能有更多消息
            ]
        },
        // ... 可能有来自其他聊天对象的消息
    ],
    "timestamp": 1678886405.123456 // 事件推送时间戳
}
```
**注意**: `messages` 数组中的每个消息对象的字段是根据 `wxauto` 的官方文档字段进行筛选和标准化的。`type` 字段保证存在。

### 3.3. 客户端可发送事件

#### 3.3.1. `ping` (客户端心跳)
客户端可以发送 `ping` 事件给服务器，服务器会回复一个 `pong` 事件。
```json
{
    "event_type": "ping"
}
```

#### 3.3.2. `pong` (响应服务器心跳)
客户端在收到服务器的 `heartbeat` 事件后，可以回复一个 `pong` 事件。这不是强制的，但可以帮助服务器了解客户端的活跃状态。
```json
{
    "event_type": "pong"
}
```
服务器在收到客户端的 `ping` 后也会回复此事件：
```json
{
    "event_type": "pong",
    "timestamp": 1678886430.123456 // Unix timestamp
}
```

## 4. HTTP API

HTTP API 用于主动控制微信执行操作。

### 4.1. 基础信息

*   **基础 URL**: `http://{HTTP_HOST}:{HTTP_PORT}/api`
    *   例如: `http://localhost:8000/api`
*   **通用成功响应模型 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "操作成功的描述信息",
        "data": {} // 可选，根据具体API包含额外数据
    }
    ```
*   **通用错误响应**:
    *   `400 Bad Request`: 请求参数错误或无效 (例如，文件过大，URL无效)。
        ```json
        {
            "detail": "错误描述"
        }
        ```
    *   `500 Internal Server Error`: 服务器内部错误。
        ```json
        {
            "detail": "错误描述"
        }
        ```
    *   `503 Service Unavailable`: 微信实例未就绪。
        ```json
        {
            "detail": "WeChat instance not ready"
        }
        ```

### 4.2. API 端点

#### 4.2.1. 发送文本消息
*   **Endpoint**: `POST /send_text_message`
*   **描述**:向指定好友或群聊发送文本消息。
*   **请求体 (`SendTextMessageRequest`)**:
    ```json
    {
        "to_who": "文件传输助手", // 接收者名称 (好友昵称、备注名、群聊名称、wxid)
        "text_content": "你好，世界！" // 要发送的文本内容
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Text message sent successfully",
        "data": null
    }
    ```

#### 4.2.2. 通过本地路径发送文件
*   **Endpoint**: `POST /send_file_by_path`
*   **描述**: 向指定好友或群聊发送本地文件。
*   **请求体 (`SendFileByPathRequest`)**:
    ```json
    {
        "to_who": "文件传输助手",
        "filepath": "/path/to/your/file.txt" // 要发送的本地文件的绝对路径
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "File sent successfully",
        "data": null
    }
    ```
*   **失败响应**: 如果文件路径不存在，返回 `400 Bad Request`。

#### 4.2.3. 通过文件上传发送文件
*   **Endpoint**: `POST /send_file_by_upload`
*   **描述**: 通过 HTTP 表单上传文件并将其发送给指定好友或群聊。
*   **请求类型**: `multipart/form-data`
*   **请求参数**:
    *   `to_who` (form data, string): 接收者名称。
    *   `file` (form data, file): 要上传的文件。
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Uploaded file sent successfully",
        "data": null
    }
    ```
*   **失败响应**: 如果文件过大，返回 `400 Bad Request`。

#### 4.2.4. 通过 URL 发送文件
*   **Endpoint**: `POST /send_file_by_url`
*   **描述**: 从指定的 URL 下载文件，并将其发送给指定好友或群聊。
*   **请求体 (`SendFileByUrlRequest`)**:
    ```json
    {
        "to_who": "文件传输助手",
        "file_url": "https://example.com/somefile.jpg", // 要下载的文件的URL
        "filename": "custom_name.jpg" // 可选，下载后保存的文件名，不提供则尝试从URL推断
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "File from URL sent successfully",
        "data": {
            "url": "https://example.com/somefile.jpg",
            "filename": "downloaded_1678886400_somefile.jpg", // 实际保存和发送的文件名
            "size": 102400 // 文件大小 (bytes)
        }
    }
    ```
*   **失败响应**: 如果 URL 无效、下载失败或文件过大，返回 `400 Bad Request`。

#### 4.2.5. 添加消息监听
*   **Endpoint**: `POST /add_listen_chat`
*   **描述**: 添加一个新的聊天对象到消息监听列表。来自该对象的消息将会通过 WebSocket 推送。
*   **请求体 (`AddListenChatRequest`)**:
    ```json
    {
        "who": "某个好友昵称", // 要监听的聊天对象名称 (好友昵称、备注名、群聊名称、wxid)
        "savepic": true,       // 是否自动下载该聊天中的图片 (默认: true)
        "savevoice": true,     // 是否自动下载该聊天中的语音 (默认: true)
        "parse_links": true    // 是否解析该聊天中的链接 (默认: true)
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Added listener for '某个好友昵称'",
        "data": null
    }
    ```
*   **注意**: 程序启动时会自动监听机器人自身和 "文件传输助手"。

#### 4.2.6. 获取机器人名称
*   **Endpoint**: `GET /get_robot_name`
*   **描述**: 获取当前登录微信的机器人账号的昵称。
*   **请求体**: 无
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Robot name retrieved",
        "data": {
            "robot_name": "机器人微信昵称"
        }
    }
    ```

#### 4.2.7. 切换聊天窗口
*   **Endpoint**: `POST /chat_with`
*   **描述**: 将微信PC客户端的当前聊天窗口切换到指定对象，即将该聊天窗口置于前台。
*   **请求体 (`ChatWithRequest`)**:
    ```json
    {
        "who": "文件传输助手" // 要切换到的聊天对象名称
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Chat window for '文件传输助手' brought to front",
        "data": null
    }
    ```

#### 4.2.8. 发起语音通话
*   **Endpoint**: `POST /api/voice_call`
*   **描述**: 向指定用户发起语音通话。
*   **请求体 (`VoiceCallRequest`)**:
    ```json
    {
        "user_id": "wxid_xxxxxxxxxxxx" // 用户的微信号或wxid
    }
    ```
*   **成功响应 (`APIResponse`)**:
    ```json
    {
        "success": true,
        "message": "Initiated voice call with 'wxid_xxxxxxxxxxxx'",
        "data": null
    }
    ```

#### 4.2.9. 健康检查
*   **Endpoint**: `GET /health`
*   **描述**: 检查服务状态。
*   **请求体**: 无
*   **成功响应**:
    ```json
    {
        "status": "healthy", // "healthy" 或 "unhealthy" (取决于微信实例是否就绪)
        "wechat_ready": true, // 微信实例是否已初始化
        "websocket_clients": 0, // 当前连接的 WebSocket 客户端数量 (代码中为 websocket_clients)
        "temp_dir": "/tmp", // 临时文件目录
        "max_file_size": "100.0MB" // 最大允许文件大小
    }
    ```

## 5. 附注

*   所有涉及 `to_who` 或 `who` 的参数，一般可以使用对方的微信昵称、备注名、群聊名称，或者在某些情况下使用原始 `wxid`。
*   文件处理功能会将下载或上传的文件暂时存放在系统临时目录中，并在操作完成后尝试清理。
*   错误处理主要通过 HTTP 状态码和响应体中的 `detail` 或 `message` 字段来指示。
*   WebSocket推送的消息中，`sender` 可能是发送者的 `wxid`，`sender_remark` 则是你给对方设置的备注名（如果对方是你的好友且设置了备注）。对于群聊消息，`sender`可能是群成员的 `wxid`，而`chat_name`是群聊名称。

---
*文档生成时间: {{CURRENT_DATETIME}}* 