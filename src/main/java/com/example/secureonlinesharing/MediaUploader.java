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
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MediaUploader extends MediaFragment {

    private MediaUploaderBinding binding;
    private boolean edit = false;
    private File attachment = null;



    private String extension = "";

    private  String  mimeType ="";

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
                        RetrieveFileFromUrl.copyToCache(getActivity(), inputStream,
                                MainActivity.MEDIA_UPLOAD_CACHE_FILE);
                        File file = new File(getActivity().getCacheDir(),
                                MainActivity.MEDIA_UPLOAD_CACHE_FILE);

                        System.out.println("Cache file size:  "+ file.length());

                        ContentResolver cR = getContext().getContentResolver();
                         mimeType = cR.getType(uri);
                        System.out.println(uri);
                        System.out.println(mimeType);
                        if (mimeType.equals("application/pdf")) {
                            RetrieveFileFromUrl.openPdf(getActivity(),
                                    binding.mediaUploadPreview, MainActivity.MEDIA_UPLOAD_CACHE_FILE,0);
                            extension = ".pdf";
                            mediaType = "pdf";
                        } else if (mimeType.startsWith("image")) {
                            RetrieveFileFromUrl.openImage(getActivity(), binding.mediaUploadPreview,
                                    MainActivity.MEDIA_UPLOAD_CACHE_FILE);
                            if (mimeType.equals("image/png")) {
                                extension = ".png";
                                mediaType = "png";
                            }
                            else if (mimeType.equals("image/jpeg")) {
                                extension = ".jpeg";
                                mediaType = "jpg";

                            }

                        } else if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                            extension = ".docx";
                            mediaType = "docx";
                        }
                        else if (mimeType.startsWith("video"))
                        {
                            if (mimeType.equals("video/mp4")) {
                                extension = ".mp4";
                                mediaType = "mpeg";
                            }

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

        mediaTitle = binding.mediaTitleInput;

        mediaDescription = binding.mediaDescriptionInput;

        mediaOwner = null;

        authUsers = binding.authUsers;

        authUserNone = binding.authUserNone;

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        File file = new File(getActivity().getCacheDir(),MainActivity.MEDIA_UPLOAD_CACHE_FILE);
        file.delete();





        edit = mediaId!= null && !mediaId.equals("");
        if (edit) {
            getMedia();
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

                if(validateForm()) {

                    saveMedia();
                }
            }


        });
        Spinner dropdown = binding.mediaAccessRuleDropdown;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.access_rules, R.layout.dropdown_item);
        adapter.setDropDownViewResource(R.layout.dropdown_item);
        dropdown.setAdapter(adapter);

        binding.addAuthUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();

                bundle.putString("action","authorize");
                bundle.putString("media_id", mediaId);

//                NavHostFragment.findNavController(MediaUploader.this)
//                        .navigate(R.id.action_mediaUploader_to_authUserSearch, bundle);

                ((MainActivity)getActivity()).navigate(R.id.authUserSearch,bundle);



            }
        });
    }



    public  void onGetMedia(JSONObject response){

        String[]values = {"concurrent_only",
        "concurrent_with_video",
        "face_authentication"};

        String value = null;
        try {
            value = response.getString("accessRules");

            binding.mediaAccessRuleDropdown.setSelection(Arrays.asList(values).indexOf(value));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    public boolean validateForm() {
        // extract the entered data from the EditText

        String mediaTitle = binding.mediaTitleInput.getText().toString();
        String mediaDescription = binding.mediaDescriptionInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean titleValid = !mediaTitle.isEmpty();
        binding.mediaTitleEmptyMessage.setVisibility(titleValid? View.GONE : View.VISIBLE);

        boolean descriptionValid = !mediaDescription.isEmpty();
        binding.mediaDescriptionEmptyMessage.setVisibility(descriptionValid? View.GONE : View.VISIBLE);

        attachment = new File(getActivity().getCacheDir(),MainActivity.MEDIA_UPLOAD_CACHE_FILE);

        boolean fileValid = edit || attachment.exists();

        binding.fileEmptyMessage.setVisibility(fileValid?View.GONE:View.VISIBLE);

        return titleValid && descriptionValid&& fileValid;

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

                            bundle.putString("media_id", mediaId);
        //                            NavHostFragment.findNavController(MediaUploader.this)
        //                                    .navigate(R.id.action_mediaUploader_to_mediaViewer, bundle);
                            ((MainActivity)getActivity()).navigate(R.id.mediaViewer,bundle);


                            Log.i("Message", message);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                binding.loadingWrapper.setVisibility(View.INVISIBLE);
                MainActivity.showVolleyError(MediaUploader.this.getContext(),error);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (edit) {
                    params.put("mediaId", mediaId);
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String accessRule= binding.mediaAccessRuleDropdown.getSelectedItem().toString();

                accessRule= accessRule.toLowerCase().replaceAll(" ","_");

                params.put("ownerId", id);
                params.put("fileTitle", binding.mediaTitleInput.getText().toString());
                if(attachment.exists())
                    params.put("fileName", "file_" + timestamp.getTime() + extension);


                params.put("accessRules",accessRule);
                params.put("concurrentAccess", "1");
                params.put("mediaDescription", binding.mediaDescriptionInput.getText().toString());
                params.put("mediaType", mediaType);

                System.out.println(accessRule);

                System.out.println(params);

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
//                File fileCopy = new File(getActivity().getCacheDir(),
//                        MainActivity.MEDIA_UPLOAD_CACHE_FILE);



                if (attachment.exists()&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        params.put("file",
                                new DataPart("file",
                                        //  AppHelper.getFileDataFromDrawable(getContext(),binding.mediaUploadPreview.getDrawable())
                                        Files.readAllBytes(attachment.toPath()),
                                        mimeType));
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
    public void onDisplayUser(View view,Bundle info){

        ImageButton editButton =  ((ImageButton) view.findViewById(R.id.authUserEditButton)),
                deleteButton = ((ImageButton) view.findViewById(R.id.authUserDeleteButton));

            String userId= info.getString("user_id");

        editButton.setVisibility(View.VISIBLE);

        deleteButton.setVisibility(View.VISIBLE);



     editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putAll(info);



                bundle.putString("media_id",mediaId);




//
//                NavHostFragment.findNavController(MediaUploader.this)
//                        .navigate(R.id.action_mediaUploader_to_authUserPermissions,bundle);
                ((MainActivity)getActivity()).navigate(R.id.authUserPermissions,bundle);




            }
        });


     deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {

                removeAccess(userId,view);




            }
        });




    }

    public void removeAccess(String userId,View userView) {
        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");



        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/removeAuthorizedAccess.php";

        url+="?mediaId="+mediaId;
        url+="&userId="+userId;




        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.GET,

                url,

                null,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object

                    try {
                        System.out.println(response);
                        System.out.println(response.getString("message"));

                        userView.setVisibility(View.GONE);

                        MainActivity.showToast(MediaUploader.this,response.getString("message"));



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(MediaUploader.this.getContext(), error);

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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }









}