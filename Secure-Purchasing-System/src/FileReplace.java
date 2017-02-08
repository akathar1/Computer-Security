
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FileReplace {

    public void doIt() {
        try {
            List<String> lines = new ArrayList<String>();
            String line = null;
            String name = "alice";
            String credit = "123";
            File f1 = new File("balance.txt");
            FileReader fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (line.contains("alice") && line.contains("123")) {
                    line = line.replace("1000", "1000\n");
                }
                lines.add(line);
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(f1);
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

    public static void main(String args[]) {
        FileReplace fr = new FileReplace();
        fr.doIt();
    }
}
