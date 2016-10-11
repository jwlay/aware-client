package com.aware.utils;

/**
 * Created by denzilferreira on 16/02/16.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.R;
import com.aware.providers.Aware_Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Service that allows plugins/applications to send data to AWARE's dashboard study
 * Note: joins a study without requiring a QRCode, just the study URL
 */
public class StudyUtils extends IntentService {

    /**
     * Received broadcast to join a study
     */
    public static final String EXTRA_JOIN_STUDY = "study_url";

    public StudyUtils() {
        super("StudyUtils Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String full_url = intent.getStringExtra(EXTRA_JOIN_STUDY);

        if (Aware.DEBUG) Log.d(Aware.TAG, "Joining: " + full_url);

        Uri study_uri = Uri.parse(full_url);
        // New study URL, chopping off query parameters.
        String protocol = study_uri.getScheme();
        String study_host = protocol + "://" + study_uri.getHost();  // misnomer: protocol+host
        List<String> path_segments = study_uri.getPathSegments();

        String study_api_key = path_segments.get(path_segments.size() - 1);
        String study_id = path_segments.get(path_segments.size() - 2);

        String request;
        if (protocol.equals("https")) {
            SSLManager.handleUrl(getApplicationContext(), full_url, true);
            try {
                request = new Https(getApplicationContext(), SSLManager.getHTTPS(getApplicationContext(), full_url)).dataGET(study_host + "/index.php/webservice/client_get_study_info/" + study_api_key, true);
            } catch (FileNotFoundException e) {
                request = null;
            }
        } else {
            request = new Http(getApplicationContext()).dataGET(study_host + "/index.php/webservice/client_get_study_info/" + study_api_key, true);
        }

        if (request != null) {

            if (request.equals("[]")) return;

            try {
                JSONObject studyInfo = new JSONObject(request);

                //Request study settings
                Hashtable<String, String> data = new Hashtable<>();
                data.put(Aware_Preferences.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                data.put("platform", "android");
                try {
                    PackageInfo package_info = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                    data.put("package_name", package_info.packageName);
                    data.put("package_version_code", String.valueOf(package_info.versionCode));
                    data.put("package_version_name", String.valueOf(package_info.versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d(Aware.TAG, "Failed to put package info: " + e);
                    e.printStackTrace();
                }

                String answer;
                if (protocol.equals("https")) {
                    // Get SSL certs
                    SSLManager.handleUrl(getApplicationContext(), full_url, true);

                    try {
                        answer = new Https(getApplicationContext(), SSLManager.getHTTPS(getApplicationContext(), full_url)).dataPOST(full_url, data, true);
                    } catch (FileNotFoundException e) {
                        answer = null;
                    }
                } else {
                    answer = new Http(getApplicationContext()).dataPOST(full_url, data, true);
                }

                if (answer == null) {
                    Toast.makeText(getApplicationContext(), "Failed to connect to server, try again.", Toast.LENGTH_SHORT).show();
                    return;
                }


                JSONArray study_config = new JSONArray(answer);

                if (study_config.getJSONObject(0).has("message")) {
                    Toast.makeText(getApplicationContext(), "This study is no longer available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor dbStudy = Aware.getStudy(getApplicationContext(), full_url);
                if (Aware.DEBUG)
                    Log.d(Aware.TAG, DatabaseUtils.dumpCursorToString(dbStudy));

                if (dbStudy == null || !dbStudy.moveToFirst()) {
                    ContentValues studyData = new ContentValues();
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_JOINED, System.currentTimeMillis());
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_KEY, study_id);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_API, study_api_key);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_URL, full_url);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_PI, studyInfo.getString("researcher_first") + " " + studyInfo.getString("researcher_last") + "\nContact: " + studyInfo.getString("researcher_contact"));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_CONFIG, study_config.toString());
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_TITLE, studyInfo.getString("study_name"));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_DESCRIPTION, studyInfo.getString("study_description"));

                    getContentResolver().insert(Aware_Provider.Aware_Studies.CONTENT_URI, studyData);

                    if (Aware.DEBUG) {
                        Log.d(Aware.TAG, "New study data: " + studyData.toString());
                    }
                } else {
                    dbStudy.close();

                    //Update the information to the latest
                    ContentValues studyData = new ContentValues();
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_JOINED, System.currentTimeMillis());
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_KEY, study_id);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_API, study_api_key);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_URL, full_url);
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_PI, studyInfo.getString("researcher_first") + " " + studyInfo.getString("researcher_last") + "\nContact: " + studyInfo.getString("researcher_contact"));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_CONFIG, study_config.toString());
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_TITLE, studyInfo.getString("study_name"));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_DESCRIPTION, studyInfo.getString("study_description"));
                    studyData.put(Aware_Provider.Aware_Studies.STUDY_EXIT, 0); //make sure the user is on the study

                    getContentResolver().update(Aware_Provider.Aware_Studies.CONTENT_URI, studyData, Aware_Provider.Aware_Studies.STUDY_URL + " LIKE '" + full_url + "'", null);

                    if (Aware.DEBUG) {
                        Log.d(Aware.TAG, "Updated study data: " + studyData.toString());
                    }
                }

                applySettings(getApplicationContext(), study_config);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets first all the settings to the client.
     * If there are plugins, apply the same settings to them.
     * This allows us to add plugins to studies from the dashboard.
     *
     * @param context
     * @param configs
     */
    public static void applySettings(Context context, JSONArray configs) {

        boolean is_developer = Aware.getSetting(context, Aware_Preferences.DEBUG_FLAG).equals("true");

        //First reset the client to default settings...
        Aware.reset(context);

        if (is_developer) Aware.setSetting(context, Aware_Preferences.DEBUG_FLAG, true);

        //Now apply the new settings
        JSONArray plugins = new JSONArray();
        JSONArray sensors = new JSONArray();

        for (int i = 0; i < configs.length(); i++) {
            try {
                JSONObject element = configs.getJSONObject(i);
                if (element.has("plugins")) {
                    plugins = element.getJSONArray("plugins");
                }
                if (element.has("sensors")) {
                    sensors = element.getJSONArray("sensors");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Set the sensors' settings first
        for (int i = 0; i < sensors.length(); i++) {
            try {
                JSONObject sensor_config = sensors.getJSONObject(i);
                Aware.setSetting(context, sensor_config.getString("setting"), sensor_config.get("value"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Set the plugins' settings now
        ArrayList<String> active_plugins = new ArrayList<>();
        for (int i = 0; i < plugins.length(); i++) {
            try {
                JSONObject plugin_config = plugins.getJSONObject(i);

                String package_name = plugin_config.getString("plugin");
                active_plugins.add(package_name);

                JSONArray plugin_settings = plugin_config.getJSONArray("settings");
                for (int j = 0; j < plugin_settings.length(); j++) {
                    JSONObject plugin_setting = plugin_settings.getJSONObject(j);
                    Aware.setSetting(context, plugin_setting.getString("setting"), plugin_setting.get("value"), package_name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for(String package_name : active_plugins) {
            PackageInfo installed = PluginsManager.isInstalled(context, package_name);
            if (installed != null) {
                Aware.startPlugin(context, package_name);
            } else {
                Aware.downloadPlugin(context, package_name, false);
            }
        }

        //Send data to server
        Intent sync = new Intent(Aware.ACTION_AWARE_SYNC_DATA);
        context.sendBroadcast(sync);
    }
}