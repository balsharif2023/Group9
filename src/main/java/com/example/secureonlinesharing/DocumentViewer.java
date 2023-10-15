package com.example.secureonlinesharing;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.graphics.pdf.PdfRenderer;




import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secureonlinesharing.databinding.DocumentViewerBinding;
import com.example.secureonlinesharing.databinding.FragmentThirdBinding;

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
    public static PdfRenderer openPdf(Activity activity, ImageView img, int pdfPageNum) throws IOException {

        // Copy sample.pdf from 'res/raw' folder into cache so PdfRenderer can handle it

     //   File fileCopy = copyToCache(activity,input);
        File fileCopy = new File(activity.getCacheDir(), "temp.pdf");
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
            File file = new File(activity.getCacheDir(), "temp.pdf");
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

       // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);


        new Thread(new RetrievePDFfromUrl(pdfurl)).start();
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

        pdfPageNum =0;





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