package com.example.boxy;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GymLocatorFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText etLocation;
    private Button btnSearch;
    private OkHttpClient httpClient = new OkHttpClient();

    public GymLocatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        // Get references to the search bar UI elements
        etLocation = view.findViewById(R.id.et_location);
        btnSearch = view.findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(v -> searchLocation());

        // Obtain the map fragment from child fragment manager
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mMap.clear();
                // Query Overpass API for boxing gyms near this location
                queryOverpassForGyms(latLng);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void queryOverpassForGyms(LatLng center) {
        // Build an Overpass QL query: search for nodes tagged as gym with "boxing" in name within 5km.
        String query = "[out:json];"
                + "node[\"amenity\"=\"gym\"][\"name\"~\"boxing\", i](around:5000,"
                + center.latitude + "," + center.longitude + ");"
                + "out;";

        String url = "https://overpass-api.de/api/interpreter?data=" + query;
        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Overpass API error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    parseOverpassResponse(jsonResponse);
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getContext(), "Overpass API error: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void parseOverpassResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray elements = jsonObject.getJSONArray("elements");

            new Handler(Looper.getMainLooper()).post(() -> mMap.clear());
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                double lat = element.getDouble("lat");
                double lon = element.getDouble("lon");
                String name;
                JSONObject tags = element.optJSONObject("tags");
                if (tags != null && tags.has("name")) {
                    name = tags.getString("name");
                } else {
                    name = "Boxing Gym";
                }
                LatLng position = new LatLng(lat, lon);
                new Handler(Looper.getMainLooper()).post(() -> {
                    mMap.addMarker(new MarkerOptions().position(position).title(name));
                });
            }
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(getContext(), "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set a default location until the user searches
        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }
}
