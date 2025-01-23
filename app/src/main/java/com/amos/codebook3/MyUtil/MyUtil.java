package com.amos.codebook3.MyUtil;
import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
public class MyUtil {


        // 生成16位随机可显示字符
        public static String generateRandomKey(int length) {
            // ASCII 可打印字符的范围：32 - 126
            int min = 32;
            int max = 126;

            Random random = new Random();

            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < length; i++) {
                char randomChar = (char) (random.nextInt(max - min + 1) + min);
                stringBuilder.append(randomChar);
            }

            return stringBuilder.toString();
        }

    /**
     * 使用SHA-256对密码进行哈希
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }




}
