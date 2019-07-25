package com.uber.pickbot;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Whereto extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public GoogleMap mMap;
    LinearLayout locatemelayout;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    TextView done_button;
    EditText userlocation;
    LatLng userlatlnglocation,pickuplocation,destinationlatlng;
    Geocoder geocoder;
    Boolean requestbol = false;
    Marker pickupmarker;
    ImageView addplus;
    String destination;
    LinearLayout mdriverinfo;
    ImageView driverprofileimage,back;
    TextView drivername,drivernumber,drivercar,for_me;
    RatingBar mRatingbar;
    List<Address> address;
    private String TAG;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        if (googleServicesAvailable()) {
//            Toast.makeText(Whereto.this, "Perfect", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_whereto);
//            initMap();
//            locatemelayout = (LinearLayout) findViewById(R.id.locatme);
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        }

        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Whereto.this, R.color.grey));

        geocoder = new Geocoder(Whereto.this, Locale.getDefault());
        destinationlatlng = new LatLng(0.0,0.0);
        done_button = (TextView)findViewById(R.id.done_button);
        userlocation = (EditText) findViewById(R.id.userlocation);
        addplus = (ImageView) findViewById(R.id.add_plus);
        mdriverinfo = (LinearLayout)findViewById(R.id.driverInfo);
        driverprofileimage = (ImageView) findViewById(R.id.driverprofileimage);
        drivername = (TextView) findViewById(R.id.drivername);
        drivernumber = (TextView) findViewById(R.id.drivernumber);
        drivercar = (TextView) findViewById(R.id.drivercar);
        for_me = (TextView) findViewById(R.id.for_me);
        mRatingbar = (RatingBar) findViewById(R.id.ratingbar);
        back = (ImageView) findViewById(R.id.back);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Whereto.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else{
            mapFragment.getMapAsync(this);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        for_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(Whereto.this,Fake_history.class);
                history.putExtra("customerOrDriver", "Customers");
                startActivity(history);
            }
        });

        addplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Whereto.this,Update_customer_info.class));
            }
        });


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationlatlng = place.getLatLng();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An Error Occured" + status);
            }
        });





        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               userlatlnglocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
//               mMap.addMarker(new MarkerOptions().position(userlatlnglocation).title("Pickup location"));
//               done_button.setText("Waiting for Driver...");
                if(requestbol){
                    endRide();
                }else{
                    requestbol = true;
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String s, DatabaseError databaseError) {

                        }
                    });

                    pickuplocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupmarker = mMap.addMarker(new MarkerOptions().position(pickuplocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin_)));
                    done_button.setText("Waiting for Driver...");
                    getClosestDriver();
                }
            }
        });
    }
    int radius = 1;
    Boolean driverFound = false;
    String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestDriver() {
        DatabaseReference driverlocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverlocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude,pickuplocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation geoLocation) {
                if(!driverFound && requestbol){
                    driverFound = true;
                    driverFoundID = s;
                }
                DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap map = new HashMap();
                map.put("customerRideId",customerId);
                map.put("destination",destination);
                map.put("destinationLat",destinationlatlng.latitude);
                map.put("destinationLng",destinationlatlng.longitude);
                driverref.updateChildren(map);

                getDriverLocation();
                getDriverInfo();
                getHasRideEnded();
                done_button.setText("Looking for driver location...");
            }

            @Override
            public void onKeyExited(String s) {

            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo() {
            mdriverinfo.setVisibility(View.VISIBLE);
            DatabaseReference mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
            mCustomerdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        java.util.Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if(map.get("name") != null){
                            drivername.setText(map.get("name").toString());
                        }
                        if(map.get("phone") != null){
                            drivernumber.setText(map.get("phone").toString());
                        }
                        if(map.get("car") != null){
                            drivercar.setText(map.get("car").toString());
                        }
                        if(map.get("profileImageUrl") != null){
                            Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(driverprofileimage);
                        }

                        int ratingSum = 0;
                        float ratingTotal = 0;
                        float ratingAvg = 0;
                        for(DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                            ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                            ratingTotal++;
                        }

                        if(ratingTotal != 0){
                            ratingAvg = ratingSum/ratingTotal;
                            mRatingbar.setRating(ratingAvg);
                        }
                    }else{

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    DatabaseReference driveHasEndedRef;
    ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");
        driverLocationrefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void endRide() {
        requestbol = false;
        geoQuery.removeAllListeners();
        driverLocationref.removeEventListener(driverLocationrefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if(driverFoundID != null){
            DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
            driverref.removeValue();
//            driverref.setValue(true);
            driverFoundID = null;
        }
        driverFound = false;
        radius = 1;

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String s, DatabaseError databaseError) {

            }
        });

        if(pickupmarker !=  null){
            pickupmarker.remove();
        }
        if(mDriverMarker != null){
            mDriverMarker.remove();
        }
        done_button.setText("Call PickBot");
        mdriverinfo.setVisibility(View.GONE);
        drivername.setText("");
        drivernumber.setText("");
        drivercar.setText("");
        driverprofileimage.setImageResource(R.drawable.profile);
    }

    Marker mDriverMarker;
    DatabaseReference driverLocationref;
    ValueEventListener driverLocationrefListener;
    private void getDriverLocation() {
        driverLocationref = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationrefListener = driverLocationref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestbol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlong = 0;
                    done_button.setText("Driver Found");
                    if(map.get(0) != null){
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationlong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverlatlng = new LatLng(locationlat,locationlong);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1  = new Location("");
                    loc1.setLatitude(pickuplocation.latitude);
                    loc1.setLongitude(pickuplocation.longitude);


                    Location loc2  = new Location("");
                    loc2.setLatitude(driverlatlng.latitude);
                    loc2.setLongitude(driverlatlng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    done_button.setText("Distance: " + String.valueOf(distance));
                    if(distance<100){
                        done_button.setText("Driver Has Arrived");

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Whereto.this)
                                .setSmallIcon(R.drawable.pickbot_logo)
                                .setContentTitle("Driver Arrived")
                                .setContentText("Your PickBot Driver Has Arrived.")
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("Your PickBot Driver Has Arrived."))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                          // notificationID allows you to update the notification later on.
                        mNotificationManager.notify(1, mBuilder.build());

                    }else{
                        done_button.setText("Distance: " + String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
//        List<Address> addresses;
//
//        try {
//            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude(),1);
//            userlocation.setText((CharSequence) addresses);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    private void gotoLocation(double lat, double lng) {
//        LatLng latLng = new LatLng(lat,lng);
//        CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
//        mMap.moveCamera(update);
//        mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("Here"));
//
//
//    }

//    private void gotoLocationZoom(double lat, double lng, float zoom) {
//        LatLng latLng = new LatLng(lat,lng);
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
//        mMap.moveCamera(update);
//    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API
        ).build();
        mGoogleApiClient.connect();
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isavailable = api.isGooglePlayServicesAvailable(this);
        if (isavailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isavailable)) {
            Dialog dialog = api.getErrorDialog(this, isavailable, 0);
            dialog.show();
        } else {
            Toast.makeText(Whereto.this, "Cannot Connect To Google Play Services", Toast.LENGTH_LONG).show();
        }


        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        try {
            address = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            userlocation.setText(address.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
//        update location of driver
//
//        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
//            @Override
//            public void onComplete(String s, DatabaseError databaseError) {
//                Toast.makeText(Whereto.this,s,Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        used by driver

//        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userid);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Whereto.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Turn Location On",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
