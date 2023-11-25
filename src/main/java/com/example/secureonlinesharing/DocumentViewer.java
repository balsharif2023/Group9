package com.example.secureonlinesharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.graphics.pdf.PdfRenderer;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
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
import com.example.secureonlinesharing.databinding.DocumentViewerBinding;
import com.example.secureonlinesharing.databinding.FragmentThirdBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DocumentViewer extends Fragment {

    private DocumentViewerBinding binding;

    private String mediaId = "";
    private int pdfPageNum;

    private PdfRenderer renderer;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = DocumentViewerBinding.inflate(inflater, container, false);
        // create a new renderer


        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mediaId = getArguments().getString("media_id");

        getMedia();


        // new Thread(new RetrievePDFfromUrl(pdfurl)).start();
        //new RetrievePDFfromUrl(pdfurl).run();
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(DocumentViewer.this)
                            .navigate(R.id.action_documentViewer_to_SecondFragment);
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

                bundle.putString("media_id", mediaId);
                NavHostFragment.findNavController(DocumentViewer.this)
                        .navigate(R.id.action_documentViewer_to_mediaUploader, bundle);


            }
        });


        binding.pdfForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    if (renderer.getPageCount() - 1 > pdfPageNum) {
                        pdfPageNum++;
                        renderer = RetrieveFileFromUrl.openPdf(getActivity(),
                                binding.mediaView,
                                MainActivity.MEDIA_VIEWER_CACHE_FILE, pdfPageNum);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        binding.pdfBackButton.setOnClickListener(view1 -> {


            try {
                if (pdfPageNum > 0) {
                    pdfPageNum--;
                    renderer = RetrieveFileFromUrl.openPdf(getActivity(), binding.mediaView,
                            MainActivity.MEDIA_VIEWER_CACHE_FILE, pdfPageNum);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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
                        binding.mediaDescription.setText(response.getString("mediaDescription"));

                        String owner = response.getString("mediaOwnerId");

                        System.out.println("owner: " + owner + " user: " + userId);
                        String accessPath = response.getString("mediaAccessPath");

                        System.out.println(accessPath);

                        new Thread(new RetrieveFileFromUrl(DocumentViewer.this, accessPath, MainActivity.MEDIA_VIEWER_CACHE_FILE) {

                            @Override
                            public void onRetrieve() {
                                try {
                                    if (contentType.equals("application/pdf")) {
                                        renderer = RetrieveFileFromUrl.openPdf(getActivity(), binding.mediaView,
                                                MainActivity.MEDIA_VIEWER_CACHE_FILE, 0);
                                        binding.pdfControls.setVisibility(View.VISIBLE);
                                    } else if (contentType.startsWith("image")) {
                                        RetrieveFileFromUrl.openImage(getActivity(), binding.mediaView, MainActivity.MEDIA_VIEWER_CACHE_FILE);
                                        binding.pdfControls.setVisibility(View.GONE);


                                    }


                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }).start();

                        if (owner.equals(userId)) {
                            String firstName = data.getString("firstName", "");
                            String lastName = data.getString("lastName", "");

                            binding.mediaOwner.setText(firstName + " " + lastName);

                        } else
                            binding.mediaOwner.setText("");
                        String json1 = "{\"ivanSendsBack\":[{\"user_name\": \"joeblow\"},{\"user_name\": \"joeblow\"},{\"user_name\": \"joeblow\"}]}";
                        JSONObject authUser = new JSONObject(json1);
                        showUserList(getActivity(),authUser,binding.authUsers);
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static void showUserList(Activity activity, JSONObject response,LinearLayout wrapper) {

        String userList;
        try {
            userList = response.getString("ivanSendsBack");
            System.out.println(userList);
            JSONArray json = new JSONArray(userList);
            View prev = null;

           // binding.authUsers.removeAllViews();
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                View view = LayoutInflater.from(activity).inflate(R.layout.user_record, null);
                ((TextView) view.findViewById(R.id.userName)).setText(entry.getString("user_name"));

                ((ImageButton) view.findViewById(R.id.authUserEditButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


                ((ImageButton) view.findViewById(R.id.authUserDeleteButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {




                    }
                });

                wrapper.addView(view);
                if (prev != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 40);
                    prev.setLayoutParams(params);
                }

                prev = view;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}


