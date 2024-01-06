package com.example.facemeshdetectproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.facemeshdetectproject.graphic.FaceMeshOverlay;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity  {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageButton record, flipCamera;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;

    private FaceMeshOverlay faceMeshOverlay;
    Camera camera;
    ProcessCameraProvider cameraProvider = null;

    ImageAnalysis.Analyzer analyzer;

    int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        record = findViewById(R.id.start_record);
        flipCamera = findViewById(R.id.flip_camera);
        faceMeshOverlay = findViewById(R.id.face_mesh_overlay);

        record.setOnClickListener(this::onClickRecord);
        flipCamera.setOnClickListener(this::onClickFlipCamera);
        previewView = findViewById(R.id.preview);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                System.out.println("Tuan NQ");
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, getExecutor());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraFacing).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
        imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(getExecutor(), new FaceMeshAnalyzer(faceMeshOverlay));
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void onClickFlipCamera(View view) {
        if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
            cameraFacing = CameraSelector.LENS_FACING_FRONT;
        } else {
            cameraFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCameraX(cameraProvider);
    }

    private void onClickRecord(View view) {
        List<FaceMesh> faceMeshes = this.faceMeshOverlay.getFaceMeshList();
        ArrayList<ArrayList<Float>> faceMeshPointList = new ArrayList<>();
        ArrayList<Double> distanceList = new ArrayList<>();
//        for (FaceMesh faceMesh : faceMeshes) {
//            List<FaceMeshPoint> faceMeshPoints = faceMesh.getAllPoints();
//            for (FaceMeshPoint faceMeshPoint : faceMeshPoints) {
//                float x = faceMeshPoint.getPosition().getX();
//                float y = faceMeshPoint.getPosition().getY();
//                float z = faceMeshPoint.getPosition().getZ();
//                ArrayList<Float> xyzValues = new ArrayList<>();
//                xyzValues.add(x);
//                xyzValues.add(y);
//                xyzValues.add(z);
//
//                faceMeshPointList.add(xyzValues);
//            }
//        }
        for (FaceMesh faceMesh : faceMeshes) {
            List<FaceMeshPoint> faceMeshPoints = new ArrayList<>();
            FaceMeshPoint rootPoint = faceMesh.getPoints(12).get(0);
            for (int i=1;i<=12;i++){
                int len = faceMesh.getPoints(i).size();
                faceMeshPoints.add(faceMesh.getPoints(i).get(0));
                faceMeshPoints.add(faceMesh.getPoints(i).get(len - 1));
            }
            int l = faceMeshPoints.size();
            for(int i=0; i<l;i+=2){
                distanceList.add(this.distance(faceMeshPoints.get(i), faceMeshPoints.get(i+1)));
                distanceList.add(this.distance(faceMeshPoints.get(i), rootPoint));
                distanceList.add(this.distance(faceMeshPoints.get(i+1), rootPoint));
            }
        }
        Log.d("Z", "onClickRecord: " + distanceList.toString());
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("faceMeshPoints", distanceList.toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private double distance(FaceMeshPoint point1, FaceMeshPoint point2){
        float x1 = point1.getPosition().getX();
        float y1 = point1.getPosition().getY();
        float z1 = point1.getPosition().getZ();

        float x2 = point2.getPosition().getX();
        float y2 = point2.getPosition().getY();
        float z2 = point2.getPosition().getZ();

        return Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2) + Math.pow((z1-z2), 2));
    }
}