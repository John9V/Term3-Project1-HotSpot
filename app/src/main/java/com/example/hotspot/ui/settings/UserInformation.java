package com.example.hotspot.ui.settings;

import java.math.BigDecimal;

public class UserInformation {
    private int accuracy;
    private boolean complete;

    public UserInformation(){
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
