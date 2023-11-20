package com.example.secureonlinesharing;

import static androidx.camera.view.CameraController.IMAGE_CAPTURE;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class CameraHandler {

    Fragment fragment;
    Button captureButton, retakeButton;

    ImageView captureView;

    PreviewView preview;






    public Bitmap mSelectedImage, cropImage;
    private GraphicOverlay mGraphicOverlay;

    private LifecycleCameraController cameraController;

    private ActivityResultLauncher<String> requestPermissionLauncher;


    public CameraHandler(Fragment fragment,Button captureButton, Button retakeButton,
                         ImageView captureView, PreviewView preview,GraphicOverlay overlay)
    {
        this.captureButton = captureButton;

        this.retakeButton = retakeButton;
        this.captureView = captureView;

        this.fragment = fragment;
        this.preview = preview;

        this.mGraphicOverlay = overlay;


              requestPermissionLauncher =
                fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startCamera();

                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                });


        if (ContextCompat.checkSelfPermission(
                fragment.getContext(),  "android.permission.CAMERA") ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            startCamera();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    "android.permission.CAMERA");
        }

       captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("capture button click");
                cameraController.takePicture(ContextCompat.getMainExecutor(fragment.getContext()),
                        new ImageCapture.OnImageCapturedCallback() {
                            @Override
                            public void onCaptureSuccess(@NonNull ImageProxy image) {
                                super.onCaptureSuccess(image);
                                mSelectedImage = rotateImage(image.toBitmap(), image.getImageInfo().getRotationDegrees());
//
//
//
//
//
                                // binding.captureView.setRotation(image.getImageInfo().getRotationDegrees());
//                                binding.captureView.setImageBitmap(mSelectedImage);
//                                  binding.captureView.setVisibility(View.VISIBLE;isVisible = true;


                                preview.setVisibility(View.GONE);
                               captureView.setVisibility(View.VISIBLE);
                                captureButton.setVisibility(View.GONE);
                                retakeButton.setVisibility(View.VISIBLE);

                                runFaceContourDetection();

                                 captureView.setImageBitmap(cropImage);
                                 onCapture();


                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                super.onError(exception);
                                System.out.println("capture error");
                                exception.printStackTrace();
                            }
                        });

                //                NavHostFragment.findNavController(FaceAuth.this)
//                        .navigate(R.id.action_faceAuth_to_SecondFragment);
            }
        });



        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                preview.setVisibility(View.VISIBLE);
                captureView.setVisibility(View.GONE);
                captureButton.setVisibility(View.VISIBLE);
                retakeButton.setVisibility(View.GONE);

                onRetake();


            }
        });



            }



    public void startCamera() {



        cameraController = new LifecycleCameraController(fragment.getContext());
        cameraController.bindToLifecycle(fragment);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_FRONT_CAMERA);
        cameraController.setEnabledUseCases(IMAGE_CAPTURE);
        preview.setController(cameraController);

    }



    private void runFaceContourDetection() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

     captureButton.setEnabled(false);
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                captureButton.setEnabled(true);
                                processFaceContourDetectionResult(faces);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                captureButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });




    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Task completed successfully
        if (faces.size() == 0) {
            MainActivity.showToast(fragment,"No face found");
            return;
        } else {
            MainActivity.showToast(fragment,"face found");
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
        Face face = faces.get(0);
        Rect box= face.getBoundingBox();
        System.out.println(box.toString());
        System.out.println(box.toShortString());
        System.out.println(mSelectedImage.getWidth()+ " x " + mSelectedImage.getHeight());


        cropImage = Bitmap.createBitmap(mSelectedImage,box.left,box.top,box.width(),box.height());
       captureView.setImageBitmap(cropImage);
    }



    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


    public static Bitmap flip(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }


    public  void onCapture(){

    }

    public  void onRetake(){}




}


