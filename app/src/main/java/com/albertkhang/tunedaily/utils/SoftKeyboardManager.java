package com.albertkhang.tunedaily.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboardManager {
    public void showSoftKeyboard(View view) {
//        if (view.requestFocus()) {
//            InputMethodManager imm = (InputMethodManager)
//                    getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        }
    }

    public static void hideSoftKeyboard(View view, Activity activity) {
        InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
