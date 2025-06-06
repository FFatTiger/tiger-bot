package com.fffattiger.wechatbot.shared.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeChatMessage {
    private long seq;
    private String time;
    private String talker;
    private String talkerName;
    private boolean isChatRoom;
    private String sender;
    private String senderName;
    private boolean isSelf;
    private int type;
    private int subType;
    private String content;
    private JsonNode contents;

    // getters and setters
    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTalker() {
        return talker;
    }

    public void setTalker(String talker) {
        this.talker = talker;
    }

    public String getTalkerName() {
        return talkerName;
    }

    public void setTalkerName(String talkerName) {
        this.talkerName = talkerName;
    }

    public boolean isChatRoom() {
        return isChatRoom;
    }

    public void setChatRoom(boolean chatRoom) {
        isChatRoom = chatRoom;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JsonNode getContents() {
        return contents;
    }

    public void setContents(JsonNode contents) {
        this.contents = contents;
    }
}