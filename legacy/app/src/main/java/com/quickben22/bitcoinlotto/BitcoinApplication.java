package com.quickben22.bitcoinlotto;

import android.app.Application;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import java.util.Arrays;
import java.util.List;

public class BitcoinApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Use a list of test device IDs.
        // Google's documentation suggests adding the hashed ID of the device if it's a real device.
        // For emulators, "B3EEABB8F87CD3E2445E6E5651165910" is often used as a generic test ID, 
        // but we'll use a broad configuration for now.
        List<String> testDeviceIds = Arrays.asList(AdRequest.DEVICE_ID_EMULATOR, "489428617CF1FB9CB9CBAC76747BF358");
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build();
        MobileAds.setRequestConfiguration(configuration);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }
}
