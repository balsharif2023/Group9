package com.example.secureonlinesharing;

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
            binding.userNameInput.setText(args.getString("user_name"));
            binding.firstNameInput.setText(args.getString("user_first"));
            binding.lastNameInput.setText(args.getString("user_last"));
            binding.phoneNumberInput.setText(args.getString("user_phone"));
            binding.emailInput.setText(args.getString("user_email"));
        }



        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateForm()) {
                    goToHeadShot();
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

        binding.firstNameInvalidMessage.setVisibility(firstNameValid ? View.GONE : View.VISIBLE);


        matcher = pattern.matcher(lastToText);

        boolean lastNameValid = matcher.find();


        binding.lastNameInvalidMessage.setVisibility(lastNameValid ? View.GONE : View.VISIBLE);
        boolean passwordMatch = passwordToText.equals(passwordConfirmToText);
        binding.passwordConfirmInvalidMessage.setVisibility(passwordMatch ? View.GONE : View.VISIBLE);
        return emailValid && phoneValid && lastNameValid && firstNameValid && passwordMatch;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}