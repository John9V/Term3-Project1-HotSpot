package com.example.hotspot.ui.settings;

/**
 * Archived class
 */

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is a settings fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}