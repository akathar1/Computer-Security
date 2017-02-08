
import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Cipher;

class Customer {

    public static void main(String argv[]) throws Exception {

//        argv[0]="localhost";
        //      argv[1]="1140";
    /*if(argv.length != 2)
		{
			System.out.println("Insufficient Arguments...Client not invoked properly!!");
			System.exit(0);
		}
         */
        //Socket sock = new Socket(argv[0], Integer.parseInt(argv[1]));    	
        Socket sock = new Socket("localhost", 1140);
        ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

        String who = (String) in.readObject();

        //to check if its really connected to purchasing server
        if (who.equals("Purchasing Server")) {

            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader input = new BufferedReader(reader);

            String fromserver = null;
            String id = "";
            String password = "";
            String to_encrypt = null;
            String to_encrypt1 = null;
            String to_send = null;
            String errorString = null;

            //getting name and password of the customer	
            System.out.println("Enter your ID....");
            id = input.readLine();
            while (id.equals("")) //checking for blank input
            {
                System.out.println("Please enter valid ID");
                id = input.readLine();
            }
            //stting up the customer public and private keys
            String cust_public = "Pu" + id.charAt(0) + ".key";
            String cust_private = "Pr" + id.charAt(0) + ".key";
            errorString = "error";

            System.out.println("Enter your Password....");

            //to check if the password entered is correct 
            while (errorString.equals("error")) {
                password = input.readLine();
                while (password.equals("")) //checking for blank input
                {
                    System.out.println("Please enter valid password");
                    password = input.readLine();
                }

                String hash = " ";

                //  System.out.println("Name : " + id + "Password" + password);
                //hashing the password using MD5
                hash = MD5(password);
                System.out.println("Hash :" + hash);

                //send the id and password to pSystem
                out.writeObject(id + "=" + hash);

                System.out.println("---------------------------\n");
                System.out.println("Verifying...");
                System.out.println("---------------------------");

                fromserver = (String) in.readObject();

                if (fromserver.equals("error")) {
                    System.out.println("WRONG PASSWORD ...!!! Please re-enter your password ");
                } else {

                    errorString = "success";

                }
                out.writeObject(errorString);
            }

            fromserver = (String) in.readObject();
            System.out.println("*******************  Login Successful ..!!!!! \n\n Welcome " + id.toUpperCase());

            System.out.println("\n--------------Available Items-------------- \n" + fromserver);

            String item_no = "", item_quantity = "";
            boolean check = false;
            System.out.println("## Enter item number of the item you want to purchase \n");
            item_no = input.readLine();
            while (check == false) {
                System.out.println("## Enter item quantity");
                item_quantity = input.readLine();
                fromserver = fromserver.replaceAll(" ", "");
                List<String> myList = new ArrayList<String>(Arrays.asList(fromserver.split("\n")));

                //checking if the entered quantity is valid or no
                for (String value : myList) {
                    String[] parts1 = value.split(",");
                    String no = parts1[0];
                    String quantity = parts1[3];

                    // System.out.println("no" + no + "++++++" + quantity);
                    if (item_no.equals(no)) {

                        // System.out.println(Integer.parseInt(item_quantity) + " sdfsdf  " + Integer.parseInt(quantity));
                        if (Integer.parseInt(item_quantity) > Integer.parseInt(quantity) || Integer.parseInt(item_quantity) < 0) {
                            System.out.println("PLease enter quantity <= " + quantity + " (available quantity)");
                        } else {
                            //System.out.println("valid quantity");
                            check = true;
                            break;
                        }

                    }
                }
            }

            String credit_no;
            System.out.println("Please enter your credit card number to purchase item");
            credit_no = input.readLine();

            try {

                // encrypting item no and quantity to send to purchasing server
                to_encrypt = item_no + "=" + item_quantity;
               // System.out.println(to_encrypt);

                // encryption using public key of purchasing system
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("Pup.key"));
                final PublicKey publicKey = (PublicKey) inputStream.readObject();
                final byte[] cipherText = encrypt(to_encrypt, publicKey);

                //sending encrypted text to pSystem
                out.writeObject(cipherText);

                //digital signature usong the private key of the customer
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(cust_private));
                PrivateKey A_privateKey = (PrivateKey) stream.readObject();
                stream.close();

                Signature sig = Signature.getInstance("SHA1withRSA");
                sig.initSign(A_privateKey);
                sig.update(cipherText);
                out.writeObject(sig.sign());
                //

                //encrypting id and credit card number to send to bank via purchasing server
                to_encrypt1 = id + "=" + credit_no;
                //System.out.println(to_encrypt1);

                // Encrypt the credit and name to send to bank via purchasing system using public key of bank
                ObjectInputStream b_inputStream = new ObjectInputStream(new FileInputStream("Pub.key"));
                final PublicKey b_publicKey = (PublicKey) b_inputStream.readObject();
                final byte[] b_cipherText = encrypt(to_encrypt1, b_publicKey);

                //sending encrypted text to purchasing server
                out.writeObject(b_cipherText);

                //checking message from purchasing system to get the confirmation of the purchase
                String final_message = (String) in.readObject();

                if (final_message.equals("OK")) {

                    System.out.println("------------Payment is been CONFIRMED-------------");
                    System.out.println("YOUR ORDER IS BEEN SUCCESSFULLY PLACED");

                } else {

                    System.out.println("************  INVALID CREDIT CARD NUMBER ");
                    System.out.println("YOUR ORDER IS NOT PLACED SUCCESSFULLY");

                }

                sock.close();

            } catch (ClassCastException e) {
                System.out.println("cannot connect");
                sock.close();
                System.exit(0);
            }
        } else {
            System.out.println("Connection problem !!");
        }
    }

    //encryption using RSA
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

    //decryption using RSA
    public static String decrypt(byte[] to_decrypt, PrivateKey key) {
        byte[] dcryptdText = null;
        try {
           
            final Cipher cipher = Cipher.getInstance("RSA");

            // private key decryption
            cipher.init(Cipher.DECRYPT_MODE, key);
            dcryptdText = cipher.doFinal(to_decrypt);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dcryptdText);
    }

//function for MD5 hashing	
    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] to_hash = md.digest(md5.getBytes());
            StringBuffer string_b = new StringBuffer();

            for (int i = 0; i < to_hash.length; ++i) {
                string_b.append(Integer.toHexString((to_hash[i] & 0xFF) | 0x100).substring(1, 3));
            }

            return (string_b.toString());
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }
}
