package com.example.secureonlinesharing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.secureonlinesharing.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public ImageButton backButton,userMenuButton;

    private DrawerLayout drawerLayout;

    public ActionBarDrawerToggle actionBarDrawerToggle;


    // variable for shared preferences.
    public SharedPreferences sharedpreferences;


    //cache filename

    public static  final String HEADSHOT_CACHE_FILE = "profile_pic";

    public static  final String MEDIA_UPLOAD_CACHE_FILE = "media_upload_temp";


    public static  final String MEDIA_VIEWER_CACHE_FILE = "media_viewer_temp";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        backButton = binding.backButton;
        userMenuButton =binding.userMenuButton;

        binding.userMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!drawerLayout.isOpen())

                   drawerLayout.open();
                else
                    drawerLayout.close();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        drawerLayout.close();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id==R.id.action_profile)
        {
            NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment_content_main);
            navController.navigateUp();
            navController.navigate(R.id.userProfilePage);


        }
        else if (id==R.id.action_media)

        {

            NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment_content_main);
            navController.navigateUp();
            navController.navigate(R.id.SecondFragment);

        }



        else if (id==R.id.action_friends)

        {

            NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment_content_main);
            navController.navigateUp();
            navController.navigate(R.id.friendList);

        }


        else if (id==R.id.action_logout)

        {
            logout();

        }



        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }




    public void logout(){



        SharedPreferences data = getSharedPreferences("user_data", Context.MODE_PRIVATE);

        String token = data.getString("jwt", "");



        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        // url of the api through which we get random dog images
        String url = "https://innshomebase.com/securefilesharing/develop/admetus/v1/controller/userLogout.php";




        // since the response we get from the api is in JSON, we
        // need to use `JsonObjectRequest` for parsing the
        // request response
        //  try {

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


                    // message = response.getString("message");



                    // SharedPreferences data = ((MainActivity)getActivity()).sharedpreferences;



                    // below two lines will put values for
                    // email and password in shared preferences.

                        System.out.println(response);

                    NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment_content_main);
                    navController.navigateUp();
                    navController.navigate(R.id.FirstFragment);

                    try {
                        Toast.makeText(MainActivity.this, response.getString("reason"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    // load the image into the ImageView using Glide.
                    //binding.textviewFirst.setText(message);

                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    MainActivity.showVolleyError(MainActivity.this,error);

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



    }

    public static void showVolleyError(Context context, VolleyError error){

        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);

                String message = null;
                if(response.has("message"))

                     message = response.getString("message");

                else if (response.has("reason"))
                {
                    message = response.getString("reason");

                }

                if(message!=null) {

                    Log.e("Error Message", message);
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                }
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





    public static void showToast(Fragment fragment, String message) {
        Toast.makeText(fragment.getContext(), message, Toast.LENGTH_SHORT).show();
    }


}