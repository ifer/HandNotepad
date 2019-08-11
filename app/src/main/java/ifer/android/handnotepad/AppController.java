package ifer.android.handnotepad;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import ifer.android.handnotepad.api.ApiInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**
 * Created by ifer on 19/6/2017.
 */

public class AppController extends Application {
    public static final String TAG = "DEBUG-HandNotepad";
    private static AppController _this;
    private static SharedPreferences settings;
    private static Context context;

    public static final String SETTINGS_NAME = "HANDNOTEPAD_SETTINGS";

    /***************************
     * DEFINITIONS
     ***************************/
    public static final String appName = "HandNotepad";
    public static final String sharedprefname = appName + "_pref";

    /***************************
     * DEVELOPMENT VALUES
     ***************************/
    //must uncomment values on strings.xml
    private static String prodAPIdomain = "http://";
//    private static String devAPIdomain = "http://192.168.1.11:8083";
//    private static String devAPIdomain = "http://10.0.2.2:3000";      //Localhost (ergo) ip for the emulator
    private static String devAPIdomain = "http://192.168.1.2:3000";      //Localhost (ergo) for devices
//    private static  String devAPIdomain = "http://192.168.1.90:8083";



    public static final boolean isDevelop = true;

    public static ApiInterface apiService;
    public static String appUser;
    public static String appPassword;

    public static boolean connectionEstablished = false;

    /**
     * Get the instance of the Application
     *
     * @return : Current instance
     */
    public static synchronized AppController getInstance() {
        return _this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _this = this;
        AppController.context = getApplicationContext();
        settings = getSharedPreferences(SETTINGS_NAME, 0);

//        if (isDevelop)
//            devAPIdomain = settings.getString(Constants.PrefServerKey, null);
//        else
//            prodAPIdomain = settings.getString(Constants.PrefServerKey, null);
//
//        appUser = settings.getString(Constants.PrefUsernameKey, "");
//        appPassword = settings.getString(Constants.PrefPasswordKey, "");


    }


    public static String getApiDomain() {
        if (isDevelop)
            return (devAPIdomain);

        return prodAPIdomain;
    }

    public static void setApiDomain(String domain) {
        if (isDevelop)
           devAPIdomain = domain;
        else
           prodAPIdomain = domain;
    }

    public static SharedPreferences getSettings() {
        return settings;
    }



}