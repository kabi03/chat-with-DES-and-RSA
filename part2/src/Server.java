import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.Base64;

public class Server {

    private int port;
    private JEncryptRSA srsa;
    private JEncryptDES sdes;
    private PublicKey spub;
    private PrivateKey spri;
    private ServerSocket sc;
    private Socket responder;
    private ObjectOutputStream so;
    private ObjectInputStream si;
    private PublicKey client_public;
    private String identity;
    private int expected_nonce;
    private SecretKey sessionkey;
    private int sent_nonce = 0;
    private int recv_nonce = 0;


    Server(int port, String identity){
        this.port = port;
        srsa = new JEncryptRSA();
        sdes = new JEncryptDES();
        this.identity = identity;


    }

    public void start(){
        try {
            System.out.println("Waiting for client to connect.");
            sc = new ServerSocket(port);
            responder = sc.accept();


            so = new ObjectOutputStream(responder.getOutputStream());
            si = new ObjectInputStream(responder.getInputStream());

            System.out.println("Client connected successfully.");

            spub = srsa.getPublic_key();
            spri = srsa.getPrivate_key();
            client_public = (PublicKey)(si.readObject());
            so.writeObject(spub);
            verify_with_initiator();
            chat();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void chat(){

      String etext;
      String dtext;
      Chatmessage send;
        Scanner in = new Scanner(System.in);


        new Listenfromclient().start();

        while(true){


            try {
                System.out.print("> ");
                dtext = in.nextLine();
                etext = sdes.encrypt(dtext);
                System.out.println("ServerE> " + etext);
                System.out.println("Server> " + dtext);
                so.writeObject(new Chatmessage(etext, ++sent_nonce));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    public void verify_with_initiator(){
        String verify;
        String output;
        String input;
        String decrypt;
        String encrypt;
        String[] parts;

        try {
            input = (String)si.readObject();
            System.out.println("Receiving encrypted message with client's nonce: " + input);
            decrypt = srsa.decrypt(input, spri);
            System.out.println("Decrypted message with client's nonce: " + decrypt);


            parts = decrypt.split(" ");
            verify = parts[0] + " " + nonce_generator();
            System.out.println("Cleartext to send with client's nonce and my nonce: " + verify);
            encrypt = srsa.encrypt(verify, client_public);
            output = encrypt;
            System.out.println("Encrypted text to send to send with client's nonce and my nonce: " + output);
            so.writeObject(output);


            input = (String) si.readObject();
            System.out.println("Receiving encrypted message with my nonce: " + input);
            decrypt = srsa.decrypt(input, spri);
            System.out.println("Decrypted message with my nonce: " + decrypt);

            if(Integer.parseInt(decrypt) != expected_nonce){
                System.out.println("Client sent wrong nonce, closing connection.");
                so.writeObject("Message from responder: You sent back the wrong nonce, closing connection.");
                responder.close();
                so.close();
                so.close();
            }

            input = (String) si.readObject();
            System.out.println("Receiving encrypted message with session key: " + input);
            decrypt = srsa.decrypt(input, spri);
            //String doubledecrypt = srsa.decrypt_pub(decrypt, client_public);
            byte[] decodedKey = Base64.getDecoder().decode(decrypt);
            sessionkey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");


            sdes.setKey(sessionkey);
            sdes.getKey();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public int nonce_generator(){
        Random rand = new Random();
        expected_nonce = rand.nextInt(100);
        return expected_nonce;
    }

    public static void main(String[] args){
        Server responder = new Server(1600, "B");
        responder.start();

    }

   class Listenfromclient extends Thread{

        public void run(){

            Chatmessage msg;
            String emessage;
            String dmessage;
            int nonce;

            while (true) {
                try {
                    msg = (Chatmessage) si.readObject();
                    emessage = msg.getMessage();
                    nonce = msg.getNonce();
                    dmessage = (sdes.decrypt(emessage));
                    if (nonce != recv_nonce+1){
                        so.writeObject("There is a replay attack, disconnecting.");
                        so.close();
                        break;
                    }else{
                        recv_nonce = nonce;
                    }


                    System.out.println("ClientE> " + emessage);
                    System.out.println("Client> " + dmessage);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }


        }

    }

}
