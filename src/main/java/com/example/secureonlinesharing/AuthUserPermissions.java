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
        mapFragment.getMapAsync(this);






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

        binding.saveButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    savePermissions();
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
                        dateInput.setText(year + "-" +(monthOfYear+1) + "-" + dayOfMonth);



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




    public void savePermissions() {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");

        String userId = data.getString("id", "");

        JSONObject json = new JSONObject();

        Bundle args = getArguments();

        try {
            json.put("media_id",args.getString("media_id"));

            json.put("user_id",args.getString("user_id"));

            json.put("start_date",binding.startDateInput.getText());

            json.put("end_date",binding.endDateInput.getText());

            json.put("latitude",""+ accessCoords.latitude);
            json.put("longitude",""+ accessCoords.longitude);





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
                        if(success){


                            MainActivity.showToast(AuthUserPermissions.this,
                                    "Permissions saved");

                            Bundle bundle = new Bundle();
                            bundle.putString("media_id",args.getString("media_id") );

                            NavHostFragment.findNavController(AuthUserPermissions.this)
                                    .navigate(R.id.action_authUserPermissions_to_mediaUploader, bundle);


                        }

                        else
                            MainActivity.showToast(AuthUserPermissions.this,
                                    "Permissions unsaved");










                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    //  Toast.makeText(getActivity(), "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                    // log the error message in the error stream
                    //    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                    NetworkResponse networkResponse = error.networkResponse;
                    //System.out.println("status code: "+ networkResponse.statusCode );
                    String errorMessage = "Unknown error";
                    if (networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            errorMessage = "Request timeout";
                        } else if (error.getClass().equals(NoConnectionError.class)) {
                            errorMessage = "Failed to connect server";
                        }
                        Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        String result = new String(networkResponse.data);
                        try {
                            JSONObject response = new JSONObject(result);

                            String message = response.getString("message");


                            Log.e("Error Message", message);
                            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                            toast.show();

                            if (networkResponse.statusCode == 404) {
                                errorMessage = "Resource not found";
                            } else if (networkResponse.statusCode == 401) {
                                errorMessage = message + " Please login again";
                            } else if (networkResponse.statusCode == 400) {
                                errorMessage = message + " Check your inputs";
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = message + " Something is getting wrong";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("Error", errorMessage);
                    error.printStackTrace();
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