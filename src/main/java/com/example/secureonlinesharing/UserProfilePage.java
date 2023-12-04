package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
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
import com.example.secureonlinesharing.databinding.RegistrationPageBinding;
import com.example.secureonlinesharing.databinding.UserProfilePageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfilePage extends Fragment {

    private UserProfilePageBinding binding;
    String userName,email,firstName,lastName,phone,userId;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = UserProfilePageBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        binding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Bundle bundle = new Bundle();
                SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
                String id = data.getString("id","");
                    bundle.putString("user_id",id);
                    bundle.putString("user_name", userName);
                 bundle.putString("user_first",firstName);
                bundle.putString("user_last", lastName);
                bundle.putString("user_phone", phone);
                bundle.putString("user_email", email);

//                NavHostFragment.findNavController(UserProfilePage.this)
//                        .navigate(R.id.action_userProfilePage_to_registrationPage,bundle);

                ((MainActivity)getActivity()).navigate(R.id.registrationPage,bundle);


            }

        });
        getUserInfo();
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getUserInfo()
    {
        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
       userId = data.getString("id","");

        String token = data.getString("jwt","");

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/getData.php";
        url+= "?userid="+ userId;




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            url+= "&token="+ URLEncoder.encode(token, Charset.defaultCharset());
        }

        System.out.println(url);


              JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.GET,
                // url we want to send the HTTP request to
                url,


                null,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object




                    try {

                        System.out.println(response);
                          firstName = response.getString("user_first_name");
                          lastName = response.getString("user_last_name");

                          userName = response.getString("user_username");
                          email = response.getString("user_email");

                          phone = response.getString("user_phone");

                        binding.firstNameDisplay.setText(firstName);

                        binding.lastNameDisplay.setText(lastName);

                        binding.userNameDisplay.setText(userName);

                        binding.emailDisplay.setText(email);

                        binding.phoneNumberDisplay.setText(phone);




                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }


                  RetrieveFileFromUrl.openImage(getActivity(),binding.profilePic,MainActivity.HEADSHOT_CACHE_FILE);


                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(UserProfilePage.this.getContext(),error);

                }
        );
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);

    }

}