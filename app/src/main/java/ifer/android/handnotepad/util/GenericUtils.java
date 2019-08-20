package ifer.android.handnotepad.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ifer on 19/6/2017.
 */

public class GenericUtils {
    private final static TimeZone timezoneAthens = TimeZone.getTimeZone("Europe/Athens");

    public static boolean isEmptyOrNull (String s){
        if (s == null || s.isEmpty())
            return (true);

        return (false);
    }

    public static boolean isZeroOrNull (Integer n){
        if (n == null || n.equals(0))
            return (true);

        return (false);
    }

    public static boolean numberToBoolean (Integer n){

        if (n == null)
            return (false);

        if (n.equals(1))
            return (true);

        return (false);
    }

    public static int booleanToNumber (boolean b){

        if (b == true)
            return (1);
        else
            return (0);

    }

    public static Byte intToByte (int n){
        Byte b = new Byte(String.valueOf(n));
        return b;

    }

    public static Short intToShort (int n){
        Short s = new Short(String.valueOf(n));
        return s;

    }

    public static int computeAge (Date birthdt){
        if (birthdt == null)
            return (0);

        Date today = Calendar.getInstance(timezoneAthens).getTime();
        long secs = (today.getTime() - birthdt.getTime())/1000;
        int years = (int) (secs / 31536000);
        return (years);

    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
}
