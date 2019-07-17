package ifer.android.handnotepad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.raed.drawingview.BrushView;
import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMPORT_IMAGE = 10;
    private DrawingView mDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mDrawingView = findViewById(R.id.drawing_view);



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
                exportImage(bitmap);
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
}
