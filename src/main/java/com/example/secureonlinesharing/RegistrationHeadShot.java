package com.example.secureonlinesharing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.secureonlinesharing.databinding.RegistrationHeadshotBinding;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class RegistrationHeadShot extends Fragment {
    private RegistrationHeadshotBinding binding;




    private String mediaId = "";
    private int pdfPageNum;

    private PdfRenderer renderer;

    private CameraHandler camera;





    // creating a variable
    // for PDF view.
    // PDFView pdfView;





    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = RegistrationHeadshotBinding.inflate(inflater, container, false);
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

    camera = new CameraHandler(this,binding.camera.detectButton
            ,binding.camera.retakeButton, binding.camera.captureView,
            binding.camera.faceView,
            binding.camera.graphicOverlay){


        @Override
        public void onCapture(){

            try {
                int quality = 100;
                File file = new File(getActivity().getCacheDir(), "temp");
                FileOutputStream output = new FileOutputStream(file);
                cropImage.compress(Bitmap.CompressFormat.JPEG, quality, output);
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            binding.registerButton.setEnabled(true);

        }


        @Override
        public void onRetake(){

            binding.registerButton.setEnabled(false);
        }

    };






        // new Thread(new RetrievePDFfromUrl(pdfurl)).start();
        //new RetrievePDFfromUrl(pdfurl).run();
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(RegistrationHeadShot.this)
                            .navigate(R.id.action_faceAuth_to_SecondFragment);
                }
            });


        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton != null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }
        pdfPageNum = 0;





//        final ZoomLinearLayout zoomLinearLayout = (ZoomLinearLayout) getActivity().findViewById(R.id.zoom_linear_layout);
//        zoomLinearLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                zoomLinearLayout.init(getContext());
//                return false;
//            }
//        });


        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                submitForm();
            }

        });


    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    public void submitForm()  {

        JSONObject data ;
        try {

            Bundle args = getArguments();
            data= new JSONObject();
            data.put("user_email", args.getString("user_email"));
            data.put("user_phone",  args.getString("user_phone"));
            data.put("user_first_name",  args.getString("user_first_name"));
            data.put("user_last_name",  args.getString("user_last_name"));
            data.put("user_username",  args.getString("user_username"));
            data.put("user_password",  args.getString("user_password"));

        } catch (JSONException e) {

            e.printStackTrace();
            return;
        }

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/addUser.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                null,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        binding.loadingWrapper.setVisibility(View.INVISIBLE);
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);

                                String message = result.getString("token");
                                System.out.println(message);

                                NavHostFragment.findNavController(RegistrationHeadShot.this)
                                        .navigate(R.id.action_registrationHeadShot_to_FirstFragment);
                                // load the image into the ImageView using Glide.
                                //binding.textviewFirst.setText(message);

                            Log.i("Message", message);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                binding.loadingWrapper.setVisibility(View.INVISIBLE);

                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
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
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();



                Bundle args = getArguments();
                params.put("user_email", args.getString("user_email"));
                params.put("user_phone",  args.getString("user_phone"));
                params.put("user_first_name",  args.getString("user_first_name"));
                params.put("user_last_name",  args.getString("user_last_name"));
                params.put("user_username",  args.getString("user_username"));
                params.put("user_password",  args.getString("user_password"));
                params.put("facial_image_format", "jpg");

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                File fileCopy = new File(getActivity().getCacheDir(), "temp");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        params.put("face_profile",
                                new DataPart("file",
                                        //  AppHelper.getFileDataFromDrawable(getContext(),binding.mediaUploadPreview.getDrawable())
                                        Files.readAllBytes(fileCopy.toPath()),

                                        "image/jpeg"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }


                return params;

            }

        };

        VolleySingleton.getInstance(

                        getContext()).

                addToRequestQueue(multipartRequest);








    }




}




