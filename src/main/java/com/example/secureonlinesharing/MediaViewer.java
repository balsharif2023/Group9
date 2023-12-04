package com.example.secureonlinesharing;

import android.app.Activity;
import android.content.Context;
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



}


