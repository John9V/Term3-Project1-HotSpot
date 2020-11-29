package com.example.hotspot.ui.risks;
/**
 * Is this used? What is this? Wilson did you write this or was it John?
 */

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RisksViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RisksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the risks fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}