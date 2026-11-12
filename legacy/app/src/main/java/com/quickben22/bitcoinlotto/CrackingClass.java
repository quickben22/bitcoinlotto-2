package com.quickben22.bitcoinlotto;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.Locale;

public class CrackingClass implements Runnable {

    private volatile boolean running = false;
    private final SqliteClass sqlcl;
    private String start;
    private long startTime;
    public int domet1 = 1000;
    public int domet2 = 10000;

    public CrackingClass(SqliteClass sq) {
        sqlcl = sq;
    }

    public void terminate() {
        running = false;
        CryptoClass.keysD.setRunning(false);
    }

    public boolean get_running() {
        return running;
    }

    public void set_running() {
        start = CryptoClass.remove_extra(CryptoClass.keysD.getPrivateKey());
        String[] povrat = sqlcl.CheckIfAlreadyChecked(start);
        if (!povrat[1].equals("")) {
            CryptoClass.keysD.setPrivateKey(CryptoClass.insertPeriodically(povrat[1], " ", 2));
            CryptoClass.keysD.setEndAddress(povrat[1]);
        } else {
            sqlcl.InsertAddressData(povrat[0], povrat[0]);
        }
        CryptoClass.keysD.setStartAddress(povrat[0]);
        startTime = System.currentTimeMillis();
        running = true;
        CryptoClass.keysD.setRunning(true);
        CryptoClass.keysD.setPrivateKeyColor(Color.parseColor("#B3FFFF"));
    }

    @Override
    public void run() {
        boolean ima_bingo = false;
        if (CryptoClass.keysD.getPrivateKey().contains("BINGO") || CryptoClass.keysD.getPrivateKey().contains("CHECKED"))
            ima_bingo = true;
        
        String PrivText = CryptoClass.remove_extra(CryptoClass.keysD.getPrivateKey());
        if (PrivText.length() != 64) {
            running = false;
            CryptoClass.keysD.setRunning(false);
            return;
        }
        
        byte[] PrivHex = CryptoClass.hexStringToByteArray(PrivText);
        if (!ima_bingo)
            PrivHex[31]--;
            
        int k = Integer.parseInt(CryptoClass.keysD.getKeysCount());
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        int k2 = 1;
        
        while (running) {
            try {
                CryptoClass.k = k2;
                k2++;
                k++;
                PrivHex[31]++;
                
                // Optimized hex increment logic
                if (PrivHex[31] == 0) {
                    for (int l = 30; l >= 0; l--) {
                        PrivHex[l]++;
                        if (PrivHex[l] != 0)
                            break;
                    }
                }
                
                PrivText = CryptoClass.bytesToHex(PrivHex);

                String[] message = CryptoClass.generateAddresses(PrivHex);
                list.add(message[0]);
                list.add(message[1]);
                list2.add(PrivText);
                list2.add(PrivText);

                String ispis_temp = CryptoClass.insertPeriodically(PrivText, " ", 2);

                if (k % 50 == 0) {
                    CryptoClass.cl.InsertSearchData(k, PrivText, 0,
                            CryptoClass.keysD.getSolution1(), CryptoClass.keysD.getSolution2(), CryptoClass.keysD.getSolution3(), CryptoClass.keysD.getSolution4(), CryptoClass.keysD.getSolution5(),
                            CryptoClass.keysD.getSolution6(), CryptoClass.keysD.getSolution7(), CryptoClass.keysD.getSolution8(), CryptoClass.keysD.getSolution9(),
                            CryptoClass.keysD.getSolution10(), CryptoClass.keysD.getSolution11());

                    String[] povrat = sqlcl.CheckIfAlreadyChecked(PrivText);
                    if (!povrat[1].equals("")) {
                        CryptoClass.cl.InsertAddressData(CryptoClass.keysD.getStartAddress(), povrat[1]);
                        PrivHex = CryptoClass.hexStringToByteArray(povrat[1]);
                        ispis_temp = CryptoClass.insertPeriodically(povrat[1], " ", 2);
                        PrivText = povrat[1];
                        CryptoClass.keysD.setEndAddress(povrat[1]);
                    } else {
                        CryptoClass.cl.InsertAddressData(CryptoClass.keysD.getStartAddress(), PrivText);
                    }

                    ArrayList<String> izlaz = sqlcl.CheckIsDataAlreadyInDBorNot(list, list2);

                    if (!izlaz.isEmpty()) {
                        CryptoClass.keysD.setPrivateKey("BINGO - " + izlaz.get(0));
                        CryptoClass.keysD.setEndAddress("BINGO - " + izlaz.get(0));
                        CryptoClass.keysD.setPrivateKeyColor(Color.parseColor("#FF0000"));
                        CryptoClass.keysD.triggerAlert("BINGO!", "You have found a private key!");
                        running = false;
                        CryptoClass.keysD.setRunning(false);
                    } else if (list.contains("1NJB66wEsqtFBd3zSFfcdZoLpV6fCrzEP1")) {
                        String foundKey = list2.get(list.indexOf("1NJB66wEsqtFBd3zSFfcdZoLpV6fCrzEP1"));
                        CryptoClass.keysD.setPrivateKey(CryptoClass.insertPeriodically(foundKey, " ", 2));
                        CryptoClass.keysD.setEndAddress(foundKey);
                        CryptoClass.keysD.setPrivateKeyColor(Color.parseColor("#FF0000"));
                        CryptoClass.keysD.triggerAlert("PUZZLE SOLVED!", "CONGRATULATIONS!");
                        running = false;
                        CryptoClass.keysD.setRunning(false);
                    }

                    list.clear();
                    list2.clear();

                    if (k >= domet1 && (k == domet1 || k % domet2 == 0)) {
                        CryptoClass.keysD.triggerAlert("SEARCH LIMIT!", "Limit reached.");
                        running = false;
                        CryptoClass.keysD.setRunning(false);
                    }

                    if (running) {
                        CryptoClass.keysD.setPrivateKey(ispis_temp);
                    }
                    
                    CryptoClass.keysD.setKeysCount(Integer.toString(k));
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed > 0) {
                        long seconds = (elapsed / 1000) % 60;
                        long minutes = (elapsed / (1000 * 60)) % 60;
                        long hours = (elapsed / (1000 * 60 * 60));
                        CryptoClass.keysD.setTimer(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));

                        double speedValue = (double) k2 / (elapsed / 1000.0);
                        CryptoClass.keysD.setSpeed(String.format(Locale.US, "%.2f", speedValue));
                    }
                }
                Thread.sleep(1);
            } catch (Exception e) {
                running = false;
                CryptoClass.keysD.setRunning(false);
            }
        }
    }
}
