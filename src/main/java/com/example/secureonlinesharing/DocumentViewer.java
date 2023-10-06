package com.example.secureonlinesharing;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.graphics.pdf.PdfRenderer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secureonlinesharing.databinding.DocumentViewerBinding;
import com.example.secureonlinesharing.databinding.FragmentThirdBinding;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class DocumentViewer extends Fragment {

    private DocumentViewerBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    )

    {

        binding = DocumentViewerBinding.inflate(inflater, container, false);
        // create a new renderer
        Resources resources= getResources();
        AssetFileDescriptor fd = resources.openRawResourceFd(R.raw.loremipsum);
//        URI pdfUri = URI.create("android.resource://" + getActivity().getPackageName() + "/" + R.raw.loremipsum);
//       File pdfFile= new File(pdfUri);
        try {
            openPDF(fd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return binding.getRoot();

    }
    public void openPDF(AssetFileDescriptor fd) throws IOException {
        PdfRenderer renderer = null;

            ParcelFileDescriptor fileDescriptor = fd.getParcelFileDescriptor();
//                    ParcelFileDescriptor.open (file,
//                    ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(fileDescriptor);

        // let us just render all pages
        final int pageCount = renderer.getPageCount();
//        for (int i = 0; i < pageCount; i++) {
//            PdfRenderer.Page page = renderer.openPage(i);
//
//            // say we render for showing on the screen
//            page.render(mBitmap, null, null, Page.RENDER_MODE_FOR_DISPLAY);
//
//            // do stuff with the bitmap
//
//            // close the page
//            page.close();
//        }


        //Display page 0
        PdfRenderer.Page rendererPage = renderer.openPage(0);
        int rendererPageWidth = rendererPage.getWidth();
        int rendererPageHeight = rendererPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(
                rendererPageWidth,
                rendererPageHeight,
                Bitmap.Config.ARGB_8888);
        rendererPage.render(bitmap, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        binding.pdfView.setImageBitmap(bitmap);
        rendererPage.close();


        fileDescriptor.close();
        // close the renderer
        renderer.close();
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

      /*  binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}