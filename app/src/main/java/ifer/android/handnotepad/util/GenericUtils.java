package ifer.android.handnotepad.util;

import java.util.Calendar;
import java.util.Date;
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

}
