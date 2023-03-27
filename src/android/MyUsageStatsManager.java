package com.podvodila.geopoll.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.ArrayList;
import android.widget.Toast;
import android.content.Context;


/**
 *
 */
public class MyUsageStatsManager extends CordovaPlugin {
    UsageStatsManager mUsageStatsManager;
    private static final String LOG_TAG = "MyUsageStatsManager";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mUsageStatsManager = (UsageStatsManager) this.cordova.getActivity().getApplicationContext().getSystemService("usagestats"); //Context.USAGE_STATS_SERVICE

        try {
            if (action.equals("openPermissionSettings")) {
                this.openPermissionSettings(callbackContext);

                return true;
            } else if (action.equals("getTimeInForeground")) {
                this.getTimeInForeground(args.getLong(0), args.getLong(1), callbackContext);

                return true;
            } else if (action.equals("getEvents")) {
                this.getEvents(args.getLong(0), args.getLong(1), callbackContext);

                return true;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "execute exception", e);

            throw e;
        }

        return false;
    }

    private void getEvents(long startTimestamp, long endTimestamp, CallbackContext callbackContext) {
        try {
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTimestamp, endTimestamp);

            HashMap<String, ArrayList<JSONObject>> packageEvents = new HashMap<>();

            while (usageEvents.hasNextEvent()) {
                Event currentEvent = new Event();
                usageEvents.getNextEvent(currentEvent);
                String packageName = currentEvent.getPackageName();

                if (currentEvent.getEventType() == Event.ACTIVITY_RESUMED ||
                    currentEvent.getEventType() == Event.ACTIVITY_PAUSED ||
                    currentEvent.getEventType() == Event.ACTIVITY_STOPPED) {
                    if (!packageEvents.containsKey(packageName)) {
                        packageEvents.put(packageName, new ArrayList<JSONObject>());
                    }

                    packageEvents
                        .get(packageName)
                        .add(this.eventToJSON(currentEvent));
                }
            }

            JSONObject resultObject = new JSONObject(packageEvents);

            callbackContext.success(resultObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
        }
    }

    private void getTimeInForeground(long startTimestamp, long endTimestamp, CallbackContext callbackContext) {
        try {
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTimestamp, endTimestamp);

            HashMap<String, ArrayList<Event>> packageEvents = new HashMap<>();
            HashMap<String, Long> result = new HashMap<>();

            while (usageEvents.hasNextEvent()) {
                Event currentEvent = new Event();
                usageEvents.getNextEvent(currentEvent);
                String packageName = currentEvent.getPackageName();

                if (currentEvent.getEventType() == Event.ACTIVITY_RESUMED ||
                    currentEvent.getEventType() == Event.ACTIVITY_PAUSED ||
                    currentEvent.getEventType() == Event.ACTIVITY_STOPPED) {
                    if (!packageEvents.containsKey(packageName)) {
                        packageEvents.put(packageName, new ArrayList<Event>());
                        result.put(packageName, (long)0);
                    }

                    packageEvents.get(packageName).add(currentEvent);
                }
            }

            for (String packageName : packageEvents.keySet()) {
                long latestResumeTimestamp = 0;
                long latestStopTimestamp = 0;

                for (Event event : packageEvents.get(packageName)) {
                    boolean shouldCalculate = false;

                    if (event.getEventType() == Event.ACTIVITY_PAUSED || event.getEventType() == Event.ACTIVITY_STOPPED) {
                        if (latestResumeTimestamp == 0) {
                            if (event.getEventType() == Event.ACTIVITY_STOPPED) {
                                continue;
                            }

                            latestResumeTimestamp = startTimestamp;
                            latestStopTimestamp = event.getTimeStamp();
                            shouldCalculate = true;
                        } else {
                            if (latestResumeTimestamp > latestStopTimestamp) {
                                shouldCalculate = true;
                            }

                            latestStopTimestamp = event.getTimeStamp();
                        }
                    } else if (event.getEventType() == Event.ACTIVITY_RESUMED) {
                        latestResumeTimestamp = event.getTimeStamp();
                    }

                    if (shouldCalculate) {
                        result.put(packageName, result.get(packageName) + (long)(latestStopTimestamp - latestResumeTimestamp));
                    }
                }

                if (latestResumeTimestamp > latestStopTimestamp) {
                    long currentTime = System.currentTimeMillis();

                    result.put(packageName, result.get(packageName) + (long)(
                        (currentTime > endTimestamp ? endTimestamp : currentTime) - latestResumeTimestamp)
                    );
                }
            }

            JSONObject resultObject = new JSONObject(result);

            callbackContext.success(resultObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
        }
    }

    public static JSONObject eventToJSON(Event event) throws Exception{
        JSONObject object = new JSONObject();
        object.put("className", event.getClassName());
        //object.put("configuration", event.getConfiguration());
        object.put("eventType", event.getEventType());
        object.put("timestamp", event.getTimeStamp());
        return object;
    }

    /**
     * Launch UsageStatsManager settings
     * @return
     */
    private void openPermissionSettings(CallbackContext callbackContext){
        try {

            Context context = this.cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            callbackContext.success("OK");

        } catch(Exception e){
            e.printStackTrace();
            callbackContext.error(e.toString());
        }

    }

}
