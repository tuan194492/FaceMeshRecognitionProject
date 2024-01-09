package com.example.facemeshdetectproject;

import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FaceMeshAnalyzer implements ImageAnalysis.Analyzer {


    FaceMeshOverlay faceMeshOverlay;
    FaceMeshDetector detector;
//
//    DatabaseHelper databaseHelper = new DatabaseHelper(this.faceMeshOverlay.getContext());
    public FaceMeshAnalyzer(FaceMeshOverlay faceMeshOverlay) {
        super();
        this.faceMeshOverlay = faceMeshOverlay;
        FaceMeshDetectorOptions.Builder optionsBuilder = new FaceMeshDetectorOptions.Builder();
        detector = FaceMeshDetection.getClient(optionsBuilder.build());
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Task<List<FaceMesh>> result = detector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FaceMesh>>() {

                                private boolean compareVectors(ArrayList<Double> v1, ArrayList<Double> v2, double delta) {
                                    double count = 0;
//                                    Log.d("TAG", "compareVectors: "+ v1.size() +" "+ v2.size());
                                    for (int i = 0; i < v1.size(); i++) {
                                        if (Math.abs(v1.get(i) - v2.get(i)) > delta) {
                                            count +=1;
                                        }
                                    }
                                    if(count/36 < 0.4){
                                        return true;
                                    }
                                    Log.d("TAG", "compareVectorss: "+ count);

                                    return false;
                                }
                                private ArrayList<Double> stringToArrayList(String inputString) {
                                    ArrayList<Double> doubleArrayList = new ArrayList<>();
                                    // Convert the array to an ArrayList
                                    String[] stringNumbersArray = inputString.substring(1, inputString.length() - 1).split(",");

                                    for (String stringNumber : stringNumbersArray) {
                                        double doubleNumber = Double.parseDouble(stringNumber.trim());
                                        doubleArrayList.add(doubleNumber);
                                    }

                                    return doubleArrayList;
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
                                @Override
                                public void onSuccess(List<FaceMesh> faceMeshes) {
                                    double delta = 1;
                                    for (FaceMesh faceMesh : faceMeshes) {
                                        ArrayList<Double> distanceList = new ArrayList<>();
                                        Rect bounds = faceMesh.getBoundingBox();


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
//                                        Log.d("D", "onSuccess: " + distanceList.toString());
                                        ArrayList<FaceData> faceData = faceMeshOverlay.getData();
//                                        Log.d("Z", "onSuccess: " + faceData.toString());
                                        for(FaceData face : faceData){
                                            String distanceString = face.getFaceMeshPoints();
                                            ArrayList<Double>distance = this.stringToArrayList(distanceString);
                                            if(this.compareVectors(distanceList, distance, delta)){
                                                Log.d("Z", "tenjsid" + face.getName());
                                            }
                                            else {
                                                Log.d("Z", "Khong biet ai ca");
                                            }
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
