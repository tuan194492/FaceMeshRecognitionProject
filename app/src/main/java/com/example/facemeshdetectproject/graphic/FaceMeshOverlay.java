package com.example.facemeshdetectproject.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.ArrayList;
import java.util.List;

public class FaceMeshOverlay extends View {

    private List<FaceMesh> faceMeshList = new ArrayList<>();
    private final Paint paint = new Paint();

    public FaceMeshOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ContextCompat.getColor(getContext(), android.R.color.black));
        paint.setStrokeWidth(3f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.init();
        for (FaceMesh facemesh : faceMeshList) {
            canvas.scale(1.5F, 1.5F);
//            paint.setColor(Color.BLACK);
            canvas.drawRect(facemesh.getBoundingBox() , paint);
            for (FaceMeshPoint faceMeshPoint : facemesh.getAllPoints()) {
                canvas.drawPoint(faceMeshPoint.getPosition().getX(), faceMeshPoint.getPosition().getY(), paint);
            }

        }
    }

    public List<FaceMesh> getFaceMeshList() {
        return faceMeshList;
    }

    public void setFaceMeshList(List<FaceMesh> faceMeshList) {
        this.faceMeshList = faceMeshList;
    }

    public void refreshFaceMeshList() {
        this.faceMeshList = new ArrayList<>();
    }

    public void drawFaceMesh(List<FaceMesh> faceMeshes) {
        this.refreshFaceMeshList();
        this.faceMeshList.addAll(faceMeshes);
        this.invalidate();
    }
}
