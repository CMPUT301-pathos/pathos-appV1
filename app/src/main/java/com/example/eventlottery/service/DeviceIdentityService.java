package com.example.eventlottery.service;

import android.content.Context;
import android.provider.Settings;

/**
 * Service for retrieving the device's unique identifier.
 * Uses Android's ANDROID_ID, with a UUID fallback if unavailable.
 *
 * @author Dmitriy Limanets
 * @version 1.0
 */
public class DeviceIdentityService {

    /**
     * Retrieves the unique device identifier.
     * Falls back to a random UUID if ANDROID_ID is unavailable.
     *
     * @param context the application context
     * @return the unique device identifier string
     */
    public static String getDeviceId(Context context){
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null){
            id = java.util.UUID.randomUUID().toString();
        }
        return id;
    }
}
