package vn.softdreams.einvoice;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.CharBuffer;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static javax.crypto.Cipher.DECRYPT_MODE;

/**
 * Created by chen on 12/1/2018.
 */
public class P12Generator {
    private static final Pattern CERT_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" + // Header
                    "([a-z0-9+/=\\r\\n]+)" +                    // Base64 text
                    "-+END\\s+.*CERTIFICATE[^-]*-+",            // Footer
            Pattern.CASE_INSENSITIVE);

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" + // Header
                    "([a-z0-9+/=\\r\\n]+)" +                       // Base64 text
                    "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",            // Footer
            Pattern.CASE_INSENSITIVE);

    private String certFileName;
    private String keyFileName;
    private String p12Pass;
    private String alias;
    private String p12FileName;
    private String passwordFileName;

    private List<?> requests;

    public P12Generator() {}

    public P12Generator(List<?> requests) {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        this.requests = requests;
    }

    public P12Generator(Request request) {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String outFileName = request.getOutFileName();
        this.certFileName = outFileName + ".cer";
        this.keyFileName = outFileName + ".key";
        this.p12FileName = outFileName + ".p12";
        this.passwordFileName = outFileName + ".pwd";
        this.p12Pass = request.getP12Pass();
        this.alias = request.getCompany();
    }

    public void generateAll() throws Exception {
        if (requests == null) throw new Exception("Input file is not valid");
        for (Object item : requests) {
            Request req = (Request) Utils.fromTreeMap((Map) item, Request.class);
            String p12File = System.getProperty("user.dir") + "/" + req.getOutFileName() + ".p12";
            String keyPass = req.getP12Pass();
            String alias = req.getCN();
            int validity = 365;

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

            X500Name x500Name = new X500Name(req.getCN(), req.getOU(), req.getO(), req.getL(), req.getS(), req.getC());

            keypair.generate(req.getKeySize());
            PrivateKey privKey = keypair.getPrivateKey();

            X509Certificate[] chain = new X509Certificate[1];

            chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) validity * 24 * 60 * 60);

            keyStore.setKeyEntry(alias, privKey, keyPass.toCharArray(), chain);

            keyStore.store(new FileOutputStream(p12File), keyPass.toCharArray());
        }
    }

    public void generateAll(List<String> data) throws Exception {
        if (data == null) throw new Exception("Config file is not valid");
        for (String item : data) {
            String[] tmp = item.split("\\t");
            if (tmp.length != 2) continue;
            String pwd = "1";
            String outFile = System.getProperty("user.dir") + "/" + tmp[0] + ".p12";

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

            X500Name x500Name = new X500Name(tmp[0], tmp[1], tmp[0], "HD", "HN", "VN");

            keypair.generate(1024);
            PrivateKey privKey = keypair.getPrivateKey();

            X509Certificate[] chain = new X509Certificate[1];

            chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) 365 * 24 * 60 * 60);

            keyStore.setKeyEntry(tmp[0], privKey, pwd.toCharArray(), chain);
            keyStore.store(new FileOutputStream(outFile), pwd.toCharArray());
        }
    }

    public void generate() throws Exception {
        File keyFile = new File(keyFileName);
        File certificateChainFile = new File(certFileName);
        PKCS8EncodedKeySpec encodedKeySpec = readPrivateKey(keyFile, null);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(encodedKeySpec);

        List<X509Certificate> certificateChain = readCertificateChain(certificateChainFile);
        if (certificateChain.isEmpty()) {
            throw new CertificateException("Certificate file does not contain any certificates: " + certificateChainFile);
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(this.alias, key, this.p12Pass.toCharArray(), certificateChain.stream().toArray(Certificate[]::new));

        //store to file
        String p12FilePath = "./" + this.p12FileName;
        FileOutputStream fos = new FileOutputStream(new File(p12FilePath));
        keyStore.store(fos, p12Pass.toCharArray());

        //also store password file
        String passFilePath = "./" + this.passwordFileName;
        Utils.writeTextToFile(this.p12Pass, passFilePath);
    }

    private List<X509Certificate> readCertificateChain(File certificateChainFile)
            throws IOException, GeneralSecurityException {
        String contents = readFile(certificateChainFile);

        Matcher matcher = CERT_PATTERN.matcher(contents);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certificates = new ArrayList<>();

        int start = 0;
        while (matcher.find(start)) {
            byte[] buffer = base64Decode(matcher.group(1));
            certificates.add((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(buffer)));
            start = matcher.end();
        }

        return certificates;
    }

    private PKCS8EncodedKeySpec readPrivateKey(File keyFile, String keyPassword)
            throws IOException, GeneralSecurityException {
        String content = readFile(keyFile);

        Matcher matcher = KEY_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new KeyStoreException("Found no private key: " + keyFile);
        }
//        System.out.println("Key content: \n" + matcher.group(1));
        byte[] encodedKey = base64Decode(matcher.group(1));

        //if private key is not encrypted
        if (Utils.isNullOrEmpty(keyPassword)) {
            return new PKCS8EncodedKeySpec(encodedKey);
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(encodedKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        SecretKey secretKey = keyFactory.generateSecret(new PBEKeySpec(keyPassword.toCharArray()));

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(DECRYPT_MODE, secretKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }

    private byte[] base64Decode(String base64) {
        return Base64.getMimeDecoder().decode(base64.getBytes(US_ASCII));
    }

    private String readFile(File file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), US_ASCII)) {
            StringBuilder stringBuilder = new StringBuilder();

            CharBuffer buffer = CharBuffer.allocate(2048);
            while (reader.read(buffer) != -1) {
                buffer.flip();
                stringBuilder.append(buffer);
                buffer.clear();
            }
            return stringBuilder.toString();
        }
    }
}
