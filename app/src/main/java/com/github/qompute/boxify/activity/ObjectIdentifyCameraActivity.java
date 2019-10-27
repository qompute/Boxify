package com.github.qompute.boxify.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.qompute.boxify.R;
import com.github.qompute.boxify.classifyutils.ObjectIdentifier;
import com.github.qompute.boxify.classifyutils.RectangleOverlay;
import com.github.qompute.boxify.data.BitmapLabelPair;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ObjectIdentifyCameraActivity extends AppCompatActivity {

    public static List<BitmapLabelPair> dataList = new ArrayList<>();

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
        private int maxAmount;

        public ObjectIdentifier(TextView labelView, RectangleOverlay overlay,
                                ImageCapture imageCapture, Executor executor) {
            labeler = FirebaseVision.getInstance().getCloudImageLabeler();
            detector = FirebaseVision.getInstance().getOnDeviceObjectDetector();
            textView = labelView;
            rectOverlay = overlay;
            prevId = -1;
            this.imageCapture = imageCapture;
            this.executor = executor;
        }

        public ObjectIdentifier(TextView labelView, RectangleOverlay overlay,
                                ImageCapture imageCapture, Executor executor, int maxAmount) {
            this(labelView, overlay, imageCapture, executor);
            this.maxAmount = maxAmount;
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
                if (!doneTakingPics) {
                    dataList.add(new BitmapLabelPair(croppedImage, best.getText()));
                }

                if (!doneTakingPics && (maxAmount > 0 && dataList.size() >= maxAmount)) {
                    doneTakingPics = true;
                    finish();
                }

            } else if (best == null) {
                textView.setText("");
            }
        }

        private void processObjects(List<FirebaseVisionObject> objects) {
            processObjects(objects, false);
        }

        private void processObjects(List<FirebaseVisionObject> objects, boolean force) {
            if (!objects.isEmpty()) {
                double scaleX = rectOverlay.getWidth() / imageSize.getWidth();
                double scaleY = rectOverlay.getHeight() / imageSize.getHeight();

                Rect imageRect = objects.get(objects.size() - 1).getBoundingBox();

                Rect viewRect = new Rect((int) (imageRect.left * scaleX),
                        (int) (imageRect.top * scaleY),
                        (int) (imageRect.right * scaleX),
                        (int) (imageRect.bottom * scaleY));
                rectOverlay.setRectangle(viewRect);

                int id = objects.get(objects.size() - 1).getTrackingId();
                if (id != prevId && !force) {
                    prevId = id;
                    lastObjectTimeStamp = System.currentTimeMillis();
                    imageTaken = false;
                } else if ((!imageTaken && System.currentTimeMillis() - lastObjectTimeStamp > 1000 && !doneTakingPics) || force) {
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
                                    .addOnSuccessListener(ObjectIdentifyCameraActivity.ObjectIdentifier.this::processLabels)
                                    .addOnFailureListener(Exception::printStackTrace);
                            image.close();
                        }
                    });
                }
            } else {
                rectOverlay.setRectangle(null);
            }
        }

        public void takePicture() {
            imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedListener() {
                @Override
                public void onCaptureSuccess(ImageProxy image, int degrees) {
                    doneTakingPics = true;
                    dataList.add(new BitmapLabelPair(FirebaseVisionImage.fromMediaImage(image.getImage(),
                            degreesToFirebaseRotation(degrees)).getBitmap(), ""));
                    finish();
                    image.close();
                }
            });
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

    private int REQUEST_CODE_PERMISSIONS = 10;
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private TextureView viewFinder;
    private TextView label;
    private RectangleOverlay rectOverlay;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ObjectIdentifier objectIdentifier;

    private boolean doneTakingPics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataList.clear();
        setContentView(R.layout.activity_camera);

        viewFinder = findViewById(R.id.view_finder);
        label = findViewById(R.id.text_view);
        rectOverlay = findViewById(R.id.rect_overlay);

        /** Code to work on the floating action button, used to add new items into the data */
        if (getIntent().getIntExtra("number", 0) > 0) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    objectIdentifier.takePicture();
                }
            });
        } else {
            findViewById(R.id.fab).setVisibility(View.GONE);
            findViewById(R.id.fab2).setVisibility(View.VISIBLE);
            FloatingActionButton fab = findViewById(R.id.fab2);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doneTakingPics = true;
                    finish();
                }
            });
        }

        if (allPermissionsGranted()) {
            viewFinder.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ObjectIdentifyCameraActivity.this.updateTransform();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void startCamera() {
        PreviewConfig previewConfig = new PreviewConfig.Builder().build();
        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                if (!doneTakingPics) {
                    ViewGroup parent = (ViewGroup) viewFinder.getParent();
                    parent.removeView(viewFinder);
                    parent.addView(viewFinder, 0);

                    viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                    ObjectIdentifyCameraActivity.this.updateTransform();
                }
            }
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

        ImageAnalysisConfig analyzerConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig);
        objectIdentifier = new ObjectIdentifier(label, rectOverlay,
                imageCapture, executor, getIntent().getIntExtra("number", 0));
        analyzerUseCase.setAnalyzer(executor, objectIdentifier);

        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();

        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        float rotationDegrees;
        switch (viewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_0: rotationDegrees = 0f; break;
            case Surface.ROTATION_90: rotationDegrees = 90f; break;
            case Surface.ROTATION_180: rotationDegrees = 180f; break;
            case Surface.ROTATION_270: rotationDegrees = 270f; break;
            default: return;
        }
        matrix.postRotate(-rotationDegrees, centerX, centerY);
        viewFinder.setTransform(matrix);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post(this::startCamera);
            } else {
                Toast.makeText(this, "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void finish() {
        if (doneTakingPics) {
            Intent data = new Intent(this, AddLocaleActivity.class);
            setResult(Activity.RESULT_OK, data);
        } else {
            dataList.clear();
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    private boolean allPermissionsGranted() {
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
