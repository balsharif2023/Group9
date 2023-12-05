package com.example.secureonlinesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
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

        getMedia();

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

    public void getMedia() {
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
                            View view = LayoutInflater.from(getContext()).inflate(R.layout.media_record, null);
                            ((TextView) view.findViewById(R.id.mediaRecordTitle)).setText(entry.getString("media_title"));
                            ((TextView) view.findViewById(R.id.mediaRecordDescription)).setText(entry.getString("media_description"));
                            view.setTag(R.id.media_record_id, entry.getString("media_Id"));

                            boolean isOwner = entry.getBoolean("is_media_owner");

                            if (!isOwner) sharedCount++;
                            final int currentSharedCount = sharedCount;
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle bundle = new Bundle();

                                    String mediaId= null;
                                    try {
                                        mediaId = entry.getString("media_Id");
                                        bundle.putString("media_id",mediaId );
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (isOwner) {
                                        ((MainActivity) getActivity()).navigate(R.id.mediaViewer, bundle);
                                    } else {
                                        if (currentSharedCount == 1)
                                            ((MainActivity) getActivity()).navigate(R.id.faceAuth, bundle);
                                        else {
                                            double lat = currentSharedCount == 2 ? 26 : 48;
                                            double lon = currentSharedCount == 2 ? -80 : 48;


                                            Location location = ((MainActivity) getActivity()).getCurrentLocation();

                                            double curLat = location.getLatitude();
                                            double curLong = location.getLongitude();

                                            if (Math.hypot(curLat - lat, curLong - lon) < 1) {
//                                                new DialogFragment() {
//                                                    @Override
//                                                    public Dialog onCreateDialog(Bundle savedInstanceState) {
//                                                        // Use the Builder class for convenient dialog construction.
//                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                                        builder.setMessage("Location verified, you may proceed: Lat " +
//                                                                        new DecimalFormat("#.###").format(curLat)
//                                                                        + "\u00B0, Long " +
//                                                                        new DecimalFormat("#.###").format(curLong) + "\u00B0")
//                                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                                    public void onClick(DialogInterface dialog, int id) {
//                                                                        ((MainActivity) getActivity()).navigate(R.id.mediaViewer, bundle);
//                                                                    }
//                                                                });
//
//                                                        // Create the AlertDialog object and return it.
//                                                        return builder.create();
//                                                    }
                                                new PermissionDialogFragment(SecondFragment.this,mediaId,true,curLat,curLong
                                                ,lat,lon).show(getParentFragmentManager(), "GPS Success!");
                                            } else {
//                                                new DialogFragment() {
//                                                    @Override
//                                                    public Dialog onCreateDialog(Bundle savedInstanceState) {
//                                                        // Use the Builder class for convenient dialog construction.
//                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                                        builder.setMessage("Location incorrect: You are at (" +
//                                                                        new DecimalFormat("#.###").format(curLat)
//                                                                        + "\u00B0, " +
//                                                                        new DecimalFormat("#.###").format(curLong) + "\u00B0)"
//                                                                +" But required location is ("+
//                                                                        new DecimalFormat("#.###").format(lat)
//                                                                        + "\u00B0, " +
//                                                                        new DecimalFormat("#.###").format(lon) + "\u00B0)" )
//                                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                                    public void onClick(DialogInterface dialog, int id) {
//                                                                    }
//                                                                });
//
//                                                        // Create the AlertDialog object and return it.
//                                                        return builder.create();
//                                                    }
                                                new PermissionDialogFragment(SecondFragment.this,mediaId,false,curLat,curLong
                                                        ,lat,lon).show(getParentFragmentManager(), "GPS Failure!");
                                            }
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
            builder.setMessage( success?"Location verified":"Location incorrect" +
        ": You are at (" +
                            new DecimalFormat("#.###").format(curLat)
                            + "\u00B0, " +
                            new DecimalFormat("#.###").format(curLong) + "\u00B0)"
                            +", required location is ("+
                            new DecimalFormat("#.###").format(lat)
                            + "\u00B0, " +
                            new DecimalFormat("#.###").format(lon) + "\u00B0)" )
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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