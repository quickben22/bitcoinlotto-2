package com.quickben22.bitcoinlotto;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeysData {
    public interface ChangeListener {
        void onKeysDataChanged(KeysData keysData);
    }

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<ChangeListener> listeners = new ArrayList<>();

    private String privateKey = "";
    private String publicKey_uncompressed = "";
    private String publicKey_compressed = "";
    private String inputKey = "";
    private String characterCount = "0";
    private String keysCount = "0";
    private String timer = "00:00:00";
    private String startAddress = "";
    private String endAddress = "";
    private String speed = "0.00 keys/s";
    private String solutionCount = "0";
    private String riddle1 = "";
    private String solution1 = "";
    private String riddle2 = "";
    private String solution2 = "";
    private String riddle3 = "";
    private String solution3 = "";
    private String riddle4 = "";
    private String solution4 = "";
    private String riddle5 = "";
    private String solution5 = "";
    private String riddle6 = "";
    private String solution6 = "";
    private String riddle7 = "";
    private String solution7 = "";
    private String riddle8 = "";
    private String solution8 = "";
    private String riddle9 = "";
    private String solution9 = "";
    private String riddle10 = "";
    private String solution11 = "0";
    private String solution10 = "";
    private String myKey = "";
    
    // New fields for UI state synchronization
    private int privateKeyColor = Color.parseColor("#B3FFFF");
    private String alertTitle = null;
    private String alertMessage = null;
    private boolean isRunning = false;

    public KeysData(String PrivateKey, String PublicKey_uncompressed, String PublicKey_compressed, String InputKey, String CharacterCount, String KeysCount,
                    String Timer, String Speed, String StartAddress, String EndAddress, String Riddle1, String Solution1, String Riddle2, String Solution2, String Riddle3, String Solution3,
                    String Riddle4, String Solution4, String Riddle5, String Solution5, String Riddle6, String Solution6, String Riddle7, String Solution7, String Riddle8, String Solution8,
                    String Riddle9, String Solution9, String Riddle10, String Solution10, String Solution11,
                    String SolutionCount, String MyKey) {
        this.privateKey = PrivateKey;
        this.publicKey_uncompressed = PublicKey_uncompressed;
        this.publicKey_compressed = PublicKey_compressed;
        this.inputKey = InputKey;
        this.characterCount = CharacterCount;
        this.keysCount = KeysCount;
        this.timer = Timer;
        this.speed = Speed;
        this.startAddress = StartAddress;
        this.endAddress = EndAddress;
        this.solutionCount = SolutionCount;
        this.riddle1 = Riddle1;
        this.solution1 = Solution1;
        this.riddle2 = Riddle2;
        this.solution2 = Solution2;
        this.riddle3 = Riddle3;
        this.solution3 = Solution3;
        this.riddle4 = Riddle4;
        this.solution4 = Solution4;
        this.riddle5 = Riddle5;
        this.solution5 = Solution5;
        this.riddle6 = Riddle6;
        this.solution6 = Solution6;
        this.riddle7 = Riddle7;
        this.solution7 = Solution7;
        this.riddle8 = Riddle8;
        this.solution8 = Solution8;
        this.riddle9 = Riddle9;
        this.solution9 = Solution9;
        this.riddle10 = Riddle10;
        this.solution11 = Solution11;
        this.solution10 = Solution10;
        this.myKey = MyKey;
    }

    public void addListener(ChangeListener listener) {
        synchronized (listeners) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void notifyChanged() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            List<ChangeListener> snapshot;
            synchronized (listeners) {
                snapshot = new ArrayList<>(listeners);
            }
            for (ChangeListener listener : snapshot) {
                listener.onKeysDataChanged(this);
            }
        } else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyChanged();
                }
            });
        }
    }

    public String getPrivateKey() { return privateKey; }
    public String getPublicKey_uncompressed() { return publicKey_uncompressed; }
    public String getPublicKey_compressed() { return publicKey_compressed; }
    public String getInputKey() { return inputKey; }
    public String getCharacterCount() { return characterCount; }
    public String getKeysCount() { return keysCount; }
    public String getTimer() { return timer; }
    public String getSpeed() { return speed; }
    public String getStartAddress() { return startAddress; }
    public String getEndAddress() { return endAddress; }
    public String getSolutionCount() { return solutionCount; }
    public String getRiddle1() { return riddle1; }
    public String getSolution1() { return solution1; }
    public String getRiddle2() { return riddle2; }
    public String getSolution2() { return solution2; }
    public String getRiddle3() { return riddle3; }
    public String getSolution3() { return solution3; }
    public String getRiddle4() { return riddle4; }
    public String getSolution4() { return solution4; }
    public String getRiddle5() { return riddle5; }
    public String getSolution5() { return solution5; }
    public String getRiddle6() { return riddle6; }
    public String getSolution6() { return solution6; }
    public String getRiddle7() { return riddle7; }
    public String getSolution7() { return solution7; }
    public String getRiddle8() { return riddle8; }
    public String getSolution8() { return solution8; }
    public String getRiddle9() { return riddle9; }
    public String getSolution9() { return solution9; }
    public String getRiddle10() { return riddle10; }
    public String getSolution11() { return solution11; }
    public String getSolution10() { return solution10; }
    public String getMyKey() { return myKey; }
    public int getPrivateKeyColor() { return privateKeyColor; }
    public String getAlertTitle() { return alertTitle; }
    public String getAlertMessage() { return alertMessage; }
    public boolean isRunning() { return isRunning; }

    public void setPrivateKey(String val) { if (!Objects.equals(privateKey, val)) { privateKey = val; notifyChanged(); } }
    public void setPublicKey_uncompressed(String val) { if (!Objects.equals(publicKey_uncompressed, val)) { publicKey_uncompressed = val; notifyChanged(); } }
    public void setPublicKey_compressed(String val) { if (!Objects.equals(publicKey_compressed, val)) { publicKey_compressed = val; notifyChanged(); } }
    public void setInputKey(String val) { if (!Objects.equals(inputKey, val)) { inputKey = val; characterCount = String.valueOf(val.length()); notifyChanged(); } }
    public void setCharacterCount(String val) { if (!Objects.equals(characterCount, val)) { characterCount = val; notifyChanged(); } }
    public void setKeysCount(String val) { if (!Objects.equals(keysCount, val)) { keysCount = val; notifyChanged(); } }
    public void setTimer(String val) { if (!Objects.equals(timer, val)) { timer = val; notifyChanged(); } }
    public void setSpeed(String val) { if (!Objects.equals(speed, val)) { speed = val; notifyChanged(); } }
    public void setStartAddress(String val) { if (!Objects.equals(startAddress, val)) { startAddress = val; notifyChanged(); } }
    public void setEndAddress(String val) { if (!Objects.equals(endAddress, val)) { endAddress = val; notifyChanged(); } }
    public void setRiddle1(String val) { if (!Objects.equals(riddle1, val)) { riddle1 = val; notifyChanged(); } }
    public void setSolution1(String val) { if (!Objects.equals(solution1, val)) { solution1 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle2(String val) { if (!Objects.equals(riddle2, val)) { riddle2 = val; notifyChanged(); } }
    public void setSolution2(String val) { if (!Objects.equals(solution2, val)) { solution2 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle3(String val) { if (!Objects.equals(riddle3, val)) { riddle3 = val; notifyChanged(); } }
    public void setSolution3(String val) { if (!Objects.equals(solution3, val)) { solution3 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle4(String val) { if (!Objects.equals(riddle4, val)) { riddle4 = val; notifyChanged(); } }
    public void setSolution4(String val) { if (!Objects.equals(solution4, val)) { solution4 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle5(String val) { if (!Objects.equals(riddle5, val)) { riddle5 = val; notifyChanged(); } }
    public void setSolution5(String val) { if (!Objects.equals(solution5, val)) { solution5 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle6(String val) { if (!Objects.equals(riddle6, val)) { riddle6 = val; notifyChanged(); } }
    public void setSolution6(String val) { if (!Objects.equals(solution6, val)) { solution6 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle7(String val) { if (!Objects.equals(riddle7, val)) { riddle7 = val; notifyChanged(); } }
    public void setSolution7(String val) { if (!Objects.equals(solution7, val)) { solution7 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle8(String val) { if (!Objects.equals(riddle8, val)) { riddle8 = val; notifyChanged(); } }
    public void setSolution8(String val) { if (!Objects.equals(solution8, val)) { solution8 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle9(String val) { if (!Objects.equals(riddle9, val)) { riddle9 = val; notifyChanged(); } }
    public void setSolution9(String val) { if (!Objects.equals(solution9, val)) { solution9 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setRiddle10(String val) { if (!Objects.equals(riddle10, val)) { riddle10 = val; notifyChanged(); } }
    public void setSolution11(String val) { if (!Objects.equals(solution11, val)) { solution11 = val; notifyChanged(); } }
    public void setSolution10(String val) { if (!Objects.equals(solution10, val)) { solution10 = val; solutionCount = zbroj(); notifyChanged(); } }
    public void setSolutionCount(String val) { if (!Objects.equals(solutionCount, val)) { solutionCount = val; notifyChanged(); } }
    public void setMyKey(String val) { if (!Objects.equals(myKey, val)) { myKey = val; notifyChanged(); } }
    
    public void setPrivateKeyColor(int val) { if (privateKeyColor != val) { privateKeyColor = val; notifyChanged(); } }
    public void setRunning(boolean val) { if (isRunning != val) { isRunning = val; notifyChanged(); } }

    public void triggerAlert(String title, String message) {
        this.alertTitle = title;
        this.alertMessage = message;
        notifyChanged();
        // Reset after notification
        this.alertTitle = null;
        this.alertMessage = null;
    }

    private String zbroj() {
        return String.valueOf(safeLen(solution1) + safeLen(solution2) + safeLen(solution3) + safeLen(solution4) + safeLen(solution5) +
                safeLen(solution6) + safeLen(solution7) + safeLen(solution8) + safeLen(solution9) + safeLen(solution10));
    }
    
    private int safeLen(String s) { return s == null ? 0 : s.length(); }
}
