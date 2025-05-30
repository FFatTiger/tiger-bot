package com.fffattiger.wechatbot.core;
// package com.tiger.wechatbot.core;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.tiger.wechatbot.config.ChatBotProperties;
// import com.tiger.wechatbot.core.context.DefaultMessageHandlerContext;
// import com.tiger.wechatbot.core.handler.DefaultMessageHandlerChain;
// import com.tiger.wechatbot.core.holder.WxChatHolder;
// import com.tiger.wechatbot.wxauto.MessageHandler;
// import com.tiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages;
// import com.tiger.wechatbot.wxauto.MessageHandlerChain;
// import com.tiger.wechatbot.wxauto.WxAuto;

// import lombok.extern.slf4j.Slf4j;

// import org.java_websocket.client.WebSocketClient;
// import org.java_websocket.handshake.ServerHandshake;

// import java.net.URI;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.Callable;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.ThreadPoolExecutor;
// import java.util.concurrent.TimeUnit;
// import java.util.ArrayList;
// import java.util.concurrent.ConcurrentHashMap;

// @Slf4j
// @SuppressWarnings("unused")
// public class WxAutoWsClient implements WxAuto {
//     private WebSocketClient client;
//     private final ObjectMapper objectMapper = new ObjectMapper();
//     private ChatBotProperties chatBotProperties;

//     // 监听线程池（并发处理消息监听/业务逻辑）
//     private final ExecutorService listenerPool = java.util.concurrent.Executors.newCachedThreadPool();
//     // 可观测的操作队列
//     private final BlockingQueue<Runnable> operationTaskQueue = new LinkedBlockingQueue<>();
//     private final ThreadPoolExecutor operationQueue = new ThreadPoolExecutor(
//             1, 1, 0L, TimeUnit.MILLISECONDS, operationTaskQueue);

//     // 等待响应的future队列
//     private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

//     // Ui操作任务，带描述，支持返回结果
//     public class UiOperationTask<T> implements Callable<T> {
//         private final String description;
//         private final Callable<T> task;

//         public UiOperationTask(String description, Callable<T> task) {
//             this.description = description;
//             this.task = task;
//         }

//         @Override
//         public T call() throws Exception {
//             log.info("执行UI操作: {}", description);
//             // 打印当前等待队列
//             if (WxAutoWsClient.this != null) {
//                 java.util.List<String> pending = WxAutoWsClient.this.getPendingUiOperationTasks();
//                 log.info("当前等待中的UI操作队列: {}", pending);
//             }
//             return task.call();
//         }

//         @Override
//         public String toString() {
//             return "UiOperationTask{" + "description='" + description + '\'' + '}';
//         }
//     }

//     /**
//      * 获取当前等待中的UI操作任务描述列表
//      */
//     public java.util.List<String> getPendingUiOperationTasks() {
//         java.util.List<String> list = new ArrayList<>();
//         for (Runnable r : operationTaskQueue) {
//             if (r instanceof UiOperationTask) {
//                 list.add(r.toString());
//             } else {
//                 list.add(r.getClass().getName());
//             }
//         }
//         return list;
//     }

//     public WxAutoWsClient(List<MessageHandler> messageHandlers, ChatBotProperties chatBotProperties) throws Exception {
//         client = new WebSocketClient(new URI("ws://192.168.31.77:8765")) {
//             @Override
//             public void onOpen(ServerHandshake handshakedata) {
//             }

//             @Override
//             public void onMessage(String originalMessage) {
//                 log.info("收到消息: {}", originalMessage);
//                 if (originalMessage.contains("event_type")) {
//                     try {
//                         BatchedSanitizedWechatMessages batchedSanitizedWechatMessages = objectMapper.readValue(
//                                 originalMessage,
//                                 new TypeReference<BatchedSanitizedWechatMessages>() {
//                                 });

//                         for (BatchedSanitizedWechatMessages.Chat chat : batchedSanitizedWechatMessages.data()) {
//                             WxChat wxChat = WxChatHolder.getWxChat(chat.chatName());
//                             if (wxChat == null) {
//                                 log.warn("未监听该对象: {}", chat.chatName());
//                                 continue;
//                             }
//                             for (BatchedSanitizedWechatMessages.Chat.Message message : chat.messages()) {
//                                 listenerPool.submit(() -> {
//                                     DefaultMessageHandlerContext defaultMessageHandlerContext = new DefaultMessageHandlerContext();
//                                     defaultMessageHandlerContext.setMessage(message);
//                                     defaultMessageHandlerContext.setWxAuto(WxAutoWsClient.this);
//                                     defaultMessageHandlerContext.setCurrentChat(wxChat);
//                                     defaultMessageHandlerContext.setChatBotProperties(chatBotProperties);
//                                     new DefaultMessageHandlerChain(messageHandlers)
//                                             .handle(defaultMessageHandlerContext);

//                                 });
//                             }
//                         }
//                     } catch (Exception e) {
//                         log.error("处理失败 originalMessage={}", originalMessage, e);
//                     }

//                 } else {
//                     // 解析request_id
//                     try {
//                         Map<String, Object> resp = objectMapper.readValue(originalMessage,
//                                 new TypeReference<Map<String, Object>>() {
//                                 });
//                         String requestId = (String) resp.get("request_id");
//                         if (requestId != null) {
//                             CompletableFuture<String> future = pendingRequests.remove(requestId);
//                             if (future != null) {
//                                 future.complete(originalMessage);
//                             } else {
//                                 log.warn("未找到对应的future, request_id={}", requestId);
//                             }
//                         } else {
//                             log.warn("响应中未包含request_id: {}", originalMessage);
//                         }
//                     } catch (Exception e) {
//                         log.error("响应解析失败", e);
//                     }
//                 }
//             }

//             @Override
//             public void onClose(int code, String reason, boolean remote) {
//                 log.info("连接关闭: {}", reason);
//             }

//             @Override
//             public void onError(Exception ex) {
//                 log.error("连接错误: {}", ex.getMessage());
//             }
//         };
//         log.info("开始连接");
//         client.connectBlocking();
//     }

//     public <T> T sendAction(String action, Object payload, TypeReference<T> responseType) throws Exception {
//         String requestId = String.valueOf(System.currentTimeMillis());
//         Map<String, Object> request = Map.of(
//                 "action", action,
//                 "payload", payload,
//                 "request_id", requestId);
//         String json = objectMapper.writeValueAsString(request);
//         CompletableFuture<String> future = new CompletableFuture<>();
//         pendingRequests.put(requestId, future);
//         client.send(json);
//         String response = future.get(); // 阻塞直到收到响应
//         return objectMapper.readValue(response, responseType);
//     }

//     @Override
//     public Result<String> addListenChat(String who, boolean savePic, boolean saveVoice, boolean parseLinks) {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<String>>("监听聊天: " + who, () -> sendAction("add_listen_chat", Map.of(
//                             "who", who,
//                             "save_pic", savePic,
//                             "save_voice", saveVoice,
//                             "parse_links", parseLinks), new TypeReference<Result<String>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public Result<String> chatWith(String who) {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<String>>("与 " + who + " 聊天",
//                             () -> sendAction("chat_with", Map.of("who", who), new TypeReference<Result<String>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public Result<RobotName> getRobotName() {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<RobotName>>("获取机器人名称",
//                             () -> sendAction("get_robot_name", "", new TypeReference<Result<RobotName>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             log.error("获取机器人名称失败", e);
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public Result<String> sendFile(String toWho, String filePath) {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<String>>("发送文件给: " + toWho, () -> sendAction("send_file",
//                             Map.of("to_who", toWho, "file_path", filePath), new TypeReference<Result<String>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public Result<String> sendText(String toWho, String text) {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<String>>("发送文本消息给: " + toWho, () -> sendAction("send_text_message",
//                             Map.of("to_who", toWho, "text_content", text), new TypeReference<Result<String>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public Result<String> voiceCall(String userId) {
//         try {
//             return operationQueue.submit(
//                     new UiOperationTask<Result<String>>("语音通话: " + userId, () -> sendAction("voice_call",
//                             Map.of("user_id", userId), new TypeReference<Result<String>>() {
//                             })))
//                     .get();
//         } catch (Exception e) {
//             return new Result<>(false, e.getMessage(), null, null);
//         }
//     }

//     @Override
//     public void setMessageHandlerChain(MessageHandlerChain messageHandlerChain) {
//         throw new UnsupportedOperationException("Not supported");
//     }
// }