package com.example.secureonlinesharing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.graphics.pdf.PdfRenderer;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
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
import com.example.secureonlinesharing.databinding.MediaViewerBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaViewer extends MediaFragment {

    private MediaViewerBinding binding;


    private int pdfPageNum;

    private PdfRenderer renderer;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = MediaViewerBinding.inflate(inflater, container, false);
        // create a new renderer

        mediaTitle = binding.mediaTitle;

        mediaDescription = binding.mediaDescription;

        authUsers= binding.authUsers;

        mediaOwner = binding.mediaOwner;

        authUserNone =binding.authUserNone;


        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



       getMedia();





        pdfPageNum = 0;

        binding.mediaEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();

                bundle.putString("media_id", mediaId);
//                NavHostFragment.findNavController(MediaViewer.this)
//                        .navigate(R.id.action_mediaViewer_to_mediaUploader, bundle);

                ((MainActivity)getActivity()).navigate(R.id.mediaUploader,bundle);



            }
        });

        binding.mediaDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DeleteMediaDialogFragment(MediaViewer.this)
                        .show(getParentFragmentManager(),"Delete Media");

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



    // create an async task class for loading pdf file from URL.


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void displayMedia(String path){


        new Thread(new RetrieveFileFromUrl(MediaViewer.this, path, MainActivity.MEDIA_VIEWER_CACHE_FILE) {

            @Override
            public void onRetrieve() {
                try {
                    boolean isVideo = contentType.startsWith("video");


                    binding.mediaLoading.setVisibility(View.GONE);


                    if (contentType.equals("application/pdf")) {
                        renderer = RetrieveFileFromUrl.openPdf(getActivity(), binding.mediaView,
                                MainActivity.MEDIA_VIEWER_CACHE_FILE, 0);
                        binding.pdfControls.setVisibility(View.VISIBLE);
                    } else if (contentType.startsWith("image")) {
                        RetrieveFileFromUrl.openImage(getActivity(), binding.mediaView, MainActivity.MEDIA_VIEWER_CACHE_FILE);
                        binding.pdfControls.setVisibility(View.GONE);


                    }
                    else if (isVideo) {
                        RetrieveFileFromUrl.openVideo(getActivity(), binding.videoView, MainActivity.MEDIA_VIEWER_CACHE_FILE);
                        binding.pdfControls.setVisibility(View.GONE);






                    }

                    binding.pdfWrapper.setVisibility(isVideo?View.GONE: View.VISIBLE);
                    binding.videoWrapper.setVisibility(isVideo?View.VISIBLE: View.GONE);



                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }).start();





    }



    public void deleteMediaInfo()
    {
        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        userId = data.getString("id","");

        String token = data.getString("jwt","");

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/deleteMedia.php";


        url+= "?mediaId="+mediaId;




        System.out.println(url);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.GET,
                // url we want to send the HTTP request to
                url,


                null,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object




                    try {

                        System.out.println(response);

                        MainActivity.showToast(MediaViewer.this,response.getString("message"));



                        ((MainActivity)getActivity()).navigate(R.id.SecondFragment,
                                null);



                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }




                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(getContext(),error);

                }
        ){
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

    }





    public static class DeleteMediaDialogFragment extends DialogFragment {

        MediaViewer viewer;

        public DeleteMediaDialogFragment(MediaViewer viewer) {
            this.viewer = viewer;


        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to delete this media?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            viewer.deleteMediaInfo();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {



                        }
                    });
            // Create the AlertDialog object and return it.
            return builder.create();
        }
    }


    @Override
    public void onGetMedia(JSONObject response) {
        try {
            String ownerId = response.getString("mediaOwnerId");
            if(!ownerId.equals(userId))
            {
                binding.authUserDivider.setVisibility(View.GONE);
                binding.authUserWrapper.setVisibility(View.GONE);
                binding.mediaActions.setVisibility(View.GONE);


            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}


