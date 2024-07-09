package com.project2.hindtransit;

import android.content.Intent;
import android.location.Address;
import java.util.List;
import java.io.IOException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.pm.PackageManager;
import android.webkit.GeolocationPermissions;
import android.content.ComponentName;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.SearchView;
import android.location.Geocoder;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private WebView olaWebView;
    private WebView uberWebView;
    private WebView blablacarWebView;
    private Button olaButton;
    private Button uberButton;
    private Button blablacarButton;
    private Button metroButton;
    private GoogleMap map;
    private Marker originMarker;
    private Marker destinationMarker;
    private SearchView fromLocationSearchView;
    private SearchView toLocationSearchView;
    private SupportMapFragment mapFragment; // Declare mapFragment as a class variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        fromLocationSearchView = findViewById(R.id.fromLocationSearchView);
        toLocationSearchView = findViewById(R.id.toLocationSearchView);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        olaWebView = findViewById(R.id.olaWebView);
        uberWebView = findViewById(R.id.uberWebView);
        blablacarWebView = findViewById(R.id.blablacarWebView);
        olaButton = findViewById(R.id.olaButton);
        uberButton = findViewById(R.id.uberButton);
        blablacarButton = findViewById(R.id.blablacarButton);
        metroButton = findViewById(R.id.metroButton);
        Button mapsButton = findViewById(R.id.mapsButton);

        // Check and request location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                initializeWebViews();
            }
        } else {
            initializeWebViews();
        }

        // Set onClick listeners for the buttons to switch between WebViews
        olaButton.setOnClickListener(v -> {
            olaWebView.setVisibility(View.VISIBLE);
            uberWebView.setVisibility(View.GONE);
            blablacarWebView.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
        });

        uberButton.setOnClickListener(v -> {
            olaWebView.setVisibility(View.GONE);
            uberWebView.setVisibility(View.VISIBLE);
            blablacarWebView.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
        });

        blablacarButton.setOnClickListener(v -> {
            olaWebView.setVisibility(View.GONE);
            uberWebView.setVisibility(View.GONE);
            blablacarWebView.setVisibility(View.VISIBLE);
            mapFragment.getView().setVisibility(View.GONE);
        });

        // onClick listener for Metro button to open SearchActivity
        metroButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // onClick listener for Maps button
        mapsButton.setOnClickListener(v -> {
            olaWebView.setVisibility(View.GONE);
            uberWebView.setVisibility(View.GONE);
            blablacarWebView.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.VISIBLE); // Use android.view.View instead of View
        });



    // Set up search listeners
        fromLocationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        toLocationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query, false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Initialize map
        mapFragment.getMapAsync(this);
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeWebViews();
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    // Initialize WebView settings
    private void initializeWebViews() {
        // Load URLs into WebViews
        olaWebView.loadUrl("https://book.olacabs.com/?utm_source=book_now_top_right");
        uberWebView.loadUrl("https://m.uber.com/go/home");
        blablacarWebView.loadUrl("https://www.blablacar.in/");

        // Enable JavaScript for all WebViews
        WebSettings webSettings = olaWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true); // Enable Geolocation for Ola WebView

        webSettings = uberWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true); // Enable Geolocation for Uber WebView

        webSettings = blablacarWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true); // Enable Geolocation for BlaBlaCar WebView

        // Set WebView clients to open links in the app instead of the default browser
        olaWebView.setWebViewClient(new WebViewClient());
        uberWebView.setWebViewClient(new WebViewClient());
        blablacarWebView.setWebViewClient(new WebViewClient());

        // Set WebView ChromeClient to handle geolocation permission requests
        olaWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        uberWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        blablacarWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // Hide all WebViews initially
        olaWebView.setVisibility(WebView.GONE);
        uberWebView.setVisibility(WebView.GONE);
        blablacarWebView.setVisibility(WebView.GONE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Enable My Location layer if permission is granted
        map.setMyLocationEnabled(true);

        // Get last known location and move camera
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }
                });
    }

    private void searchLocation(String locationName, boolean isFromLocation) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                if (isFromLocation) {
                    if (originMarker != null) originMarker.remove();
                    originMarker = map.addMarker(new MarkerOptions().position(latLng).title("From: " + locationName));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    if (destinationMarker != null) destinationMarker.remove();
                    destinationMarker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("To: " + locationName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Set green marker icon
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
