package com.example.secureonlinesharing;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.example.secureonlinesharing.databinding.RegistrationPageBinding;

public class RegistrationPage extends Fragment {

    private RegistrationPageBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    )

    {

        binding = RegistrationPageBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    NavHostFragment.findNavController(RegistrationPage.this)
                            .navigate(R.id.action_registrationPage_to_FirstFragment);
                }
            });
        }

       binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(RegistrationPage.this)
                        .navigate(R.id.action_registrationPage_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}