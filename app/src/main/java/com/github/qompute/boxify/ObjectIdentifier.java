package com.github.qompute.boxify;

import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

public class ObjectIdentifier implements ImageAnalysis.Analyzer {
    private FirebaseVisionImageLabeler labeler;
    private String prevId;
    private TextView textView;
    private ImageCapture imageCapture;
    private long lastAnalyzedTimeStamp = 0L;
    private Executor executor;

    public ObjectIdentifier(TextView labelView, ImageCapture imageCapture, Executor executor) {
        labeler = FirebaseVision.getInstance().getCloudImageLabeler();
        textView = labelView;
        this.imageCapture = imageCapture;
        this.executor = executor;
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
        if (best != null && best.getConfidence() > 0.7 && !best.getEntityId().equals(prevId)) {
            textView.setText(best.getText());
            prevId = best.getEntityId();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    System.currentTimeMillis() + "_" + best.getText() + ".jpg");
            Log.d("TRYING TO SAVE", file.getPath());
            imageCapture.takePicture(file, executor, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {}

                @Override
                public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) { }
            });
        } else if (best == null) {
            textView.setText("");
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
