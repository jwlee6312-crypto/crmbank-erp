package com.crmbank.erp.comm.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 암호화 유틸리티 클래스 (util 패키지로 이동)
 */
public class Cm_encrypt {

    // 비밀번호 암호화용 (단방향 해시)
    public static String encryptSha256(String value) {
        if (value == null) {
            throw new IllegalArgumentException("암호화하려는 값이 null입니다.");
        }
        MessageDigest md;
        StringBuilder sb = new StringBuilder("");

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes());
            byte[] byteData = md.digest();

            for (byte tmpStrByte : byteData) {
                String tmpEncTxt = Integer.toString((tmpStrByte & 0xff) + 0x100, 16).substring(1);
                sb.append(tmpEncTxt);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // 개인정보 (이름, 이메일, 주소, 주민번호 등) 암호화
    public static String encryptAes(String str, String key) throws Exception {
        if (str == null || key == null) {
            return null;
        }

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encPassword = cipher.doFinal(str.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encPassword);
    }

    // 개인정보 복호화
    public static String decryptAes(String str, String key) throws Exception {
        if (str == null || key == null) {
            return null;
        }

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decPassword = cipher.doFinal(Base64.getDecoder().decode(str));
        return new String(decPassword, "UTF-8");
    }
}
