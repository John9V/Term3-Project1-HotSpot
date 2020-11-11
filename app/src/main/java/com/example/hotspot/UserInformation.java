package com.example.hotspot;

import java.math.BigDecimal;

public class UserInformation {
    private int breakoutNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public UserInformation(){
    }

    public int getBreakoutNumber() {
        return breakoutNumber;
    }

    public void setBreakoutNumber(int breakoutNumber) {
        this.breakoutNumber = breakoutNumber;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}
