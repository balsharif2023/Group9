package com.example.secureonlinesharing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
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

import com.example.secureonlinesharing.databinding.MediaUploaderBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
                        DocumentViewer.openPdfFromRaw(getActivity(),binding.mediaUploadPreview,inputStream,0);
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

                // getting a new volley request queue for making new requests
               /* RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
                // url of the api through which we get random dog images
                String url = "https://innshomebase.com/securefilesharing/develop/battus/v1/controller/registerLogin/registerLogin.php?userId=1";

                // since the response we get from the api is in JSON, we
                // need to use `JsonObjectRequest` for parsing the
                // request response
              //  try {

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            // we are using GET HTTP request method
                            Request.Method.GET,
                            // url we want to send the HTTP request to
                            url,
                            // this parameter is used to send a JSON object to the
                            // server, since this is not required in our case,
                            // we are keeping it `null`
                          //  new JSONObject("{userId:1}"),
                            null,

                            // lambda function for handling the case
                            // when the HTTP request succeeds
                            (Response.Listener<JSONObject>) response -> {
                                // get the image url from the JSON object
                                String message;
                                try {
                                    message = response.getString("message");
                                    // load the image into the ImageView using Glide.
                                     //binding.textviewFirst.setText(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },

                            // lambda function for handling the case
                            // when the HTTP request fails
                            (Response.ErrorListener) error -> {
                                // make a Toast telling the user
                                // that something went wrong
                                //     Toast.makeText(getActivity(), "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                                // log the error message in the error stream
                                //    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                            }
                    );
                    // add the json request object created above
                    // to the Volley request queue
                    volleyQueue.add(jsonObjectRequest);
                //} // catch(JSONException e){} */

            }

        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}