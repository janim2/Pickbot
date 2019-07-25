package com.uber.pickbot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Fake_Single_history extends FragmentActivity implements OnMapReadyCallback, RoutingListener{
    GoogleMap mMap;
    SupportMapFragment mMapFragment;
    String rideId, currentuserId,customerId,driverId,userDriverOrCustomer;
    TextView ridelocation,ridedistance,ridedate,username,userphone;
    ImageView userImge;
    DatabaseReference historyRideInfoDb;
    LatLng destinationLatLng,pickupLatLng;
    RatingBar mRatingBar;
    String distance;
    Double rideprize;
    Button pay;
    ImageView goback;
    int PAYPAL_REQUEST_CODE = 1;
    Boolean customerPaid = false;


//    configure paypal in sandbox for development;
    static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
        .clientId(PayPalconfig.PAYPAL_CLIENT_Id);

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fake__single_history);


        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Fake_Single_history.this,R.color.black));


//        paypal intent initialization;
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        rideId = getIntent().getExtras().getString("rideId");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        goback = (ImageView)findViewById(R.id.goback);
        mMapFragment.getMapAsync(this);

        polylines = new ArrayList<>();
        ridelocation = (TextView)findViewById(R.id.ridelocation);
        ridedistance = (TextView)findViewById(R.id.ridedistance);
        ridedate = (TextView)findViewById(R.id.rideDate);
        username = (TextView)findViewById(R.id.username);
        userphone = (TextView)findViewById(R.id.userphone);
        userImge = (ImageView)findViewById(R.id.userImage);
        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);
        pay = (Button) findViewById(R.id.pay);

        currentuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child:dataSnapshot.getChildren()){
                        if(child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
//                            distingusish driver from customer
                            if(!customerId.equals(currentuserId)){
                                userDriverOrCustomer = "Drivers";
                                getUserInformation("Customers",customerId);
                            }
                        }

                        //customer side
                        if(child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
//                            distingusish driver from customer
                            if(!driverId.equals(currentuserId)){
                                userDriverOrCustomer = "Customers";
                                getUserInformation("Drivers",driverId);
                                displayCustomerRelatedObject();
                            }
                        }
                        if(child.getKey().equals("timestamp")){
                            ridedate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if(child.getKey().equals("rating")){
                          mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }

                        if(child.getKey().equals("customerPaid")){
                            customerPaid = true;
                        }

                        if(child.getKey().equals("distance")){ //in km
                            distance = child.getValue().toString();
                            ridedistance.setText(distance.substring(0,Math.min(distance.length(),5)) + " km");
                            //calculating prize/
                             rideprize = Double.valueOf(distance) * 0.5;
                        }
                        if(child.getKey().equals("destination")){
                            ridelocation.setText(getDate(Long.valueOf(child.getValue().toString())));

                        }
                        if(child.getKey().equals("location")){
                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatLng !=  new LatLng(0,0)){
                                getRouteToMarker();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObject() {
        mRatingBar.setVisibility(View.VISIBLE);
        pay.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyRideInfoDb.child("rating").setValue(rating);
                DatabaseReference mDriverratingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                mDriverratingDb.child(rideId).setValue(rating);
            }
        });

        if(customerPaid){
            pay.setEnabled(false);
            pay.setBackgroundColor(getResources().getColor(R.color.grey));
        }else{
            pay.setEnabled(true);
        }
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payPalPayment();
            }
        });
    }

    private void payPalPayment() {
//        if(rideprize != 0.0 || rideprize > 0.0){
//        10 here is the ridePrize for a particullar ride.
//        rideprize variable changed to 10 for testing purposes
        PayPalPayment payment = new PayPalPayment(new BigDecimal("1"), "USD", "Ride Prize",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        }
// else{
//            Toast.makeText(Fake_Single_history.this,"Ride Prize Is $0.0",Toast.LENGTH_LONG).show();
//        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm != null){
                    try{
                        //getting the paypal response that tells if a user has paid or not
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());
                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            Toast.makeText(getApplicationContext(),"Payment Successful",Toast.LENGTH_LONG).show();

                            // saving the payment confirmation to database
                            historyRideInfoDb.child("customerPaid").setValue(true);
                            pay.setEnabled(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                Toast.makeText(getApplicationContext(),"Payment unsucessful",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void getUserInformation(String otheruserDriverOrCustomer, String otheruserID) {
        DatabaseReference motherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otheruserDriverOrCustomer).child(otheruserID);
        motherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name") != null){
                        username.setText("Your Ride With " + map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userphone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("name").toString()).into(userImge);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .key("AIzaSyAnUcHSUb1jZHI6I9JDrAEDH8Q71Tg0hwE")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng,destinationLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();
        return date;
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null){
            Toast.makeText(Fake_Single_history.this,"Error" + e.getMessage(),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Fake_Single_history.this,"Something Went Wrong, Try Again",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds  = builder.build();

        int width  = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width*0.2);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);

        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("PickUp Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin_)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines(){
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
    }
}
