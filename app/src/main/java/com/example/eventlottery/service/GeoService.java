package com.example.eventlottery.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * GeoService
 *
 * Provides location-related helper methods for the Event Lottery application.
 *
 * Responsibilities:
 * - check whether the app currently has location permission
 * - check whether device location services are enabled
 * - retrieve the entrant's current device location when needed
 * - expose location data in a simple format that can later be stored with a
 *   wait-list join record
 *
 * Intended use:
 * - used when an entrant attempts to join the waiting list for an event that
 *   requires geolocation
 * - the caller is responsible for requesting runtime permissions before
 *   calling getCurrentLocation(...)
 *
 * Notes:
 * - this service does not request permissions itself
 * - this service returns either a fresh location update or a recent last-known
 *   location as a fallback
 *
 * Supports:
 * - US 02.02.02 indirectly by helping capture the join location that can later
 *   be shown on a map
 * - US 02.02.03 indirectly by supporting events that require geolocation
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class GeoService {

    private static final long LOCATION_TIMEOUT_MS = 10000L;
    private static final long FRESH_LOCATION_WINDOW_MS = 2 * 60 * 1000L;

    /**
     * Callback used when requesting the current device location.
     */
    public interface GeoCallback {
        void onSuccess(@NonNull LocationData locationData);
        void onFailure(@NonNull String errorMessage);
    }

    /**
     * Immutable location payload that can be attached to a wait-list record.
     */
    public static final class LocationData {
        private final double latitude;
        private final double longitude;
        private final float accuracyMeters;
        private final long capturedAtMillis;

        public LocationData(double latitude, double longitude,
                            float accuracyMeters, long capturedAtMillis) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyMeters = accuracyMeters;
            this.capturedAtMillis = capturedAtMillis;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public float getAccuracyMeters() {
            return accuracyMeters;
        }

        public long getCapturedAtMillis() {
            return capturedAtMillis;
        }

        /**
         * Convenience method for writing location data into Firestore later.
         */
        @NonNull
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("accuracyMeters", accuracyMeters);
            map.put("capturedAt", capturedAtMillis);
            return map;
        }

        @NonNull
        public static LocationData fromLocation(@NonNull Location location) {
            return new LocationData(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.hasAccuracy() ? location.getAccuracy() : 0f,
                    location.getTime()
            );
        }
    }

    /**
     * Returns true if the app currently has either fine or coarse location permission.
     */
    public boolean hasLocationPermission(@NonNull Context context) {
        return hasFineLocationPermission(context) || hasCoarseLocationPermission(context);
    }

    /**
     * Returns true if fine location permission is granted.
     */
    public boolean hasFineLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if coarse location permission is granted.
     */
    public boolean hasCoarseLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if either GPS or network location is enabled on the device.
     */
    public boolean isLocationEnabled(@NonNull Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            return false;
        }

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        return gpsEnabled || networkEnabled;
    }

    /**
     * Attempts to retrieve the device's current location.
     *
     * The caller must request runtime permissions before calling this method.
     * If a fresh update cannot be obtained in time, a recent last-known location
     * will be used as a fallback if available.
     */
    public void getCurrentLocation(@NonNull Context context,
                                   @NonNull GeoCallback callback) {
        if (!hasLocationPermission(context)) {
            callback.onFailure("Location permission not granted.");
            return;
        }

        if (!isLocationEnabled(context)) {
            callback.onFailure("Location services are disabled.");
            return;
        }

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            callback.onFailure("Location service unavailable.");
            return;
        }

        Location fallbackLocation = getBestLastKnownLocation(context, locationManager);

        if (fallbackLocation != null && isFreshEnough(fallbackLocation)) {
            callback.onSuccess(LocationData.fromLocation(fallbackLocation));
            return;
        }

        requestFreshLocation(context, locationManager, fallbackLocation, callback);
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation(@NonNull Context context,
                                      @NonNull LocationManager locationManager,
                                      @Nullable Location fallbackLocation,
                                      @NonNull GeoCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] completed = {false};
        final LocationListener[] listenerHolder = new LocationListener[1];

        Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (completed[0]) {
                    return;
                }

                completed[0] = true;
                removeUpdatesSafely(locationManager, listenerHolder[0]);

                if (fallbackLocation != null) {
                    callback.onSuccess(LocationData.fromLocation(fallbackLocation));
                } else {
                    callback.onFailure("Unable to determine device location.");
                }
            }
        };

        listenerHolder[0] = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (completed[0]) {
                    return;
                }

                completed[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                removeUpdatesSafely(locationManager, this);
                callback.onSuccess(LocationData.fromLocation(location));
            }
        };

        boolean requestedAtLeastOneProvider = false;

        try {
            if (hasFineLocationPermission(context)
                    && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        listenerHolder[0],
                        Looper.getMainLooper()
                );
                requestedAtLeastOneProvider = true;
            }

            if ((hasFineLocationPermission(context) || hasCoarseLocationPermission(context))
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        listenerHolder[0],
                        Looper.getMainLooper()
                );
                requestedAtLeastOneProvider = true;
            }
        } catch (SecurityException e) {
            callback.onFailure("Location permission not granted.");
            return;
        }

        if (!requestedAtLeastOneProvider) {
            if (fallbackLocation != null) {
                callback.onSuccess(LocationData.fromLocation(fallbackLocation));
            } else {
                callback.onFailure("No enabled location provider available.");
            }
            return;
        }

        handler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS);
    }

    @Nullable
    @SuppressLint("MissingPermission")
    private Location getBestLastKnownLocation(@NonNull Context context,
                                              @NonNull LocationManager locationManager) {
        Location best = null;

        try {
            if (hasFineLocationPermission(context)
                    && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                best = chooseBetterLocation(
                        best,
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                );
            }

            if ((hasFineLocationPermission(context) || hasCoarseLocationPermission(context))
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                best = chooseBetterLocation(
                        best,
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                );
            }

            if ((hasFineLocationPermission(context) || hasCoarseLocationPermission(context))
                    && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                best = chooseBetterLocation(
                        best,
                        locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                );
            }
        } catch (SecurityException ignored) {
            return null;
        }

        return best;
    }

    @Nullable
    private Location chooseBetterLocation(@Nullable Location currentBest,
                                          @Nullable Location candidate) {
        if (candidate == null) {
            return currentBest;
        }

        if (currentBest == null) {
            return candidate;
        }

        boolean candidateIsNewer = candidate.getTime() > currentBest.getTime();
        boolean candidateIsMoreAccurate =
                candidate.hasAccuracy()
                        && currentBest.hasAccuracy()
                        && candidate.getAccuracy() < currentBest.getAccuracy();

        if (candidateIsNewer) {
            return candidate;
        }

        if (candidateIsMoreAccurate) {
            return candidate;
        }

        return currentBest;
    }

    private boolean isFreshEnough(@NonNull Location location) {
        long age = Math.abs(System.currentTimeMillis() - location.getTime());
        return age <= FRESH_LOCATION_WINDOW_MS;
    }

    private void removeUpdatesSafely(@NonNull LocationManager locationManager,
                                     @Nullable LocationListener listener) {
        if (listener == null) {
            return;
        }

        try {
            locationManager.removeUpdates(listener);
        } catch (Exception ignored) {
        }
    }
}