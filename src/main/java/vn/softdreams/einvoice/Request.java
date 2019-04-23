package vn.softdreams.einvoice;

/**
 * Created by chen on 11/30/2018.
 */
public class Request {
    private String company;
    private int keySize = 1024;
    private String C;
    private String S;
    private String L;
    private String O;
    private String OU;
    private String CN;

    private String outFileName;
    private String p12Pass;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getC() {
        return C;
    }

    public void setC(String c) {
        C = c;
    }

    public String getS() {
        return S;
    }

    public void setS(String s) {
        S = s;
    }

    public String getL() {
        return L;
    }

    public void setL(String l) {
        L = l;
    }

    public String getO() {
        return O;
    }

    public void setO(String o) {
        O = o;
    }

    public String getOU() {
        return OU;
    }

    public void setOU(String OU) {
        this.OU = OU;
    }

    public String getCN() {
        return CN;
    }

    public void setCN(String CN) {
        this.CN = CN;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public String getP12Pass() {
        return p12Pass;
    }

    public void setP12Pass(String p12Pass) {
        this.p12Pass = p12Pass;
    }

    @Override
    public String toString() {
        return "CN=" + getCN() + ", OU=" + getOU() + ", O=" + getO()
                + ", L=" + getL() + ", S=" + getS() + ", C=" + getC();
    }
}
