package edu.nyp.deafapp;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class IPAddress {

    private static String ipAddress = "http://192.168.1.18";

    public static String getIpAddress() {
        return ipAddress;
    }

    public static void setIpAddress(String ipAddress) {
        IPAddress.ipAddress = ipAddress;
    }
}
