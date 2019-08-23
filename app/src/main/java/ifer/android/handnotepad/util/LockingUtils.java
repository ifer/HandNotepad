package ifer.android.handnotepad.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.raed.drawingview.DrawingView;

import java.io.IOException;
import java.net.SocketTimeoutException;

import ifer.android.handnotepad.AppController;
import ifer.android.handnotepad.R;
import ifer.android.handnotepad.api.ResponseMessage;
import ifer.android.handnotepad.ui.MainActivity;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;
import static ifer.android.handnotepad.util.AndroidUtils.showToastMessage;

public class LockingUtils {
    public static int ACTION_DRAW=1;
    public static int ACTION_CLEAR=2;

    public static boolean lockRequested = false;

    private DrawingView mDrawingView;

    // This must have the same value as in HandnotepadServer: config.json: lockTimeout;
    private static long lockTimeout = 30000;
    private static Handler timeoutHandler = new Handler();



    public static void requireLock (final Context context, final int action, final DrawingView drawingView){
        if ( AppController.apiService == null)
            return;


        if (lockRequested == true)
            return;

        lockRequested = true;


        String ipaddr = GenericUtils.getIPAddress(true);
//Log.d("DRAW", "ipaddr=" + ipaddr);

        final MainActivity mainActivity = (MainActivity) context;

        Call<ResponseMessage> call =  AppController.apiService.requireLock(ipaddr);
        call.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    ResponseMessage msg = response.body();
                    if (msg.getStatus() == 1 ){
//                        setLockTimeout(lockTimeout);
                        if (action == ACTION_CLEAR)
                            drawingView.clear();
                    }
                    else {
                        showToastMessage(context, context.getString(R.string.error_resource_locked));
                        //Lock request rejected, so reload image as it was
                        mainActivity.remoteLoadBase64();
                    }

                } else {
                    showToastMessage(context, context.getString(R.string.error_server_not_running) + " (3)");
                }
                lockRequested = false;
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                if (t instanceof IOException) {
                    Log.d("DRAW","Network Error:" + t.getLocalizedMessage());
                    if(t instanceof SocketTimeoutException){
                        Log.d("DRAW", "Socket Time out");
                    }
                    // logging probably not necessary
                }
                else {
                    Log.d("DRAW", "conversion issue! big problems :(");
                    // todo log to some central bug tracking service
                }
                lockRequested = false;
                showToastMessage(context,  context.getString(R.string.error_server_not_running) + " (4)");
            }
        });
    }


    public static void releaseLock(final Context context){
        if ( AppController.apiService == null)
            return;
//Log.d("DRAW", "releasing lock..");
        String ipaddr = GenericUtils.getIPAddress(true);

        Call<ResponseMessage> call =  AppController.apiService.releaseLock(ipaddr);
        call.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.isSuccessful()) {
                    ResponseMessage msg = response.body();
                    if (msg.getStatus() == 1 ) {
                        unsetLockTimeout();
                    }
                    else {
                        showToastMessage(context, msg.getMessage());
                    }
                } else {
                    showToastMessage(context,  context.getString(R.string.error_server_not_running)+ " (5)");
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                showToastMessage(context,  context.getString(R.string.error_server_not_running)+ " (6)");
            }
        });
    }

    private static void setLockTimeout (long millis){
        timeoutHandler.postDelayed(
                new Runnable() {
                    public void run() {
//Log.d("DRAW", "lockGranted set to false");
                    }
                }, millis);
    }

    private static void unsetLockTimeout (){
//Log.d("DRAW", "Unsetting lock timeout");
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}
