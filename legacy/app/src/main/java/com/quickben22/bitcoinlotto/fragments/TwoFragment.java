package com.quickben22.bitcoinlotto.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.quickben22.bitcoinlotto.CryptoClass;
import com.quickben22.bitcoinlotto.KeysData;
import com.quickben22.bitcoinlotto.R;

public class TwoFragment extends Fragment implements KeysData.ChangeListener {

    private View rootView;
    private TextView inputText;
    private EditText privateInput;
    private boolean syncingInput;

    public TwoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_two, container, false);

        Button private_input_button = rootView.findViewById(R.id.private_button);
        final TextView mSwitcher = rootView.findViewById(R.id.error3);
        final TextView mSwitcher2 = rootView.findViewById(R.id.error5);

        private_input_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = CryptoClass.GetPrivateKey(CryptoClass.keysD.getInputKey());
                boolean isHex = message.toUpperCase().matches("[0-9A-F]+");
                if (message.length() == 64 && isHex) {
                    CryptoClass.keysD.setPrivateKey(CryptoClass.insertPeriodically(message, " ", 2));
                } else {
                    mSwitcher.setVisibility(View.VISIBLE);
                    fadeOutAndHideImage(mSwitcher);
                }
            }
        });

        Button random_button = rootView.findViewById(R.id.random_button);
        random_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = CryptoClass.random();
                CryptoClass.keysD.setPrivateKey(CryptoClass.insertPeriodically(message, " ", 2));
                CryptoClass.keysD.setInputKey(message);
            }
        });

        Button private_public_button = rootView.findViewById(R.id.private_public_button);
        private_public_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String privText = CryptoClass.remove_extra(CryptoClass.keysD.getPrivateKey());
                if (privText.length() != 64) return;
                byte[] hex = CryptoClass.hexStringToByteArray(privText);
                String[] message = CryptoClass.generateAddresses(hex);
                CryptoClass.keysD.setPublicKey_compressed(message[0]);
                CryptoClass.keysD.setPublicKey_uncompressed(message[1]);
            }
        });

        Button private_WIF_button = rootView.findViewById(R.id.private_wif_button);
        private_WIF_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String privText = CryptoClass.remove_extra(CryptoClass.keysD.getPrivateKey());
                if (privText.length() != 64) return;
                byte[] hex = CryptoClass.hexStringToByteArray(privText);
                String[] message = CryptoClass.GeneratePrivHexToWIF(hex);
                CryptoClass.keysD.setPublicKey_compressed(CryptoClass.insertPeriodically(message[0], "", 1));
                CryptoClass.keysD.setPublicKey_uncompressed(CryptoClass.insertPeriodically(message[1], "", 1));
            }
        });

        Button copy_button1 = rootView.findViewById(R.id.copy_button1);
        copy_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Public key compressed", CryptoClass.remove_extra(CryptoClass.keysD.getPublicKey_compressed()));
                clipboard.setPrimaryClip(clip);
                mSwitcher2.setVisibility(View.VISIBLE);
                fadeOutAndHideImage(mSwitcher2);
            }
        });

        Button copy_button2 = rootView.findViewById(R.id.copy_button2);
        copy_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Public key uncompressed", CryptoClass.remove_extra(CryptoClass.keysD.getPublicKey_uncompressed()));
                clipboard.setPrimaryClip(clip);
                mSwitcher2.setVisibility(View.VISIBLE);
                fadeOutAndHideImage(mSwitcher2);
            }
        });

        Button info_button = rootView.findViewById(R.id.wif_info_button);
        info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Accessing bitcoins?");
                alertDialog.setMessage("How to access a lost bitcoin the app finds? \n 1. First you need to click the 'Wallet Key' button, which generates the 'Wallet import format' key (WIF)."
                        + "\n 2. Install a bitcoin wallet app (for example Mycelium wallet)."
                        + "\n 3. Add a new account based on a private key to the wallet. "
                        + "\n 4. Copy one of the generated WIF keys and insert them into the account made in step 3.  "
                        + "\n That's it! Now you can access the bitcoins.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        privateInput = rootView.findViewById(R.id.private_tx);
        privateInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncingInput) {
                    CryptoClass.keysD.setInputKey(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        inputText = rootView.findViewById(R.id.LetterCounter);
        render();
        return rootView;
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
    public void onKeysDataChanged(KeysData keysData) {
        render();
    }

    private void render() {
        if (rootView == null || CryptoClass.keysD == null) {
            return;
        }
        syncingInput = true;
        String inputKey = CryptoClass.keysD.getInputKey();
        if (!privateInput.getText().toString().equals(inputKey)) {
            privateInput.setText(inputKey);
            privateInput.setSelection(privateInput.getText().length());
        }
        syncingInput = false;

        inputText.setText(CryptoClass.keysD.getCharacterCount());
        ((TextView) rootView.findViewById(R.id.public_tb)).setText(CryptoClass.keysD.getPublicKey_compressed());
        ((TextView) rootView.findViewById(R.id.public_tb2)).setText(CryptoClass.keysD.getPublicKey_uncompressed());

        int visibility = CryptoClass.keysD.getPublicKey_compressed().isEmpty() ? View.GONE : View.VISIBLE;
        rootView.findViewById(R.id.copy_button1).setVisibility(visibility);
        rootView.findViewById(R.id.copy_button2).setVisibility(
                CryptoClass.keysD.getPublicKey_uncompressed().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void fadeOutAndHideImage(final TextView img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(3000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
}
