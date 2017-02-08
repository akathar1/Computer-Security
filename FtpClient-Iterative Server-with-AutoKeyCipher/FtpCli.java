
import java.io.*;
import java.net.*;
import java.util.Scanner;

class FtpCli {

    public static void main(String argv[]) throws Exception {

        String modifiedSentence;
        Socket sock = new Socket(argv[0], Integer.parseInt(argv[1]));

        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        System.out.print("ftp > ");
        Scanner input = new Scanner(System.in);
        String cmd = null;
        String chk = "quit";
        String s = "";
        Scanner sc = new Scanner(System.in);

        char[][] cipherMat = new char[26][26];
        char my_char[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        String security_key = "security";

        for (int var1 = 0; var1 <= 25; var1++) {
            for (int var2 = 0; var2 <= 25; var2++) {
                cipherMat[var1][var2] = my_char[(var1 + var2) % 26];
            }
        }

        while ((cmd = input.nextLine()) != null) {

            if (cmd.equals("quit")) {
                input.close();
                sock.close();
                System.out.println("Terminated");
                System.exit(0);
            } else if (cmd.equals("lls")) {

                Process p = Runtime.getRuntime().exec("ls");
                BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));

                while ((s = pin.readLine()) != null) {
                    System.out.println("\t" + s);
                }

                out.println("done");
            } else if (cmd.contains("get")) {
                //System.out.println("In get client");
                out.println(cmd);
                String argvLine = in.readLine();
                int m = argvLine.indexOf(".");
                int row = 0, column = 0;
                String file = argvLine.substring(0, m);
                String rest = argvLine.substring(m);
                String encryptd_line;
                String my_file1 = file + "_cd" + rest;

                file += "_ce" + rest;
                FileWriter filewriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(filewriter);

                FileWriter filewriter1 = new FileWriter(my_file1);
                BufferedWriter bufferedWriter1 = new BufferedWriter(filewriter1);

                while (!(s = in.readLine()).equals("EOF")) {
                    s += "\n";                  
                    bufferedWriter.write(s);
                }
                bufferedWriter.close();
                FileReader fileReader = new FileReader(file);
                Scanner scan = new Scanner(fileReader);

                while (scan.hasNextLine()) {
                    security_key = "security";
                    encryptd_line = scan.nextLine();
                    encryptd_line += "\n";
                    for (int i = 0; i < security_key.length() && i < encryptd_line.length(); i++) {	
                        if (encryptd_line.charAt(i) == '\n') {
                            
                            security_key += '\n';
                            bufferedWriter1.write('\n');
                        } else if (security_key.charAt(i) == '\n') {
                            
                            security_key += encryptd_line.charAt(i);
                            bufferedWriter1.write(encryptd_line.charAt(i));

                        } else if (encryptd_line.charAt(i) == ' ') {
                            security_key += ' ';
                            bufferedWriter1.write(' ');

                        } else if (security_key.charAt(i) == ' ') {
                            security_key += encryptd_line.charAt(i);
                            bufferedWriter1.write(encryptd_line.charAt(i));

                        } else {
			                    	row=0;
                            while ((security_key.charAt(i) != (cipherMat[0][row]))) {
                                row++;
                            }
				                    column=0;
                            while ((encryptd_line.charAt(i) != (cipherMat[column][row]))) {
                                column++;
                            }
                            security_key += cipherMat[column][0];

                            bufferedWriter1.write(cipherMat[column][0]);
                        }
                    }
                }
//System.out.println("Heloooooo fileReaderom other side");

                bufferedWriter1.close();
                out.println("done");
            } else {
                out.println(cmd);
            }

            while (!(modifiedSentence = in.readLine()).equals("done")) {
                System.out.println(modifiedSentence);
                //System.out.println("\n");
            }
            System.out.print("ftp > ");
        }

    }
}
