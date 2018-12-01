package vn.softdreams.einvoice;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    private static Request _request;

    public static void main(String[] args) {
        int f = -1;
        if (args == null || args.length == 0)
            System.out.println("Bad params!");
        else
            f = Integer.parseInt(args[0]);

        try {
            initConfig();
            if (_request != null && f > 0) {
                switch (f) {
                    case 1:
                        System.out.println("Start create new certificate request!");
                        genCSR();
                        break;
                    case 2:
                        System.out.println("Start create p12 file!");
                        genP12();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initConfig() throws IOException {
        final String configFile = "./keygen-config.json";
        String content = Utils.readTextFile(configFile);
        Request request = (Request) Utils.fromJsonText(content, Request.class);
        if (request != null) {
            System.out.println("Load config successfully!");
            _request = request;
        } else {
            System.out.println("Could not load configuration!");
        }
    }

    private static void genCSR() throws Exception {
        CSRGenerator csrGenerator = new CSRGenerator(_request);
        String csr = csrGenerator.getCSR();
        String csrPath = "./" + _request.getOutFileName() + ".csr";
        Utils.writeTextToFile(csr, csrPath);
        String keyContent = csrGenerator.getPrivateKey();
        String keyPath = "./" + _request.getOutFileName() + ".key";
        Utils.writeTextToFile(keyContent, keyPath);

        System.out.println("Done!");
    }

    private static void genP12() throws Exception {
        P12Generator p12Generator = new P12Generator(_request);
        p12Generator.generate();
        System.out.println("Done!");
    }
}
