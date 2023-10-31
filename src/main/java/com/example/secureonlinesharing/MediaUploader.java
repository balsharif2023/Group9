package com.example.secureonlinesharing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.secureonlinesharing.databinding.MediaUploaderBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaUploader extends Fragment {

    private MediaUploaderBinding binding;



    // GetContent creates an ActivityResultLauncher<String> to let you pass
// in the mime type you want to let the user select
    private ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    try {
                        InputStream inputStream =
                                getActivity().getContentResolver().openInputStream(uri);
                        DocumentViewer.copyToCache(getActivity(),inputStream);
                        DocumentViewer.openPdf(getActivity(),binding.mediaUploadPreview,0);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = MediaUploaderBinding.inflate(inflater, container, false);
        getActivity().setTitle("Olympus");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //((MainActivity) getActivity()).backButton.setVisibility(View.INVISIBLE);
       ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(MediaUploader.this)
                            .navigate(R.id.action_mediaUploader_to_SecondFragment);

                }
            });
        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }

        binding.filePicker.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mGetContent.launch("application/pdf");

             }

         });
        binding.mediaUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/createMedia.php";
                SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
                String id = data.getString("id","");

                String token = data.getString("jwt","");
                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        token,
                        new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);

                            String message = result.getString("message");


                                Log.i("Message", message);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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

                                if (networkResponse.statusCode == 404) {
                                    errorMessage = "Resource not found";
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message+" Please login again";
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message+ " Check your inputs";
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message+" Something is getting wrong";
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

                        params.put("ownerId", id);
                        params.put("fileTitle", "preview");
                        params.put("fileName","preview.jpg");
                        params.put("accessRules", "concurrent_only");
                        params.put("concurrentAccess", "1");
                        params.put("mediaDescription", "Cool Picture");
                        params.put("mediaType", "word");

                        return params;
                    }

                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        // file name could found file base or direct access from real path
                        // for now just get bitmap data from ImageView
                        params.put("file", new DataPart("preview.jpg", AppHelper.getFileDataFromDrawable(getContext(), binding.mediaUploadPreview.getDrawable()), "image/jpeg"));

                        return params;
                    }
                };

                VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);
            }





        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}