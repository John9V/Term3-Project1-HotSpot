package com.example.hotspot.ui.settings;

/**
 * Class for testing purposes only, to extract information from Firebase to the app
 */

public class UserInformation {
    private long accuracy;
    private boolean complete;

    public UserInformation() {
    }

    public long getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(long accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
