package com.example.secureonlinesharing;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.secureonlinesharing.databinding.MediaUploaderBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MediaUploader extends Fragment {

    private MediaUploaderBinding binding;
    private boolean edit = false;

    private String mediaId = "";

    private String extension = "";

    private  String mediaType="";

    // GetContent creates an ActivityResultLauncher<String> to let you pass
// in the mime type you want to let the user select
    private ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    System.out.println(uri.getPath());
                    System.out.println(uri.getPathSegments());

                    try {
                        InputStream inputStream =
                                getActivity().getContentResolver().openInputStream(uri);
                        RetrieveFileFromUrl.copyToCache(getActivity(), inputStream,"temp2 ");
                        File file = new File(getActivity().getCacheDir(),"profile_pic");

                        System.out.println("Cache file size:  "+ file.length());

                        ContentResolver cR = getContext().getContentResolver();
                        String type = cR.getType(uri);
                        System.out.println(uri);
                        System.out.println(type);
                        if (type.equals("application/pdf")) {
                            DocumentViewer.openPdf(getActivity(), binding.mediaUploadPreview, 0);
                            extension = ".pdf";
                            mediaType = "pdf";
                        } else if (type.startsWith("image")) {
                            RetrieveFileFromUrl.openImage(getActivity(), binding.mediaUploadPreview, "temp2");
                            if (type.equals("image/png")) {
                                extension = ".png";
                                mediaType = "png";
                            }
                            else if (type.equals("image/jpeg")) {
                                extension = ".jpeg";
                                mediaType = "jpg";

                            }

                        } else if (type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                            extension = ".docx";
                            mediaType = "docx";
                        }
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
        if (backButton != null) {
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
        if (userMenuButton != null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }

        mediaId = getArguments().getString("mediaId");
        edit = !mediaId.equals("");
        if (edit) {
            getMedia(mediaId);
            binding.mediaUploadButton.setText(R.string.media_edit_button_label);
        } else {
            binding.mediaUploadButton.setText(R.string.media_upload_button_label);
        }

        binding.filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("*/*");

            }

        });
        binding.mediaUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMedia();
            }


        });
        Spinner dropdown = binding.mediaAccessRuleDropdown;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.access_rules, R.layout.dropdown_item);
        adapter.setDropDownViewResource(R.layout.dropdown_item);
        dropdown.setAdapter(adapter);
    }

    public void getMedia(String mediaId) {
        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");


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
                        binding.mediaTitleInput.setText(response.getString("mediaTitle"));
                        binding.mediaDescriptionInput.setText(response.getString("mediaDescription"));

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


    public void saveMedia() {
        binding.loadingWrapper.setVisibility(View.VISIBLE);
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/";
        if (edit) {
            url += "editMedia.php";

        } else {
            url += "createMedia.php";
        }
        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String id = data.getString("id", "");

        String token = data.getString("jwt", "");
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                token,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        binding.loadingWrapper.setVisibility(View.INVISIBLE);
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);

                            String message = result.getString("message");
                            if (result.has("mediaId"))
                                mediaId = result.getString("mediaId");

                            Bundle bundle = new Bundle();

                            bundle.putString("mediaId", mediaId);
                            NavHostFragment.findNavController(MediaUploader.this)
                                    .navigate(R.id.action_mediaUploader_to_documentViewer, bundle);


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
                if (edit) {
                    params.put("mediaId", mediaId);
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                params.put("ownerId", id);
                params.put("fileTitle", binding.mediaTitleInput.getText().toString());
                params.put("fileName", "file_" + timestamp.getTime() + extension);
                params.put("accessRules", binding.mediaAccessRuleDropdown.getSelectedItem().toString());
                params.put("concurrentAccess", "1");
                params.put("mediaDescription", binding.mediaDescriptionInput.getText().toString());
                params.put("mediaType", mediaType);

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                File fileCopy = new File(getActivity().getCacheDir(), "temp2");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        params.put("file",
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}