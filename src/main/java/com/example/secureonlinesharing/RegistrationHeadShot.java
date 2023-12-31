package com.example.secureonlinesharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import java.io.ByteArrayOutputStream;
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








    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    camera = new CameraHandler(this,binding.camera.detectButton
            ,binding.camera.retakeButton, binding.camera.captureView,
            binding.camera.faceView,
            binding.camera.graphicOverlay){


        @Override
        public void onCapture(){

            try {
                Bitmap cropImage = getCropImage();
                int quality = 100;
                File file = new File(getActivity().getCacheDir(), MainActivity.HEADSHOT_CACHE_FILE);
                FileOutputStream output = new FileOutputStream(file);
                System.out.println("headshot: "+ cropImage.getWidth()+"x" + cropImage.getHeight());
                cropImage.compress(Bitmap.CompressFormat.JPEG, quality, output);
                output.close();
                System.out.println("file size: "+ file.length());
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

        Bundle args = getArguments();
        boolean editing= args!=null&&args.containsKey("user_id");

        SharedPreferences sharedData = null;



        String token = "";



        String userId = "";


        if(editing)
        {
          sharedData =   getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
          token = sharedData.getString("jwt", "");

         // userId=  sharedData.getString("id", "");
        }




        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/";
        if(editing){
            url+= "updateUserInfo.php";
        }
        else  {
            url+= "addUser.php" ;
        }

        String finalToken = token;
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                finalToken,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        binding.loadingWrapper.setVisibility(View.INVISIBLE);
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);

                                String message = result.getString("token");
                                System.out.println(message);
//
//                                NavHostFragment.findNavController(RegistrationHeadShot.this)
//                                        .navigate(R.id.action_registrationHeadShot_to_FirstFragment);
                            ((MainActivity)getActivity()).navigate(R.id.FirstFragment,null);

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
                MainActivity.showVolleyError(RegistrationHeadShot.this.getContext(),error);



            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();



                Bundle args = getArguments();

                if(editing){
                    params.put("user_id", args.getString("user_id"));
                    params.put("token", finalToken);
                }

                params.put("user_email", args.getString("user_email"));
                params.put("user_phone",  args.getString("user_phone"));
                params.put("user_first_name",  args.getString("user_first_name"));
                params.put("user_last_name",  args.getString("user_last_name"));
                params.put("user_username",  args.getString("user_username"));
                params.put("user_password",  args.getString("user_password"));
                params.put("facial_image_format", "jpg");

                System.out.println(params);

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                File fileCopy = new File(getActivity().getCacheDir(),
                        MainActivity.HEADSHOT_CACHE_FILE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                    byte[] bytes = new byte[0];
                    try {
                        bytes = Files.readAllBytes(fileCopy.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("headshot size:  "+ bytes.length);
                        params.put("face_profile",
                                new DataPart("file",
                                        //  AppHelper.getFileDataFromDrawable(getContext(),binding.mediaUploadPreview.getDrawable())
                                        bytes,

                                        "image/jpeg"));



//
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }



                }


                return params;

            }

        };

        VolleySingleton.getInstance(

                        getContext()).

                addToRequestQueue(multipartRequest);








    }




}





