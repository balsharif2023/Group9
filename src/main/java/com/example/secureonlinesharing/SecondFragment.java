package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
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
                bundle.putString("mediaId","");
                ((MainActivity)getActivity()).navigateFrom(SecondFragment.this,R.id.mediaUploader,bundle);

            }
        });
    }

    public void getMedia()  {
        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt","");

        String userId = data.getString("id","");



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
                        System.out.println(""+json.length()+" media retrieved");
                        System.out.println(mediaList);

                        for (int i =0; i< json.length();i++)
                        {
                            JSONObject entry =json.getJSONObject(i);
                            View view = LayoutInflater.from(getContext()).inflate(R.layout.media_record, null);
                            ((TextView) view.findViewById(R.id.mediaRecordTitle)).setText(entry.getString("media_title"));
                            ((TextView) view.findViewById(R.id.mediaRecordDescription)).setText(entry.getString("media_description"));
                            view.setTag(R.id.media_record_id,entry.getString("media_Id"));

                          view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle bundle = new Bundle();
                                    try {
                                        bundle.putString("media_id",entry.getString("media_Id"));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    ((MainActivity)getActivity()).navigateFrom(SecondFragment.this,R.id.mediaViewer,bundle);

//                                    ((MainActivity)getActivity()).navigateFrom(SecondFragment.this,R.id.faceAuth,bundle);
                                }
                            });


                            boolean isOwner = entry.getBoolean("is_media_owner");

                            if (isOwner)
                            {
                                myPrev = addToList(binding.myMedia, view,myPrev);
                            }
                            else
                                sharedPrev =addToList(binding.sharedMedia,view,sharedPrev);


                        }

                        if(myPrev!= null)
                        {
                            binding.myMediaLabel.setVisibility(View.VISIBLE);

                            binding.myMedia.setVisibility(View.VISIBLE);
                            if(sharedPrev!= null)
                            {
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
                    MainActivity.showVolleyError(SecondFragment.this.getContext(),error);

                }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers =  super.getHeaders();
                HashMap<String, String> headers2 =new HashMap<String,String>();
                for(String s: headers.keySet())
                {
                    headers2.put(s,headers.get(s));
                }
                headers2.put("Authorization","Bearer " + token);
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

}