
import java.io.*;
import java.net.*;
import java.io.File;
import java.util.Scanner;
//package com.tutorialpoint; 

class FtpServ {

    public static void main(String argv[]) throws Exception {

        String s1 = System.getProperty("user.dir");
        File directory = new File(s1);
        ServerSocket listen = new ServerSocket(Integer.parseInt(argv[0]));
        String s;
        Socket conn = listen.accept();
        Scanner sc = new Scanner(System.in);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        PrintWriter out = new PrintWriter(conn.getOutputStream(), true);

        
        //code for cipher security_key matrix generation
        char cipherMat[][] = new char[26][26];
        char my_char[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        for (int p = 0; p <= 25; p++) {
            for (int q = 0; q <= 25; q++) {
                cipherMat[p][q] = my_char[(q + p) % 26];
            }
        }
        try {
            while (true) {
                String str = " ";
                String clientSentence;
                String capitalizedSentence;
                clientSentence = in.readLine();

                if (!(clientSentence.equals(null))) {

                    try {
                        int i = clientSentence.indexOf(' ');
                        String first = clientSentence.substring(0, i);
                        String rest = clientSentence.substring(i + 1);

                        if (first.equals("cd")) {

                            directory = new File(s1 + '/' + rest);
                            System.setProperty("user.dir", s1 + '/' + rest);
                            s1 = System.getProperty("user.dir");

                            out.println("done");
                            continue;

                        } else if (clientSentence.contains("get")) {
                            
                        FileReader fileReader = new FileReader(System.getProperty("user.dir")+'/'+rest);
			int i1 = rest.indexOf('.');
                        String first1 = rest.substring(0, i1);
                        String rest1 = rest.substring(i1+1);
                            FileWriter fileWriter = new FileWriter(first1+"_se."+rest1);
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                            Scanner scan = new Scanner(fileReader); 
                            String security_key = "security";
                            String text,cipherLine = security_key;
                            int row=0, column=0;
                                                                                
                            out.println(rest);
                            while (scan.hasNextLine()) {
                                i = 0;
                                cipherLine = security_key;
                                text = scan.nextLine();
                                text += "\n";
                                if (security_key.length() < text.length()) {
                                    while (cipherLine.length() != text.length()) {
                                        cipherLine += text.charAt(i++);
                                    }
                                }
                               
                                for (i = 0; i < cipherLine.length(); i++) {

                                    if (text.charAt(i) == ' ') {
                                        bufferedWriter.write(" ");
                                        out.print(" ");
                                       
                                    } else if (text.charAt(i) == '\n') {
                                        bufferedWriter.write("\n");
                                        out.print("\n");
                                       

                                    }else if (cipherLine.charAt(i) == ' ') {
                                        bufferedWriter.write(text.charAt(i));
                                        out.print(text.charAt(i));
                                       

                                    }  else if (cipherLine.charAt(i) == '\n') {
                                          bufferedWriter.write(text.charAt(i));
                                        out.print(text.charAt(i));
                                      
                                    } else {
                                       
                                        for(column=0;cipherLine.charAt(i) != (cipherMat[0][column]);column++){
                                        continue;
                                        }         
                                        for(row=0;text.charAt(i) != (cipherMat[row][0]);row++){
                                            continue;
                                        }
                                        //System.out.println("Hieeeeeeeeeeeee "+row + column);
                                        out.print(cipherMat[row][column]);
                                        bufferedWriter.write(cipherMat[row][column]);
                                    }
                                }

                            }
                            out.println("\nEOF");
                            bufferedWriter.close();

                          
                        } else {

                            //code to execute mkdir command
                            Process p = Runtime.getRuntime().exec(clientSentence, null, directory);
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                            while ((s = stdInput.readLine()) != null) {

                                str += "\n" + s;
                            }

                            str += "\n" + "done";

                            System.out.println("FROM CLIENT:" + clientSentence + str);
                            capitalizedSentence = str;

                            out.println(capitalizedSentence);
                        }
                    } catch (StringIndexOutOfBoundsException n) {
                        //for ls pwd lls commands 

                        if (clientSentence.equals("done")) {

                            out.println("done");
                        } else {

                            Process p = Runtime.getRuntime().exec(clientSentence, null, directory);
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                            while ((s = stdInput.readLine()) != null) {
                                str += "\n" + s;
                            }

                            str += "\n" + "done";

                            System.out.println("FROM CLIENT:" + clientSentence + str);
                            capitalizedSentence = str;

                            out.println(capitalizedSentence);

                            //}
                        }

                    }

                }
            }
        } catch (NullPointerException n) {
            System.out.println("Exiting.....");
        }

        listen.close();

        conn.close();
    }

}
