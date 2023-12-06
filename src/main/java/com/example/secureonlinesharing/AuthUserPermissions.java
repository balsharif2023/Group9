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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class AuthUserPermissions extends Fragment implements OnMapReadyCallback {

    private AuthUserPermissionsBinding binding;

    private LatLng accessCoords = null;

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


        Bundle args = getArguments();

        binding.startDateInput.setText(args.getString("start_date"));

        binding.endDateInput.setText(args.getString("end_date"));


        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getParentFragmentManager()
                .beginTransaction()
                .add(R.id.mapContainer, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);


        binding.startDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               DatePickerFragment newFragment = new DatePickerFragment();
//               newFragment.show(getParentFragmentManager(), "datePicker");

                showDatePicker(binding.startDateInput);

            }

        });


        binding.endDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               DatePickerFragment newFragment = new DatePickerFragment();
//               newFragment.show(getParentFragmentManager(), "datePicker");

                showDatePicker(binding.endDateInput);

            }

        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    savePermissions();

                }

            }

        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void showDatePicker(TextView dateInput) {
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
                        dateInput.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);


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

        System.out.println("map ready");

        String latStr = getArguments().getString("lat");

        String lonStr = getArguments().getString("long");

        if (latStr != null && !latStr.equals("")) {
            try {


                double lat = Double.parseDouble(latStr);

                double lon = Double.parseDouble(lonStr);

                googleMap.addMarker(new MarkerOptions().position(
                        new LatLng(lat, lon)));
                binding.mapCoords.setText("Lat " +
                        new DecimalFormat("#.###").format(lat)
                        + "\u00B0, Long " +
                        new DecimalFormat("#.###").format(lon) + "\u00B0");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                System.out.println("CLicked on Map");
                System.out.println(latLng);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng));
                accessCoords = latLng;
                binding.mapCoords.setText("Lat " +
                        new DecimalFormat("#.###").format(latLng.latitude)
                        + "\u00B0, Long " +
                        new DecimalFormat("#.###").format(latLng.longitude) + "\u00B0");

            }
        });

    }


    public boolean validateForm() {
        // extract the entered data from the EditText

        String startDateToText = binding.startDateInput.getText().toString();
        String endDateToText = binding.endDateInput.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean startDateValid = !startDateToText.isEmpty();
        binding.startDateEmptyMessage.setVisibility(startDateValid ? View.GONE : View.VISIBLE);

        boolean endDateValid = !endDateToText.isEmpty();
        binding.endDateEmptyMessage.setVisibility(endDateValid ? View.GONE : View.VISIBLE);

        return startDateValid && endDateValid;

    }


    public void savePermissions() {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");

        JSONObject json = new JSONObject();

        Bundle args = getArguments();

        try {
            json.put("media_id", args.getString("media_id"));

            json.put("user_id", args.getString("user_id"));

            json.put("start_date", binding.startDateInput.getText());

            json.put("end_date", binding.endDateInput.getText());
            if (accessCoords != null) {


                json.put("latitude", "" + accessCoords.latitude);
                json.put("longitude", "" + accessCoords.longitude);
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        System.out.println(json);

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/aristotle/v1/controller/addUpdateAuthorizedUser.php";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.POST,

                url,

                json,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String message;
                    try {
                        message = response.getString("message");
                        System.out.println(message);

                        boolean success = response.getBoolean("isSuccessful");
                        if (success) {


                            MainActivity.showToast(AuthUserPermissions.this,
                                    "Permissions saved");

                            Bundle bundle = new Bundle();
                            bundle.putString("media_id", args.getString("media_id"));

//                            NavHostFragment.findNavController(AuthUserPermissions.this)
//                                    .navigate(R.id.action_authUserPermissions_to_mediaUploader, bundle);
                            ((MainActivity) getActivity()).navigate(R.id.mediaUploader, bundle);


                        } else
                            MainActivity.showToast(AuthUserPermissions.this,
                                    "Permissions unsaved");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(AuthUserPermissions.this.getContext(), error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                HashMap<String, String> headers2 = new HashMap<String, String>();
                for (String s : headers.keySet()) {
                    headers2.put(s, headers.get(s));
                }
                headers2.put("Authorization", "Bearer " + token);
                return headers2;
            }
        };
        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
        //} // catch(JSONException e){} */


    }


}