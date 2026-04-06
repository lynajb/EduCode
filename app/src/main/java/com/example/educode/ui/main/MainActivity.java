package com.example.educode.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Context;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.educode.R;
import com.example.educode.data.DatabaseHelper;
import com.example.educode.data.RoboflowApi;
import com.example.educode.ui.camera.ResultView;
import com.example.educode.ui.history.DetectionsActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends ComponentActivity {

    private PreviewView viewFinder;
    private ImageView capturedImageView;
    private ResultView resultView;
    private TextView resultText;
    private Button btnScan;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private RoboflowApi roboflowApi;
    private DatabaseHelper dbHelper;

    private boolean isResultState = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) startCamera();
                else Toast.makeText(this, "Permission caméra requise", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);
        capturedImageView = findViewById(R.id.capturedImageView);
        resultView = findViewById(R.id.resultView);
        resultText = findViewById(R.id.resultText);
        btnScan = findViewById(R.id.btnScan);

        roboflowApi = new RoboflowApi();
        cameraExecutor = Executors.newSingleThreadExecutor();
        dbHelper = new DatabaseHelper(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        btnScan.setOnClickListener(v -> {
            if (!isResultState) takePhotoAndDetect();
            else resetCamera();
        });

        ImageButton btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DetectionsActivity.class));
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                Log.e("Camera", "Binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhotoAndDetect() {
        if (imageCapture == null) return;
        File photoFile = new File(getExternalCacheDir(), "scan.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        resultText.setText("Capture en cours...");
        btnScan.setEnabled(false);

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        Bitmap rotated = fixRotation(bitmap, photoFile.getAbsolutePath());
                        showFrozenImage(rotated);
                        resultText.setText("Analyse...");
                        runDetection(rotated);
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        btnScan.setEnabled(true);
                        resultText.setText("Erreur de capture");
                    }
                });
    }

    private void runDetection(Bitmap bitmap) {
        roboflowApi.detect(bitmap, new RoboflowApi.ApiCallback() {
            @Override
            public void onSuccess(List<ResultView.DetectionResult> results) {
                runOnUiThread(() -> {
                    btnScan.setEnabled(true);
                    btnScan.setText("RÉINITIALISER");
                    isResultState = true;

                    if (results.isEmpty()) {
                        resultText.setText("Aucun panneau détecté.");
                    } else {
                        // 1. Get current user safely
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        String currentUsername = prefs.getString("username", "Invité");
                        if (currentUsername == null) currentUsername = "default_user";

                        int totalFound = results.size();
                        StringBuilder sb = new StringBuilder();
                        sb.append(totalFound > 1 ? totalFound + " panneaux détectés :\n" : "1 panneau détecté :\n");

                        // 2. Loop through results
                        for (ResultView.DetectionResult res : results) {
                            // ONLY SAVE TO HISTORY (Detections)
                            // We DO NOT increment "Signs Learned" here.
                            dbHelper.saveDetectionRaw(currentUsername, res.label, (float) res.confidence);
                        }

                        resultText.setText(sb.toString());
                        resultView.setResults(results, bitmap.getWidth(), bitmap.getHeight());

                        Toast.makeText(MainActivity.this, "+" + totalFound + " panneaux ajoutés à l'historique !", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnScan.setEnabled(true);
                    resultText.setText("Erreur API : " + error);
                });
            }
        });
    }

    private Bitmap fixRotation(Bitmap bitmap, String path) {
        try {
            android.media.ExifInterface exif = new android.media.ExifInterface(path);
            int orient = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_UNDEFINED);
            float angle = 0;
            switch (orient) {
                case android.media.ExifInterface.ORIENTATION_ROTATE_90: angle = 90; break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_180: angle = 180; break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_270: angle = 270; break;
                default: if (bitmap.getWidth() > bitmap.getHeight()) angle = 90;
            }
            if (angle == 0) return bitmap;
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
    }

    private void showFrozenImage(Bitmap bitmap) {
        viewFinder.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.VISIBLE);
        capturedImageView.setImageBitmap(bitmap);
    }

    private void resetCamera() {
        resultView.setResults(null, 0, 0);
        resultText.setText("Pointez la caméra et appuyez sur SCAN");
        capturedImageView.setVisibility(View.GONE);
        viewFinder.setVisibility(View.VISIBLE);
        btnScan.setText("SCAN");
        isResultState = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}