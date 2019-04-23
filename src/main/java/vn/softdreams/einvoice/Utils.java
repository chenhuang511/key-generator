package vn.softdreams.einvoice;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chen on 11/30/2018.
 */
public class Utils {
    public static String readTextFile(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        StringBuilder sb = new StringBuilder(512);
        Reader r = new InputStreamReader(fis, "UTF-8");
        int c = 0;
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static List<String> readTextFileLineByLine(String filePath) throws Exception {
        BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        String readLine = "";
        List<String> result = new ArrayList<>();
        while ((readLine = b.readLine()) != null) {
            result.add(readLine);
        }
        return result;
    }

    public static Object fromJsonText(String content, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(content, type);
    }

    public static Object fromTreeMap(Map map, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJsonTree(map).getAsJsonObject(), type);
    }

    public static void writeTextToFile(String content, String path) throws IOException {
        File file = new File(path);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(content);
        output.close();
    }

    public static void writeByToFile(byte[] content, String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
