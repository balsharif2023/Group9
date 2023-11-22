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


        // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(UserProfilePage.this)
                            .navigate(R.id.action_userProfilePage_to_SecondFragment);
                }
            });
        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.GONE);


        }

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

                NavHostFragment.findNavController(UserProfilePage.this)
                        .navigate(R.id.action_userProfilePage_to_registrationPage,bundle);

            }

        });
        getUserInfo();
    }

    public boolean validateForm() {
        // extract the entered data from the EditText
        String emailToText = binding.emailInput.getText().toString();
        String phoneToText = binding.phoneNumberInput.getText().toString();

        String firstToText = binding.firstNameInput.getText().toString();
        String lastToText = binding.lastNameInput.getText().toString();

        String passwordToText = binding.passwordInput.getText().toString();
        String passwordConfirmToText = binding.passwordConfirmInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean emailValid = (!emailToText.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailToText).matches());
        binding.emailInvalidMessage.setVisibility(emailValid ? View.GONE : View.VISIBLE);


       boolean phoneValid = !phoneToText.isEmpty() && Patterns.PHONE.matcher(phoneToText).matches();
        binding.phoneNumInvalidMessage.setVisibility(phoneValid ? View.GONE : View.VISIBLE);


        Pattern pattern = Pattern.compile("^[A-Za-z-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(firstToText);
        boolean firstNameValid = matcher.find();

        binding.firstNameInvalidMessage.setVisibility(firstNameValid? View.GONE : View.VISIBLE);


        matcher = pattern.matcher(lastToText);

        boolean lastNameValid = matcher.find();


        binding.lastNameInvalidMessage.setVisibility(lastNameValid? View.GONE : View.VISIBLE);
        boolean passwordMatch= passwordToText.equals(passwordConfirmToText);
        binding.passwordConfirmInvalidMessage.setVisibility(passwordMatch ? View.GONE : View.VISIBLE);
        return emailValid && phoneValid && lastNameValid && firstNameValid && passwordMatch;
    }

    public void submitForm()  {

        JSONObject data ;
        try {


            data= new JSONObject();
            data.put("user_email", binding.emailInput.getText().toString());
            data.put("user_phone", binding.phoneNumberInput.getText().toString());
            data.put("user_first_name", binding.firstNameInput.getText().toString());
            data.put("user_last_name", binding.lastNameInput.getText().toString());
            data.put("user_username", binding.userNameInput.getText().toString());
            data.put("user_password", binding.passwordInput.getText().toString());

        } catch (JSONException e) {

            e.printStackTrace();
            return;
        }

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/addUser.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.POST,

                url,

                data,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String message;
                    try {
                        message = response.getString("token");
                        System.out.println(message);
                        NavHostFragment.findNavController(UserProfilePage.this)
                                .navigate(R.id.action_registrationPage_to_FirstFragment);
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


                  RetrieveFileFromUrl.openImage(getActivity(),binding.profilePic,"profile_pic");


                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    System.out.println(error.getMessage());
                    error.printStackTrace();
                    //binding.loginErrorMessage.setVisibility(View.VISIBLE);
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

    }

}