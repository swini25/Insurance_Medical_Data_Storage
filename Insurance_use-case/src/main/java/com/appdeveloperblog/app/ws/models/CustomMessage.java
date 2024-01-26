package com.appdeveloperblog.app.ws.models;

public class CustomMessage {
    private String json;
    private String type;

    public CustomMessage(String json, String type) {
        this.json = json;
        this.type = type;
    }


    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
