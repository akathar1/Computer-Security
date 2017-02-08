
/**
 * *******************************************************************
 * Filename: VfSer.java
 * Author: Anand Vadaje
 *
 * This file consists of the VF server part of the code for the project,
 * wherein when the voter connects with the Vf server it check the veri-
 * fication acquired by the LA and if its same then it begins with the
 * voting procedure for the connected voter.
 ********************************************************************
 */
import java.io.*;
import java.net.*;
import java.io.File;
import javax.crypto.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Bank {

    public static void main(String argv[]) throws Exception {

        //Check for command line arguments
       /* if (argv.length != 1) {
            System.out.println("ONE Command line argument has to be Entered!!");
            System.exit(0);
        }
         */
        ServerSocket listen = new ServerSocket(8610);
        Socket conn;
        ObjectInputStream b_in;
        ObjectOutputStream b_out;
        try {
            while (true) {
                conn = listen.accept();
                b_in = new ObjectInputStream(conn.getInputStream());
                b_out = new ObjectOutputStream(conn.getOutputStream());

                byte[] cipher_FromClient = (byte[]) b_in.readObject();
                System.out.println("********** Purchasing Server Connected to Bank *****************");

                //Decrypting input (name and credit card) of the client forwaded from the purchasing server
                ObjectInputStream b_inputStream = new ObjectInputStream(new FileInputStream("Prb.key"));
                final PrivateKey b_privateKey = (PrivateKey) b_inputStream.readObject();
                String b_plainText = decrypt(cipher_FromClient, b_privateKey);

               // System.out.println("Decrypted plain text : " + b_plainText);
                byte[] price_cipher = (byte[]) b_in.readObject();

                //to decrypt total price of the selected item and quantity sent from purchasing system
                ObjectInputStream b_inputStream_pub = new ObjectInputStream(new FileInputStream("Pup.key"));
                final PublicKey b_publicKey = (PublicKey) b_inputStream_pub.readObject();
                String b_plainText_price = decrypt_pb(price_cipher, b_publicKey);
                //System.out.println("Decrypted price text : " + b_plainText_price);
                String[] b_info = b_plainText.split("=");
                String b_name = b_info[0];
                String b_credit = b_info[1];
                String oldBal = "";
                String newBal = "";
                int balance = 0;

                String datafilename = "balance.txt";
                String p_tosend = "error";

                Scanner infrmfile = new Scanner(new File(datafilename));
                List<String> lines = new ArrayList<String>();

                while (infrmfile.hasNextLine()) {
                    lines.add(infrmfile.nextLine());
                }

                for (String value : lines) {
                    String[] parts1 = value.split(",");
                    String name = parts1[0];
                    String credit = parts1[1];
                    String bal = parts1[2];
                    credit = credit.replace(" ", "");
                    bal = bal.replace(" ", "");

                    //System.out.println("Name loop:"+name+"credit"+credit+"name/credit"+b_name+b_credit);
                    
                    //checking whether the given credit card number is valid or not 
                    if (name.equals(b_name) && credit.equals(b_credit)) {
                        p_tosend = "OK";
                        oldBal = bal;
                        //deducting the price of the purchase from the balance of the customer
                        balance = Integer.parseInt(bal) - Integer.parseInt(b_plainText_price);
                        newBal = Integer.toString(balance);
                        System.out.println("******** Credit Card number is verfied *********");

                    }
                }
                try {
                    if (p_tosend.equals("OK")) {
                        //updating the balance file with new balance
                        updateFile(b_name, b_credit, oldBal, newBal);
                        System.out.println("PAYMENT SUCCESSFULL");

                    }
                    //sending payment confirmation/error  code to client
                    b_out.writeObject(p_tosend);
                } catch (Exception e) {
                    System.out.println("Error Updating the balance file");
                    conn.close();
                    System.exit(0);
                }
                conn.close();

            }
        } catch (Exception e) {
            System.out.println("Cannot connect to the bank server");
        }

    }

    public static String decrypt(byte[] toDecrypt, PrivateKey key) {
        byte[] dcryptText = null;
        try {
             // using RSA cipher
            final Cipher cipher = Cipher.getInstance("RSA");

            // private key descryption
            cipher.init(Cipher.DECRYPT_MODE, key);
            dcryptText = cipher.doFinal(toDecrypt);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dcryptText);
    }

    //The RSA Decrypting function
    public static String decrypt_pb(byte[] toDecrypt, PublicKey key) {
        byte[] dcryptText = null;
        try {
            // using RSA cipher
            final Cipher cipher = Cipher.getInstance("RSA");

           //public key description
            cipher.init(Cipher.DECRYPT_MODE, key);
            dcryptText = cipher.doFinal(toDecrypt);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dcryptText);
    }

    public static void updateFile(String name, String credit, String old_bal, String new_bal) {
        try {
            List<String> lines = new ArrayList<String>();
            String line = null;
            String newLine;
            File file = new File("balance.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            newLine = name + "," + credit + "," + new_bal + "\n";
            while ((line = bufferedReader.readLine()) != null) {
                line = line + "\n";
                if (line.contains(name) && line.contains(credit)) {
                    line = newLine;
                    line = line.replace(" ", "");
                }
                lines.add(line);
            }
            fileReader.close();
            bufferedReader.close();

            FileWriter fw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fw);
            for (String my_string : lines) {
                out.write(my_string);
            }
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
