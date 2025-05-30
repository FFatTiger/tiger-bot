# TigerBot: 基于 Spring AI 与 DeepSeek 的智能微信聊天机器人

## 项目简介

TigerBot 是一个创新的微信聊天机器人项目，其核心是一个基于 Java Spring Boot 构建的智能对话应用。它利用了强大的 Spring AI 框架和 DeepSeek 大语言模型，能够以特定角色（例如，一位19岁、刚开始大学生活并初尝恋爱滋味的女生）与微信好友进行生动、自然的日常对话。

该 Java 应用依赖一个 Python 实现的微信网关 (`wx_http_sse_gateway.py`) 作为底层基础设施，负责与微信PC客户端进行实际的交互和消息传递。

## 核心功能

*   **智能角色扮演对话**: AI 能够根据预设的详细人设（包括背景、性格、外貌、经历、喜好等）进行聊天，提供高度情境化和个性化的交互体验。
*   **集成 DeepSeek 大模型**: 利用 DeepSeek 强大的自然语言理解和生成能力，确保对话的流畅性和智能性。
*   **上下文感知与聊天记忆**: 通过 `JsonChatMemoryRepository` 实现聊天记忆功能，能够记录和回顾之前的对话内容，使得交流更加连贯和深入。
*   **模块化消息处理**: 采用责任链模式（`MessageHandlerChain`）处理接收到的微信消息，可以方便地扩展和定制消息处理逻辑（如AI对话、群消息特殊处理、消息撤回响应等）。
*   **通过网关与微信交互**: Java 应用作为客户端，通过 WebSocket 连接到 Python 微信网关，间接实现微信消息的收发和部分控制操作。

## 系统架构

TigerBot 项目主要由两大部分协同工作：

1.  **Java TigerBot AI 应用 (核心层)**:
    *   **技术栈**: Spring Boot, Spring AI, DeepSeek SDK。
    *   **主要职责**:
        *   实现智能对话逻辑，包括角色定义、与 DeepSeek 模型的交互、聊天记忆管理。
        *   处理来自 Python 网关的微信消息，并通过 `MessageHandler` 链进行分发和处理。
        *   通过 `WxAutoWsClient` (或类似实现的 WebSocket 客户端) 将指令和回复发送给 Python 网关。
    *   **关键组件**:
        *   `AiChatMessageHandler`: AI 对话的核心处理器，定义了AI角色并调用 DeepSeek 模型。
        *   `JsonChatMemoryRepository`: 负责将聊天会话历史持久化到本地 JSONL 文件。
        *   `WxAutoWsClient`: (需要确认具体实现是否完整或被注释) 作为 WebSocket 客户端连接到 Python 网关。
        *   其他 `MessageHandler` 实现 (如 `HistoryCollectorMessageHandler`, `GroupMessageHandler`)。

2.  **Python 微信网关 (基础设施层)**:
    *   **脚本**: `wx_http_sse_gateway.py`
    *   **技术栈**: FastAPI, `websockets` (Python库), `Mwxauto` (微信UI自动化库)。
    *   **主要职责**:
        *   直接与本地运行的微信PC客户端进行交互，实现消息的监听和发送。
        *   提供 HTTP API 和 WebSocket 服务，供 Java 应用 (或其他客户端) 调用。
        *   详细的 API 说明见 `API_DOCUMENTATION_WX_GATEWAY.md`。

## 主要组件概览

*   **Java 应用 (`src/main/java/com/fffattiger/wechatbot/`)**:
    *   `TigerBotApplication.java`: Spring Boot 应用主入口。
    *   `ai/JsonChatMemoryRepository.java`: 聊天记忆的 JSONL 文件存储实现。
    *   `core/handler/AiChatMessageHandler.java`: AI 对话逻辑与 DeepSeek 模型集成的核心。
    *   `core/WxAutoWsClient.java`: (注释部分展示了其设计) 用于连接 Python 网关的 WebSocket 客户端。
    *   `pom.xml`: Maven 配置文件，声明了 Spring AI, DeepSeek 等关键依赖。
*   **Python 网关**:
    *   `wx_http_sse_gateway.py`: FastAPI 和 WebSocket 实现的微信通信网关。
    *   `API_DOCUMENTATION_WX_GATEWAY.md`: Python 网关的详细API文档。

## 核心类图概览 (Java 应用)

```mermaid
classDiagram
    direction LR

    package "wxauto (External, Assumed)" {
        interface WxAuto {
            +sendText(toWho, text) Result~String~
            +sendFileByUpload(toWho, File file) Result~String~
            +addListenChat(who, savePic, saveVoice, parseLinks) Result~String~
            +getRobotName() Result~RobotNameResponse~
            # Other wxauto methods...
        }
        class Message {}
        class MessageHandlerChain {} # External chain interface
        class MessageHandlerContext { # External context interface
             +message() Message
             +setMessage(Message)
             +wx() WxAuto
             +setWxAuto(WxAuto)
             +currentChat() WxChat
             +setCurrentChat(WxChat)
             +chatBotProperties() ChatBotProperties
             +setChatBotProperties(ChatBotProperties)
             +cleanContent() String
             +setCleanContent(String)
        }
    }

    package "com.fffattiger.wechatbot.config" {
        class ChatBotProperties {
            +List~WxChatConfig~ listeners
            +String model
            +String apiKey
            +String commandPrefix
            # Other global properties...
            +String chatMemoryDir
            +int chatMemoryFileMaxCount
            +String baseUrl
        }
        class WxChatConfig {
            +WxInfo wxInfo
            +AiChatInfo aiChatInfo
            +CommandInfo commandInfo
            +boolean isListen
        }
        class WxInfo {
            +String chatName
            +boolean groupFlg
        }
        class AiChatInfo {
            +boolean aiChatEnable
            +String roleName
            # Other AI config...
        }
        class CommandInfo {
            +boolean commandEnable
            +List~String~ allowCommandParttens
            +Map~String, Set~String~~ commandAllowUser
        }
        class ChatBotConfiguration {
            +WxAuto wxAuto(ChatBotProperties, ...) WxAutoWebSocketHttpClient
            +ChatHistoryCollector chatHistoryCollector() AsyncChatHistoryCollector
            # Other beans...
        }
        ChatBotConfiguration ..> ChatBotProperties : uses
        ChatBotConfiguration ..> WxAutoWebSocketHttpClient : creates
        ChatBotConfiguration ..> AsyncChatHistoryCollector : creates
        WxChatConfig *-- WxInfo
        WxChatConfig *-- AiChatInfo
        WxChatConfig *-- CommandInfo
        ChatBotProperties *-- WxChatConfig : has multiple
    }

    package "com.fffattiger.wechatbot.core" {
        class WxChat {
            - WxChatConfig wxChatConfig
            - ChatClient chatClient
            +getChatName() String
            +isAiChatEnable() boolean
        }
        WxChat o-- WxChatConfig

        class WxChatHolder {
            {static} +Map~String, WxChat~ WX_CHAT_MAP
            {static} +registerWxChat(WxChat)
            {static} +getWxChat(String) WxChat
        }
        WxChatHolder o-- WxChat

        class DefaultMessageHandlerContext {
            # ThreadLocal~Map~ threadLocal
        }
        DefaultMessageHandlerContext ..|> MessageHandlerContext


        interface MessageHandler {
            +handle(MessageHandlerContext context, MessageHandlerChain chain) boolean
            +getOrder() int
        }

        class DefaultMessageHandlerChain {
            - List~MessageHandler~ handlers
            +handle(MessageHandlerContext context)
        }
        DefaultMessageHandlerChain ..|> "wxauto.MessageHandlerChain"
        DefaultMessageHandlerChain *-- MessageHandler : chains

        class WxAutoWebSocketHttpClient {
            # ChatBotProperties chatBotProperties
            # OperationTaskManager operationTaskManager
            # WebClient webClient
            # List~MessageHandler~ messageHandlers (for onMessage callback)
        }
        WxAutoWebSocketHttpClient ..|> WxAuto


        package "handler" {
            class AiChatMessageHandler {
                - ChatClient chatClient (Spring AI)
                +init() # Defines AI role
            }
            AiChatMessageHandler ..|> MessageHandler
            AiChatMessageHandler --> "ai.JsonChatMemoryRepository" : uses

            class HistoryCollectorMessageHandler {
                - ChatHistoryCollector chatHistoryCollector
            }
            HistoryCollectorMessageHandler ..|> MessageHandler
            HistoryCollectorMessageHandler --> "ChatHistoryCollector" : uses

            class GroupMessageHandler {} # Specific logic for group messages
            GroupMessageHandler ..|> MessageHandler

            class RecallMessageHandler {} # Logic for recalled messages
            RecallMessageHandler ..|> MessageHandler

            package "cmd" {
                abstract AbstractCommandMessageHandler {
                    # AntPathMatcher antPathMatcher
                    +handle(context, chain) boolean
                    {abstract} +canHandle(String command) boolean
                    {abstract} +doHandle(command, args, context) void
                    {abstract} +description() String
                    #hasPermission(command, sender, context) boolean
                }
                AbstractCommandMessageHandler ..|> MessageHandler

                class HelpCommandMessageHandler {}
                HelpCommandMessageHandler --|> AbstractCommandMessageHandler
                class StatusCommandMessageHandler {}
                StatusCommandMessageHandler --|> AbstractCommandMessageHandler
                class AddListenerCommandMessageHandler {}
                AddListenerCommandMessageHandler --|> AbstractCommandMessageHandler
                class ChatSummaryCommandMessageHandler {
                    - DeepSeekChatModel chatModel
                    - ChatLogClient chatLogClient
                    - PromptTemplate promptTemplate # "贴吧嘴臭老哥"
                    #renderImageFile(...) File
                    #chat(...) String
                }
                ChatSummaryCommandMessageHandler --|> AbstractCommandMessageHandler
                ChatSummaryCommandMessageHandler --> "util.MarkdownToImageConverter" : uses
                ChatSummaryCommandMessageHandler --> "chatlog.ChatLogClient" : uses
            }
        }
    }

    package "com.fffattiger.wechatbot.ai" {
        class JsonChatMemoryRepository {
            - File outputDir
        }
        JsonChatMemoryRepository ..|> "org.springframework.ai.chat.memory.ChatMemoryRepository"
        JsonChatMemoryRepository ..> "util.JsonlHelper" : uses
    }

    package "com.fffattiger.wechatbot.util" {
        class JsonlHelper {}
        class MarkdownToImageConverter {}
    }
    
    package "com.fffattiger.wechatbot.chatlog" {
        class ChatLogClient {
            # String chatLogApiUrl
            +getChatHistory(chatName, date, sender) String
        }
    }

    ChatBotConfiguration --> WxChat : creates and registers
    ChatBotConfiguration --> WxAuto : creates (WxAutoWebSocketHttpClient)
    WxAutoWebSocketHttpClient --> ChatBotProperties : uses
    DefaultMessageHandlerContext o-- WxAuto : holds
    DefaultMessageHandlerContext o-- WxChat : holds
    DefaultMessageHandlerContext o-- ChatBotProperties : holds
    DefaultMessageHandlerContext o-- Message : holds
    AiChatMessageHandler o-- WxAuto : uses (via context)
    AbstractCommandMessageHandler o-- WxAuto : uses (via context)
    AbstractCommandMessageHandler o-- WxChatConfig : uses (via context) for permissions
    GroupMessageHandler --> AbstractCommandMessageHandler : can delegate to cmd handlers

```
**Note**: 
*   `WxAutoWsClient.java` (大部分代码被注释) 与 `WxAutoWebSocketHttpClient.java` (在 `ChatBotConfiguration` 中实际使用) 的关系和具体实现细节，尤其是前者是否仍在使用或已被后者完全替代，需要结合实际运行情况或更详细的代码分析来确定。类图中将 `WxAutoWebSocketHttpClient` 作为 `WxAuto` 的主要实现。
*   `wxauto` 包下的具体类（如 `OperationTaskManager`, `Message`, `MessageHandlerChain`, `MessageHandlerContext` 接口）为根据使用情况推断的外部依赖或项目自定义的底层接口。

## 运行步骤

### 1. 准备环境

*   **微信**: 确保本地已安装并登录微信 PC 版客户端。
*   **Python 环境**: 安装 Python 及 `wx_http_sse_gateway.py` 所需的依赖库 (如 `fastapi`, `uvicorn`, `websockets`, `requests`, `Mwxauto`)。
*   **Java 环境**: JDK 17 或更高版本，以及 Apache Maven。
*   **(可能需要)** DeepSeek API Key: 如果 `spring-ai-starter-deepseek` 需要配置 API Key，请确保已获取并配置在 Java 应用的 `application.yml` 或环境变量中。

### 2. 启动 Python 微信网关

```bash
python wx_http_sse_gateway.py
```

默认情况下，Python 网关的 HTTP 服务监听 `8000` 端口，WebSocket 服务监听 `8765` 端口。请根据实际情况和脚本内配置进行调整。

### 3. 启动 Java TigerBot AI 应用

进入 Java 项目的根目录 (包含 `pom.xml` 的目录)，执行：

```bash
cd path/to/tiger-bot 
mvn spring-boot:run
```

Java 应用启动后，会尝试连接到 Python 网关的 WebSocket 服务。

## 配置说明

### Java TigerBot 应用配置

主要配置文件为 `src/main/resources/application.yml` (或 `application.properties`)。可能包含以下配置项：

*   **Python 网关地址**: `WxAutoWsClient` (如果代码被启用) 或相关配置类中需要指定 Python WebSocket 网关的正确  URI (例如 `ws://localhost:8765`)。
*   **DeepSeek 模型配置**: 可能包括模型名称、API Key (如果 Spring AI 的 DeepSeek 集成需要直接配置)。
*   **聊天记忆存储路径**: 在 `ChatBotProperties` 类中配置，例如 `chatBotProperties.getChatMemoryDir()` 用于指定 `JsonChatMemoryRepository` 的存储位置。
*   **聊天记忆文件最大数量**: `chatBotProperties.getChatMemoryFileMaxCount()`。

### Python 网关配置

配置项直接定义在 `wx_http_sse_gateway.py` 脚本的顶部，包括：

*   `WEBSOCKET_HOST`, `WEBSOCKET_PORT`
*   `HTTP_HOST`, `HTTP_PORT`
*   `LOG_LEVEL`, `WECHAT_POLL_INTERVAL`, `TEMP_DIR`, `MAX_FILE_SIZE`, `DOWNLOAD_TIMEOUT`

## 工作流程简述

1.  用户向已登录的微信账号发送消息 (目标为机器人所控制的微信账号)。
2.  Python 网关 (`wx_http_sse_gateway.py`) 监听到该消息，并通过其 WebSocket 服务将消息数据推送出去。
3.  Java 应用中的 `WxAutoWsClient` (或类似组件) 接收到来自网关的 WebSocket 消息。
4.  消息在 Java 应用内部流转到 `MessageHandlerChain`。
5.  如果消息是好友的文本消息，`AiChatMessageHandler` 会被触发。
6.  `AiChatMessageHandler`:
    a.  从 `JsonChatMemoryRepository` 加载与该好友的聊天历史作为上下文。
    b.  结合预设的系统角色提示 (定义AI的性格、背景等) 和用户的当前消息。
    c.  调用 `ChatClient` (集成了 DeepSeek 模型) 生成回复。
    d.  将生成的回复保存到聊天记忆中。
7.  `AiChatMessageHandler` 通过 `context.wx().sendText()` (内部调用 `WxAutoWsClient` 的发送方法) 将 AI 回复发送给 Python 网关。
8.  Python 网关接收到指令后，通过 `wxauto` 控制微信客户端将文本消息发送给原始用户。
9.  **命令处理流程**:
    a. 如果消息满足命令格式 (如以 `/` 开头)，且为好友消息，`AbstractCommandMessageHandler` 的子类会尝试处理。
    b. `AbstractCommandMessageHandler`首先检查命令是否在当前聊天配置中启用，以及用户是否有权限执行该命令。
    c. 如果权限通过，匹配的命令处理器 (如 `HelpCommandMessageHandler`, `ChatSummaryCommandMessageHandler` 等) 的 `doHandle` 方法会被调用，执行具体操作。
    d. 命令执行结果通常会通过微信消息回复给用户。

## 自定义与扩展

### 修改 AI 角色人设

AI 的角色和行为主要由 `AiChatMessageHandler.java` 文件中的 `init()` 方法内的系统提示 (system prompt) 定义。您可以直接编辑此处的文本内容来改变AI的：

*   身份、年龄、职业
*   性格、语气、说话风格
*   背景故事、经历
*   知识范围和对话偏好

修改后重新启动 Java 应用即可生效。

### 添加新的消息处理器

您可以创建新的类实现 `com.fffattiger.wechatbot.wxauto.MessageHandler` 接口，并将其注册到 Spring 上下文 (例如使用 `@Service` 注解)，即可将其加入到消息处理链中。通过实现 `getOrder()` 方法控制其在链中的执行顺序。

### 添加新的命令处理器

1.  创建一个新的 Java 类，继承自 `com.fffattiger.wechatbot.core.handler.cmd.AbstractCommandMessageHandler`。
2.  实现以下抽象方法：
    *   `canHandle(String command)`: 返回 `true` 如果你的处理器能处理传入的命令字符串 (例如 `command.startsWith("/mycommand")`)。
    *   `doHandle(String command, String[] args, MessageHandlerContext context)`: 实现命令的具体逻辑。`command` 是完整的命令字符串 (如 `/mycommand`)，`args` 是去除命令本身后的参数数组。使用 `context.wx()` 与微信交互。
    *   `description()`: 返回命令的描述和用法，用于 `/help` 命令的输出 (例如 `"/mycommand [param1] - 执行我的神奇操作"`).
3.  用 `@Service` 注解该类，使其被 Spring 扫描并自动加入到命令处理器列表中。
4.  （可选）在 `application.yml` 中配置此命令的启用状态和权限：
    ```yaml
    chatbot:
      listeners:
        - wx-info:
            chat-name: "文件传输助手" # 或其他聊天对象
          command-info:
            command-enable: true # 是否对该聊天对象开启命令功能
            allow-command-parttens: # 允许的命令模式 (Ant-style patterns)
              - "/mycommand*" # 允许 /mycommand 及其子命令
              - "/help"
            # command-allow-user: # 如果需要更细致的权限控制
            #   "/mycommand": ["user_wxid_1", "user_wxid_2"] # 只有这些用户能执行 /mycommand
            #   "/supercmd": ["admin_wxid"]
    ```

### 添加新的命令处理器

1.  创建一个新的 Java 类，继承自 `com.fffattiger.wechatbot.core.handler.cmd.AbstractCommandMessageHandler`。
2.  实现以下抽象方法：
    *   `canHandle(String command)`: 返回 `true` 如果你的处理器能处理传入的命令字符串 (例如 `command.startsWith("/mycommand")`)。
    *   `doHandle(String command, String[] args, MessageHandlerContext context)`: 实现命令的具体逻辑。`command` 是完整的命令字符串 (如 `/mycommand`)，`args` 是去除命令本身后的参数数组。使用 `context.wx()` 与微信交互。
    *   `description()`: 返回命令的描述和用法，用于 `/help` 命令的输出 (例如 `"/mycommand [param1] - 执行我的神奇操作"`).
3.  用 `@Service` 注解该类，使其被 Spring 扫描并自动加入到命令处理器列表中。
4.  （可选）在 `application.yml` 中配置此命令的启用状态和权限：
    ```yaml
    chatbot:
      listeners:
        - wx-info:
            chat-name: "文件传输助手" # 或其他聊天对象
          command-info:
            command-enable: true # 是否对该聊天对象开启命令功能
            allow-command-parttens: # 允许的命令模式 (Ant-style patterns)
              - "/mycommand*" # 允许 /mycommand 及其子命令
              - "/help"
            # command-allow-user: # 如果需要更细致的权限控制
            #   "/mycommand": ["user_wxid_1", "user_wxid_2"] # 只有这些用户能执行 /mycommand
            #   "/supercmd": ["admin_wxid"]
    ```

## 内置命令列表

以下是 TigerBot 当前内置的命令及其功能 (默认命令前缀为 `/`):

*   **/help** (别名: `/h`, `/?`, `/帮助`)
    *   功能: 显示所有可用命令及其描述。
    *   用法: `/help`

*   **/status** (别名: `/s`)
    *   功能: 显示机器人当前的一些运行状态。
    *   用法: `/status`
    *   当前输出:
        *   监听对象列表。
    *   (未来可能包含：机器人开关状态、今日回复数、当前AI模型、当前AI角色、运行时间、版本等)

*   **/增加监听** (别名: `/addlistener`)
    *   功能: 动态添加一个新的微信聊天对象到监听列表。
    *   用法: `/增加监听 <chatName> [savePic] [saveVoice] [parseLinks]`
        *   `<chatName>`: (必填) 要监听的好友昵称、备注名或群聊名称。
        *   `[savePic]`: (可选, boolean, 默认 false) 是否自动下载该聊天中的图片。
        *   `[saveVoice]`: (可选, boolean, 默认 false) 是否自动下载该聊天中的语音。
        *   `[parseLinks]`: (可选, boolean, 默认 false) 是否解析该聊天中的链接。
    *   示例: `/增加监听 张三 true true false`

*   **/总结** (别名: `/summary`)
    *   功能: 对指定日期的聊天记录进行 AI 总结，并以图片形式发送。AI 会以 "贴吧嘴臭老哥" 的角色进行总结。
    *   用法: `/总结 <date>`
        *   `<date>`: (必填) 日期。可以是：
            *   `今天`
            *   `昨天`
            *   `yyyy-MM-dd` 格式的具体日期 (例如 `2023-10-27`)
    *   示例: `/总结 今天`
    *   注意: 此功能依赖 `ChatLogClient` 获取聊天记录，并将使用 DeepSeek 模型进行总结和 `MarkdownToImageConverter` 生成图片。

## 注意事项

*   `WxAutoWsClient.java` 中的大部分代码在当前版本中被注释掉了。需要检查并确保其 WebSocket 连接和消息收发逻辑是完整且可用的，或者由项目中的其他组件负责实现与 Python 网关的通信。
*   本项目依赖微信PC版客户端，且 `wxauto` 库可能对微信版本有一定要求。

---
*本文档已根据最新信息更新，重点描述了Java AI应用的核心功能及其与Python网关的协作关系。* 