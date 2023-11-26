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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class FaceAuth extends Fragment {
    private FaceAuthBinding binding;





    private CameraHandler camera;




    private int imgSize = 112;
// Set this image size according to our choice of DNN Model (160 for FaceNet, 112 for MobileFaceNet)

    private ImageProcessor imageTensorProcessor = new ImageProcessor.Builder()
            .add(new ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(new CastOp(DataType.FLOAT32))
            .build();

    private ByteBuffer convertBitmapToBuffer(Bitmap image) {
        return imageTensorProcessor.process(TensorImage.fromBitmap(image)).getBuffer();
    }

    Interpreter.Options interpreterOptions;


    Interpreter interpreter;

    private float[][] runFaceNet(Bitmap ref, Bitmap capture) {
        Map<Integer, Object> faceNetModelOutputs = new HashMap<>();
        //  faceNetModelOutputs[0] = Array(1) { FloatArray(embeddingDim) }

        // The shape of *1* output's tensor
        int[] OutputShape = {};
// The type of the *1* output's tensor
        DataType OutputDataType;
// The multi-tensor ready storage


// For each model's tensors (there are getOutputTensorCount() of them for this tflite model)

        OutputShape = interpreter.getOutputTensor(0).shape();
        OutputDataType = interpreter.getOutputTensor(0).dataType();
        TensorBuffer outputTensor = TensorBuffer.createFixedSize(OutputShape, OutputDataType);
        ByteBuffer outputBuffer = outputTensor.getBuffer();

        //  System.out.println("Created a buffer of " + outputBuffer.limit()+ " bytes for tensor "+ 0);

        int[] InputShape = interpreter.getInputTensor(0).shape();
        DataType InputDataType = interpreter.getInputTensor(0).dataType();

        ByteBuffer b1 = convertBitmapToBuffer(ref), b2 = convertBitmapToBuffer(capture);
        ByteBuffer inputBuffer = //ByteBuffer.allocate(b1.capacity()+b2.capacity())
                TensorBuffer.createFixedSize(InputShape, InputDataType).getBuffer()
                        .put(b1).put(b2)
                        .order(ByteOrder.nativeOrder());


        TensorBuffer inputTensor = TensorBuffer.createFixedSize(InputShape, InputDataType);
        inputTensor.loadBuffer(inputBuffer);
        //  System.out.println("Created a tflite output of %d output tensors."+ faceNetModelOutputs.size());
        Object[] inputs = {inputTensor.getBuffer()};
        interpreter.run(inputTensor.getBuffer(), outputBuffer);

        float[] arr = outputTensor.getFloatArray();

        float[][] matrix = new float[2][arr.length / 2];

        for (int i = 0; i < arr.length / 2; i++) {
            matrix[0][i] = arr[i];
            matrix[1][i] = arr[arr.length / 2 + i];
        }

//      //  System.out.println(Arrays.toString(OutputShape));
//        System.out.println(Arrays.toString(matrix[0]));
//
//        System.out.println(Arrays.toString(matrix[1]));

        // return ((TensorBuffer)faceNetModelOutputs.get(0)).getFloatArray();
        return matrix;
    }

    private float l2Norm(float[] x1, float[] x2) {
        float sum = 0.0f, mag1 = 0.0f, mag2 = 0.0f;

        for (int i = 0; i < x1.length; i++) {
            mag1 += x1[i] * x1[i];

            mag2 += x2[i] * x2[i];
        }
        mag1 = (float) Math.sqrt(mag1);
        mag2 = (float) Math.sqrt(mag2);

        for (int i = 0; i < x1.length; i++) {

            sum += Math.pow((x1[i] / mag1) - (x2[i] / mag2), 2);
        }
        return (float) Math.sqrt(sum);
    }

    private float cosineSim(float[] x1, float[] x2) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < x1.length; i++) {
            dotProduct += x1[i] * x2[i];
            normA += Math.pow(x1[i], 2);
            normB += Math.pow(x2[i], 2);
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = FaceAuthBinding.inflate(inflater, container, false);
        // create a new renderer


        return binding.getRoot();

    }





    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        camera = new CameraHandler(this, binding.camera.detectButton
                , binding.camera.retakeButton, binding.camera.captureView,
                binding.camera.faceView,
                binding.camera.graphicOverlay) {

            @Override
            public void onCapture() {


                if(getCropImage()!= null){
                   if(faceMatch()) {
                       NavHostFragment.findNavController(FaceAuth.this)
                               .navigate(R.id.action_faceAuth_to_mediaViewer,
                                       getArguments());
                   }

                }

            }
        };


        interpreterOptions = new Interpreter.Options().setNumThreads(4);


        try {
            interpreter = new Interpreter(FileUtil.loadMappedFile(getContext(), "MobileFaceNet.tflite"), interpreterOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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



//        final ZoomLinearLayout zoomLinearLayout = (ZoomLinearLayout) getActivity().findViewById(R.id.zoom_linear_layout);
//        zoomLinearLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                zoomLinearLayout.init(getContext());
//                return false;
//            }
//        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public boolean faceMatch() {


        int[] img = {
                R.drawable.jay1,

                R.drawable.jay2,
                R.drawable.jay3,
                R.drawable.jay4,

                R.drawable.jay5,

                R.drawable.tom1,
                R.drawable.tom2,
                R.drawable.tom3,
                R.drawable.tom4,
                R.drawable.tom5,
                R.drawable.tom6,
                R.drawable.shaq,
                R.drawable.owl,
                R.drawable.green,
                R.drawable.boston

        };

        String[] names = {
                "jay1",
                "jay2",
                "jay3",
                "jay4",
                "jay5",
                "tom1",
                "tom2",
                "tom3",
                "tom4",
                "tom5",
                "tom6",
                "shaq",
                "owl",
                "green",
                "boston"

        };

        float bestScore = 0;
        int bestIndex = 0;
        for (int i = -1; i < img.length; i++) {
            Bitmap img1 = null;
            if(i<0)
            {
                File imgFile = new File(getActivity().getCacheDir(), MainActivity.HEADSHOT_CACHE_FILE);


                img1 = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                System.out.println("comparing profile_pic " );

            }
            else {
                img1 = BitmapFactory.decodeResource(getContext().getResources(),
                        img[i]);
                System.out.println("comparing image " + names[i]);
            }


            float[][] output = runFaceNet(img1, camera.getCropImage());

            float score = cosineSim(output[0], output[1]);
            System.out.println("\t" + ( i<0? "profile_pic":names[i]) + ": " + score);

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }


        } //*/

        System.out.println("best match is " +  ( bestIndex<0? "profile_pic":names[bestIndex])  + " with score " + bestScore);
        return bestIndex<0;

    }

    public  void cropAll(){

        int[] img = {
                R.drawable.tom1,

                R.drawable.jay2,
                R.drawable.jay3,
                R.drawable.jay4,

                R.drawable.jay5,

                R.drawable.tom1,
                R.drawable.tom2,
                R.drawable.tom3,
                R.drawable.tom4,
                R.drawable.tom5,
                R.drawable.tom6,
                R.drawable.shaq,


        };
        String fileName ="";

        String cropFile ="";


        camera = new CameraHandler(this, binding.camera.detectButton
                , binding.camera.retakeButton, binding.camera.captureView,
                binding.camera.faceView,
                binding.camera.graphicOverlay) {

            @Override
            public void onCapture() {
                File file = new File(fileName);
                FileOutputStream output = null ;
                try {
                    output = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                getCropImage().compress(Bitmap.CompressFormat.JPEG,100,output);
            }
        };

        for(int  i=0 ; i< 1;i++)
        {
            TypedValue value = new TypedValue();
            getResources().getValue(img[i], value, true);
            // check value.string if not null - it is not null for drawables...

            fileName =value.string.toString();

            camera.fileName = fileName.replace(".jpg","_crop.jpg")
                    .replace(".jpeg","_crop.jpeg")
                            .replace("res/drawable","sdcard");

            camera.setSelectedImage(BitmapFactory.decodeResource(getContext().getResources(),
                    img[i]));
            camera.runFaceContourDetection();
        }
    }



}




