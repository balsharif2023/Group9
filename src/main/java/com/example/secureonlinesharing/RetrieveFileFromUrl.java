package com.example.secureonlinesharing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

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
    private String urlStr, fileName;

    private Fragment fragment;

    public String contentType;
    public RetrieveFileFromUrl (Fragment fragment, String url, String fileName)
    {
        this.fragment= fragment;

        this.urlStr = url;

        this.fileName = fileName;
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
                copyToCache(fragment.getActivity(),inputStream,fileName);
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

    public static Bitmap openImage(Activity activity, ImageView img, String fileName)
    {
        File imgFile = new File(activity.getCacheDir(), fileName);

        System.out.println(imgFile);

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









 public  void onRetrieve(){}

}