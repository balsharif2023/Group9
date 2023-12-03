package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.secureonlinesharing.databinding.AuthUserSearchBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AuthUserSearch extends Fragment {

    private AuthUserSearchBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AuthUserSearchBinding.inflate(inflater, container, false);
        getActivity().setTitle("Olympus");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] cache = {
                MainActivity.MEDIA_VIEWER_CACHE_FILE,
                MainActivity.HEADSHOT_CACHE_FILE,
                MainActivity.MEDIA_UPLOAD_CACHE_FILE
        };

        for(String filename: cache)
        {
            File file = new File(getActivity().getCacheDir(),filename);
            file.delete();
        }
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

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton!= null) {
            userMenuButton.setVisibility(View.GONE);


        }


        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if(validateForm()){
                    search();
                }
            }

        });

        binding.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.modifyButton.setVisibility(View.GONE);

                binding.searchWrapper.setVisibility(View.VISIBLE);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void  search(){



        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt","");


        JSONObject json = new JSONObject();
        try {
            json.put("user_first_name", binding.firstNameInput.getText().toString());
            json.put("user_last_name", binding.lastNameInput.getText().toString());

            json.put("user_username", binding.userNameInput.getText().toString());
            json.put("user_email", binding.emailInput.getText().toString());



        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/searchUsers.php";








        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.POST,
                // url we want to send the HTTP request to
                url,
                // this parameter is used to send a JSON object to the
                // server, since this is not required in our case,
                // we are keeping it `null`
                //  new JSONObject("{userId:1}"),
                json,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object

                    System.out.println(response);
                    String message;


                    // message = response.getString("message");

                    binding.searchWrapper.setVisibility(View.GONE);
                    binding.modifyButton.setVisibility(View.VISIBLE);

                    binding.userRecordWrapper.removeAllViews();
                    Bundle args = getArguments();

                    String action = args.getString("action");

                    String mediaId = args.getString("media_id");





                    String userList;
                    try {
                        userList = response.getString("searchResults");
                        System.out.println(userList);
                        JSONArray users = new JSONArray(userList);
                        View prev = null;

                        // binding.authUsers.removeAllViews();
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject entry = users.getJSONObject(i);

                            String userName = entry.getString("user_username");

                            String userId= entry.getString("user_id");

                            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_record, null);
                            ((TextView) view.findViewById(R.id.userName)).setText(userName);

                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if(action.equals("friend"))
                                    {
                                        sendFriendRequest(userName);
                                    } else if (action.equals("authorize")) {



                                        Bundle bundle = new Bundle();

                                        bundle.putString("media_id",mediaId);

                                        bundle.putString("user_id",userId);




                                        NavHostFragment.findNavController(AuthUserSearch.this)
                                                .navigate(R.id.action_authUserSearch_to_authUserPermissions,bundle);


                                    }
                                }
                            });

                            binding.userRecordWrapper.addView(view);
                            if (prev != null) {
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, 40);
                                prev.setLayoutParams(params);
                            }

                            prev = view;
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(AuthUserSearch.this.getContext(),error);

                }
        )
        {
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

    public boolean validateForm() {
        // extract the entered data from the EditText

        String firstName = binding.firstNameInput.getText().toString();
        String lastName = binding.lastNameInput.getText().toString();

        String userName = binding.userNameInput.getText().toString();
        String email = binding.emailInput.getText().toString();


        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        boolean valid = !firstName.isEmpty()||!lastName.isEmpty()||!userName.isEmpty()||
                !email.isEmpty();
        binding.userSearchEmptyMessage.setVisibility(valid? View.GONE : View.VISIBLE);



        return valid;

    }



    public void  sendFriendRequest(String userName){

        SharedPreferences data =getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt","");



        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/sendFriendRequest.php";
        url+= "?requestedFriendUserName="+ userName;







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





                    // below two lines will put values for
                    // email and password in shared preferences.

                    System.out.println(response);

                    try {
                        MainActivity.showToast(AuthUserSearch.this,response.getString("reason"));


                        boolean success = response.getBoolean("wasSuccessful");
                        if(success){

                            NavHostFragment.findNavController(AuthUserSearch.this)
                                    .navigate(R.id.action_authUserSearch_to_friendList);



                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }



                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(AuthUserSearch.this.getContext(),error);
                }
        ){
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