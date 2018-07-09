package com.tan.iotwfirebase;

public class generalData {

    private Float data;
    private Long timestamp;

    public generalData (Float data, Long timestamp){
        this.data = data;
        this.timestamp = timestamp;
    }

    public generalData(){}

    public Float getData() {
        return data;
    }

    public void setData(Float data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
