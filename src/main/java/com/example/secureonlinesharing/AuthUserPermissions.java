package com.example.secureonlinesharing;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
import com.example.secureonlinesharing.databinding.AuthUserPermissionsBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthUserPermissions extends Fragment implements OnMapReadyCallback {

    private AuthUserPermissionsBinding binding;

    private LatLng accessCoords= null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AuthUserPermissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.mapContainer, mapFragment)
                .commit();






        // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Bundle bundle = new Bundle();
                    bundle.putString("media_id",getArguments().getString("media_id"));
                    NavHostFragment.findNavController(AuthUserPermissions.this)
                            .navigate(R.id.action_authUserPermissions_to_mediaUploader,bundle);
                }
            });
        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }


       binding.startDateInput.setOnClickListener (new View.OnClickListener() {
           @Override
           public void onClick(View view) {
//               DatePickerFragment newFragment = new DatePickerFragment();
//               newFragment.show(getParentFragmentManager(), "datePicker");

                showDatePicker(binding.startDateInput);

           }

       });


        binding.endDateInput.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               DatePickerFragment newFragment = new DatePickerFragment();
//               newFragment.show(getParentFragmentManager(), "datePicker");

                showDatePicker(binding.endDateInput);

            }

        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void showDatePicker(TextView dateInput)
    {
        final Calendar c = Calendar.getInstance();

        // on below line we are getting
        // our day, month and year.
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // on below line we are creating a variable for date picker dialog.
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                // on below line we are passing context.
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // on below line we are setting date to our text view.
                        dateInput.setText((monthOfYear+1) + "-" + dayOfMonth + "-" + year);



                    }
                },
                // on below line we are passing year,
                // month and day for selected date in our date picker.
                year, month, day);
        // at last we are calling show to
        // display our date picker dialog.
        datePickerDialog.show();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                System.out.println("CLicked on Map");
                System.out.println(latLng);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng));
                accessCoords= latLng;
            }
        });

    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it.
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date the user picks.
        }
    }




}