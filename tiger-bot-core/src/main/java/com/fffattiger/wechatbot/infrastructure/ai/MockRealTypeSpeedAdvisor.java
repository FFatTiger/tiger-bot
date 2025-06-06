package com.fffattiger.wechatbot.infrastructure.ai;
// package com.fffattiger.wechatbot.ai;

// import org.springframework.ai.chat.client.ChatClientRequest;
// import org.springframework.ai.chat.client.ChatClientResponse;
// import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
// import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
// import org.springframework.lang.NonNull;

// /**
//  * 模拟真实打字速度的顾问 最终打字速度 = 字数 * random(typeOneWordSpeedMin, typeOneWordSpeedMax) 趋近 typeAvgSpeed
//  */
// public class MockRealTypeSpeedAdvisor implements BaseAdvisor {
//     /**
//      * 模拟的单个打字速度区间
//      */
//     private final double typeOneWordSpeedMin;
    
//     /**
//      * 模拟的单个打字速度区间
//      */
//     private final double typeOneWordSpeedMax;

//     /**
//      * 模拟的打字平均速度
//      */
//     private final double typeAvgSpeed;

//     /**
//      * 分隔符 用于分割多行内容 分段记速
//      */
//     private final String splitStr = "\\\\";


//     public MockRealTypeSpeedAdvisor(double typeOneWordSpeedMin, double typeOneWordSpeedMax, double typeAvgSpeed) {
//         this.typeOneWordSpeedMin = typeOneWordSpeedMin;
//         this.typeOneWordSpeedMax = typeOneWordSpeedMax;
//         this.typeAvgSpeed = typeAvgSpeed;
//     }

//     @Override
//     public int getOrder() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'getOrder'");
//     }

//     @Override
//     @NonNull
//     public ChatClientRequest before(@NonNull ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
//         chatClientRequest.context().computeIfAbsent("startTime", k -> System.currentTimeMillis());
//         return chatClientRequest;
//     }

//     @Override
//     @NonNull
//     public ChatClientResponse after(@NonNull ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
//         Long startTime = (Long) chatClientResponse.context().getOrDefault("startTime", System.currentTimeMillis());
//         if (startTime != null) {
//             long start = startTime;
//             long end = System.currentTimeMillis();
//             long duration = end - start;
//             double speed = chatClientResponse.chatResponse().getResult().getOutput().getText().length() / duration;
//             if (speed < typeAvgSpeed) {
//                 // 等待
//             }
//         }
        
//     }
    
// }
