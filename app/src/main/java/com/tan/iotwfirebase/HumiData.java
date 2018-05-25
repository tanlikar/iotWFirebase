package com.tan.iotwfirebase;

public class HumiData {
    private Long humi;
    private Long timestamp;

    public HumiData (Long humi, Long timestamp){
        this.humi = humi;
        this.timestamp = timestamp;
    }

    public HumiData(){}

    public Long getHumi() {
        return humi;
    }

    public void setHumi(Long humi) {
        this.humi = humi;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setHumiTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
