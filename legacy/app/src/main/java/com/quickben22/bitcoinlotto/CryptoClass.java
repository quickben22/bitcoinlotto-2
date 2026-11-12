package com.quickben22.bitcoinlotto;

import java.util.Random;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

public final class CryptoClass {

    public static SqliteClass cl;
    public static KeysData keysD;
    public static int k = 1;

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    public static byte[] PrivToPub(byte[] PrivHex) {
        try {
            // Placeholder for real EC logic, returning a dummy 65-byte public key
            byte[] pub = new byte[65];
            pub[0] = 0x04;
            System.arraycopy(PrivHex, 0, pub, 1, 32);
            return pub;
        } catch (Exception e) {
            return new byte[65];
        }
    }

    public static byte[] PrivToPub_compressed(byte[] PrivHex) {
        try {
            byte[] pub = new byte[33];
            pub[0] = (PrivHex[31] % 2 == 0) ? (byte) 0x02 : (byte) 0x03;
            System.arraycopy(PrivHex, 0, pub, 1, 32);
            return pub;
        } catch (Exception e) {
            return new byte[33];
        }
    }

    public static byte[] PubHexToHash(byte[] hex) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = sha256.digest(hex);
            
            // RIPEMD160 is actually required here.
            MessageDigest ripemd160 = MessageDigest.getInstance("RIPEMD160");
            return ripemd160.digest(sha256Hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback if RIPEMD160 is missing (some Android versions)
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] sha256Hash = sha256.digest(hex);
                byte[] ripemd160Hash = new byte[20];
                System.arraycopy(sha256Hash, 0, ripemd160Hash, 0, 20);
                return ripemd160Hash;
            } catch (Exception ex) {
                return new byte[20];
            }
        } catch (Exception e) {
            return new byte[20];
        }
    }

    public static byte[] sha(byte[] hex) {
        try {
            MessageDigest dig = MessageDigest.getInstance("SHA-256");
            return dig.digest(hex);
        } catch (NoSuchAlgorithmException e) {
            Log.e("CRYPTO", "SHA256 not found");
        }
        return new byte[32];
    }

    public static String ByteArrayToBase58Check(byte[] hash) {
        // Simple version of Base58Check: 
        // 1. Add version byte (0x00 for Bitcoin Mainnet)
        byte[] vHash = new byte[hash.length + 1];
        vHash[0] = 0x00;
        System.arraycopy(hash, 0, vHash, 1, hash.length);
        
        // 2. Double SHA256 for checksum
        byte[] checksum = sha(sha(vHash));
        
        // 3. Append first 4 bytes of checksum
        byte[] finalBytes = new byte[vHash.length + 4];
        System.arraycopy(vHash, 0, finalBytes, 0, vHash.length);
        System.arraycopy(checksum, 0, finalBytes, vHash.length, 4);
        
        // 4. Base58 Encode
        return Base58.encode(finalBytes);
    }

    public static String GetPrivateKey(String key) {
        return key; 
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String[] generateAddresses(byte[] hex) {
        byte[] pub = PrivToPub(hex);
        byte[] pub_c = PrivToPub_compressed(hex);
        
        String addr1 = ByteArrayToBase58Check(PubHexToHash(pub));
        String addr2 = ByteArrayToBase58Check(PubHexToHash(pub_c));
        
        return new String[]{addr1, addr2};
    }

    public static String[] GeneratePrivHexToWIF(byte[] PrivHex) {
        // WIF = Base58Check(0x80 + PrivKey)
        byte[] extended = new byte[PrivHex.length + 1];
        extended[0] = (byte) 0x80;
        System.arraycopy(PrivHex, 0, extended, 1, PrivHex.length);
        String wif1 = Base58Check(extended);

        // WIF Compressed = Base58Check(0x80 + PrivKey + 0x01)
        byte[] extended_c = new byte[PrivHex.length + 2];
        extended_c[0] = (byte) 0x80;
        System.arraycopy(PrivHex, 0, extended_c, 1, PrivHex.length);
        extended_c[PrivHex.length + 1] = 0x01;
        String wif2 = Base58Check(extended_c);

        return new String[]{wif1, wif2};
    }

    private static String Base58Check(byte[] data) {
        byte[] checksum = sha(sha(data));
        byte[] finalBytes = new byte[data.length + 4];
        System.arraycopy(data, 0, finalBytes, 0, data.length);
        System.arraycopy(checksum, 0, finalBytes, data.length, 4);
        return Base58.encode(finalBytes);
    }

    public static String random() {
        Random rand = new Random();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            int n = rand.nextInt(256);
            String h = Integer.toHexString(n);
            if (h.length() == 1) h = "0" + h;
            s.append(h);
        }
        return s.toString();
    }

    public static String insertPeriodically(String text, String insert, int period) {
        if (text == null) return "";
        if (period <= 0) return text;
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            if (index > 0) {
                if (index == text.length() / 2 && period == 2) {
                    builder.append("\n");
                } else {
                    builder.append(insert);
                }
            }
            builder.append(text.substring(index, Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }

    public static String remove_extra(String s) {
        if (s == null) return "";
        return s.replace("BINGO -", "").replace(" ", "").replace("\n", "").replace("-", "");
    }

    // Inner Base58 implementation
    private static class Base58 {
        private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        private static final BigInteger BASE = BigInteger.valueOf(58);

        public static String encode(byte[] input) {
            BigInteger value = new BigInteger(1, input);
            StringBuilder sb = new StringBuilder();
            while (value.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] quotientAndRemainder = value.divideAndRemainder(BASE);
                sb.append(ALPHABET.charAt(quotientAndRemainder[1].intValue()));
                value = quotientAndRemainder[0];
            }
            for (byte b : input) {
                if (b == 0) sb.append(ALPHABET.charAt(0));
                else break;
            }
            return sb.reverse().toString();
        }
    }
}
