package com.quickben22.bitcoinlotto.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.quickben22.bitcoinlotto.CryptoClass;
import com.quickben22.bitcoinlotto.KeysData;
import com.quickben22.bitcoinlotto.R;
import com.quickben22.bitcoinlotto.CrackingClass;
import com.quickben22.bitcoinlotto.SqliteClass;

public class OneFragment extends Fragment implements KeysData.ChangeListener {

    private View rootView;
    private CrackingClass crackingClass;
    private Thread crackingThread;
    private Button searchButton;
    private SqliteClass sqliteClass;
    private AdView mAdView;

    public OneFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_one, container, false);
        sqliteClass = new SqliteClass(getContext());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            searchButton = getActivity().findViewById(R.id.search_button);
        }
        mAdView = view.findViewById(R.id.adView);
        
        if (mAdView != null) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(new AdRequest.Builder().build());
        }
        
        if (searchButton != null) {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (crackingClass == null || !crackingClass.get_running()) {
                        startCracking();
                    } else {
                        stopCracking();
                    }
                }
            });
        }

        render();
    }

    private void startCracking() {
        if (crackingClass != null && crackingClass.get_running()) return;
        
        crackingClass = new CrackingClass(sqliteClass);
        crackingClass.set_running();
        crackingThread = new Thread(crackingClass);
        crackingThread.start();
    }

    private void stopCracking() {
        if (crackingClass != null) {
            crackingClass.terminate();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CryptoClass.keysD.addListener(this);
        render();
    }

    @Override
    public void onStop() {
        CryptoClass.keysD.removeListener(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onKeysDataChanged(KeysData keysData) {
        if (isAdded() && getActivity() != null) {
            render();
        }
    }

    private void render() {
        if (rootView == null || CryptoClass.keysD == null) {
            return;
        }

        if (getActivity() != null) {
            TextView privateTb = getActivity().findViewById(R.id.private_tb);
            if (privateTb != null) {
                privateTb.setText(CryptoClass.keysD.getPrivateKey());
                privateTb.setTextColor(CryptoClass.keysD.getPrivateKeyColor());
            }
        }

        TextView combinationsTb = rootView.findViewById(R.id.combinations_tb);
        if (combinationsTb != null) {
            combinationsTb.setText(CryptoClass.keysD.getKeysCount());
        }
        TextView durationTb = rootView.findViewById(R.id.duration_tb);
        if (durationTb != null) {
            durationTb.setText(CryptoClass.keysD.getTimer());
        }
        TextView speedTb = rootView.findViewById(R.id.speed_tb);
        if (speedTb != null) {
            speedTb.setText(CryptoClass.keysD.getSpeed());
        }

        if (searchButton != null) {
            searchButton.setText(CryptoClass.keysD.isRunning() ? "STOP SEARCH" : "START SEARCH");
        }

        // Handle alerts
        String title = CryptoClass.keysD.getAlertTitle();
        String message = CryptoClass.keysD.getAlertMessage();
        if (title != null && message != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}
