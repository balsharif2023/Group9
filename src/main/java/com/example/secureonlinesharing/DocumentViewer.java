package com.example.secureonlinesharing;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DocumentViewer extends Fragment {

    private DocumentViewerBinding binding;
    private int pdfPageNum;

    private PdfRenderer renderer;

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
    void openPdfFromRaw() throws IOException {
        if(renderer!=null && pdfPageNum== renderer.getPageCount())
        {
            pdfPageNum--;
            return;
        }

        if(renderer!=null && pdfPageNum== -1)
        {
            pdfPageNum++;
            return;
        }
        // Copy sample.pdf from 'res/raw' folder into cache so PdfRenderer can handle it
        File fileCopy = new File(getActivity().getCacheDir(), "temp.pdf");
        copyToCache(fileCopy, R.raw.loremipsum);
        // We get a page from the PDF doc by calling 'open'
        ParcelFileDescriptor fileDescriptor =
                ParcelFileDescriptor.open(fileCopy,
                        ParcelFileDescriptor.MODE_READ_ONLY);
         if (renderer!=null)
         {

         }
        renderer = new PdfRenderer(fileDescriptor);


        PdfRenderer.Page mPdfPage = renderer.openPage(pdfPageNum);
        // Create a new bitmap and render the page contents into it
        DisplayMetrics displayMetrics = new DisplayMetrics();
       getActivity().getWindowManager()
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
        binding.pdfView.setImageBitmap(bitmap);
    }
    void copyToCache(File file, @RawRes int pdfResource) throws IOException {


//Get input stream object to read the pdf
            InputStream input = getResources().openRawResource(pdfResource);
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

        try {
            openPdfFromRaw();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        binding.pdfForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    pdfPageNum++;
                    openPdfFromRaw();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        binding.pdfBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    pdfPageNum--;
                    openPdfFromRaw();
                } catch (IOException e) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}