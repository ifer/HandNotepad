package ifer.android.handnotepad.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import ifer.android.handnotepad.AppController;
import ifer.android.handnotepad.R;


/**
 * Created by ifer on 19/6/2017.
 */

public class AndroidUtils {


    public enum Popup {
        SUCCESS,
        ERROR,
        WARNING ,
        QUESTION,
        INFO;
    }

    public enum ToastLength {
        SHORT,
        LONG,
        CENTER;
    }
    //====================================================================================
    //                                 VIEWS
    //====================================================================================

    // Assigns Integer to TextView as a string, ommitting "null" coming from MySQL null values
    public static void assignIntegerToTextView (TextView tv, Integer n){
        if (n != null)
            tv.setText (String.valueOf(n));
        else
            tv.setText ("");
    }

    // Returns the text of an  EditText as Integer, taking care of empty and null values
    public static Integer getIntegerFromTextView(TextView tv){
        if(tv.getText() == null || tv.getText().toString().trim().equals(""))
            return (null);

        return (Integer.valueOf(tv.getText().toString()));

    }
//
//    // Returns the text of an EditText as String, taking care of empty and null values
//    public static String getTextFromTextView(TextView tv){
//        if(tv.getText() == null )
//            return (null);
//        else if (tv.getText().toString().trim().equals(""))
//            return ("");
//        else
//            return (tv.getText().toString());
//
//    }

    //====================================================================================
    //                                  L O G S
    //====================================================================================
    public static void printLog(String src, String msg) {
        if(AppController.isDevelop) {
            String tag = src + " - " + AppController.TAG;
            Log.d(tag, msg);
        }
    }

    public static void printLog(String msg) {
        if(AppController.isDevelop) {
            Log.d(AppController.TAG, msg);
        }
    }

    public ProgressDialog showProgress(Activity mActivity) {
        ProgressDialog mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Wait while loading...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        return mProgressDialog;
    }

    //====================================================================================
    //                                  P O P U P S
    //====================================================================================


    /**
     * Shows a popup message to interface
     *
     * @param context  The activity
     * @param mode      The mode of the popup
     * @param title     The title of the popup
     * @param message   The message of the popup
     * @param posAction The "OK" button press action
     * @param negAction The "Cancel" button press action
     */
    public static void showPopup(Context context,
                                  Popup  mode,
                                  String title,
                                  String message,
                                  DialogInterface.OnClickListener posAction,
                                  DialogInterface.OnClickListener negAction) {
        // The alert dialog builder
        AlertDialog.Builder alt_bld;
        try {
            alt_bld = new AlertDialog.Builder(context);
        } catch (NullPointerException np) {
            // DEBUG PRINT
//            printLog( "showPopup - NullPointerException");
            return;
        }

        alt_bld.setCancelable(false);

        // set the listeners
        if (mode.equals(Popup.INFO) || mode .equals(Popup.SUCCESS) || mode.equals(Popup.ERROR)) {
            alt_bld.setPositiveButton(context.getResources().getString(R.string.popup_ok), posAction);
        }
        else if (mode .equals(Popup.WARNING)) {
            alt_bld.setPositiveButton(context.getResources().getString(R.string.popup_yes), posAction);
            alt_bld.setNegativeButton(context.getResources().getString(R.string.popup_no), negAction);
        }
        else if (mode .equals(Popup.QUESTION)) {
            alt_bld.setPositiveButton(context.getResources().getString(R.string.popup_yes), posAction);
            alt_bld.setNegativeButton(context.getResources().getString(R.string.popup_no), negAction);
        }

        alt_bld.setMessage(message); // set the message
        android.support.v7.app.AlertDialog alert = alt_bld.create();
        alert.setTitle(title); // set the title


        switch (mode) { // set the icon
            case SUCCESS:
                alert.setIcon(R.drawable.popup_ok);
                if(title == null)
                    alert.setTitle(context.getResources().getString(R.string.popup_title_info));
                break;
            case ERROR:
                alert.setIcon(R.drawable.popup_error);
                if(title == null)
                    alert.setTitle(context.getResources().getString(R.string.popup_title_error));
                break;
            case WARNING:
                alert.setIcon(R.drawable.popup_warning);
                if(title == null)
                    alert.setTitle(context.getResources().getString(R.string.popup_title_warning));
                break;
            case INFO:
                alert.setIcon(R.drawable.popup_info);
                if(title == null)
                    alert.setTitle(context.getResources().getString(R.string.popup_title_info));
                break;
            case QUESTION:
                alert.setIcon(R.drawable.popup_question);
                if(title == null)
                    alert.setTitle(context.getResources().getString(R.string.popup_title_question));
                break;

            default:
                break;
        }
        try {
            alert.show();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.red));
        } catch (WindowManager.BadTokenException bt) {
            // DEBUG PRINT
           printLog("showPopup - WindowManager.BadTokenException");
        } catch (Exception e) {
            // DEBUG PRINT
            printLog( "showPopup - General exception");
        }
    }

    /**
     * Shows a popup message to interface
     *
     * @param context  The activity
     * @param mode      The mode of the popup
     * @param message   The message of the popup
     * @param posAction The "OK" button press action
     * @param negAction The "Cancel" button press action
     */
    public static void showPopup(Context context,
                                 Popup  mode,
                                 String message,
                                 DialogInterface.OnClickListener posAction,
                                 DialogInterface.OnClickListener negAction) {
        showPopup(context,mode,null,message,posAction,negAction);
    }

    public static void showPopupInfo (Context context, String message ){
        showPopup(context, Popup.INFO, null, message, null, null);
    }

    public static void showPopupInfo (Context context, String message, DialogInterface.OnClickListener posAction ){
        showPopup(context, Popup.INFO, null, message, posAction, null);
    }
    public static void showPopupError (Context context, String message ){
        showPopup(context, Popup.ERROR, null, message, null, null);
    }

    public static void showPopupSuccess (Context context, String message ){
        showPopup(context, Popup.SUCCESS, null, message, null, null);
    }

    public static void showPopupWarning(Context context,
                                 String message,
                                 DialogInterface.OnClickListener posAction,
                                 DialogInterface.OnClickListener negAction) {
        showPopup(context,Popup.WARNING,null, message, posAction, negAction);
    }


    public static void showPopupQuestion(Context context,
                                        String message,
                                        DialogInterface.OnClickListener posAction,
                                        DialogInterface.OnClickListener negAction) {
        showPopup(context,Popup.QUESTION,null, message, posAction, negAction);
    }

    //====================================================================================
    //                                  T O A S T S
    //====================================================================================

    /**
     * Show a toast message
     *
     * @param activity : The activity
     * @param message  : The message to show
     * @param flag     : The length of the notification duration
     */
    public static void showToastMessage(Activity activity, String message, ToastLength  flag) {

        switch (flag) {
            case SHORT:
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                break;
            case LONG:
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                break;
            case CENTER:
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                // center message
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                // show toast
                toast.show();
                break;
        }
    }

    public static void showToastMessage(Context context, String message) {

                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                // center message
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                // show toast
                    toast.show();
    }


    /***************************
     * Shared Preferences Functions
     ***************************/
    public void setValueString(String key, String value) {
        AppController.getSettings().edit().putString(key, value).apply();
    }

    public String getValueString(String key) {
        return AppController.getSettings().getString(key, "");
    }

    public String getValueString(String key, String defaultValues) {
        return AppController.getSettings().getString(key, defaultValues);
    }

    public void setValueBoolean(String key, boolean value) {
        AppController.getSettings().edit().putBoolean(key, value).apply();
    }

    public boolean getValueBoolean(String key, boolean defaultValue) {
        return AppController.getSettings().getBoolean(key, defaultValue);
    }

    public void setValueInt(String key, int value) {
        AppController.getSettings().edit().putInt(key, value).apply();
    }

    public int getValueInt(String key) {
        return AppController.getSettings().getInt(key, 0);
    }

    public void setValueDouble(String key, Double value) {
        AppController.getSettings().edit().putString(key, String.valueOf(value)).apply();
    }

    public Double getValueDouble(String key) {
        return Double.parseDouble(AppController.getSettings().getString(key, "0"));
    }

    public void clearSharedPref(String key) {
        AppController.getSettings().edit().remove(key).apply();
    }

    public void clearSharedPref() {
        AppController.getSettings().edit().clear().apply();
    }




}
