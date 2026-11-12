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
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.quickben22.bitcoinlotto.CryptoClass;
import com.quickben22.bitcoinlotto.KeysData;
import com.quickben22.bitcoinlotto.R;

import java.util.ArrayList;

public class ThreeFragment extends Fragment implements KeysData.ChangeListener {

    private View rootView;
    private final ArrayList<TextView> riddleViews = new ArrayList<>();
    private final ArrayList<EditText> solutionViews = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean syncingSolutions;

    public ThreeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_three, container, false);

        collectViews();
        registerWatchers();

        Button next_button = rootView.findViewById(R.id.nextButton);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedIndex = (selectedIndex + 1) % riddleViews.size();
                updateVisibleRiddle();
            }
        });

        Button back_button = rootView.findViewById(R.id.backButton);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedIndex = (riddleViews.size() + selectedIndex - 1) % riddleViews.size();
                updateVisibleRiddle();
            }
        });

        final TextView mSwitcher = rootView.findViewById(R.id.error1);
        final TextView mSwitcher2 = rootView.findViewById(R.id.error2);

        Button inputRiddle_button = rootView.findViewById(R.id.inputRiddle);
        inputRiddle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = CryptoClass.keysD.getSolution1() + CryptoClass.keysD.getSolution2() + CryptoClass.keysD.getSolution3()
                        + CryptoClass.keysD.getSolution4() + CryptoClass.keysD.getSolution5() + CryptoClass.keysD.getSolution6() + CryptoClass.keysD.getSolution7()
                        + CryptoClass.keysD.getSolution8() + CryptoClass.keysD.getSolution9() + CryptoClass.keysD.getSolution10();

                boolean isHex = message.toUpperCase().matches("[0-9A-F]+");
                if (!CryptoClass.keysD.getSolutionCount().equals("64")) {
                    mSwitcher.setVisibility(View.VISIBLE);
                    fadeOutAndHideImage(mSwitcher);
                } else if (!isHex) {
                    mSwitcher2.setVisibility(View.VISIBLE);
                    fadeOutAndHideImage(mSwitcher2);
                } else {
                    CryptoClass.keysD.setPrivateKey(CryptoClass.insertPeriodically(message, " ", 2));
                    CryptoClass.keysD.setInputKey(message);
                    insertToDb();
                }
            }
        });

        Button question_button = rootView.findViewById(R.id.question_button);
        question_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("INFO");
                alertDialog.setMessage("By solving this puzzle you will receive a private key that contains bitcoins. There are 10 riddles, "
                        + "and each riddle contains a part of the private key. To get the full private key you need to solve them all, and combine them. The combined length of all solutions has to be 64. "
                        + "The solutions are comprised of only hexadecimal numbers (123456789ABCDEF). New hints will be published with newer versions. Good luck!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        final TextView mSwitcher3 = rootView.findViewById(R.id.error4);
        Button copy_button3 = rootView.findViewById(R.id.copy_button3);
        copy_button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitcher3.setVisibility(View.VISIBLE);
                fadeOutAndHideImage(mSwitcher3);
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Public key mine", CryptoClass.keysD.getMyKey());
                clipboard.setPrimaryClip(clip);
            }
        });

        final Button enableRiddle = rootView.findViewById(R.id.enableRiddle);
        enableRiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CryptoClass.keysD.setSolution11("1");
                enableRiddle.setEnabled(false);

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("RIDDLE ENABLED");
                alertDialog.setMessage("The riddle challenge is now unlocked.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        render();
        updateVisibleRiddle();
        return rootView;
    }

    private void updateVisibleRiddle() {
        for (int i = 0; i < riddleViews.size(); i++) {
            boolean isSelected = (i == selectedIndex);
            riddleViews.get(i).setVisibility(isSelected ? View.VISIBLE : View.GONE);
            solutionViews.get(i).setVisibility(isSelected ? View.VISIBLE : View.GONE);
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
    public void onKeysDataChanged(KeysData keysData) {
        if (isAdded()) {
            render();
        }
    }

    private void collectViews() {
        riddleViews.clear();
        solutionViews.clear();
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle1));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle2));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle3));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle4));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle5));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle6));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle7));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle8));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle9));
        riddleViews.add((TextView) rootView.findViewById(R.id.Riddle10));

        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText1));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText2));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText3));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText4));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText5));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText6));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText7));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText8));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText9));
        solutionViews.add((EditText) rootView.findViewById(R.id.solutionText10));
    }

    private void registerWatchers() {
        for (int i = 0; i < solutionViews.size(); i++) {
            final int index = i;
            solutionViews.get(i).addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (syncingSolutions) return;
                    setSolution(index, s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void render() {
        if (rootView == null || CryptoClass.keysD == null) return;

        String[] riddleValues = {
                CryptoClass.keysD.getRiddle1(), CryptoClass.keysD.getRiddle2(), CryptoClass.keysD.getRiddle3(),
                CryptoClass.keysD.getRiddle4(), CryptoClass.keysD.getRiddle5(), CryptoClass.keysD.getRiddle6(),
                CryptoClass.keysD.getRiddle7(), CryptoClass.keysD.getRiddle8(), CryptoClass.keysD.getRiddle9(),
                CryptoClass.keysD.getRiddle10()
        };
        for (int i = 0; i < riddleViews.size(); i++) {
            riddleViews.get(i).setText(riddleValues[i]);
        }

        syncingSolutions = true;
        String[] solutionValues = {
                CryptoClass.keysD.getSolution1(), CryptoClass.keysD.getSolution2(), CryptoClass.keysD.getSolution3(),
                CryptoClass.keysD.getSolution4(), CryptoClass.keysD.getSolution5(), CryptoClass.keysD.getSolution6(),
                CryptoClass.keysD.getSolution7(), CryptoClass.keysD.getSolution8(), CryptoClass.keysD.getSolution9(),
                CryptoClass.keysD.getSolution10()
        };
        for (int i = 0; i < solutionViews.size(); i++) {
            EditText editText = solutionViews.get(i);
            if (!editText.getText().toString().equals(solutionValues[i])) {
                editText.setText(solutionValues[i]);
                if (editText.hasFocus()) {
                    editText.setSelection(editText.getText().length());
                }
            }
        }
        syncingSolutions = false;

        ((TextView) rootView.findViewById(R.id.SolutionCounter)).setText("Length: " + CryptoClass.keysD.getSolutionCount());
        ((TextView) rootView.findViewById(R.id.mykey2)).setText(CryptoClass.keysD.getMyKey());

        boolean riddleEnabled = !CryptoClass.keysD.getSolution11().equals("0");
        rootView.findViewById(R.id.nextButton).setEnabled(riddleEnabled);
        rootView.findViewById(R.id.backButton).setEnabled(riddleEnabled);
        rootView.findViewById(R.id.inputRiddle).setEnabled(riddleEnabled);
        rootView.findViewById(R.id.enableRiddle).setEnabled(!riddleEnabled);
    }

    private void setSolution(int index, String value) {
        switch (index) {
            case 0: CryptoClass.keysD.setSolution1(value); break;
            case 1: CryptoClass.keysD.setSolution2(value); break;
            case 2: CryptoClass.keysD.setSolution3(value); break;
            case 3: CryptoClass.keysD.setSolution4(value); break;
            case 4: CryptoClass.keysD.setSolution5(value); break;
            case 5: CryptoClass.keysD.setSolution6(value); break;
            case 6: CryptoClass.keysD.setSolution7(value); break;
            case 7: CryptoClass.keysD.setSolution8(value); break;
            case 8: CryptoClass.keysD.setSolution9(value); break;
            case 9: CryptoClass.keysD.setSolution10(value); break;
        }
    }

    private void fadeOutAndHideImage(final TextView img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(3000);
        fadeOut.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) { img.setVisibility(View.GONE); }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });
        img.startAnimation(fadeOut);
    }

    private void insertToDb() {
        CryptoClass.cl.InsertSearchData(Integer.parseInt(CryptoClass.keysD.getKeysCount()), CryptoClass.remove_extra(CryptoClass.keysD.getPrivateKey()), 0,
                CryptoClass.keysD.getSolution1(), CryptoClass.keysD.getSolution2(), CryptoClass.keysD.getSolution3(), CryptoClass.keysD.getSolution4(), CryptoClass.keysD.getSolution5(),
                CryptoClass.keysD.getSolution6(), CryptoClass.keysD.getSolution7(), CryptoClass.keysD.getSolution8(), CryptoClass.keysD.getSolution9(),
                CryptoClass.keysD.getSolution10(), CryptoClass.keysD.getSolution11());
    }
}
