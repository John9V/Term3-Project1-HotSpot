package com.example.hotspot.data;

/**
 * Class that represents a risk of exposure.
 */

public class Risk {
    /**
     * User address.
     */
    private String userAdd;
    /**
     * Outbreak address
     */
    private String outbreakAdd;

    /**
     * Constructor for address.
     * @param userAdd user's address.
     * @param outbreakAdd outbreak address.
     */
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

    /**
     * Used to check for duplicates when forming the risk of lists for the user.
     * @param object usually a risk to check equality against.
     * @return whether the passed in risk is a duplicate of this risk.
     */
    @Override
    public boolean equals(Object object) {
        boolean isEqual= false;

        if (object != null && object instanceof Risk) {
            isEqual = (this.userAdd.equals(((Risk) object).userAdd) && this.outbreakAdd.equals(((Risk) object).outbreakAdd));
        }

        return isEqual;
    }

}
