package com.example.secureonlinesharing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class RetrieveFileFromUrl implements Runnable{
    private String urlStr, file_name;

    private Fragment fragment;

    public String contentType;
    public RetrieveFileFromUrl (Fragment fragment, String url,String file_name)
    {
        this.fragment= fragment;

        this.urlStr = url;
        this.file_name = file_name;
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
                contentType = urlConnection.getContentType();
                System.out.println(contentType);
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                copyToCache(fragment.getActivity(),inputStream,file_name);
            }
            urlConnection.disconnect();

        } catch (IOException e) {
            // this is the method
            // to handle errors.
            e.printStackTrace();

        }


        fragment.getActivity().runOnUiThread(new Runnable(){
            public void run(){
                onRetrieve();

            }
        });
    }


    public static File copyToCache(Activity activity, InputStream input, String fileName) throws IOException {


//Get input stream object to read the pdf
        File file = new File(activity.getCacheDir(), fileName);
        FileOutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int size, bytes =0;
        // Copy the entire contents of the file
        while ((size = input.read(buffer)) != -1) {
            output.write(buffer, 0, size);

            bytes+= size;
        }
//Close the buffer
        input.close();
        output.close();
        System.out.println("Wrote " + bytes + " bytes to "+ file.getAbsolutePath());
        System.out.println("Written file size:  "+ file.length());
        File file2 = new File(activity.getCacheDir(), fileName);

        System.out.println("N  file size:  "+ file.length());




        return file;

    }

    public static Bitmap openImage(Activity activity, ImageView img, String fileName)
    {
        File imgFile = new File(activity.getCacheDir(), fileName);

        System.out.println("loading from cache "+ imgFile.getAbsolutePath());
        System.out.println("file size:  "+ imgFile.length());

        // on below line we are checking if the image file exist or not.
        if (imgFile.exists()) {
            System.out.println("exists!");
            // on below line we are creating an image bitmap variable
            // and adding a bitmap to it from image file.
            Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            System.out.println(imgBitmap);

            // on below line we are setting bitmap to our image view.
            img.setImageBitmap(imgBitmap);
            return imgBitmap;
        }
        return null;
    }


    public static void openVideo(Activity activity, VideoView videoView,String fileName) {

        File videoFile = new File(activity.getCacheDir(), fileName);
        videoView.setZOrderOnTop(true);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoFile.getAbsolutePath());

        MediaController mediaController = new MediaController(activity);
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        //videoView.setVideoPath("android.resource://" + getActivity().getPackageName() + "/" + R.raw.video_clip); //set the path of the video that we need to use in our VideoView
        videoView.start();  //start() method of the VideoView class will start the video to play
    }
    public static PdfRenderer openPdf(Activity activity, ImageView img,String file_name,
                                      int pdfPageNum) throws IOException {

        // Copy sample.pdf from 'res/raw' folder into cache so PdfRenderer can handle it

        //   File fileCopy = copyToCache(activity,input);
        File fileCopy = new File(activity.getCacheDir(), file_name);
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
        int pageHeight = mPdfPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, pageHeight * width / pageWidth,
                Bitmap.Config.ARGB_8888);
        mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // Set the bitmap in the ImageView
        img.setImageBitmap(bitmap);

        return renderer;
    }






 public  void onRetrieve(){}

}