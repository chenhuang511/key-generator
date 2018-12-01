package vn.softdreams.einvoice;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.X500Name;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.security.*;

/**
 * Created by chen on 11/30/2018.
 */
public class CSRGenerator {
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private KeyPairGenerator keyGen = null;

    private Request _request;

    public CSRGenerator(Request request) {
        this._request = request;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(_request.getKeySize(), new SecureRandom());
        KeyPair keypair = keyGen.generateKeyPair();
        publicKey = keypair.getPublic();
        privateKey = keypair.getPrivate();
    }

    public String getCSR() throws Exception {
        byte[] csr = generatePKCS10();
        return new String(csr);
    }

    public String getPrivateKey() throws Exception {
        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(this.privateKey);
        pemWriter.close();

        return stringWriter.toString();
    }

    private byte[] generatePKCS10() throws Exception {
        // generate PKCS10 certificate request
        String sigAlg = "MD5WithRSA";
        PKCS10 pkcs10 = new PKCS10(publicKey);
        Signature signature = Signature.getInstance(sigAlg);
        signature.initSign(privateKey);
        // common, orgUnit, org, locality, state, country
        X500Name x500name = new X500Name(_request.getCN(), _request.getOU(), _request.getO(),
                _request.getL(), _request.getS(), _request.getC());
        pkcs10.encodeAndSign(x500name, signature);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);
        pkcs10.print(ps);
        byte[] c = bs.toByteArray();
        try {
            if (ps != null)
                ps.close();
            if (bs != null)
                bs.close();
        } catch (Throwable th) {
        }
        return c;
    }
}