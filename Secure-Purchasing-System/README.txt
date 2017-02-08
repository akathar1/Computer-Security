
Name: Abhishek kathar
Email: akathar1@binghamton.edu

*************************************
Coding language of the project : JAVA
*************************************
Successfully tested on BINGSUNS
*************************************

EXECUTION STEPS:
1 After untaring the file run make command on the terminal.
    this will compile all the java files i.e Bank.java Customer.java and Psystem.java

2 Start both the servers i.e Bank and Psystem    
  
	*For Psystem
		java Psystem <purchasing-system-port><bank-domain><bank-port>

	*For Bank 
		java Bank <bank-port>

3 Now Run the Customer for placing our order 
	*For Customer
		java Customer <purchasing-system-domain><purchasing-system-port>


****************************************************************************************8
* Core Code for Encryption and Decryption:

	 //encryption using RSA and public key
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

    //decryption using RSA and private key
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


 //The RSA Decrypting function using public key
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
