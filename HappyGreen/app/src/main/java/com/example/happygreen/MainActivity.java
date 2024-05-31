package com.example.happygreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    String newUserLocation;

    final String APP_ID = "ab3bad0d9f71519b0564c7dee7437de3";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;


    String Location_Provider = LocationManager.GPS_PROVIDER;
    TextView NameofCity, weatherState, Temperatures;
    RelativeLayout mCityFinder;
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    ProgressBar pgrbar;
    String prog = "50";
    String ID = "1";
    int choice = 1;
    int running = 0;
    String timeTxt = "Tue Jun 00 00:00:00 2023";
    String pmp_inf = "Pump: ";
    ConstraintLayout proglay;
    TextView pgrText;
    Button ProgBtn;
    Button PumpBtn;
    TextView timeText;
    TextView PumpInfo;
    DatabaseReference rootDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherState = findViewById(R.id.weatherCondition);
        Temperatures = findViewById(R.id.temperature);
        mCityFinder = findViewById(R.id.cityFinder);
        NameofCity = findViewById(R.id.cityName);
        Spinner spinner = findViewById(R.id.spinner);
        PumpInfo = findViewById(R.id.pumpinfo);
        proglay = findViewById(R.id.proglay);
        pgrbar = proglay.findViewById(R.id.progress_bar);
        pgrText = proglay.findViewById(R.id.text_progress);
        ProgBtn = findViewById(R.id.prog_butn);
        timeText = proglay.findViewById(R.id.time_input);
        rootDatabase = FirebaseDatabase.getInstance().getReference().child("id").child(ID);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(user.getEmail());

        }

        updateProgbar();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
                finish();
            }
        });

        mCityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

            }
        });

        ProgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIDDialog();

                rootDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && running == 0){
                        prog = snapshot.child("Humidity").getValue().toString();
                        pgrbar.setProgress(Integer.parseInt(prog));
                        pgrText.setText(String.valueOf(prog) + "%");
                        timeTxt = snapshot.child("Time").getValue().toString();
                        timeText.setText(timeTxt);
                        PumpInfo.setText(pmp_inf + snapshot.child("Pump").getValue().toString());
                    }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
            }

        });

        PumpBtn = findViewById(R.id.pump_butn);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.menu_items, R.layout.spinner_item_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("Basic")) {
                    choice = 1;
                    updateChoice();
                } else if (selectedItem.equals("Cereals")) {
                    choice = 2;
                    updateChoice();
                } else if (selectedItem.equals("Vegetables")) {
                    choice = 3;
                    updateChoice();
                } else if (selectedItem.equals("Flowers")) {
                    choice = 4;
                    updateChoice();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do something when nothing is selected
            }
        });


        PumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PumpInfo.setText("Pump: ON");
                DatabaseReference AddrootDatabase = FirebaseDatabase.getInstance().getReference().child("id").child(ID);
                AddrootDatabase.child("Btn").setValue("ON");
                //PumpInfo.setText("Pump: ON");

                try {
                    PumpInfo.setText("Pump: ON");
                    Thread.sleep(7000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                AddrootDatabase.child("Btn").setValue("OFF");
                }


        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent mIntent=getIntent();
        String city = newUserLocation;
        if(city!=null)
        {
            getWeatherForNewCity(city);
        }
        else
        {
            getWeatherForCurrentLocation();
        }

    }

    private void getWeatherForNewCity(String city)
    {
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

    }

    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params =new RequestParams();
                params.put("lat" ,Latitude);
                params.put("lon",Longitude);
                params.put("appid",APP_ID);
                letsdoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //not able to get location
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Locationget Succesffully",Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }

        }

    }


    private  void letsdoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Toast.makeText(MainActivity.this,"Data Get Success",Toast.LENGTH_SHORT).show();

                weatherData weatherD=weatherData.fromJson(response);
                updateUI(weatherD);

                // super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }

    private  void updateUI(weatherData weather){

        Temperatures.setText(weather.getmTemperature());
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getmWeatherType());

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void getLocation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_user_location, null);
        builder.setView(dialogView);

        final EditText location = dialogView.findViewById(R.id.location);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newUserLocation = location.getText().toString();
                getWeatherForNewCity(newUserLocation);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public void updateProgbar(){
        running = 1;
        rootDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    prog = snapshot.child("Humidity").getValue().toString();
                    pgrbar.setProgress(Integer.parseInt(prog));
                    pgrText.setText(String.valueOf(prog) + "%");
                    updateChoice();
                    timeTxt = snapshot.child("Time").getValue().toString();
                    timeText.setText(timeTxt);
                    PumpInfo.setText(pmp_inf + snapshot.child("Pump").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showIDDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_id, null);
        builder.setView(dialogView);

        final EditText IDtext = dialogView.findViewById(R.id.id);

        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ID = IDtext.getText().toString();
                rootDatabase = FirebaseDatabase.getInstance().getReference().child("id").child(ID);
                updateProgbar();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void updateChoice(){
        if (choice == 1){
            if(Integer.parseInt(prog) < 35)
                pgrText.setTextColor(getResources().getColor(R.color.red));
            else if (Integer.parseInt(prog) < 55) {
                pgrText.setTextColor(getResources().getColor(R.color.yellow));
            }
            else pgrText.setTextColor(getResources().getColor(R.color.green));

        }
        if (choice == 2){
            if(Integer.parseInt(prog) < 40)
                pgrText.setTextColor(getResources().getColor(R.color.red));
            else if (Integer.parseInt(prog) < 60) {
                pgrText.setTextColor(getResources().getColor(R.color.yellow));
            }
            else pgrText.setTextColor(getResources().getColor(R.color.green));

        }
        if (choice == 3){
            if(Integer.parseInt(prog) < 30)
                pgrText.setTextColor(getResources().getColor(R.color.red));
            else if (Integer.parseInt(prog) < 50) {
                pgrText.setTextColor(getResources().getColor(R.color.yellow));
            }
            else pgrText.setTextColor(getResources().getColor(R.color.green));

        }
        if (choice == 4){
            if(Integer.parseInt(prog) < 20)
                pgrText.setTextColor(getResources().getColor(R.color.red));
            else if (Integer.parseInt(prog) < 40) {
                pgrText.setTextColor(getResources().getColor(R.color.yellow));
            }
            else pgrText.setTextColor(getResources().getColor(R.color.green));

        }
    }

}
