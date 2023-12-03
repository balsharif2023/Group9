package com.example.secureonlinesharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MediaFragment extends Fragment {



    protected String mediaId = "";
    private int pdfPageNum;


    protected boolean isViewer = false;
    private PdfRenderer renderer;

    protected TextView mediaTitle, mediaDescription, mediaOwner;

    protected LinearLayout authUsers;




    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args= getArguments();

        mediaId = args == null?"": args.getString("media_id");






        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton != null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }
        pdfPageNum = 0;








//        final ZoomLinearLayout zoomLinearLayout = (ZoomLinearLayout) getActivity().findViewById(R.id.zoom_linear_layout);
//        zoomLinearLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                zoomLinearLayout.init(getContext());
//                return false;
//            }
//        });


    }

    public void getMedia() {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");

        JSONObject json;


        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/accessMedia.php";
        url += "?mediaId=" + mediaId;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.GET,

                url,

                null,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String message;
                    try {
                        // message = response.getString("token");
                        //System.out.println(message);

                        System.out.println(response);

                        String owner = response.getString("mediaOwnerId");

                        System.out.println("owner: " + owner + " user: " + userId);
                        String accessPath = response.getString("mediaAccessPath");

                        System.out.println(accessPath);

                        mediaTitle.setText(response.getString("mediaTitle"));
                        mediaDescription.setText(response.getString("mediaDescription"));







                            if(mediaOwner!=null) {
                                if (owner.equals(userId)) {
                                    String firstName = data.getString("firstName", "");
                                    String lastName = data.getString("lastName", "");

                                    mediaOwner.setText(firstName + " " + lastName);

                                } else
                                    mediaOwner.setText("");
                            }
                        String json1 = "{\"ivanSendsBack\":[{\"user_name\": \"johndoe\", \"user_id\":\"28\",\"start_date\": \"2023-12-2\",\"end_date\":\"2023-12-09\",\"lat\":\"5.2614\",\"long\":\"3.2164\"}," +
                                "{\"user_name\": \"joeblow\", \"user_id\":\"2\",\"start_date\": \"2023-12-2\",\"end_date\":\"2023-12-09\",\"lat\":\"5.2614\",\"long\":\"3.2164\"}" +
                                ",{\"user_name\": \"joeblow\", \"user_id\":\"3\",\"start_date\": \"2023-12-2\",\"end_date\":\"2023-12-09\",\"lat\":\"5.2614\",\"long\":\"3.2164\"}]}";
                        //String json1 = response.getString("with be the one that has array after the : probably called authorizedUsers has to match the key verbatim");

                        JSONObject authUser = new JSONObject(json1);
                        showUserList(authUser,authUsers);

                        displayMedia(accessPath);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(MediaFragment.this.getContext(),error);


                }
        ) {
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
        //} // catch(JSONException e){} */


    }

    // create an async task class for loading pdf file from URL.


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    public void displayMedia(String path){}

    public void showUserList(JSONObject response,LinearLayout wrapper) {

        String userList;
        try {
            userList = response.getString("ivanSendsBack");
            System.out.println(userList);
            JSONArray json = new JSONArray(userList);
            View prev = null;

           // binding.authUsers.removeAllViews();
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_record, null);
                ((TextView) view.findViewById(R.id.userName)).setText(entry.getString("user_name"));

                Bundle info = new Bundle();

                info.putString("user_id", entry.getString("user_id"));

                info.putString("start_date", entry.getString("start_date"));

                info.putString("end_date", entry.getString("end_date"));

                info.putString("lat", entry.getString("lat"));

                info.putString("long", entry.getString("long"));





                onDisplayUser(view, info);

                wrapper.addView(view);
                if (prev != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 40);
                    prev.setLayoutParams(params);
                }

                prev = view;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void  onDisplayUser(View view,Bundle info){}


}


