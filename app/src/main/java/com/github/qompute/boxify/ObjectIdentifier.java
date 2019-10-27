package com.github.qompute.boxify;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.os.Environment;
import android.util.Size;
import android.widget.TextView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.Executor;

public class ObjectIdentifier implements ImageAnalysis.Analyzer {
    private FirebaseVisionImageLabeler labeler;
    private FirebaseVisionObjectDetector detector;
    private int prevId;
    private Size imageSize;
    private Bitmap croppedImage;
    private TextView textView;
    private ImageCapture imageCapture;
    private RectangleOverlay rectOverlay;
    private long lastObjectTimeStamp = 0L;
    private boolean imageTaken = false;
    private Executor executor;

    public ObjectIdentifier(TextView labelView, RectangleOverlay overlay,
                            ImageCapture imageCapture, Executor executor) {
        labeler = FirebaseVision.getInstance().getCloudImageLabeler();
        detector = FirebaseVision.getInstance().getOnDeviceObjectDetector();
        textView = labelView;
        rectOverlay = overlay;
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

    private Size findImageSize(Image image, int degrees) {
        switch (degrees) {
            case 90: case 270:
                return new Size(image.getHeight(), image.getWidth());
            default:
                return new Size(image.getWidth(), image.getHeight());
        }
    }

    private void processLabels(List<FirebaseVisionImageLabel> labels) {
        FirebaseVisionImageLabel best = null;
        for (FirebaseVisionImageLabel label : labels) {
            if (best == null || label.getConfidence() > best.getConfidence()) {
                best = label;
            }
        }
        if (best != null && best.getConfidence() > 0.7) {
            textView.setText(best.getText());
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    System.currentTimeMillis() + "_" + best.getText() + ".jpg");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (best == null) {
            textView.setText("");
        }
    }

    private void processObjects(List<FirebaseVisionObject> objects) {
        if (!objects.isEmpty()) {
            double scaleX = rectOverlay.getWidth() / imageSize.getWidth();
            double scaleY = rectOverlay.getHeight() / imageSize.getHeight();

            Rect imageRect = objects.get(0).getBoundingBox();

            Rect viewRect = new Rect((int) (imageRect.left * scaleX),
                    (int) (imageRect.top * scaleY),
                    (int) (imageRect.right * scaleX),
                    (int) (imageRect.bottom * scaleY));
            rectOverlay.setRectangle(viewRect);

            int id = objects.get(0).getTrackingId();
            if (id != prevId) {
                prevId = id;
                lastObjectTimeStamp = System.currentTimeMillis();
                imageTaken = false;
            } else if (!imageTaken && System.currentTimeMillis() - lastObjectTimeStamp > 1000) {
                imageTaken = true;
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedListener() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image, int degrees) {
                        Bitmap bitmap = FirebaseVisionImage.fromMediaImage(image.getImage(),
                                degreesToFirebaseRotation(degrees)).getBitmap();
                        double stretchX = (double) bitmap.getWidth() / imageSize.getWidth();
                        double stretchY = (double) bitmap.getHeight() / imageSize.getHeight();
                        croppedImage = Bitmap.createBitmap(bitmap,
                                (int) (imageRect.left * stretchX),
                                (int) (imageRect.top * stretchY),
                                (int) (imageRect.width() * stretchX),
                                (int) (imageRect.height() * stretchY));

                        FirebaseVisionImage fvImage = FirebaseVisionImage.fromBitmap(croppedImage);
                        labeler.processImage(fvImage)
                                .addOnSuccessListener(ObjectIdentifier.this::processLabels)
                                .addOnFailureListener(Exception::printStackTrace);
                        image.close();
                    }
                });
            }
        } else {
            rectOverlay.setRectangle(null);
        }
    }

    @Override
    public void analyze(ImageProxy imageProxy, int degrees) {
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastAnalyzedTimeStamp < 1000) {
//            return;
//        }
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
        imageSize = new Size(image.getBitmap().getWidth(), image.getBitmap().getHeight());

        detector.processImage(image)
                .addOnSuccessListener(this::processObjects)
                .addOnFailureListener(Exception::printStackTrace);

//        labeler.processImage(image)
//                .addOnSuccessListener(this::processLabels)
//                .addOnFailureListener(Exception::printStackTrace);
//        lastAnalyzedTimeStamp = currentTime;
    }
}
