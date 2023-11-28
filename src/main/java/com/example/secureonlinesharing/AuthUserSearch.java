package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.secureonlinesharing.databinding.AuthUserSearchBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AuthUserSearch extends Fragment {

    private AuthUserSearchBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AuthUserSearchBinding.inflate(inflater, container, false);
        getActivity().setTitle("Olympus");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] cache = {
                MainActivity.MEDIA_VIEWER_CACHE_FILE,
                MainActivity.HEADSHOT_CACHE_FILE,
                MainActivity.MEDIA_UPLOAD_CACHE_FILE
        };

        for(String filename: cache)
        {
            File file = new File(getActivity().getCacheDir(),filename);
            file.delete();
        }
       ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.INVISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });


        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.GONE);


        }


        binding.searchField.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              search();
            }

        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void  search(){


        String searchString = (String) binding.searchField.getQuery();

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/userLogin.php";
        url+= "?ivanSendsBack="+ searchString;







        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.GET,
                // url we want to send the HTTP request to
                url,
                // this parameter is used to send a JSON object to the
                // server, since this is not required in our case,
                // we are keeping it `null`
                //  new JSONObject("{userId:1}"),
                null,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String message;


                    // message = response.getString("message");



                    // SharedPreferences data = ((MainActivity)getActivity()).sharedpreferences;

                    SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = data.edit();

                    // below two lines will put values for
                    // email and password in shared preferences.
                    try {

                        System.out.println(response);

                        editor.putString("id", response.getString("user_id"));

                        editor.putString("userName", response.getString("user_username"));

                        editor.putString("firstName", response.getString("user_first_name"));

                        editor.putString("lastName", response.getString("user_last_name"));



                        String token = response.getString("token");
                        editor.putString("token",token );

                        JSONObject json = null;

                        json = new JSONObject(token);
                        String jwt = json.getString("jwt");
                        editor.putString("jwt",jwt );

                        System.out.println(jwt);

                        new Thread(new RetrieveFileFromUrl(AuthUserSearch.this,
                                response.getString("facial_profile_file"),
                                MainActivity.HEADSHOT_CACHE_FILE)).start();



                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }





                    // to save our data with key and value.
                    editor.apply();
                    NavHostFragment.findNavController(AuthUserSearch.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                    // load the image into the ImageView using Glide.
                    //binding.textviewFirst.setText(message);

                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    //     Toast.makeText(getActivity(), "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                    // log the error message in the error stream
                    //    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                }
        );
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
        //} // catch(JSONException e){} */




    }

}