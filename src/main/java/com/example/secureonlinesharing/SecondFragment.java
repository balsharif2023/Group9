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

       // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_FirstFragment);
                }
            });
        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }





        binding.addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("mediaId","");
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_mediaUploader,bundle);

            }
        });
    }

    public void getMedia()  {
        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt","");



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
                        System.out.println(mediaList);
                        JSONArray json = new JSONArray(mediaList);
                        View prev= null;
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
                                  NavHostFragment.findNavController(SecondFragment.this)
                                          .navigate(R.id.action_SecondFragment_to_mediaViewer,bundle);

//                                  NavHostFragment.findNavController(SecondFragment.this)
//                                   .navigate(R.id.action_SecondFragment_to_faceAuth,bundle);
                                }
                            });

                            binding.mediaRecordWrapper.addView(view);
                            if (prev!= null)
                            {
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, 40);
                                prev.setLayoutParams(params);
                            }

                            prev =view;
                        }
//                        System.out.println(response.getString("mediaAccessPath"));
//                        binding.mediaTitleInput.setText(response.getString("mediaTitle"));
//                        binding.mediaDescriptionInput.setText(response.getString("mediaDescription"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    //  Toast.makeText(getActivity(), "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                    // log the error message in the error stream
                    //    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                    NetworkResponse networkResponse = error.networkResponse;
                    //System.out.println("status code: "+ networkResponse.statusCode );
                    String errorMessage = "Unknown error";
                    if (networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            errorMessage = "Request timeout";
                        } else if (error.getClass().equals(NoConnectionError.class)) {
                            errorMessage = "Failed to connect server";
                        }
                        Toast toast = Toast.makeText(getContext(),errorMessage,Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        String result = new String(networkResponse.data);
                        try {
                            JSONObject response = new JSONObject(result);

                            String message = response.getString("message");


                            Log.e("Error Message", message);
                            Toast toast = Toast.makeText(getContext(),message,Toast.LENGTH_SHORT);
                            toast.show();

                            if (networkResponse.statusCode == 404) {
                                errorMessage = "Resource not found";
                            } else if (networkResponse.statusCode == 401) {
                                errorMessage = message + " Please login again";
                            } else if (networkResponse.statusCode == 400) {
                                errorMessage = message + " Check your inputs";
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = message + " Something is getting wrong";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("Error", errorMessage);
                    error.printStackTrace();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}