# TigerBot - 智能微信聊天机器人（开发中，README仅供参考）
## 目前支持：
- 插件管理
- 命令管理
- 监听群聊
- 总结群聊聊天记录
- 支持DeepSeek、OpenAI兼容格式
- 命令权限控制
- Web管理
- 角色管理
- ChatLog整合
- WXAuto整合（最稳定的wx自动方案）

## 开发计划：
- 支持多AI提供商
- 支持RAG、MCP
- 角色的长期记忆
- 扩展不同的机器人渠道（如：钉钉、飞书、企业微信）
- 插件市场

## 部分效果展示

### 总结群聊记录
![image1](./pic/img1.png)
![image1](./pic/img4.png)

### 角色切换
![image1](./pic/img2.png)

### 激情对喷
![image1](./pic/img3.png)

## 运行
由于项目开发阶段，完全需要手动运行

Windows服务器，wx 3.9.x版本，chatlog, postgres13+

python3 wxauto/wx_http_sse_gateway.py

cd tiger-bot-management && mvn spring-boot:run


## 技术栈
- SpringAI
- SpringBoot
- SpringDataJPA
- DDD结构