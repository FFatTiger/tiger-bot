package com.fffattiger.wechatbot.infrastructure.external.wxauto;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler.ResultSpecification;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler.RobotNameSpecification;

@Service
public interface WxAuto {

    /**
     * 向指定的联系人或群聊发送文本消息。
     * 
     * @param toWho 接收消息的联系人昵称、备注名或群聊名称。
     * @param text  要发送的文本内容。
     * @return 发送结果。
     */
    ResultSpecification<String> sendText(String toWho, String text);

    /**
     * 向指定的联系人或群聊发送文件。
     * 
     * @param toWho    接收文件的联系人昵称、备注名或群聊名称。
     * @param filePath 要发送的文件的本地绝对路径。
     * @return 发送结果。
     */
    ResultSpecification<String> sendFile(String toWho, String filePath);


    /**
     * 向指定的联系人或群聊发送文件。
     * 
     * @param toWho 接收文件的联系人昵称、备注名或群聊名称。
     * @param file  要发送的文件。
     * @return 发送结果。
     */
    ResultSpecification<String> sendFileByUpload(String toWho, File file);

    /**
     * 添加一个聊天对象（联系人或群聊）到监听列表，之后该对象的新消息会被推送到客户端。
     * 
     * @param who        要监听的联系人昵称、备注名或群聊名称。
     * @param savePic    是否自动保存接收到的图片。
     * @param saveVoice  是否自动保存接收到的语音。
     * @param parseLinks 是否解析消息中的链接。
     * @return 添加结果。
     */
    ResultSpecification<String> addListenChat(String who, boolean savePic, boolean saveVoice, boolean parseLinks);

    /**
     * 获取当前登录微信机器人的昵称。
     * 
     * @return 当前登录微信机器人的昵称。
     */
    MessageHandler.ResultSpecification<RobotNameSpecification> getRobotName();

    /**
     * 打开一个聊天窗口。
     * 
     * @param who 要打开的聊天窗口的联系人昵称、备注名或群聊名称。
     * @return 打开结果。
     */
    MessageHandler.ResultSpecification<String> chatWith(String who);

    /**
     * 向指定用户发起语音通话。
     * 
     * @param userId 要呼叫的用户的微信ID或准确昵称。
     * @return 呼叫结果。
     */
    ResultSpecification<String> voiceCall(String userId);

    /**
     * 获取当前监听的聊天对象
     * @return 当前监听的聊天对象
     */
    List<String> getListeners();

    /**
     * 设置消息处理。
     * 
     * @param handler 消息处理。
     */
    void setMessageHandlerChain(MessageHandlerChain chain);


}