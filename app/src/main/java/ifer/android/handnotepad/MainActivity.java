package ifer.android.handnotepad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.raed.drawingview.BrushView;
import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMPORT_IMAGE = 10;
    private DrawingView mDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mDrawingView = findViewById(R.id.drawing_view);

        String b64text = null;
        try {
            b64text = loadBase64File();
        }
        catch (IOException e){
            Log.d("DRAW", "base64 read error: " + e.getLocalizedMessage());
        }
        if (b64text != null && !(b64text.trim().length() == 0)){
            byte[] decodedString = Base64.decode(b64text, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            mDrawingView.setBackgroundImage(decodedByte);
            mDrawingView.initializeDrawingFromBitmap(decodedByte);

        }


        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingView.clear();
            }
        });
        findViewById(R.id.pen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBrushSelected (Brushes.PEN);
            }
        });
        findViewById(R.id.eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBrushSelected (Brushes.ERASER);
            }
        });
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);//ignoring the request code
                    return;
                }
                Bitmap bitmap = mDrawingView.exportDrawing();
                saveAsBase64(bitmap);
//                exportImage(bitmap);
            }
        });

    }

    private void setBrushSelected(int brushID){
        BrushSettings settings = mDrawingView.getBrushSettings();
        settings.setSelectedBrush(brushID);
        Log.d("DRAW", "setBrushSelected: " + String.valueOf(brushID));

        int sizeInPercentage = (int) (settings.getSelectedBrushSize() * 100);
//        mSizeSeekBar.setProgress(sizeInPercentage);
    }

    private void exportImage(Bitmap bitmap) {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        folder.mkdirs();
        File imageFile = new File(folder, UUID.randomUUID() + ".png");
        try {
            storeBitmap(imageFile, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(
                this,
                new String[]{},
                new String[]{"image/png"},
                null);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
    }

    private void storeBitmap(File file, Bitmap bitmap) throws Exception {
        if (!file.exists() && !file.createNewFile())
            throw new Exception("Not able to create " + file.getPath());
        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.flush();
        stream.close();
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
//        MediaScannerConnection.scanFile(
//                this,
//                new String[]{},
//                new String[]{"image/png"},
//                null);
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
    }

    private String loadBase64File ()  throws IOException {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        folder.mkdirs();

        RandomAccessFile file = new RandomAccessFile(new File(folder,   "note-01.b64"), "r");
        byte[] arr = new byte[(int) file.length()];
        file.readFully(arr);
        String result = new String(arr);
        file.close();
Log.d("DRAW", "base64=" + result.substring(0, 99));
        return result;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
