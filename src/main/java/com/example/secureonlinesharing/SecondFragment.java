package com.example.secureonlinesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.secureonlinesharing.databinding.FragmentSecondBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMediaList();

        //  ((ImageButton)getActivity().findViewById(R.id.backButton)).setVisibility(View.GONE);


        binding.addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("mediaId", "");
                ((MainActivity) getActivity()).navigateFrom(SecondFragment.this, R.id.mediaUploader, bundle);

            }
        });
    }

    public void getMediaList() {
        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");


        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/mediaList.php";



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.GET,

                url,

                null,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String mediaList;
                    try {
                        mediaList = response.getString("mediaList");

                        JSONArray json = new JSONArray(mediaList);
                        View myPrev = null, sharedPrev = null;

                        int sharedCount = 0;
                        System.out.println("" + json.length() + " media retrieved");
                        System.out.println(mediaList);

                        for (int i = 0; i < json.length(); i++) {
                            JSONObject entry = json.getJSONObject(i);
                             String mediaId= entry.getString("media_Id");

                            View view = LayoutInflater.from(getContext()).inflate(R.layout.media_record, null);
                            ((TextView) view.findViewById(R.id.mediaRecordTitle)).setText(entry.getString("media_title"));
                            ((TextView) view.findViewById(R.id.mediaRecordDescription)).setText(entry.getString("media_description"));
                            view.setTag(R.id.media_record_id,mediaId );

                            boolean isOwner = entry.getBoolean("is_media_owner");

                            if (!isOwner) sharedCount++;
                            final int currentSharedCount = sharedCount;
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {



                                    Bundle bundle = new Bundle();

                                    String mediaId= null, accessRule= null;
                                    try {
                                        mediaId = entry.getString("media_Id");
                                        bundle.putString("media_id",mediaId );
                                        accessRule= entry.getString("access_rules");
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (isOwner) {
                                        ((MainActivity) getActivity()).navigate(R.id.mediaViewer, bundle);
                                    } else {
                                        if (accessRule.equals("face_authentication"))
                                            ((MainActivity) getActivity()).navigate(R.id.faceAuth, bundle);
                                        else {

                                            getMedia(mediaId);
//                                            double lat = currentSharedCount == 2 ? 26 : 48;
//                                            double lon = currentSharedCount == 2 ? -80 : 48;
//
//
//                                            Location location = ((MainActivity) getActivity()).getCurrentLocation();
//
//                                            double curLat = location.getLatitude();
//                                            double curLong = location.getLongitude();
//
//                                            if (Math.hypot(curLat - lat, curLong - lon) < 1) {
////
//                                                new PermissionDialogFragment(SecondFragment.this,mediaId,true,curLat,curLong
//                                                ,lat,lon).show(getParentFragmentManager(), "GPS Success!");
//                                            } else {
////
//                                                new PermissionDialogFragment(SecondFragment.this,mediaId,false,curLat,curLong
//                                                        ,lat,lon).show(getParentFragmentManager(), "GPS Failure!");
//                                            }
                                        }
                                    }


                                }
                            });


                            if (isOwner) {
                                myPrev = addToList(binding.myMedia, view, myPrev);
                            } else
                                sharedPrev = addToList(binding.sharedMedia, view, sharedPrev);


                        }

                        if (myPrev != null) {
                            binding.myMediaLabel.setVisibility(View.VISIBLE);

                            binding.myMedia.setVisibility(View.VISIBLE);
                            if (sharedPrev != null) {
                                binding.mediaDivider.setVisibility(View.VISIBLE);
                            }


                        }
                        if (sharedPrev != null
                        ) {
                            binding.sharedMediaLabel.setVisibility(View.VISIBLE);

                            binding.sharedMedia.setVisibility(View.VISIBLE);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(SecondFragment.this.getContext(), error);

                    binding.myMediaLabel.setVisibility(View.VISIBLE);
                    binding.mediaListNone.setVisibility(View.VISIBLE);


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


    public View addToList(ViewGroup list, View item, View prev) {

        list.addView(item);
        if (prev != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 40);
            prev.setLayoutParams(params);
        }

        return item;


    }




    public void getMedia(String mediaId) {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");

      Location location = ((MainActivity)getActivity()).getCurrentLocation();

      double lat = location.getLatitude(), lon= location.getLongitude();




        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/accessMedia.php";
        url += "?mediaId=" + mediaId;

        url += "&latitude=" + lat;

        url += "&longitude=" + lon;

        System.out.println(url);





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

                        System.out.println(response.getString("message"));

                        boolean success = response.getBoolean("isSuccessful");

                        new PermissionDialogFragment(SecondFragment.this,mediaId,success,
                                lat,lon,
                                Double.parseDouble(response.getString("latitude")),
                                Double.parseDouble(response.getString("longitude")))
                                .show(getParentFragmentManager(),"GPS response");



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(SecondFragment.this.getContext(),error);

                    System.out.println(error);
                    System.out.println(new String(error.networkResponse.data));






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





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static class PermissionDialogFragment extends DialogFragment {

        SecondFragment fragment;
        String mediaId;
        boolean success;

        double curLat, curLong, lat,lon;


        public PermissionDialogFragment(SecondFragment fragment, String mediaId,
                                        boolean success, double curLat,double curLong,
                                        double lat, double lon) {
            this.mediaId= mediaId;
            this.fragment = fragment;
            this.success = success;

            this.curLat= curLat;
            this.curLong= curLong;

            this.lat= lat;

            this.lon=lon;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String s1 = "<b>" +(success?"Location verified":"Location incorrect")+ "</b>";

            String s2 =  "You are at (" +
                    new DecimalFormat("#.###").format(curLat)
                    + "\u00B0, " +
                    new DecimalFormat("#.###").format(curLong) + "\u00B0)";

            String s3 =  "Required Location is  (" +
                    new DecimalFormat("#.###").format(lat)
                    + "\u00B0, " +
                    new DecimalFormat("#.###").format(lon) + "\u00B0)";


            Spanned strMessage = Html.fromHtml(s1+  "<br>" + s2 + "<br>"+s3 ,Html.FROM_HTML_MODE_COMPACT);
            builder.setMessage(strMessage)

                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if(success)
                            {
                                Bundle bundle = new Bundle();
                                bundle.putString("media_id", mediaId);

                                ((MainActivity)getActivity()).navigate(R.id.mediaViewer,bundle);
                            }
                        }
                    });

            // Create the AlertDialog object and return it.
            return builder.create();
        }
    }

}