package com.example.secureonlinesharing;

import static androidx.camera.view.CameraController.IMAGE_CAPTURE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import androidx.camera.view.PreviewView;

import com.example.secureonlinesharing.databinding.FaceAuthBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class FaceAuth extends Fragment {
    private FaceAuthBinding binding;


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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


    private String mediaId = "";
    private int pdfPageNum;

    private PdfRenderer renderer;


    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    private LifecycleCameraController cameraController;


    // creating a variable
    // for PDF view.
    // PDFView pdfView;

    // url of our PDF file.
    String pdfurl = "https://innshomebase.com/securefilesharing/develop/public/loremipsum.pdf";

    private int imgSize = 112;
// Set this image size according to our choice of DNN Model (160 for FaceNet, 112 for MobileFaceNet)

    private ImageProcessor imageTensorProcessor = new ImageProcessor.Builder()
            .add( new ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR))
            .add( new CastOp( DataType.FLOAT32 ))
            .build();

    private ByteBuffer convertBitmapToBuffer( Bitmap image)  {
        return imageTensorProcessor.process(TensorImage.fromBitmap(image)).getBuffer();
    }

    Interpreter.Options interpreterOptions;



    Interpreter interpreter;

    private FloatBuffer runFaceNet(Bitmap ref, Bitmap capture) {
        Map<Integer, Object> faceNetModelOutputs =  new HashMap<>();
      //  faceNetModelOutputs[0] = Array(1) { FloatArray(embeddingDim) }

        // The shape of *1* output's tensor
        int[] OutputShape= {};
// The type of the *1* output's tensor
        DataType OutputDataType;
// The multi-tensor ready storage
     


// For each model's tensors (there are getOutputTensorCount() of them for this tflite model)

            OutputShape = interpreter.getOutputTensor(0).shape();
            OutputDataType = interpreter.getOutputTensor(0).dataType();
           TensorBuffer outputTensor = TensorBuffer.createFixedSize(OutputShape, OutputDataType);
            ByteBuffer outputBuffer =outputTensor.getBuffer();

            System.out.println("Created a buffer of " + outputBuffer.limit()+ " bytes for tensor "+ 0);

        int[] InputShape = interpreter.getInputTensor(0).shape();
        DataType InputDataType = interpreter.getInputTensor(0).dataType();

        ByteBuffer b1 = convertBitmapToBuffer(ref), b2 = convertBitmapToBuffer(capture);
        ByteBuffer inputBuffer = //ByteBuffer.allocate(b1.capacity()+b2.capacity())
                TensorBuffer.createFixedSize(InputShape, InputDataType).getBuffer().put(b1).put(b2);



        TensorBuffer inputTensor = TensorBuffer.createFixedSize(InputShape, InputDataType);
        inputTensor.loadBuffer(inputBuffer);
        System.out.println("Created a tflite output of %d output tensors."+ faceNetModelOutputs.size());
        Object[]inputs = {inputTensor.getBuffer()};
        interpreter.run(inputTensor.getBuffer(), outputBuffer);

        System.out.println(Arrays.toString(OutputShape));
        System.out.println(Arrays.toString(outputTensor.getFloatArray()));
       // return ((TensorBuffer)faceNetModelOutputs.get(0)).getFloatArray();
        return outputBuffer.asFloatBuffer();
    }

//    private float l2Norm(float[] x1 , float[]x2 )  {
//        float sum = 0.0;
//         for( int i =0; i<x1.length; i++)
//        {
//
//        }
//        float mag1 = sqrt(x1.map { xi -> xi.pow(2) }.sum())
//        float mag2 = sqrt(x2.map { xi -> xi.pow(2) }.sum())
//        for (i in x1.indices) {
//            sum += ((x1[i] / mag1) - (x2[i] / mag2)).pow(2)
//        }
//        return sqrt(sum)
//    }
//







    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = FaceAuthBinding.inflate(inflater, container, false);
        // create a new renderer


        return binding.getRoot();

    }

    public static Bitmap openImage(Activity activity, ImageView img) {
        File imgFile = new File(activity.getCacheDir(), "temp");


        // on below line we are checking if the image file exist or not.
        if (imgFile.exists()) {
            // on below line we are creating an image bitmap variable
            // and adding a bitmap to it from image file.
            Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            // on below line we are setting bitmap to our image view.
            img.setImageBitmap(imgBitmap);
            return imgBitmap;
        }
        return null;
    }

    public static PdfRenderer openPdf(Activity activity, ImageView img, int pdfPageNum) throws IOException {

        // Copy sample.pdf from 'res/raw' folder into cache so PdfRenderer can handle it

        //   File fileCopy = copyToCache(activity,input);
        File fileCopy = new File(activity.getCacheDir(), "temp");
        // We get a page from the PDF doc by calling 'open'
        ParcelFileDescriptor fileDescriptor =
                ParcelFileDescriptor.open(fileCopy,
                        ParcelFileDescriptor.MODE_READ_ONLY);

        PdfRenderer renderer = new PdfRenderer(fileDescriptor);


        PdfRenderer.Page mPdfPage = renderer.openPage(pdfPageNum);
        // Create a new bitmap and render the page contents into it
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int pageWidth = mPdfPage.getWidth();
        int pageHeight = mPdfPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, pageHeight * width / pageWidth,
                Bitmap.Config.ARGB_8888);
        mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // Set the bitmap in the ImageView
        img.setImageBitmap(bitmap);

        return renderer;
    }

    public static File copyToCache(Activity activity, InputStream input) throws IOException {


//Get input stream object to read the pdf
        File file = new File(activity.getCacheDir(), "temp");
        FileOutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int size;
        // Copy the entire contents of the file
        while ((size = input.read(buffer)) != -1) {
            output.write(buffer, 0, size);
        }
//Close the buffer
        input.close();
        output.close();
        return file;

    }


    public void startCamera() {


        PreviewView previewView = binding.faceView;
        cameraController = new LifecycleCameraController(getContext());
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_FRONT_CAMERA);
        cameraController.setEnabledUseCases(IMAGE_CAPTURE);
        previewView.setController(cameraController);

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGraphicOverlay = binding.graphicOverlay;

//        mediaId = getArguments().getString("mediaId");
//
//        getMedia();

     interpreterOptions =new Interpreter.Options().setNumThreads(4);


        try {
            interpreter = new Interpreter(FileUtil.loadMappedFile(getContext(), "MobileFaceNet.tflite"), interpreterOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        if (ContextCompat.checkSelfPermission(
                getContext(), "android.permission.CAMERA") ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            startCamera();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    "android.permission.CAMERA");
        }

        binding.detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("capture button click");
                cameraController.takePicture(ContextCompat.getMainExecutor(getContext()),
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
                                binding.captureView.setImageBitmap(mSelectedImage);
//                                  binding.captureView.setVisibility(View.VISIBLE;isVisible = true;


                                binding.faceView.setVisibility(View.GONE);
                                binding.captureView.setVisibility(View.VISIBLE);
                                binding.detectButton.setVisibility(View.GONE);
                                binding.retakeButton.setVisibility(View.VISIBLE);

                                //runFaceContourDetection();
                                runFaceNet(mSelectedImage,mSelectedImage);

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


        binding.retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                binding.faceView.setVisibility(View.VISIBLE);
                binding.captureView.setVisibility(View.GONE);
                binding.detectButton.setVisibility(View.VISIBLE);
                binding.retakeButton.setVisibility(View.GONE);


            }
        });


        // new Thread(new RetrievePDFfromUrl(pdfurl)).start();
        //new RetrievePDFfromUrl(pdfurl).run();
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(FaceAuth.this)
                            .navigate(R.id.action_faceAuth_to_SecondFragment);
                }
            });


        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton != null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }
        pdfPageNum = 0;


        binding.mediaEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();

                bundle.putString("mediaId", mediaId);
                NavHostFragment.findNavController(FaceAuth.this)
                        .navigate(R.id.action_documentViewer_to_mediaUploader, bundle);


            }
        });


//        final ZoomLinearLayout zoomLinearLayout = (ZoomLinearLayout) getActivity().findViewById(R.id.zoom_linear_layout);
//        zoomLinearLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                zoomLinearLayout.init(getContext());
//                return false;
//            }
//        });


    }

    public void getMedia() {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");

        JSONObject json;


        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/accessMedia.php";
        url += "?mediaId=" + mediaId;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.GET,

                url,

                null,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String message;
                    try {
                        // message = response.getString("token");
                        //System.out.println(message);

                        System.out.println(response.getString("mediaAccessPath"));
                        binding.mediaTitle.setText(response.getString("mediaTitle"));

                        String owner = response.getString("mediaOwnerId");

                        System.out.println("owner: " + owner + " user: " + userId);
                        String accessPath = response.getString("mediaAccessPath");

                        System.out.println(accessPath);

                        new Thread(new RetrieveFileFromUrl(accessPath)).start();

                        if (owner.equals(userId)) {
                            String firstName = data.getString("firstName", "");
                            String lastName = data.getString("lastName", "");


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    //  Toast.makeText(getActivity(), "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                    // log the error message in the error stream
                    //    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                    NetworkResponse networkResponse = error.networkResponse;
                    //System.out.println("status code: "+ networkResponse.statusCode );
                    String errorMessage = "Unknown error";
                    if (networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            errorMessage = "Request timeout";
                        } else if (error.getClass().equals(NoConnectionError.class)) {
                            errorMessage = "Failed to connect server";
                        }
                        Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        String result = new String(networkResponse.data);
                        try {
                            JSONObject response = new JSONObject(result);

                            String message = response.getString("message");


                            Log.e("Error Message", message);
                            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                            toast.show();

                            if (networkResponse.statusCode == 404) {
                                errorMessage = "Resource not found";
                            } else if (networkResponse.statusCode == 401) {
                                errorMessage = message + " Please login again";
                            } else if (networkResponse.statusCode == 400) {
                                errorMessage = message + " Check your inputs";
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = message + " Something is getting wrong";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("Error", errorMessage);
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                HashMap<String, String> headers2 = new HashMap<String, String>();
                for (String s : headers.keySet()) {
                    headers2.put(s, headers.get(s));
                }
                headers2.put("Authorization", "Bearer " + token);
                return headers2;
            }
        };
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
        //} // catch(JSONException e){} */


    }

    // create an async task class for loading pdf file from URL.
    class RetrieveFileFromUrl implements Runnable {
        private String urlStr;

        private String contentType;

        public RetrieveFileFromUrl(String url) {
            this.urlStr = url;
        }

        @Override
        public void run() {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlStr);
                // below is the step where we are
                // creating our connection.
                urlConnection = (HttpsURLConnection) url.openConnection();
                System.out.println(urlConnection);
                System.out.println(urlConnection.getResponseCode());

                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    contentType = urlConnection.getContentType();
                    System.out.println(contentType);
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    copyToCache(getActivity(), inputStream);
                }
                urlConnection.disconnect();

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();

            }


            getActivity().runOnUiThread(new Runnable() {
                public void run() {
//                    try {
//                        if (contentType.equals("application/pdf"))
//                        {
//                            renderer = openPdf(getActivity(), binding.mediaView, 0);
//                            binding.pdfControls.setVisibility(View.VISIBLE);
//                        }
//                        else if (contentType.startsWith("image")) {
//                          mSelectedImage =  openImage(getActivity(), binding.mediaView);
//                            binding.pdfControls.setVisibility(View.GONE);
//
//
//                        }
//
//
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                }
            });
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void runFaceContourDetection() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        binding.detectButton.setEnabled(false);
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                binding.detectButton.setEnabled(true);
                                processFaceContourDetectionResult(faces);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                binding.detectButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });

    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Task completed successfully
        if (faces.size() == 0) {
            showToast("No face found");
            return;
        } else {
            showToast("face found");
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

}




