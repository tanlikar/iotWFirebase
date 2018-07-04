package com.tan.iotwfirebase;

public class generalData {

    private Long data;
    private Long timestamp;

    public generalData (Long data, Long timestamp){
        this.data = data;
        this.timestamp = timestamp;
    }

    public generalData(){}

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
