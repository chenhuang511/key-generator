package vn.softdreams.einvoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    private static List<Request> _requests;

    public static void main(String[] args) {
//        int f = -1;
//        if (args == null || args.length == 0)
//            System.out.println("Bad params!");
//        else
//            f = Integer.parseInt(args[0]);
////        f = 3;
        try {
//            initConfig();
//            if (_requests != null && f > 0) {
//                switch (f) {
//                    case 1:
//                        System.out.println("Start create new certificate request!");
//                        genCSR();
//                        break;
//                    case 2:
//                        System.out.println("Start create p12 file!");
//                        genP12();
//                        break;
//                    case 3:
//                        System.out.println("Start create new p12 file with self-signed certificate");
//                        genP12WithSelfSignedCert();
//                }
//            }
            genP12WithConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void genP12WithConfig() throws Exception {
        final String configFile = "./keygen.txt";
        List<String> data = Utils.readTextFileLineByLine(configFile);
        System.out.println("Start gen " + data.size() + " p12 files...");
        new P12Generator().generateAll(data);
        System.out.println("Done!");
    }

    private static void initConfig() throws IOException {
        final String configFile = "./keygen.config";
        String content = Utils.readTextFile(configFile);
        List<Request> requests = (ArrayList<Request>) Utils.fromJsonText(content, ArrayList.class);
        if (requests != null) {
            System.out.println("Load config successfully!");
            _requests = requests;
        } else {
            System.out.println("Could not load configuration!");
        }
    }

    private static void genP12WithSelfSignedCert() throws Exception {
        P12Generator p12Generator = new P12Generator(_requests);
        p12Generator.generateAll();
        System.out.println("Done!");
    }

    private static void genCSR() throws Exception {
        CSRGenerator csrGenerator = new CSRGenerator(_requests.get(0));
        String csr = csrGenerator.getCSR();
        String csrPath = "./" + _requests.get(0).getOutFileName() + ".csr";
        Utils.writeTextToFile(csr, csrPath);
        String keyContent = csrGenerator.getPrivateKey();
        String keyPath = "./" + _requests.get(0).getOutFileName() + ".key";
        Utils.writeTextToFile(keyContent, keyPath);

        System.out.println("Done!");
    }

    private static void genP12() throws Exception {
        P12Generator p12Generator = new P12Generator(_requests.get(0));
        p12Generator.generate();
        System.out.println("Done!");
    }
}
