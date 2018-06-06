import java.io.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;

public class JEncrypRSA {
    
    private static PrivateKey private_key;
    private static PublicKey public_key;
    private static Cipher ecipher;
    private static Cipher dcipher;

    public static void generateKey(){
        KeyPairGenerator key_pair = null;
        try {
            key_pair = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        key_pair.initialize(1024);
        KeyPair keys = key_pair.generateKeyPair();

        private_key = keys.getPrivate();
        public_key = keys.getPublic();
    }
    public static String encrypt(String str, PublicKey key){
        byte[] encrypted_byte = new byte[0];
        byte[] encoded_byte;
        String encoded_string;

        try {
            ecipher = Cipher.getInstance("RSA");
            ecipher.init(Cipher.ENCRYPT_MODE,key);
            encrypted_byte = ecipher.doFinal(str.getBytes("UTF8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        encoded_byte = Base64.getEncoder().encode(encrypted_byte);
        encoded_string = new String(encoded_byte);

        return encoded_string;
    }

    public static String decrypt(String str, PrivateKey key){
        byte[] decrypted_byte;
        byte[] decoded_byte;
        String decoded_string = null;

        decoded_byte = Base64.getDecoder().decode(str.getBytes());

        try {
            dcipher = Cipher.getInstance("RSA");
            dcipher.init(Cipher.DECRYPT_MODE,key);
            decrypted_byte = dcipher.doFinal(decoded_byte);
            decoded_string = new String(decrypted_byte, "UTF8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return decoded_string;
    }
    public static void main(String[] args) throws Exception{
        String message;
        String encrypted;
        String decrypted;
        Scanner input = new Scanner(System.in);

        generateKey();
        System.out.print("Enter a message: ");
        message = input.nextLine();

        encrypted = encrypt(message,public_key);
        decrypted = decrypt(encrypted,private_key);

        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);

    }
}
