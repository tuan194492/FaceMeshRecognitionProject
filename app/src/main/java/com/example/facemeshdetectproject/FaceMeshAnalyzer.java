package com.example.facemeshdetectproject;

import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.facemeshdetectproject.graphic.FaceMeshOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.List;

public class FaceMeshAnalyzer implements ImageAnalysis.Analyzer {

    FaceMeshOverlay faceMeshOverlay;
//
    public FaceMeshAnalyzer(FaceMeshOverlay faceMeshOverlay) {
        super();
        this.faceMeshOverlay = faceMeshOverlay;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        FaceMeshDetector detector =
                FaceMeshDetection.getClient(new FaceMeshDetectorOptions.Builder()
                        .setUseCase(1)
                        .build());
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Task<List<FaceMesh>> result = detector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FaceMesh>>() {
                                @Override
                                public void onSuccess(List<FaceMesh> faceMeshes) {
                                    for (FaceMesh faceMesh : faceMeshes) {
                                        Rect bounds = faceMesh.getBoundingBox();

                                        // Gets all points
                                        List<FaceMeshPoint> faceMeshPoints = faceMesh.getAllPoints();
                                        for (FaceMeshPoint faceMeshpoint : faceMeshPoints) {
                                            int index = faceMeshPoints.indexOf(faceMeshpoint);
                                            PointF3D position = faceMeshpoint.getPosition();
                                            Log.d("Z", "Got image " + index + " at " + position.toString());

                                        }
                                    }
                                    faceMeshOverlay.drawFaceMesh(faceMeshes);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                }
                    });
            imageProxy.close();
        }

    }
}
