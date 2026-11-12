package com.quickben22.bitcoinlotto;

import android.content.Context;

public class GdprHelper {

    private final Context context;

    public GdprHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void initialise() {
        // Legacy consent SDKs were removed with the old ad stack.
    }

    public void resetConsent() {
        // No persisted consent state remains in the app layer.
    }
}
