package com.findr.findr.config;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.TimeUnit;

public class LocationConfig {

    private int multiplier = 100000;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationConfig(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Get rough location using ACCESS_COARSE_LOCATION.
     * @return Point object containing latitude and longitude.
     * @throws SecurityException if permission is missing.
     * @throws Exception if location retrieval fails.
     */
    public Point getRoughLocation() throws Exception {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Permission ACCESS_COARSE_LOCATION is required.");
        }

        return getLocation();
    }

    /**
     * Get precise location using ACCESS_FINE_LOCATION.
     * @return Point object containing latitude and longitude.
     * @throws SecurityException if permission is missing.
     * @throws Exception if location retrieval fails.
     */
    public Point getPreciseLocation() throws Exception {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Permission ACCESS_FINE_LOCATION is required.");
        }

        return getLocation();
    }

    // Retrieves location synchronously
    private Point getLocation() throws Exception {
        @SuppressLint("MissingPermission")
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        Location location = Tasks.await(locationTask, 5, TimeUnit.SECONDS);

        if (location != null) {
            return new Point((int) location.getLatitude()*multiplier, (int) location.getLongitude()*multiplier);
        } else {
            throw new Exception("Unable to retrieve location");
        }
    }
}
