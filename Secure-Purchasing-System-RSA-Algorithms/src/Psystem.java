
import java.io.*;
import java.net.*;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.crypto.Cipher;
//package com.tutorialpoint; 

class Psystem {

    public static void main(String argv[]) throws Exception {

        	//Check for command line arguments
		/*if(argv.length != 3)
		{
			System.out.println("Arguments Insufficient!!");
			System.exit(0);
		}
         */
        //Socket Creation
        //ServerSocket listen = new ServerSocket(Integer.parseInt(argv[0]));
        ServerSocket listen = new ServerSocket(1140);
        Socket conn;
        ObjectInputStream in1;
        ObjectOutputStream out;

        //for iterative working of the server
        while (true) {
            conn = listen.accept();

            in1 = new ObjectInputStream(conn.getInputStream());
            out = new ObjectOutputStream(conn.getOutputStream());

            //to check if i am connecting the customer 
            out.writeObject("Purchasing Server");
            System.out.println("************CONNECTED TO THE PURCHASING SERVER***************\n");
            String stringtosend = " ";
            String toencrypt = null;

            //getting the contents from data file to check if the password is valid or no
            String datafilename = "data.txt";
            Scanner in_Dfile = new Scanner(new File(datafilename));
            List<String> lines = new ArrayList<String>();

            while (in_Dfile.hasNextLine()) {
                lines.add(in_Dfile.nextLine());
            }

            // System.out.println("Ayyyeeee"+lines.toString());
            String[] arr = lines.toArray(new String[0]);

            String errorString = "error";
            String cust_public = " ";
            String cust_private = " ";

            //verifying the password 
            while (errorString.equals("error")) {

                String inputString = (String) in1.readObject();

                String[] info = inputString.split("=");
                String cli_name = info[0];
                String cli_hash_pass = info[1];

                //System.out.println("\nThe name of the Client is:" + cli_name);
                cust_public = "Pu" + cli_name.charAt(0) + ".key";
                cust_private = "Pr" + cli_name.charAt(0) + ".key";
                System.out.println("\n Verifying password");

                String check_id = " ";

                for (String value : lines) {
                    String[] parts1 = value.split(",");
                    check_id = parts1[0];
                    String passwd = parts1[1];

                    // System.out.println("server check " + check_id + "++++++" + passwd);
                    if (cli_name.equals(check_id) && cli_hash_pass.equals(passwd)) {
                        System.out.println("CLIENT PASSWORD VERIFIED");
                        stringtosend = "OK";
                    }
                }
                //if entered password does not match the database
                if (!stringtosend.equals("OK")) {
                    stringtosend = "error";
                    System.out.println("Incorrect password..waiting for client to enter correct password");
                }

                //notifying client about the password (correct/incorrect)
                out.writeObject(stringtosend);

                errorString = (String) in1.readObject();

            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Getting the contents of the item file to send to client once logged in
            String itemfilename = "item.txt";

            Scanner initemfile = new Scanner(new File(itemfilename));
            List<String> item_lines = new ArrayList<String>();

            while (initemfile.hasNextLine()) {
                item_lines.add(initemfile.nextLine());
            }

            String[] item_arr = item_lines.toArray(new String[0]);
            stringtosend = "";
            for (String item_value : item_lines) {
                stringtosend = stringtosend + item_value + "\n";
            }
            //sending the contents of the item file to display to client for purchase
            out.writeObject(stringtosend);

            byte[] in = (byte[]) in1.readObject();
            byte[] to_decrypt = in;

            //Decrypting the item number and quantity using the private key of PSystem
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("Prp.key"));
            final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
            String plainText = decrypt(to_decrypt, privateKey);

            // System.out.println("Decrypted plain text : " + plainText);
            String[] item_info = plainText.split("=");
            int final_quantity = Integer.parseInt(item_info[1]);
            String original_quantity = "";
            String item_name = "";

            //getting the price and original quantity of the item for which the purchase is being made
            stringtosend = stringtosend.replaceAll(" ", "");
            List<String> myList = new ArrayList<String>(Arrays.asList(stringtosend.split("\n")));
            String item_price = "";
            for (String value : myList) {
                String[] parts1 = value.split(",");
                String no = parts1[0];
                String price = parts1[2];
                String quantity = parts1[3];
                String name = parts1[1];

                if (item_info[0].equals(no)) {

                    item_price = price;
                    original_quantity = quantity;
                    item_name = name;
                }

            }
            item_price = item_price.replace("$", "");

            //calculating the total price of the item
            int total_price = final_quantity * Integer.parseInt(item_price);

            //quantity to be updated in item file after the purchase was made successfully
            int updated_quantity = Integer.parseInt(original_quantity) - final_quantity;

            //  System.out.println("----Total price = " + total_price);
            ObjectInputStream stream2 = new ObjectInputStream(new FileInputStream(cust_public));
            PublicKey public_key = (PublicKey) stream2.readObject();
            stream2.close();

            //Verifying Digital Signature
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(public_key);
            sig.update(to_decrypt);

            System.out.println("\nClient signature is being verified...");
            System.out.println("----------------------------------------");
            if (!sig.verify((byte[]) in1.readObject())) {
                System.out.println("Client signature failed");
                conn.close();
            } else {
                System.out.println("Signature has been verified succesfully.");
            }

            byte[] cipher_FromClient = (byte[]) in1.readObject();

            //to encrypt total price to send to bank
            ObjectInputStream private_ps_iStream = new ObjectInputStream(new FileInputStream("Prp.key"));
            final PrivateKey private_p = (PrivateKey) private_ps_iStream.readObject();
            final byte[] price_cipher = encrypt_pr(Integer.toString(total_price), private_p);

            //System.out.println("Abhishek : " + price_cipher.length);
            //System.out.println("Abhishek2 : " + cipher_FromClient.length);
            String fromBank = "";
            try {
                //connecting to the bank server
                Socket sock = new Socket("localhost", 8610);
                ObjectOutputStream bank_out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream bank_in = new ObjectInputStream(sock.getInputStream());
                //forwading the name and credit card number sent by customer to the bank
                bank_out.writeObject(cipher_FromClient);

                //sending the encrypted price to the bank
                bank_out.writeObject(price_cipher);

                //get the order verification from bank
                fromBank = (String) bank_in.readObject();

                sock.close();
            } catch (Exception e) {
                System.out.println("Connection to the bank failed");
            }

            if (fromBank.equals("OK")) {

                //updating the item file with the new quantity
                updateFile(item_info[0], item_name, item_price, Integer.toString(updated_quantity));
            }

            //sending the order status to client
            out.writeObject(fromBank);

            conn.close();

        }

    }

    //decrypting using RSA and private key
    public static String decrypt(byte[] to_decrypt, PrivateKey key) {
        byte[] plaintext = null;
        try {
           
            final Cipher cipher = Cipher.getInstance("RSA");

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            plaintext = cipher.doFinal(to_decrypt);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(plaintext);
    }

    //encrypting using RSA and public key
    public static byte[] encrypt(String to_encrypt, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(to_encrypt.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (cipherText);
    }

    //encrypting using RSA and private key
    public static byte[] encrypt_pr(String to_encrypt, PrivateKey key) {
        byte[] cipherText = null;
        try {

            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(to_encrypt.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (cipherText);
    }

    //updating item file with new quantity
    public static void updateFile(String item_no, String item_name, String item_price, String new_quantity) {
        try {
            List<String> lines = new ArrayList<String>();
            String line = null;
            File file = new File("item.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String newLine = item_no + "," + item_name + ",$" + item_price + "," + new_quantity + "\n";
            while ((line = bufferedReader.readLine()) != null) {
                line = line + "\n";
                if (line.contains(item_no) && line.contains(item_name)) {

                    line = newLine;
                    line = line.replace(" ", "");
                }
                lines.add(line);
            }
            fileReader.close();
            bufferedReader.close();

            FileWriter fw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines) {
                out.write(s);
            }
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
