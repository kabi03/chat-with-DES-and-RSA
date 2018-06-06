import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.Base64;

public class Client {

    private JEncryptRSA crsa;
    private JEncryptDES cdes;
    private PublicKey cpub;
    private PrivateKey cpri;
    private Socket initiator;
    private int port;
    private String server_address;
    private ObjectOutputStream co;
    private ObjectInputStream ci;
    private PublicKey server_public;
    private String identity;
    private int expected_nonce;
    private SecretKey sessionkey;
    private int sent_nonce = 0;
    private int recv_nonce = 0;

    Client(int port, String server_address, String identity){
        this.port = port;
        this.server_address = server_address;
        crsa = new JEncryptRSA();
        cdes = new JEncryptDES();
        this.identity = identity;
        crsa.generateKey();



    }



    public void start(){
        try {
            initiator = new Socket(server_address, port);
            co = new ObjectOutputStream(initiator.getOutputStream());
            ci = new ObjectInputStream(initiator.getInputStream());
            System.out.println("Successfully connected to server.");

            cpri = crsa.getPrivate_key();
            cpub = crsa.getPublic_key();
            sessionkey = cdes.getKey();
            co.writeObject(cpub);
            server_public = (PublicKey)(ci.readObject());
            verify_with_responder();
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


        new Listenfromserver().start();

        while(true){


            try {
                System.out.print("> ");
                dtext = in.nextLine();
                etext = cdes.encrypt(dtext);
                System.out.println("ClientE> " + etext);
                System.out.println("Client> " + dtext);
                co.writeObject(new Chatmessage(etext, ++sent_nonce));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public void verify_with_responder(){
            String verify;
            String output;
            String input;
            String decrypt;
            String encrypt;
            String[] parts;

        try {
            verify = nonce_generator() + " " + identity;
            System.out.println("Cleartext to send with my nonce and identity: " + verify);
            encrypt = crsa.encrypt(verify, server_public);
            output = encrypt;
            System.out.println("Encrypted text to send with my nonce and identity: " + output);
            co.writeObject(output);


            input = (String)ci.readObject();
            System.out.println("Receiving encrypted message with my nonce and server's nonce: "+input);
            decrypt = crsa.decrypt(input, cpri);
            System.out.println("Decrypted message containing my nonce and server's nonce: " + decrypt);
            parts = decrypt.split(" ");
            if(Integer.parseInt(parts[0]) != expected_nonce){
                System.out.println("Server sent wrong nonce, closing connection.");
                co.writeObject("Message from initiator: You sent back the wrong nonce, closing connection.");
                initiator.close();
                co.close();
                ci.close();
            }

            verify = parts[1];
            System.out.println("Cleartext to send with server's nonce: " + verify);
            encrypt = crsa.encrypt(verify, server_public);
            output = encrypt;
            System.out.println("Encrypted text to send with server's nonce: " + output);
            co.writeObject(output);


            System.out.println("Sending the session key.");

            String encodedKey = Base64.getEncoder().encodeToString(sessionkey.getEncoded());



            //encrypt = crsa.encrypt_priv(encodedKey, cpri);

            String doubleencrypt = crsa.encrypt(encodedKey, server_public);
            output = doubleencrypt;
            co.writeObject(output);




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

    public static void main (String[] args){
        Client initiator = new Client(1600, "localhost", "A");
        initiator.start();
    }

    class Listenfromserver extends Thread{




        public void run() {

           Chatmessage msg;
           String emessage;
           String dmessage;
           int nonce;

            while (true) {
                try {
                    msg = (Chatmessage) ci.readObject();
                    emessage = msg.getMessage();
                    nonce = msg.getNonce();
                    dmessage = (cdes.decrypt(emessage));
                    if (nonce != recv_nonce+1){
                        co.writeObject("There is a replay attack, disconnecting.");
                        co.close();
                        break;
                    }else{
                        recv_nonce = nonce;
                    }


                    System.out.println("ServerE> " + emessage);
                    System.out.println("Server> " + dmessage);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }


    }
}
