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
    public static PdfRenderer openPdfFromRaw(Activity activity, ImageView img, InputStream input, int pdfPageNum) throws IOException {

        // Copy sample.pdf from 'res/raw' folder into cache so PdfRenderer can handle it
        File fileCopy = new File(activity.getCacheDir(), "temp.pdf");
        copyToCache(fileCopy, input);
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
    public static void copyToCache(File file, InputStream input) throws IOException {


//Get input stream object to read the pdf

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

    }






    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);


        new RetrievePDFfromUrl().execute(pdfurl);
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
        int pdfResource = R.raw.sample;


//        try {
////          //  renderer = openPdfFromRaw(getActivity(),binding.pdfView,
////                    getResources().openRawResource(pdfResource),
////                    pdfPageNum);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        binding.pdfForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    if (renderer.getPageCount() - 1 > pdfPageNum) {
                        pdfPageNum++;
                        renderer = openPdfFromRaw(getActivity(),binding.pdfView,
                                getResources().openRawResource(pdfResource),
                                pdfPageNum);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        binding.pdfBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    if(pdfPageNum >0)
                    {
                    pdfPageNum--;
                        renderer = openPdfFromRaw(getActivity(),binding.pdfView,
                                getResources().openRawResource(pdfResource),
                                pdfPageNum);
                    }
                }
                    catch (IOException e) {
                    throw new RuntimeException(e);
                }


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
    class RetrievePDFfromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // below is the step where we are
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // after the execution of our async
            // task we are loading our pdf in our pdf view.
            try {
                openPdfFromRaw(getActivity(),binding.pdfView,inputStream,0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}