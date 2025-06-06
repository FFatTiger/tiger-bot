# TigerBot - 智能微信聊天机器人

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue)
![Python](https://img.shields.io/badge/Python-3.8+-blue)
![License](https://img.shields.io/badge/License-MIT-green)

一个基于 **DDD架构** 和 **Spring AI** 的企业级智能微信聊天机器人，支持模块化扩展、权限管理、AI角色扮演等功能。

</div>

## 📖 项目简介

TigerBot 是一个采用领域驱动设计(DDD)的企业级微信聊天机器人，具备完整的模块化架构：

- **🏛️ DDD架构**：领域驱动设计，清晰的分层架构和模块边界
- **🧠 智能核心**：基于 Spring AI 框架的多AI提供商支持
- **🔗 微信集成**：Python wxauto + WebSocket 实现的稳定微信通信
- **🎭 角色系统**：可配置的AI角色和提示词管理
- **🔐 权限控制**：细粒度的命令权限管理系统
- **📊 管理后台**：Web管理界面，支持配置和监控

### 核心特性

- ✨ **模块化架构**：tiger-bot-core(核心) + tiger-bot-management(管理)
- 🎭 **AI角色管理**：数据库配置AI提供商、模型和角色
- 🔐 **权限系统**：基于聊天对象、命令、用户的三级权限控制
- 📊 **智能总结**：AI分析群聊记录并生成图片摘要
- 🔄 **消息处理链**：责任链模式，支持自定义处理器扩展
- 📁 **文件处理**：支持多种文件格式的发送和接收
- 🌐 **Web管理**：实时监控系统状态和配置管理

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    TigerBot 系统架构                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐                 │
│  │  Management     │    │   Core Module   │                 │
│  │   Module        │    │                 │                 │
│  │  (Web管理界面)   │    │  (核心业务逻辑)  │                 │
│  │   :8080         │    │    :8080        │                 │
│  └─────────────────┘    └─────────────────┘                 │
│           │                       │                         │
│           └───────────┬───────────┘                         │
│                       │                                     │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              共享基础设施层                              │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │ │
│  │  │ PostgreSQL  │  │   Spring    │  │   Python    │     │ │
│  │  │  Database   │  │     AI      │  │   Gateway   │     │ │
│  │  │             │  │             │  │   :8000     │     │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │    微信PC客户端      │
                    │     (wxauto)       │
                    └─────────────────────┘
```

### 模块说明

- **tiger-bot-core**: 核心业务模块，实现DDD架构的消息处理和AI对话
- **tiger-bot-management**: 管理模块，提供Web界面进行系统配置和监控
- **Python Gateway**: 微信通信网关，处理微信消息的收发
- **PostgreSQL**: 数据持久化，存储配置、权限、聊天记录等

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17+
- **Python**: 3.8+
- **数据库**: PostgreSQL 12+
- **微信**: PC 版微信客户端
- **API**: DeepSeek API Key

### 1. 克隆项目

```bash
git clone https://github.com/your-username/tiger-bot.git
cd tiger-bot
```

### 2. 创建数据库

创建 PostgreSQL 数据库：

```sql
CREATE DATABASE tigerbot;
```

### 3. 配置应用

编辑 `tiger-bot-core/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tigerbot
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  sql:
    init:
      mode: always  # 首次启动设置为always，后续可改为never
      platform: postgresql
      schema-locations: classpath:scripts/schema-postgresql.sql
      data-locations: classpath:scripts/data.sql

chatbot:
  wx-auto-gateway-http-url: http://localhost:8000
  wx-auto-gateway-ws-url: ws://localhost:8765
```

**重要说明**：
- `sql.init.mode: always` 用于首次启动时自动创建表结构和初始化数据
- 首次启动成功后，建议改为 `never` 避免重复初始化
- 系统会自动创建所有必要的表和初始化示例数据

### 4. 安装 Python 依赖

```bash
pip install -r requirements.txt
```

### 5. 启动服务

**方式一：分别启动（推荐开发）**

启动微信网关（终端1）：
```bash
cd wxauto
python wx_http_sse_gateway.py
```

启动核心模块（终端2）：
```bash
cd tiger-bot-core
mvn spring-boot:run
```

启动管理模块（终端3）：
```bash
cd tiger-bot-management
mvn spring-boot:run
```

**方式二：Docker启动（推荐生产）**

```bash
docker-compose up -d
```

### 6. 首次启动配置

**重要**：首次启动前确保配置正确：

1. **数据库初始化**: 确保 `sql.init.mode: always` 用于自动创建表和数据
2. **API密钥配置**: 在 `data.sql` 中更新你的DeepSeek API密钥：
   ```sql
   -- 修改这行中的API密钥
   INSERT INTO "ai_providers" ("provider_type", "provider_name", "api_key", "base_url")
   VALUES ('deepseek', 'deepseek', 'sk-your-actual-api-key', 'https://api.deepseek.com/v1');
   ```

3. **启动后修改配置**: 首次启动成功后，将 `sql.init.mode` 改为 `never`

### 7. 访问系统

- **微信机器人**: 确保微信PC客户端已登录，网关会自动检测并连接
- **管理界面**: 访问 http://localhost:8080 进行系统配置和监控
- **查看日志**: 观察控制台输出，确认数据库初始化和微信连接状态

## 🎯 功能使用

### 基础聊天功能

- **智能对话**：支持多AI提供商（DeepSeek、OpenAI等）
- **角色扮演**：通过管理界面配置AI角色和提示词
- **上下文记忆**：自动记录对话历史，支持连续对话
- **群聊@回复**：支持群聊中@机器人进行对话

### 命令系统

| 命令 | 功能 | 权限要求 | 示例 |
|------|------|----------|------|
| `/help` | 显示帮助信息 | 无 | 查看所有可用命令 |
| `/status` | 查看系统状态 | 管理员 | 显示运行状态和配置 |
| `/总结 [日期] [群名]` | AI总结聊天记录 | 配置权限 | `/总结 今天 工作群` |
| `/addListener [群名]` | 添加监听群聊 | 管理员 | `/addListener 新群聊` |

### 权限管理

系统支持三级权限控制：
- **聊天对象级别**：哪些群聊/私聊可以使用机器人
- **命令级别**：哪些命令可以在特定聊天中使用
- **用户级别**：哪些用户可以执行特定命令

### Web管理界面

访问 http://localhost:8080 可以：
- 查看系统运行状态
- 管理AI提供商和模型配置
- 配置聊天监听器
- 设置命令权限
- 查看聊天记录和统计

## ⚙️ 配置管理

### 数据库自动初始化

系统采用数据库配置方式，首次启动时会自动：

1. **创建表结构**: 自动执行 `schema-postgresql.sql` 创建所有必要的表
2. **初始化数据**: 自动执行 `data.sql` 插入示例配置数据
3. **AI配置**: 预置DeepSeek提供商、模型和多个AI角色
4. **示例聊天**: 创建测试群聊和权限配置

### 预置AI角色

系统预置了多个AI角色供选择：

| 角色名称 | 特点 | 适用场景 |
|---------|------|----------|
| 测试机器人 | 专业、简洁、高效 | 测试环境、正式对话 |
| 贴吧老哥 | 幽默、犀利、情绪化 | 娱乐群聊、活跃气氛 |
| 迷糊小学妹 | 可爱、天然呆、校园风 | 校园群聊、轻松聊天 |
| 贴吧老哥-总结 | 专用于聊天总结 | `/总结` 命令专用 |

### 修改AI配置

**方法一：通过Web管理界面**
1. 访问 http://localhost:8080
2. 在AI提供商管理中修改API密钥
3. 在AI模型管理中调整模型参数
4. 在聊天配置中切换AI角色

**方法二：直接修改数据库**
```sql
-- 更新DeepSeek API密钥
UPDATE ai_providers
SET api_key = 'sk-your-new-key'
WHERE provider_type = 'deepseek';

-- 为特定聊天切换AI角色
UPDATE chats
SET ai_role_id = 2  -- 切换到贴吧老哥角色
WHERE name = '你的群聊名称';
```

### 添加新的聊天监听

```sql
-- 添加新的聊天对象
INSERT INTO chats (name, group_flag, ai_provider_id, ai_model_id, ai_role_id)
VALUES ('新群聊', true, 1, 1, 2);

-- 添加监听器配置
INSERT INTO listeners (chat_id, at_reply_enable, save_pic, save_voice, parse_links)
VALUES (LASTVAL(), true, true, false, true);

-- 添加命令权限（可选）
INSERT INTO chat_command_auths (chat_id, command_id, user_id)
VALUES (LASTVAL(), 1, 1);  -- 给用户ID为1的用户分配所有命令权限
```

## 🔧 扩展开发

### 项目结构（DDD架构）

```
tiger-bot/
├── tiger-bot-core/                     # 核心业务模块
│   └── src/main/java/com/fffattiger/wechatbot/
│       ├── domain/                      # 领域层
│       │   ├── chat/                   # 聊天聚合
│       │   ├── command/                # 命令聚合
│       │   ├── listener/               # 监听器聚合
│       │   ├── user/                   # 用户聚合
│       │   └── ai/                     # AI聚合
│       ├── application/                # 应用层
│       │   └── service/                # 应用服务
│       ├── infrastructure/             # 基础设施层
│       │   ├── external/               # 外部服务集成
│       │   └── ai/                     # AI集成
│       └── interfaces/                 # 接口层
│           └── event/                  # 事件处理
│               └── handlers/           # 消息处理器
├── tiger-bot-management/               # 管理模块
│   └── src/main/java/com/fffattiger/wechatbot/management/
│       ├── controller/                 # Web控制器
│       └── service/                    # 管理服务
└── wxauto/                            # Python微信网关
    └── wx_http_sse_gateway.py
```

### 扩展开发指南

#### 1. 添加自定义消息处理器

创建处理器类实现 `MessageHandler` 接口：

```java
@Service
public class CustomMessageHandler implements MessageHandler {

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        // 自定义处理逻辑
        BatchedSanitizedWechatMessages.Chat.Message message = context.message();
        if (message.content().contains("特殊关键词")) {
            context.wx().sendText(context.currentChat().chat().name(), "检测到特殊关键词");
            return false; // 停止处理链
        }
        return chain.handle(context); // 继续处理链
    }

    @Override
    public int getOrder() {
        return 5; // 处理优先级，数字越小优先级越高
    }
}
```

#### 2. 添加自定义命令处理器

继承 `AbstractCommandMessageHandler`：

```java
@Service
public class CustomCommandHandler extends AbstractCommandMessageHandler {

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/custom");
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        String chatName = context.currentChat().chat().name();
        context.wx().sendText(chatName, "执行自定义命令: " + command);
    }

    @Override
    public String description() {
        return "/custom [参数] - 自定义命令说明";
    }
}
```

#### 3. 添加新的AI提供商

1. 在数据库中添加AI提供商配置：
```sql
INSERT INTO ai_providers (provider_type, provider_name, api_key, base_url)
VALUES ('openai', 'OpenAI', 'sk-your-key', 'https://api.openai.com');
```

2. 添加对应的AI模型配置：
```sql
INSERT INTO ai_models (ai_provider_id, model_name, description, max_tokens, temperature)
VALUES (1, 'gpt-4', 'GPT-4模型', 4096, 0.7);
```

#### 4. 自定义Web管理功能

在 `tiger-bot-management` 模块中添加新的控制器：

```java
@RestController
@RequestMapping("/api/custom")
public class CustomManagementController {

    @GetMapping("/status")
    public ResponseEntity<String> getCustomStatus() {
        // 自定义状态查询逻辑
        return ResponseEntity.ok("自定义状态信息");
    }
}
```

## 🐛 故障排除

### 常见问题

**1. 微信网关连接失败**
- 检查微信 PC 客户端是否已登录
- 确认防火墙设置允许相关端口
- 检查 Python 依赖是否正确安装

**2. AI 回复异常**
- 验证 DeepSeek API Key 是否有效
- 检查网络连接和 API 配额
- 查看应用日志中的错误信息

**3. 数据库连接问题**
- 确认 PostgreSQL 服务正在运行
- 检查数据库连接配置是否正确
- 验证数据库用户权限

**4. 消息处理延迟**
- 调整 `WECHAT_POLL_INTERVAL` 参数
- 检查系统资源使用情况
- 优化消息处理器逻辑

### 日志配置

在 `application.yml` 中调整日志级别：

```yaml
logging:
  level:
    '[com.fffattiger.wechatbot]': DEBUG  # 应用日志
    '[org.springframework.ai]': INFO     # Spring AI 日志
    '[root]': WARN                       # 根日志级别
```

## 📚 API参考

### Python网关API

详细文档：[API_DOCUMENTATION_WX_GATEWAY.md](wxauto/API_DOCUMENTATION_WX_GATEWAY.md)

**主要端点**:
- `POST /api/send_text_message` - 发送文本消息
- `POST /api/send_file_by_upload` - 上传并发送文件
- `POST /api/add_listen_chat` - 添加监听聊天
- `GET /api/get_robot_name` - 获取机器人名称
- `WebSocket ws://localhost:8765` - 实时消息推送

### 管理API

**系统管理**:
- `GET /management/status` - 系统状态
- `POST /management/start-python` - 启动Python进程
- `POST /management/stop-python` - 停止Python进程

**配置管理**:
- `GET /config/listeners` - 获取监听器列表
- `POST /config/listeners` - 创建监听器
- `GET /config/commands` - 获取命令列表
- `POST /config/command-auth` - 配置命令权限

### 技术文档

完整的技术架构文档：[TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## ⚠️ 免责声明

本项目仅供学习和研究使用，请遵守相关法律法规和微信使用条款。使用本项目所产生的任何后果由使用者自行承担。

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

## 🙏 致谢

感谢以下开源项目的支持：

- [wxauto](https://github.com/cluic/wxauto) - 微信自动化操作库
- [Spring AI](https://github.com/spring-projects/spring-ai) - Spring 生态 AI 框架
- [WeChatBot_WXAUTO_SE](https://github.com/iwyxdxl/WeChatBot_WXAUTO_SE) - 微信机器人参考实现
- [chatlog](https://github.com/sjzar/chatlog) - 聊天记录处理工具

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！**

</div>