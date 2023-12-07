package com.example.secureonlinesharing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {



    private  int PERMISSION_ID = 44;
    private Location currentLocation;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public ImageButton backButton,userMenuButton;

    private DrawerLayout drawerLayout;

    public ActionBarDrawerToggle actionBarDrawerToggle;


    // variable for shared preferences.
    public SharedPreferences sharedpreferences;

    private Stack<Integer> navHistory;

    private  Stack<Bundle> argHistory;


    //cache filename

    public static  final String HEADSHOT_CACHE_FILE = "profile_pic";

    public static  final String MEDIA_UPLOAD_CACHE_FILE = "media_upload_temp";


    public static  final String MEDIA_VIEWER_CACHE_FILE = "media_viewer_temp";

    private NavController navController;

    FusedLocationProviderClient mFusedLocationClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

         navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        System.out.println(navController.getCurrentDestination().getDisplayName());
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

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int last = navHistory.pop();
//                Bundle lastArgs= argHistory.pop();


                navController.popBackStack();
               // MainActivity.this.navigate(last,lastArgs);
                setBackButtonDisplay();


            }
        });

        navHistory =new Stack<Integer>();

        argHistory = new Stack<Bundle>();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();


    }

    public void  navigate(int dest,Bundle args){
      //  NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment_content_main);
        System.out.println(navController.getCurrentDestination().getDisplayName());
     //   navController.navigateUp();
        System.out.println(navController.getCurrentDestination().getDisplayName());

        String srcName = navController.getCurrentDestination().getDisplayName();

        if(dest==R.id.FirstFragment)
        {
            binding.userMenuButton.setVisibility(View.GONE);
        }


        navController.navigate(dest,args);



        setBackButtonDisplay();
       // binding.backButton.setVisibility(navHistory.isEmpty()?View.GONE:View.VISIBLE);

    }
    public void navigateFrom(Fragment src, int dest,Bundle args){
//        navHistory.push(src.getId());
//        argHistory.push(src.getArguments());

        navigate(dest, args);




    }

    public void setBackButtonDisplay()
    {
        NavBackStackEntry entry = navController.getCurrentBackStackEntry();
        System.out.println("backStack entry "+entry.getDestination().getDisplayName());
        binding.backButton.setVisibility(
               entry==null
                       //||entry.getDestination().getDisplayName().contains("FirstFragment")
                      ||entry.getDestination().getId()==R.id.FirstFragment
                       ||entry.getDestination().getId()==R.id.SecondFragment
                        ?View.GONE:View.VISIBLE);
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
        if (id==R.id.action_profile)
        {

            navigate(R.id.userProfilePage,null);


        }
        else if (id==R.id.action_media)

        {


            navigate(R.id.SecondFragment,null);


        }



        else if (id==R.id.action_friends)

        {


            navigate(R.id.friendList,null);


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


                    navigate(R.id.FirstFragment,null);


                    binding.userMenuButton.setVisibility(View.GONE);

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
        Toast.makeText(fragment.getContext(), message, Toast.LENGTH_LONG).show();
    }









    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {

                            System.out.println("Latitude: " + location.getLatitude() + "");
                            System.out.println("Longitude: " + location.getLongitude() + "");
                            currentLocation = location;

                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
           System.out.println("Latitude: " + mLastLocation.getLatitude() + "");
            System.out.println("Longitude: " + mLastLocation.getLongitude() + "");

            currentLocation = mLastLocation;

        }
    };

    public Location getCurrentLocation(){
        return currentLocation;
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this,
               "android.permission.ACCESS_COARSE_LOCATION") ==
                PackageManager.PERMISSION_GRANTED;
        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.ACCESS_COARSE_LOCATION"}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }




}