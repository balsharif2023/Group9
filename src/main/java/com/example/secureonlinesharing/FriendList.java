package com.example.secureonlinesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.secureonlinesharing.databinding.FriendListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FriendList extends Fragment {

    private FriendListBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FriendListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getFriends();

        // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(FriendList.this)
                            .navigate(R.id.action_friendList_to_SecondFragment);
                }
            });
        }

        ImageButton userMenuButton = getActivity().findViewById(R.id.userMenuButton);
        if (userMenuButton != null) {
            userMenuButton.setVisibility(View.VISIBLE);


        }


        binding.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("action", "friend");

                NavHostFragment.findNavController(FriendList.this)
                        .navigate(R.id.action_friendList_to_authUserSearch, bundle);

            }
        });
    }

    public void getFriends() {

        binding.friendRequestToMe.removeAllViews();

        binding.friendRequestByMe.removeAllViews();
        binding.friendListCurrent.removeAllViews();

        binding.friendRequestToMe.setVisibility(View.GONE);

        binding.friendRequestByMe.setVisibility(View.GONE);

        binding.friendListCurrent.setVisibility(View.GONE);

        binding.friendRequestToMeLabel.setVisibility(View.GONE);

        binding.friendRequestByMeLabel.setVisibility(View.GONE);

        binding.friendListCurrentLabel.setVisibility(View.GONE);

        binding.dividerCurrent.setVisibility(View.GONE);

        binding.dividerToMe.setVisibility(View.GONE);


        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");


        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/getFriendList.php";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

                Request.Method.GET,

                url,

                null,


                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String friendList;
                    try {
                        friendList = response.getString("friend_list");
                        System.out.println(friendList);
                        JSONArray json = new JSONArray(friendList);
                        View toMePrev = null, currentPrev = null, byMePrev = null;


                        for (int i = 0; i < json.length(); i++) {
                            JSONObject entry = json.getJSONObject(i);

                            String userName = entry.getString("user_username");
                            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_record, null);
                            ((TextView) view.findViewById(R.id.userName)).setText(userName);


                            String status = entry.getString("friend_status");

                            if (status.equals("requested")) {
                                String role = entry.getString("sender_or_reciever");
                                if (role.contains("receiver")) {
                                    byMePrev = addToList(binding.friendRequestByMe, view, byMePrev);
                                } else {
                                    toMePrev = addToList(binding.friendRequestToMe, view, toMePrev);
                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new FriendRequestDialogFragment(FriendList.this, userName).show(getParentFragmentManager(), "Friend Request");


                                        }
                                    });
                                }
                            } else if (status.equals("friends")) {
                                ImageButton deleteButton= (ImageButton) view.findViewById(R.id.authUserDeleteButton);
                                deleteButton.setVisibility(View.VISIBLE);

                                deleteButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FriendList.this.respondToFriendRequest(userName,false);
                                    }
                                });

                                currentPrev = addToList(binding.friendListCurrent, view, currentPrev);


                            }


                        }

                        if (toMePrev != null) {
                            binding.friendRequestToMeLabel.setVisibility(View.VISIBLE);
                            binding.friendRequestToMe.setVisibility(View.VISIBLE);

                            if (currentPrev != null || byMePrev != null)

                                binding.dividerToMe.setVisibility(View.VISIBLE);

                        }
                        if (currentPrev != null) {

                            binding.friendListCurrentLabel.setVisibility(View.VISIBLE);
                            binding.friendListCurrent.setVisibility(View.VISIBLE);

                            if (byMePrev != null)

                                binding.dividerCurrent.setVisibility(View.VISIBLE);

                        }

                        if (byMePrev != null) {
                            binding.friendRequestByMe.setVisibility(View.VISIBLE);
                            binding.friendRequestByMeLabel.setVisibility(View.VISIBLE);

                        }


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

    public View addToList(ViewGroup list, View item, View prev) {

        list.addView(item);
        if (prev != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 40);
            prev.setLayoutParams(params);
        }

        return item;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static class FriendRequestDialogFragment extends DialogFragment {

        FriendList friendList;
        String userName;


        public FriendRequestDialogFragment(FriendList friendList, String userName) {
            this.userName = userName;
            this.friendList = friendList;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("New Friend Request from " + userName)
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            friendList.respondToFriendRequest(userName, true);
                        }
                    })
                    .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            friendList.respondToFriendRequest(userName, false);

                        }
                    });
            // Create the AlertDialog object and return it.
            return builder.create();
        }
    }

    public void respondToFriendRequest(String userName, boolean accept) {

        SharedPreferences data = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");


        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/";

        if (accept) {
            url += "acceptFriendRequest.php?requesterFriendUserName=" + userName;
        } else
            url += "declineFriendRequest.php?requesterUserName=" + userName;


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
                        MainActivity.showToast(FriendList.this, response.getString("reason"));


                        boolean success = response.getBoolean("wasSuccessful");
                        if (success) {

                            getFriends();


                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
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