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
import android.graphics.drawable.Drawable;
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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class FaceAuth extends Fragment {
    private FaceAuthBinding binding;




    private String mediaId = "";
    private int pdfPageNum;

    private PdfRenderer renderer;

    private CameraHandler camera;





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

    private float[][] runFaceNet(Bitmap ref, Bitmap capture) {
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
        Object[]inputs = {inputTensor.getBuffer()};
        interpreter.run(inputTensor.getBuffer(), outputBuffer);

        float[] arr = outputTensor.getFloatArray();

        float[][] matrix = new float[2][arr.length/2];

        for (int i =0;i< arr.length/2;i++)
        {
            matrix[0][i] =arr[i];
            matrix[1][i]=arr[arr.length/2+i];
        }

//      //  System.out.println(Arrays.toString(OutputShape));
//        System.out.println(Arrays.toString(matrix[0]));
//
//        System.out.println(Arrays.toString(matrix[1]));

        // return ((TensorBuffer)faceNetModelOutputs.get(0)).getFloatArray();
        return matrix;
    }

    private float l2Norm(float[] x1 , float[]x2 )  {
        float sum = 0.0f, mag1 =0.0f, mag2 =0.0f;

         for( int i =0; i<x1.length; i++)
        {
            mag1+= x1[i]* x1[i];

            mag2+= x2[i] * x2[i];
        }
         mag1= (float) Math.sqrt(mag1);
         mag2 = (float) Math.sqrt(mag2);

        for( int i =0; i<x1.length; i++)
        {

            sum += Math.pow((x1[i] / mag1) - (x2[i] / mag2),2);
        }
        return (float) Math.sqrt(sum);
    }

    private float cosineSim(float[] x1 , float[]x2 )
    {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i =0; i<x1.length; i++)
        {
            dotProduct += x1[i] * x2[i];
            normA += Math.pow(x1[i],2);
            normB += Math.pow(x2[i],2);
        }
        return (float) (dotProduct / (Math.sqrt(normA) *Math.sqrt(normB)));
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





    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    camera = new CameraHandler(this,binding.camera.detectButton,binding.camera.retakeButton, binding.camera.captureView,binding.camera.faceView);

     interpreterOptions =new Interpreter.Options().setNumThreads(4);


        try {
            interpreter = new Interpreter(FileUtil.loadMappedFile(getContext(), "MobileFaceNet.tflite"), interpreterOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }









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




public  void faceMatch() {


    Bitmap tom1 = BitmapFactory.decodeResource(getContext().getResources(),
            R.drawable.tom5);

    Bitmap tom2 = BitmapFactory.decodeResource(getContext().getResources(),
            R.drawable.shaq);





                           /*     float[][] output = runFaceNet(tom1,tom2);

                                float score = cosineSim(output[0], output[1]);
                                System.out.println("matching score: "+ score);

                               showToast("matching score: "+ score);

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

                                String[] names =   {
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


                               for(int i =0; i<img.length;i++)
                               {
                                   Bitmap img1 = BitmapFactory.decodeResource(getContext().getResources(),
                                          img[i]);
                                   System.out.println("comparing image "+ names[i]);

                                   for(int j =0; j<img.length;j++)
                                   {
                                       Bitmap img2 = BitmapFactory.decodeResource(getContext().getResources(),
                                              img[j]);



                                        output = runFaceNet(img1,img2);

                                        score = cosineSim(output[0], output[1]);
                                       System.out.println("\t" +names[j]+  ": "+ score);

                                   }


                               } //*/

}
}




