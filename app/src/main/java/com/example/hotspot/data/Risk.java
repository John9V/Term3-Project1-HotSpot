package com.example.hotspot.data;

public class Risk {
    private String userAdd;
    private String outbreakAdd;

    public Risk(String userAdd, String outbreakAdd) {
        this.userAdd = userAdd;
        this.outbreakAdd = outbreakAdd;
    }

    public String getUserAdd() {
        return userAdd;
    }

    public void setUserAdd(String userAdd) {
        this.userAdd = userAdd;
    }

    public String getOutbreakAdd() {
        return outbreakAdd;
    }

    public void setOutbreakAdd(String outbreakAdd) {
        this.outbreakAdd = outbreakAdd;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual= false;

        if (object != null && object instanceof Risk) {
            isEqual = (this.userAdd.equals(((Risk) object).userAdd) && this.outbreakAdd.equals(((Risk) object).outbreakAdd));
        }

        return isEqual;
    }

}
