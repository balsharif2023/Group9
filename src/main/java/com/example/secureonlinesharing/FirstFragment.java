package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.navigation.fragment.NavHostFragment;

import com.example.secureonlinesharing.databinding.FragmentFirstBinding;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
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

        binding.createAccountLink.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 NavHostFragment.findNavController(FirstFragment.this)
                         .navigate(R.id.action_FirstFragment_to_registrationPage);

             }

         });
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                System.out.println("Login button clicked!");
                binding.loginErrorMessage.setVisibility(View.GONE);
                //TODO remember to uncomment the code below
//                if(!validateForm())
//                    return;

                    login();





                //} // catch(JSONException e){} */

            }

        });
    }



    public void login(){



        // getting a new volley request queue for making new requests
        String userNameToText = binding.userNameInput.getText().toString();
        String passwordToText = binding.passwordInput.getText().toString();
        if(userNameToText.equals(""))userNameToText="batman123";

        if(passwordToText.equals(""))passwordToText="batman123";



        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/userLogin.php";
        url+= "?username="+ userNameToText;

        url+= "&password="+ passwordToText;



        // since the response we get from the api is in JSON, we
        // need to use `JsonObjectRequest` for parsing the
        // request response
        //  try {

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

                    System.out.println(response);


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

                        new Thread(new RetrieveFileFromUrl(FirstFragment.this,
                                response.getString("facial_profile_file"),
                                MainActivity.HEADSHOT_CACHE_FILE)).start();



                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }





                    // to save our data with key and value.
                    editor.apply();
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                    // load the image into the ImageView using Glide.
                    //binding.textviewFirst.setText(message);

                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(FirstFragment.this.getContext(),error);

                }
        );
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);



    }

    public boolean validateForm() {
        // extract the entered data from the EditText

        String passwordToText = binding.passwordInput.getText().toString();
        String userNameToText = binding.userNameInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean passwordValid = !passwordToText.isEmpty();
        binding.passwordEmptyMessage.setVisibility(passwordValid? View.GONE : View.VISIBLE);

        boolean userNameValid = !userNameToText.isEmpty();
        binding.userNameEmptyMessage.setVisibility(userNameValid? View.GONE : View.VISIBLE);

        return userNameValid && passwordValid;

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}