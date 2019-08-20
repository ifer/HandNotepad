package ifer.android.handnotepad.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import ifer.android.handnotepad.AppController;
import ifer.android.handnotepad.R;
import ifer.android.handnotepad.api.ResponseMessage;
import ifer.android.handnotepad.ui.MainActivity;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;
import static ifer.android.handnotepad.util.AndroidUtils.showToastMessage;

public class LockingUtils {


    // This must have the same value as in HandnotepadServer: config.json: lockTimeout;
    private static long lockTimeout = 30000;
    private static Handler timeoutHandler = new Handler();

    public static void requireLock(final Context context){
        if ( AppController.apiService == null)
            return;
Log.d("DRAW", "[LockingUtils] MainActivity.lockGranted = " + MainActivity.lockGranted) ;

        if (MainActivity.lockGranted == true)
            return;

        String ipaddr = GenericUtils.getIPAddress(true);
Log.d("DRAW", "ipaddr=" + ipaddr);

        Call<ResponseMessage> call =  AppController.apiService.requireLock(ipaddr);
        call.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    ResponseMessage msg = response.body();
                    if (msg.getStatus() == 1 ){
                        MainActivity.lockGranted = true;
                        setLockTimeout(lockTimeout);
                    }
                    else {
                        showToastMessage(context, context.getString(R.string.error_resource_locked));
                    }
                } else {
                    showToastMessage(context, context.getString(R.string.error_server_not_running));
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                showToastMessage(context, context.getString(R.string.error_server_not_running));
            }
        });
    }

    public static void releaseLock(final Context context){
        if ( AppController.apiService == null)
            return;
        Log.d("DRAW", "releasing lock..");
        String ipaddr = GenericUtils.getIPAddress(true);

        Call<String> call =  AppController.apiService.releaseLock(ipaddr);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String msg = response.body();
                    if (msg.equals("OK") ) {
                        MainActivity.lockGranted = false;
                        unsetLockTimeout();
                    }
                    else {
                        showToastMessage(context, context.getString(R.string.error_release_lock));
                    }
                } else {
                    showToastMessage(context,context.getString(R.string.error_server_not_running));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToastMessage(context, context.getString(R.string.error_server_not_running));
            }
        });
    }

    private static void setLockTimeout (long millis){
        timeoutHandler.postDelayed(
                new Runnable() {
                    public void run() {
                        MainActivity.lockGranted = false;
Log.d("DRAW", "lockGranted set to false");
                    }
                }, millis);
    }

    private static void unsetLockTimeout (){
Log.d("DRAW", "Unsetting lock timeout");
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}
