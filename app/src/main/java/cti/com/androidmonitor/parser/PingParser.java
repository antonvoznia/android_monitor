package cti.com.androidmonitor.parser;

public class PingParser {

    public static String getIP(String str) {
        int start = str.indexOf("(")+1;
        int end = str.indexOf(")");
        if (start > 0 && end > 0) {
            return str.substring(start, end);
        }
        return "";
    }

    public static String getTime(String str) {
        int start = str.indexOf("time=");
        int end = str.indexOf("ms");
        if (end < 0 || start < 0)
            return "";
        String str2 = str.substring(start, end);
        if (str2 == null) {
            return str;
        }
        return str2;
    }

    public static String getTTL(String str) {
        int start = str.indexOf("ttl");
        int end = str.indexOf("time=")-1;
        if (start < 0 && end < 0) {
            return "";
        }
        return str.substring(start, end);
    }

    public static String getResult(String str) {
        String ip = getIP(str);
        String ttl = getTTL(str);
        String time = getTime(str);
        if (ip.length()*ttl.length()*time.length() < 1)
            return "Error";
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append("    ");
        sb.append(ttl);
        sb.append("    ");
        sb.append(time);
        return sb.toString();
    }
}
