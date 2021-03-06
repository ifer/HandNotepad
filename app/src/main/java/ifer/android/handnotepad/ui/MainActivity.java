package ifer.android.handnotepad.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ifer.android.handnotepad.AppController;
import ifer.android.handnotepad.R;
import ifer.android.handnotepad.api.ApiClient;
import ifer.android.handnotepad.api.ApiInterface;
import ifer.android.handnotepad.api.Drawing;
import static ifer.android.handnotepad.util.AndroidUtils.*;
import static ifer.android.handnotepad.util.GenericUtils.isEmptyOrNull;

import ifer.android.handnotepad.util.AndroidUtils;
import ifer.android.handnotepad.util.Constants;
import ifer.android.handnotepad.util.LockingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected Context context;
    private final String VERSION_PATTERN = "@version@";
    private static final int REQUEST_CODE_IMPORT_IMAGE = 10;
    private DrawingView mDrawingView;
    private ApiInterface apiInterface;
    private ImageButton btnPen;
    private ImageButton btnEraser;

//    public static boolean lockGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_main);

        mDrawingView = findViewById(R.id.drawing_view);
        btnPen = findViewById(R.id.btnPen);
        btnPen.setSelected(true);
        btnEraser = findViewById(R.id.btnEraser);
        btnEraser.setSelected(false);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupButtons();

        if (AppController.connectionEstablished == false)
            setupConnection(getApplicationContext());

        remoteLoadBase64 ();


    }

    public void setupButtons(){
        findViewById(R.id.btnPen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBrushSelected (Brushes.PEN);
                view.setSelected(true);
                btnEraser.setSelected(false);
            }
        });
        findViewById(R.id.btnEraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBrushSelected (Brushes.ERASER);
                view.setSelected(true);
                btnPen.setSelected(false);
            }
        });
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(MainActivity.this,  Popup.WARNING,  getString(R.string.warn_clear_refresh),  new ClearPosAction(), new DoNothingAction());
            }
        });
        findViewById(R.id.btnRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(MainActivity.this, Popup.WARNING, getString(R.string.warn_clear_refresh), new RefreshPosAction(), new DoNothingAction());
            }
        });
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DrawingView.drawingChanged == false)
                    return;
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);//ignoring the request code
                    return;
                }
                Bitmap bitmap = mDrawingView.exportDrawing();
                remoteSaveAsBase64(bitmap);

//                exportImage(bitmap);
            }
        });
    }

    private void setBrushSelected(int brushID){
        BrushSettings settings = mDrawingView.getBrushSettings();
        settings.setSelectedBrush(brushID);
//        Log.d("DRAW", "setBrushSelected: " + String.valueOf(brushID));

        int sizeInPercentage = (int) (settings.getSelectedBrushSize() * 100);
//        mSizeSeekBar.setProgress(sizeInPercentage);
    }


    public static void setupConnection (Context context){
        SharedPreferences settings = context.getSharedPreferences(Constants.SETTINGS_NAME, 0);

        String serverURL = settings.getString(Constants.PrefServerKey, null);
        AppController.setApiDomain(serverURL);


        if (isEmptyOrNull(AppController.getApiDomain())) {
//            AndroidUtils.showPopupInfo(context, context.getString(R.string.warn_credentials_needed));
            showToastMessage(context, context.getString(R.string.warn_credentials_needed));
            return;
        }


        try {
            AppController.apiService = ApiClient.createService(ApiInterface.class);
        } catch (Exception e) {
            e.printStackTrace();
            showToastMessage(context, context.getString(R.string.connection_error) + " " + e.getLocalizedMessage());
            return;
        }

        if (AppController.apiService != null) {
            testConnection(context, false);
        }
        else {
            showToastMessage(context, context.getString(R.string.wrong_credentials));
        }
    }

    public static void testConnection(Context c, boolean showSuccessMessage) {

        final Context context = c;
        final boolean showSuccess = showSuccessMessage;

        if (AppController.apiService == null){
            showToastMessage(context, context.getString(R.string.wrong_credentials));
            return;
        }

        Call<String> call = AppController.apiService.testConnection();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String msg = response.body();
                    if (msg.equals("OK")) {
                        AppController.connectionEstablished = true;
                        if(showSuccess)
                            showToastMessage(context, context.getResources().getString(R.string.connection_ok));
                    }
                } else {
                    String e = response.errorBody().source().toString();
                    showToastMessage(context, context.getResources().getString(R.string.wrong_credentials) + "\n" + e);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToastMessage(context, context.getResources().getString(R.string.wrong_credentials));
            }
        });
    }




    public void remoteSaveAsBase64 (Bitmap bitmap){
        String b64text = bitmapToBase64(bitmap);
        Drawing drawing = new Drawing(b64text);
//Log.d("DRAW", "size=" + b64text.length());


        Call<String> call =  AppController.apiService.saveImage(drawing);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String msg = response.body();
                    if (msg.equals("OK")) {
                        showToastMessage(getApplicationContext(), getResources().getString(R.string.saved));
                    }

                    DrawingView.drawingChanged = false;
                } else {
                    showToastMessage(getApplicationContext(), getResources().getString(R.string.error_save));
                }
                LockingUtils.releaseLock(getApplicationContext());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToastMessage(getApplicationContext(), getResources().getString(R.string.error_server_not_running) + " (1)");
            }
        });
    }

    public void remoteLoadBase64 () {
        if ( AppController.apiService == null)
            return;
        Call<String> call =  AppController.apiService.readImage();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String base64drawing = response.body();
                    if (base64drawing != null && base64drawing.trim().length() != 0) {
                        byte[] decodedString = Base64.decode(base64drawing, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mDrawingView.initializeDrawingFromBitmap(decodedByte);
                        DrawingView.drawingChanged = false;
//                        showToastMessage(getApplicationContext(), "Success!");
                    }
                } else {
                    String e = response.errorBody().source().toString();
                    showToastMessage(getApplicationContext(), getResources().getString(R.string.error_drawing_retrieval) + " " + e);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
               showToastMessage(getApplicationContext(), getResources().getString(R.string.error_server_not_running) + " (2)");
            }
        });


    }

    private void saveAsBase64 (Bitmap bitmap){
        String b64text = bitmapToBase64(bitmap);

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        folder.mkdirs();
        File b64File = new File(folder,   "note-01.b64");

        try {
            FileOutputStream stream = new FileOutputStream(b64File);
            stream.write(b64text.getBytes());
            stream.flush();
            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String loadBase64File ()  throws IOException {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        folder.mkdirs();

        RandomAccessFile file = new RandomAccessFile(new File(folder,   "note-01.b64"), "r");
        byte[] arr = new byte[(int) file.length()];
        file.readFully(arr);
        String result = new String(arr);
        file.close();
//Log.d("DRAW", "base64=" + result.substring(0, 99));
        return result;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP); // NO_WRAP: do not insert newline characters
    }

    public void showAbout (){
        String version = null;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e){
            printLog( e.getLocalizedMessage());
        }
        if (version == null)
            version = getResources().getString(R.string.version_uknown);

        String text = getResources().getString(R.string.text_about);
        text = text.replace(VERSION_PATTERN, version);

//        AndroidUtils.showPopupInfo(this, text);
        AndroidUtils.showPopup(context, AndroidUtils.Popup.INFO, getString(R.string.action_about), text, null, null);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent wc = new Intent(this, SettingsActivity.class);
            startActivity(wc);
        }
        else if (id == R.id.nav_check_connection) {
            testConnection(this, true);
        }
        else if (id == R.id.nav_about) {
            showAbout ();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    class ClearPosAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
                LockingUtils.requireLock(MainActivity.this.context, LockingUtils.ACTION_CLEAR, mDrawingView); // Will be cleared by LockingUtils
        }
    }

    class DoNothingAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    }

    class RefreshPosAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            remoteLoadBase64 ();
            DrawingView.drawingChanged = false;
        }
    }



    @Override
    public void onBackPressed() {
//printLog("exit", "drawingChanged=" + DrawingView.drawingChanged);
            if (DrawingView.drawingChanged){
                 showPopup(this,  Popup.WARNING,  getString(R.string.warn_not_saved),  new ExitPosAction(), new DoNothingAction());
            }
            else {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAndRemoveTask();
                else
                    finish();
            }
    }

    class ExitPosAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (Build.VERSION.SDK_INT >= 21)
                finishAndRemoveTask();
            else
                finish();

            System.exit(0);
        }
    }


}
