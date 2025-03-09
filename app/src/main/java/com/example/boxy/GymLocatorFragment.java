package com.example.boxy;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // For debug logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GymLocatorFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText etLocation;
    private Button btnSearch;
    private ImageButton btnBack;

    // Increase timeouts to reduce network-related failures.
    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // Replace with your Foursquare API Key (obtained from https://developer.foursquare.com/)
    private static final String FOURSQUARE_API_KEY = "fsq3iWXvcBQCaEABFX20iDu7iSRtKLbwfsj1koSdKImoYqI=";

    public GymLocatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        // Get references to the UI elements.
        etLocation = view.findViewById(R.id.et_location);
        btnSearch = view.findViewById(R.id.btn_search);
        btnBack = view.findViewById(R.id.btn_back);

        btnSearch.setOnClickListener(v -> searchLocation());

        // Set up the back button to pop the fragment from the back stack.
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Obtain the map fragment from the child fragment manager.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Map fragment not found", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void searchLocation() {
        String location = etLocation.getText().toString();
        if (location.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a location or postcode", Toast.LENGTH_SHORT).show();
            return;
        }
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                Log.d("GymLocator", "Geocoder lat: " + address.getLatitude() +
                        ", lon: " + address.getLongitude());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mMap.clear();
                // Add a test marker to verify marker functionality.
                mMap.addMarker(new MarkerOptions().position(latLng).title("Test Marker"));
                queryFoursquareGyms(latLng);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void queryFoursquareGyms(LatLng center) {
        // Foursquare search endpoint: using "gym" as the query.
        // You can also filter by gym chain name in the query parameter if needed.
        String url = "https://api.foursquare.com/v3/places/search?query=gym" +
                "&ll=" + center.latitude + "," + center.longitude +
                "&radius=20000&limit=50";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", FOURSQUARE_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(),
                                "Foursquare API error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.d("FoursquareResponse", "Response: " + jsonResponse);
                    parseFoursquareResponse(jsonResponse);
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getContext(),
                                    "Foursquare API error: " + response.message(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void parseFoursquareResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray results = jsonObject.getJSONArray("results");

            // Clear map markers on the main thread.
            new Handler(Looper.getMainLooper()).post(() -> mMap.clear());

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                // Foursquare returns geocodes under "geocodes.main"
                JSONObject geocodes = result.getJSONObject("geocodes");
                JSONObject mainGeo = geocodes.getJSONObject("main");
                double lat = mainGeo.getDouble("latitude");
                double lon = mainGeo.getDouble("longitude");

                String name = result.optString("name", "Gym");

                LatLng position = new LatLng(lat, lon);
                new Handler(Looper.getMainLooper()).post(() ->
                        mMap.addMarker(new MarkerOptions().position(position).title(name))
                );
            }
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(getContext(),
                            "Error parsing Foursquare response: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set a default location (Sydney) so that something is visible on load.
        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }
}
