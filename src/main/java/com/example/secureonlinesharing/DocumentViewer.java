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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.graphics.pdf.PdfRenderer;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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

import javax.net.ssl.HttpsURLConnection;

public class DocumentViewer extends Fragment {

    private DocumentViewerBinding binding;

    private  String mediaId = "";
    private int pdfPageNum;

    private PdfRenderer renderer;

    // creating a variable
    // for PDF view.
   // PDFView pdfView;

    // url of our PDF file.
    String pdfurl = "https://innshomebase.com/securefilesharing/develop/public/loremipsum.pdf";


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    )

    {

        binding = DocumentViewerBinding.inflate(inflater, container, false);
        // create a new renderer
     

        return binding.getRoot();

    }

    public static void openImage(Activity activity,ImageView img)
    {
        File imgFile = new File(activity.getCacheDir(), "temp");



        // on below line we are checking if the image file exist or not.
        if (imgFile.exists()) {
            // on below line we are creating an image bitmap variable
            // and adding a bitmap to it from image file.
            Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            // on below line we are setting bitmap to our image view.
            img.setImageBitmap(imgBitmap);
        }
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
        int pageHeight= mPdfPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, pageHeight*width/pageWidth,
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

        mediaId = getArguments().getString("mediaId");

        getMedia();


       // new Thread(new RetrievePDFfromUrl(pdfurl)).start();
        //new RetrievePDFfromUrl(pdfurl).run();
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    NavHostFragment.findNavController(DocumentViewer.this)
                            .navigate(R.id.action_documentViewer_to_SecondFragment);
                }
            });


        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }
        pdfPageNum =0;

        binding.mediaEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();

                bundle.putString("mediaId","16");
                NavHostFragment.findNavController(DocumentViewer.this)
                        .navigate(R.id.action_documentViewer_to_mediaUploader,bundle);


            }
        });







        binding.pdfForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    if (renderer.getPageCount() - 1 > pdfPageNum) {
                        pdfPageNum++;
                        renderer = openPdf(getActivity(),binding.pdfView, pdfPageNum);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        binding.pdfBackButton.setOnClickListener(view1 -> {


            try {
                if(pdfPageNum >0)
                {
                pdfPageNum--;
                    renderer = openPdf(getActivity(),binding.pdfView, pdfPageNum);
                }
            }
                catch (IOException e) {
                throw new RuntimeException(e);
            }


        });


        final ZoomLinearLayout zoomLinearLayout = (ZoomLinearLayout) getActivity().findViewById(R.id.zoom_linear_layout);
        zoomLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomLinearLayout.init(getContext());
                return false;
            }
        });


    }

    public void getMedia()  {
        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        JSONObject json ;
        try {


            json= new JSONObject();
            json.put("media_Id", mediaId);
            json.put("user_id", data.getString("id",""));
            json.put("user_first_name", data.getString("firstName",""));
            json.put("user_last_name", data.getString("lastName",""));
            json.put("user_username", data.getString("userName",""));
            json.put("token", data.getString("jwt",""));

        } catch (JSONException e) {

            e.printStackTrace();
            return;
        }

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/accessMedia.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.POST,

                url,

                json,


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
                        Toast toast = Toast.makeText(getContext(),errorMessage,Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        String result = new String(networkResponse.data);
                        try {
                            JSONObject response = new JSONObject(result);

                            String message = response.getString("message");


                            Log.e("Error Message", message);
                            Toast toast = Toast.makeText(getContext(),message,Toast.LENGTH_SHORT);
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
        );
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
        //} // catch(JSONException e){} */


    }

    // create an async task class for loading pdf file from URL.
    class RetrievePDFfromUrl implements Runnable{
        private String urlStr;
        public RetrievePDFfromUrl(String url)
        {
            this.urlStr = url;
        }
        @Override
        public void run (){
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
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    copyToCache(getActivity(),inputStream);
                }
                urlConnection.disconnect();

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();

            }


            getActivity().runOnUiThread(new Runnable(){
                public void run(){
                    try {
                       renderer= openPdf(getActivity(),binding.pdfView,0);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }



    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}