import java.io.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;


public class JEncrypDES {

    private static Cipher ecipher;
    private static Cipher dcipher;
    private static SecretKey key;

    public static void main(String[] args){
        String message;
        Scanner input = new Scanner(System.in);
        System.out.print("Enter a message: ");
        message = input.nextLine();

        try {
            key = KeyGenerator.getInstance("DES").generateKey();
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        String encrypted = encrypt(message);
        System.out.println("Encrypted: " + encrypted);
        String decrypted = decrypt(encrypted);
        System.out.println("Decrypted: " + decrypted);


    }

    public static String encrypt(String str){


        byte[] encrypted_byte = new byte[0];
        byte[] encoded_to_base64;
        String encoded_string;
        try {
            encrypted_byte = ecipher.doFinal(str.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        encoded_to_base64 = Base64.getEncoder().encode(encrypted_byte);
        encoded_string = new String(encoded_to_base64);


        return encoded_string;
    }

    public static String decrypt(String str){
        byte[] decrypted_byte;
        byte[] decoded_from_base64;
        String decrypted_string = new String();

        decoded_from_base64 = Base64.getDecoder().decode(str.getBytes());
        try {
            decrypted_byte = dcipher.doFinal(decoded_from_base64);
            decrypted_string = new String(decrypted_byte, "UTF8");

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return decrypted_string;


    }

}
