package com.lucario.gpt4allandroid;

import java.io.Serializable;

public class Message implements Serializable {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT= "bot";

    public static String FAILED_RESPONSE = "failed";

    String message;
    String sentBy;
    int currentIndex;
    boolean firstTime;

    public void setFinished(boolean finished) {
        this.finished = finished;
//        this.message = null;
    }

    boolean finished;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void append(String msg){message = message + msg;}
    public void setFirstTime(boolean firstTime) {this.firstTime = firstTime;}
    public Message(String message, String sentBy) {
        this.message = "";
        append(message);
        this.sentBy = sentBy;
        this.firstTime = true;
        this.currentIndex = 0;
    }
}