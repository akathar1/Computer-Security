
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/**
 *
 * @author abhishek kathar
 */
public class DesCounter {

    public static void main(String args[]) throws IOException, NoSuchAlgorithmException, Exception {
        String inputFile = null;
        String outputFile = null;
        String details = null;
        int choice = 0;
        FileInputStream in = null;
        FileOutputStream out = null;
        String my_counter = "00001234";
        int numberOfBlocks = 0, numberOfChar = 0, length = 0;

        inputFile = args[0];
        outputFile = args[1];
        choice = Integer.parseInt(args[2]);
        try {
            File file = new File(inputFile);
            out = new FileOutputStream(outputFile);            
            int i = 0, j=0, k=0;

            byte[] bFile = new byte[(int) file.length()];
            byte[] plainText = new byte[8];

            //convert file into array of bytes
            in = new FileInputStream(file);
            in.read(bFile);
            if (choice == 1) {
                length = bFile.length - 1;
                details=" Encryption ";
            } else {
            details=" Decryption ";
                length = bFile.length;
            }

            for (i = 0; i < length; i = i + 8) {
                k = 0;
                for (j = i; j < i + 8 && j < length; j++) {
                    plainText[k] = bFile[j];
                    numberOfChar++;
                    k++;

                }
               
                numberOfBlocks++;

                Cryptage c = new Cryptage();
                byte[] res = c.Encrypt(my_counter);
                my_counter = incrementCTR(my_counter);
                byte[] output;
                output = xor(plainText, res, k);                
                out.write(output);

            }
            if(choice==0){
			          out.write("\n".getBytes());
			          }
            System.out.println("***************"+ details + "*************");
            System.out.println("Number of Blocks : " + numberOfBlocks + "\nNumber of characters = " + numberOfChar);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }

    }

    public static byte[] xor(byte[] a, byte[] b, int length) {

        byte[] result;
        if (length < 8) {
            result = new byte[length];
        } else {
            result = new byte[a.length];
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }
        
        return result;
    }

    public static String incrementCTR(String counter) {

        String incrementedCTR;

        int ctr = Integer.parseInt(counter);
        ctr = ctr + 1;
        incrementedCTR = Integer.toString(ctr);
       
        while (incrementedCTR.length() != 8) {

            incrementedCTR = '0' + incrementedCTR;
        }
        

        return incrementedCTR;

    }
}
