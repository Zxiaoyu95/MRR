package solution_in_paper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class JAES {
	/**
     * AES�����ַ���
     * 
     * @param content
     *            ��Ҫ�����ܵ��ַ���
     * @param password
     *            ������Ҫ������
     * @return ����
     */
    public static byte[] encrypt(String content, String password) {
        try {
        	KeyGenerator kgen = KeyGenerator.getInstance("AES");// ����AES��Key������
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();// �����û����룬����һ����Կ
            byte[] enCodeFormat = secretKey.getEncoded();// ���ػ��������ʽ����Կ
			 SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// ת��ΪAESר����Կ
			Cipher cipher = Cipher.getInstance("AES");// ����������
			 byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// ��ʼ��Ϊ����ģʽ��������
			byte[] result = cipher.doFinal(byteContent);// ����
			return result;

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
	/**
     * ����AES���ܹ����ַ���
     * 
     * @param content
     *            AES���ܹ���������
     * @param password
     *            ����ʱ������
     * @return ����
     */
    public static byte[] decrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");// ����AES��Key������
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();// �����û����룬����һ����Կ
            byte[] enCodeFormat = secretKey.getEncoded();// ���ػ��������ʽ����Կ
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// ת��ΪAESר����Կ
            Cipher cipher = Cipher.getInstance("AES");// ����������
            cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��Ϊ����ģʽ��������
            byte[] result = cipher.doFinal(content);  
            return result; // ����   
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * ��������ת����16����
     * 
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * ��16����ת��Ϊ������
     * 
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
		String content = "2018-1-31";
		String password = "123";
		System.out.println("����ǰ��"+content);
		byte[] encrypt = JAES.encrypt(content, password);
		String code = JAES.parseByte2HexStr(encrypt);
	    System.out.println("�����ַ�����" + code);
		byte[] decrypt = JAES.decrypt(JAES.parseHexStr2Byte(code), password);
		System.out.println("���ܺ�"+ new String(decrypt,"UTF-8"));
		
	}
}

