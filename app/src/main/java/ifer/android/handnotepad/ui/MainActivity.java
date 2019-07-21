package ifer.android.handnotepad.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

import org.json.JSONException;
import org.json.JSONObject;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMPORT_IMAGE = 10;
    private DrawingView mDrawingView;
    private ApiInterface apiInterface;
    private ImageButton btnPen;
    private ImageButton btnEraser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main_activity);

        mDrawingView = findViewById(R.id.drawing_view);
        btnPen = findViewById(R.id.btnPen);
        btnPen.setSelected(true);
        btnEraser = findViewById(R.id.btnEraser);
        btnEraser.setSelected(false);

        try {
            AppController.apiService = ApiClient.createService(ApiInterface.class);
        } catch (Exception e) {
            e.printStackTrace();
            showToastMessage(getApplicationContext(), getResources().getString(R.string.connection_error) + " " + e.getLocalizedMessage());
            return;
        }


        remoteLoadBase64 ();
//        String b64text = null;
//        try {
//            b64text = loadBase64File();
//        }
//        catch (IOException e){
//            Log.d("DRAW", "base64 read error: " + e.getLocalizedMessage());
//        }
//
//
//        if (b64text != null && !(b64text.trim().length() == 0)){
//            byte[] decodedString = Base64.decode(b64text, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            mDrawingView.initializeDrawingFromBitmap(decodedByte);
//        }


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


    public void remoteSaveAsBase64 (Bitmap bitmap){
        String b64text = bitmapToBase64(bitmap);
        Drawing drawing = new Drawing(b64text);

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
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToastMessage(getApplicationContext(), getResources().getString(R.string.error_server_not_running));
            }
        });
    }

    public void remoteLoadBase64 () {
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
               showToastMessage(getApplicationContext(), getResources().getString(R.string.error_server_not_running));
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

    class ClearPosAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mDrawingView.clear();
            DrawingView.drawingChanged = false;
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
