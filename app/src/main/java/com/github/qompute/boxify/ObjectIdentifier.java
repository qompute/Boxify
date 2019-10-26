package com.github.qompute.boxify;

import android.media.Image;
import android.widget.TextView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.util.List;

public class ObjectIdentifier implements ImageAnalysis.Analyzer {
    private FirebaseVisionImageLabeler labeler;
    private String prevId;
    private TextView textView;
    private long lastAnalyzedTimeStamp = 0L;

    public ObjectIdentifier(TextView labelView) {
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        textView = labelView;
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    private void processLabels(List<FirebaseVisionImageLabel> labels) {
        FirebaseVisionImageLabel best = null;
        for (FirebaseVisionImageLabel label : labels) {
            if (best == null || label.getConfidence() > best.getConfidence()) {
                best = label;
            }
        }
        if (best != null && !best.getEntityId().equals(prevId)) {
            textView.setText(best.getText());
            prevId = best.getEntityId();
        }
    }

    @Override
    public void analyze(ImageProxy imageProxy, int degrees) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnalyzedTimeStamp < 1000) {
            return;
        }
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

        labeler.processImage(image)
                .addOnSuccessListener(this::processLabels)
                .addOnFailureListener(Exception::printStackTrace);
        lastAnalyzedTimeStamp = currentTime;
    }
}
