package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.secureonlinesharing.databinding.RegistrationPageBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationPage extends Fragment {

    private RegistrationPageBinding binding;
    private boolean editing= false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        binding = RegistrationPageBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args= getArguments();
        editing =  args!= null && args.containsKey("user_id");

        if(editing)
        {
            binding.saveButton.setVisibility(View.VISIBLE);

            binding.passwordExpand.setVisibility(View.VISIBLE);
            binding.userNameInput.setText(args.getString("user_name"));
            binding.firstNameInput.setText(args.getString("user_first"));
            binding.lastNameInput.setText(args.getString("user_last"));
            binding.phoneNumberInput.setText(args.getString("user_phone"));
            binding.emailInput.setText(args.getString("user_email"));
        }
        else
        {
            binding.nextButton.setVisibility(View.VISIBLE);
            binding.passwordWrapper.setVisibility(View.VISIBLE);

            binding.confirmPasswordWrapper.setVisibility(View.VISIBLE);
        }

        binding.passwordExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean hidden= binding.passwordWrapper.getVisibility()== View.GONE;




                binding.passwordWrapper.setVisibility(hidden?View.VISIBLE:View.GONE);

                binding.confirmPasswordWrapper.setVisibility(hidden?View.VISIBLE:View.GONE);

            }
        });



        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateForm()) {
                    goToHeadShot();
                }

            }

        });


        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateForm()) {
                    submitForm();
                }

            }

        });


    }

    public boolean validateForm() {
        // extract the entered data from the EditText
        String emailToText = binding.emailInput.getText().toString();
        String phoneToText = binding.phoneNumberInput.getText().toString();

        String firstToText = binding.firstNameInput.getText().toString();
        String lastToText = binding.lastNameInput.getText().toString();

        String passwordToText = binding.passwordInput.getText().toString();
        String passwordConfirmToText = binding.passwordConfirmInput.getText().toString();

        String userNameToText = binding.userNameInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean emailEmpty= emailToText.isEmpty();

        binding.emailEmptyMessage.setVisibility(!emailEmpty?View.GONE : View.VISIBLE);



        boolean emailValid = emailEmpty|| (Patterns.EMAIL_ADDRESS.matcher(emailToText).matches());
        binding.emailInvalidMessage.setVisibility(emailValid ? View.GONE : View.VISIBLE);


        boolean phoneEmpty= phoneToText.isEmpty();

        binding.phoneNumEmptyMessage.setVisibility(!phoneEmpty?View.GONE : View.VISIBLE);


        boolean phoneValid = phoneEmpty|| Patterns.PHONE.matcher(phoneToText).matches();
        binding.phoneNumInvalidMessage.setVisibility(phoneValid ? View.GONE : View.VISIBLE);


        boolean firstNameEmpty= firstToText.isEmpty();

        binding.firstNameEmptyMessage.setVisibility(!firstNameEmpty?View.GONE : View.VISIBLE);



        Pattern pattern = Pattern.compile("^[A-Za-z-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(firstToText);
        boolean firstNameValid = firstNameEmpty|| matcher.find();

        binding.firstNameInvalidMessage.setVisibility(firstNameValid ? View.GONE : View.VISIBLE);



        boolean lastNameEmpty= lastToText.isEmpty();

        binding.lastNameEmptyMessage.setVisibility(!lastNameEmpty?View.GONE : View.VISIBLE);


        matcher = pattern.matcher(lastToText);

        boolean lastNameValid = lastNameEmpty|| matcher.find();


        binding.lastNameInvalidMessage.setVisibility(lastNameValid ? View.GONE : View.VISIBLE);

        boolean passWordEmpty =false;
        if(!editing) {


            passWordEmpty = passwordToText.isEmpty();

            binding.passwordEmptyMessage.setVisibility(!passWordEmpty ? View.GONE : View.VISIBLE);
        }

        boolean passwordMatch = passwordToText.equals(passwordConfirmToText);
        binding.passwordConfirmInvalidMessage.setVisibility(passwordMatch ? View.GONE : View.VISIBLE);
        return !emailEmpty && emailValid && !phoneEmpty&& phoneValid &&
                !lastNameEmpty&&lastNameValid &&
                !firstNameEmpty&&firstNameValid &&
                (editing||!passWordEmpty)&&passwordMatch;
    }

    public void goToHeadShot() {

        Bundle data = new Bundle();
        if(editing) data.putString("user_id", getArguments().getString("user_id"));
        data.putString("user_email", binding.emailInput.getText().toString());
        data.putString("user_phone", binding.phoneNumberInput.getText().toString());
        data.putString("user_first_name", binding.firstNameInput.getText().toString());
        data.putString("user_last_name", binding.lastNameInput.getText().toString());
        data.putString("user_username", binding.userNameInput.getText().toString());
        data.putString("user_password", binding.passwordInput.getText().toString());

//        NavHostFragment.findNavController(RegistrationPage.this)
//                .navigate(R.id.action_registrationPage_to_registrationHeadShot, data);
        ((MainActivity)getActivity()).navigate(R.id.registrationHeadShot,data);



    }


    public void submitForm()  {


        SharedPreferences sharedData =   getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
       String  token = sharedData.getString("jwt", "");

       String userId= getArguments().getString("user_id");

        JSONObject data ;
        try {


            data= new JSONObject();
            data.put("user_id",userId);

            data.put("token",token);

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
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/updateUserInfo.php";

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
                            System.out.println(response);

                            MainActivity.showToast(RegistrationPage.this,response.getString("reason"));
                        ((MainActivity)getActivity()).navigate(R.id.userProfilePage,null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(getContext(), error);
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

}