# Overview (开发中～～～～～～～～～～～～)

一个基于wxauto + Spring AI的微信机器人项目，支持微信消息的接收、处理和回复。
仅为学习SpringAI使用，勿用于其他用途。

# 目前支持功能
1. 支持消息监听
2. AI聊天，暂时只支持DeepSeek，其他模型适配中
3. 命令
    - /summary AI总结聊天记录自动发送到群里（测试群中疯狂被骂
    ![alt text](summary.png)
    - /addListener 添加监听的群聊
    - /help 帮助
    - /status 状态



# 使用

1. 修改/src/main/resources/application.yml配置
```yml
server:
  port: 8080 
#logging:
#  level:
#    '[com.fffattiger.wechatbot]': DEBUG
        
spring:
  ai:
    deepseek:
      chat:
        api-key: sk-3237d5d4bbf8471a89ae771c70ca0a20
chatbot:
  # 聊天记录API地址
  chatLogApiUrl: http://127.0.0.1:5030
  # 微信网关基础URL
  baseUrl: http://127.0.0.1:8000
  # 监听的微信窗口对象
  listeners:
    - wxInfo:
        # 微信窗口名称
        chatName: 测试一号
        # 是否是群聊
        groupFlg: true
      # 是否开启@回复
      at-reply-enable: true
      # 允许的命令
      command-info:
        # 允许的命令通配符
        allow-command-parttens:
        - '/*'
        # 允许的命令用户
        command-allow-user:
          '[/*]':
            - 'fffattiger'
    - wxInfo:
        chatName: 测试二号
        groupFlg: true
      at-reply-enable: true
      command-info:
        allow-command-parttens:
        - '/*'
        command-allow-user:
          '[/*]':
            - 'fffattiger'
```

2. 登录wxauto支持的微信客户端

3. 安装依赖
```bash
pip install -r requirements.txt
```

4. 运行网关
```bash
python wx_http_sse_gateway.py
```

4. 运行项目
```bash
mvn spring-boot:run
```

5. 测试
对监听的群聊发送信息，查看是否能收到回复


# 参考项目
https://github.com/cluic/wxauto

https://github.com/spring-projects/spring-ai

https://github.com/iwyxdxl/WeChatBot_WXAUTO_SE