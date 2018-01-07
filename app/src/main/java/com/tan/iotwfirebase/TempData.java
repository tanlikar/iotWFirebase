package com.tan.iotwfirebase;

import java.util.Date;

/**
 * Created by tanli on 1/4/2018.
 */

public class TempData {
    private Long temp;
    private Long timestamp;

    public TempData (Long temp, Long timestamp){
        this.temp = temp;
        this.timestamp = timestamp;
    }

    public TempData(){}

    public Long getTemp() {
        return temp;
    }

    public void setTemp(Long temp) {
        this.temp = temp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
