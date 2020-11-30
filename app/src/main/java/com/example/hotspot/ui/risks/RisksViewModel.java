package com.example.hotspot.ui.risks;
/**
 * Risks view model to help connect our list of risks
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