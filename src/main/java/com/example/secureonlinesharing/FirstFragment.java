package com.example.secureonlinesharing;

import android.graphics.ImageDecoder;
import android.os.Bundle;
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
        //((MainActivity) getActivity()).backButton.setVisibility(View.INVISIBLE);
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
                // getting a new volley request queue for making new requests
                String userNameToText = binding.userNameInput.getText().toString();
                String passwordToText = binding.passwordInput.getText().toString();
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
                                try {
                                    message = response.getString("message");

                                    NavHostFragment.findNavController(FirstFragment.this)
                                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                                    // load the image into the ImageView using Glide.
                                     //binding.textviewFirst.setText(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}