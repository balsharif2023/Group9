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


import com.example.secureonlinesharing.databinding.RegistrationPageBinding;

public class RegistrationPage extends Fragment {

    private RegistrationPageBinding binding;

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

        // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(RegistrationPage.this)
                            .navigate(R.id.action_registrationPage_to_FirstFragment);
                }
            });
        }

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                validateForm();


//                    NavHostFragment.findNavController(RegistrationPage.this)
//                        .navigate(R.id.action_registrationPage_to_FirstFragment);
            }
        });
    }

    public void validateForm() {
        // extract the entered data from the EditText
        String emailToText = binding.emailInput.getText().toString();
        String phoneToText = binding.phoneNumberInput.getText().toString();

        String firstToText = binding.firstNameInput.getText().toString();
        String lastToText = binding.lastNameInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean invalid = (emailToText.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailToText).matches());
            binding.emailInvalidMessage.setVisibility(invalid ? View.VISIBLE : View.GONE);


        invalid = phoneToText.isEmpty() || !Patterns.PHONE.matcher(phoneToText).matches();
            binding.phoneNumInvalidMessage.setVisibility(invalid ? View.VISIBLE : View.GONE);


        Pattern pattern = Pattern.compile("^[A-Za-z-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(firstToText);
        boolean matchFound = matcher.find();

        binding.firstNameInvalidMessage.setVisibility(matchFound ? View.GONE : View.VISIBLE);



        matcher = pattern.matcher(lastToText);

        matchFound = matcher.find();


        binding.lastNameInvalidMessage.setVisibility(matchFound ? View.GONE : View.VISIBLE);




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}