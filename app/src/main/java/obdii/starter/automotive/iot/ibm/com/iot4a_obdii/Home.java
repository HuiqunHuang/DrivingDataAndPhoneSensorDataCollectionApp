/**
 * Copyright 2016,2019 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package obdii.starter.automotive.iot.ibm.com.iot4a_obdii;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.pires.obd.enums.ObdProtocols;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.AbstractVehicleDevice;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.AccessInfo;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.IVehicleDevice;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.Notification;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.NotificationHandler;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.Protocol;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.obd.EventDataGenerator;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.obd.ObdBridge;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.obd.ObdBridgeBluetooth;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.obd.ObdBridgeWifi;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.qrcode.SpecifyServer;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.settings.AppSettingsActivity;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.settings.SettingsFragment;
import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.CSVOperation;

import static com.ibm.iotf.client.api.APIClient.ContentType.json;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppConstants.DEFAULT_FREQUENCY_SEC;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppConstants.DOESNOTEXIST;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppConstants.MAX_FREQUENCY_SEC;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppConstants.MIN_FREQUENCY_SEC;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppIntents.BLUETOOTH_REQUEST;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppIntents.GPS_INTENT;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppIntents.SETTINGS_INTENT;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppIntents.SPECIFY_SERVER_INTENT;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppPermissions.INITIAL_LOCATION_PERMISSIONS;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.ObdAppPermissions.INITIAL_PERMISSIONS;
import static obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device.Protocol.HTTP;


public class Home extends AppCompatActivity implements LocationListener, SensorEventListener {
    private static final int OBD_SCAN_DELAY_MS = 200;

    private static final int BLUETOOTH_CONNECTION_RETRY_DELAY_MS = 100;
    private static final int BLUETOOTH_CONNECTION_RETRY_INTERVAL_MS = 5000;
    private static final int MAX_RETRY = 10;

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private String provider;
    private Location location = null;
    private boolean networkIntentNeeded = false;

    private String trip_id;

    private TextView moIdText;
    private ProgressBar progressBar;
    private ActionBar supportActionBar;
    private Button changeNetwork;
    private Button changeFrequency;
    //private Switch protocolSwitch;
    private ToggleButton sendButton, pauseButton;
    private TextView smartphoneAccText;
    private TextView smartphonegravityText;
    private TextView smartphoneOrientationText;
    private TextView smartphoneGyroscopeText;
    private TextView smartphoneRotationVectorText;
    private TextView vehicleHeadingText;
    private TextView gpsLongtitudeText;
    private TextView gpsLatitudeText;
    private TextView rpmText;
    private TextView vehicleSpeedText;
    private TextView engineOilText;
    private TextView engineCoolantText;
    private TextView fuelLevelText;
    private TextView smartphoneGravityAccText;
    private TextView smartphoneMagneticText;
    private TextView currentCSVFileNameText;
    private TextView pressureText;
    private TextView phoneLinearAccText;

    private String userDeviceAddress;
    private String userDeviceName;
    private int obd_timeout_ms;
    private ObdProtocols obd_protocol;

    /**
     * ATTENTION: support for ELM 327 WiFi dongle is experimental. With Android, it seems not possible to
     * have a WiFi connection to the dongle and an internet connection simultaneously.
     */
    private final boolean bluetooth_mode = true;
    private final ObdBridgeBluetooth obdBridgeBluetooth = new ObdBridgeBluetooth(this);
    private final ObdBridgeWifi obdBridgeWifi = new ObdBridgeWifi(this); // experimental
    private final ObdBridge obdBridge = bluetooth_mode ? obdBridgeBluetooth : obdBridgeWifi;

    private Protocol protocol = HTTP;
    private IVehicleDevice vehicleDevice;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> bluetoothConnectorHandle = null;

    private boolean initialized = false;
    private boolean deviceRegistryChecking = false;
    private boolean realOrSimuChecking = false;
    private boolean realOrSimuSelected = false; // Flag if user has already selected to use real obd device or simulation. It should be asked once per a app session.

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public String tripId = "";
    public List<List<String>> dataListForWritingCSV = null;
    public List<List<String>> secondaryDataListForWritingCSV = null;
    private String smartphoneAccX = "-";
    private String smartphoneAccY = "-";
    private String smartphoneAccZ = "-";
    private String smartphoneGravityX = "-";
    private String smartphoneGravityY = "-";
    private String smartphoneGravityZ = "-";
    private String smartphoneOrientationX = "-";
    private String smartphoneOrientationY = "-";
    private String smartphoneOrientationZ = "-";
    private String smartphoneGyroscopeX = "-";
    private String smartphoneGyroscopeY = "-";
    private String smartphoneGyroscopeZ = "-";
    private String smartphoneRotationVectorX = "-";
    private String smartphoneRotationVectorY = "-";
    private String smartphoneRotationVectorZ = "-";
    private float[] gravityValues = null;
    private float[] magneticValues = null;
    private String earthAccX = null;
    private String earthAccY = null;
    private String earthAccZ = null;
    private String magneticX = null;
    private String magneticY = null;
    private String magneticZ = null;
    private String pressure = null;
    private String smartphoneLinearAccX = "-";
    private String smartphoneLinearAccY = "-";
    private String smartphoneLinearAccZ = "-";
    private String earthLinearAccX= "-";
    private String earthLinearAccY = "-";
    private String earthLinearAccZ = "-";
    private String earthGyroscopeX= "-";
    private String earthGyroscopeY = "-";
    private String earthGyroscopeZ = "-";
    private String phoneRelativeRotationDegreeXToEarth = "-";
    private String phoneRelativeRotationDegreeYToEarth = "-";
    private String phoneRelativeRotationDegreeZToEarth = "-";

    private String currentDataSelectFrequency = "0.1";
    private Thread syncTask = null;
    private Thread writeCSVFileThread = null;
    private Timer timer = null;
    private Timer csvWwritingTimer = null;
    public String currentCSVFileName = "";
    public String serverIPAddress = "";
    private EditText et_ip_address;
    private String dataUploadPath = "http://"+serverIPAddress+":8080/OBDServer/receiveOBDData";

    void clean() {
        completeConnectingBluetoothDevice();
        if(scheduler != null){
            scheduler.shutdown();
        }
        if(vehicleDevice != null){
            vehicleDevice.clean();
        }
        if(obdBridge != null){
            obdBridge.clean();
        }
    }

    public static String getPresentTimeForFileName(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());

        return simpleDateFormat.format(date).toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String appUrl = getPreference(SettingsFragment.APP_SERVER_URL, null);
        String appUser = getPreference(SettingsFragment.APP_SERVER_USERNAME, null);
        String appPass = getPreference(SettingsFragment.APP_SERVER_PASSWORD, null);
        if(appUrl == null){
            API.useDefault();
        }else{
            API.doInitialize(appUrl, appUser, appPass);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        moIdText = (TextView)findViewById(R.id.mo_id);
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        progressBar.setScaleX(0.5f);
        progressBar.setScaleY(0.5f);


        vehicleHeadingText = (TextView)findViewById(R.id.headingValue);
        gpsLongtitudeText = (TextView)findViewById(R.id.longitudeValue);
        gpsLatitudeText = (TextView)findViewById(R.id.latitudeValue);
        rpmText = (TextView)findViewById(R.id.engineRPMValue);
        vehicleSpeedText = (TextView)findViewById(R.id.speedValue);
        engineOilText = (TextView)findViewById(R.id.engineOilValue);
        engineCoolantText = (TextView)findViewById(R.id.engineCoolantValue);
        fuelLevelText = (TextView)findViewById(R.id.fuelLevelValue);
        smartphoneAccText = (TextView)findViewById(R.id.smartphone_acc_value);
        smartphonegravityText = (TextView)findViewById(R.id.smartphone_gravity_value);
        smartphoneOrientationText = (TextView)findViewById(R.id.smartphone_orientation_value);
        smartphoneGyroscopeText = (TextView)findViewById(R.id.smartphone_gyroscope_value);
        smartphoneRotationVectorText  = (TextView)findViewById(R.id.smartphone_rotation_vector_value);
        et_ip_address = (EditText)findViewById(R.id.editTextDialogUserInput);
        smartphoneGravityAccText = (TextView)findViewById(R.id.smartphone_gravity_acc_value);
        smartphoneMagneticText = (TextView)findViewById(R.id.smartphone_magnetic_value);
        currentCSVFileNameText = (TextView)findViewById(R.id.trip_csv_file_name);
        pressureText = (TextView)findViewById(R.id.smartphone_pressure_value);
        phoneLinearAccText = (TextView)findViewById(R.id.smartphone_linearacc_value);

        supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayShowCustomEnabled(true);
        supportActionBar.setCustomView(progressBar);

        changeNetwork = (Button) findViewById(R.id.changeNetwork);
        changeFrequency = (Button) findViewById(R.id.changeFrequency);
        //protocolSwitch = (Switch) findViewById(R.id.protocolSwitch);
        changeNetwork.setVisibility(View.GONE);
        changeFrequency.setVisibility(View.GONE);
//        protocolSwitch.setVisibility(View.GONE);
//        protocolSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Protocol newProtocol = b ? Protocol.MQTT : HTTP;
//                if(newProtocol == protocol){
//                    return;
//                }
//                stopPublishingProbeData();
//                if(vehicleDevice != null){
//                    vehicleDevice.clean();
//                }
//
//                protocol = newProtocol;
//                setPreference(SettingsFragment.PROTOCOL, protocol.name());
//                final String endpoint = getPreference(protocol.prefName(SettingsFragment.ENDPOINT), null);
//                final String vendor = getPreference(protocol.prefName(SettingsFragment.VENDOR), null);
//                final String mo_id = getPreference(protocol.prefName(SettingsFragment.MO_ID), null);
//                if(endpoint == null || vendor == null || mo_id == null){
//                    vehicleDevice = null;
//                    checkDeviceRegistry(obdBridge.isSimulation());
//                }else{
//                    final String tenant_id = getPreference(protocol.prefName(SettingsFragment.TENANT_ID), "public");
//                    final String username = getPreference(protocol.prefName(SettingsFragment.USERNAME), null);
//                    final String password = getPreference(protocol.prefName(SettingsFragment.PASSWORD), null);
//                    AccessInfo accessInfo = AbstractVehicleDevice.createAccessInfo(endpoint, tenant_id, vendor, mo_id, username, password);
//                    final String userAgent = getPreference(protocol.prefName(SettingsFragment.USER_AGENT), null);
//                    if(userAgent != null){
//                        accessInfo.put(AccessInfo.ParamName.USER_AGENT, userAgent);
//                    }
//                    vehicleDevice = protocol.createDevice(accessInfo);
//                    deviceRegistered(obdBridge.isSimulation());
//                }
//            }
//        });

        sendButton = (ToggleButton) findViewById(R.id.send_btn);
        pauseButton = (ToggleButton)findViewById(R.id.pause_btn);
        sendButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        startNewTrip();
    }

    public void startNewTrip(){
        checkMQTTAvailable();
        obdBridge.initializeObdParameterList(this);
        startApp();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        createFolderToSaveSCVData();
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
    }

    private void createFolderToSaveSCVData(){
        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CSVData");
        boolean hasDir = fileDir.exists();
        if (!hasDir) {
            fileDir.mkdirs();// 这里创建的是目录
        }
    }

    private void updateDatalistForWritingCSV(){
        if(dataListForWritingCSV == null){
            dataListForWritingCSV = new ArrayList<>();
        }else{
            List<String> list = new ArrayList<>();
            list.add(tripId);
            list.add(vehicleHeadingText.getText().toString());
            list.add(location.getLongitude()+"");
            list.add(location.getLatitude()+"");
            list.add(rpmText.getText().toString());
            list.add(vehicleSpeedText.getText().toString());
            list.add(engineOilText.getText().toString());
            list.add(engineCoolantText.getText().toString());
            list.add(fuelLevelText.getText().toString());
            list.add(smartphoneAccX);
            list.add(smartphoneAccY);
            list.add(smartphoneAccZ);
            list.add(smartphoneGravityX);
            list.add(smartphoneGravityY);
            list.add(smartphoneGravityZ);
            list.add(smartphoneOrientationX);
            list.add(smartphoneOrientationY);
            list.add(smartphoneOrientationZ);
            list.add(smartphoneGyroscopeX);
            list.add(smartphoneGyroscopeY);
            list.add(smartphoneGyroscopeZ);
            list.add(smartphoneRotationVectorX);
            list.add(smartphoneRotationVectorY);
            list.add(smartphoneRotationVectorZ);
            list.add(earthAccX);
            list.add(earthAccY);
            list.add(earthAccZ);
            list.add(magneticX);
            list.add(magneticY);
            list.add(magneticZ);
            list.add(pressure);
            list.add(smartphoneLinearAccX);
            list.add(smartphoneLinearAccY);
            list.add(smartphoneLinearAccZ);
            list.add(phoneRelativeRotationDegreeXToEarth);
            list.add(phoneRelativeRotationDegreeYToEarth);
            list.add(phoneRelativeRotationDegreeZToEarth);
            list.add(earthLinearAccX);
            list.add(earthLinearAccY);
            list.add(earthLinearAccZ);
            list.add(earthGyroscopeX);
            list.add(earthGyroscopeY);
            list.add(earthGyroscopeZ);
            list.add(getPresentTime());
            list.add(currentDataSelectFrequency);
            dataListForWritingCSV.add(list);
        }
    }

    private void initSensorTv() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onSensorChanged(SensorEvent event) {
        DecimalFormat decimalFormat=new DecimalFormat("#.00");
        //String str=data.format(avg);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                smartphoneAccX = event.values[0] + "";
                smartphoneAccY = event.values[1] + "";
                smartphoneAccZ = event.values[2] + "";
                String accelerometer = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphoneAccText.setText(accelerometer);
                if ((gravityValues != null) && (magneticValues != null)){
                    float[] deviceRelativeAcceleration = new float[4];
                    deviceRelativeAcceleration[0] = event.values[0];
                    deviceRelativeAcceleration[1] = event.values[1];
                    deviceRelativeAcceleration[2] = event.values[2];
                    deviceRelativeAcceleration[3] = 0;
                    // Change the device relative acceleration values to earth relative values
                    // X axis -> East
                    // Y axis -> North Pole
                    // Z axis -> Sky
                    float[] R = new float[16], I = new float[16], earthAcc = new float[16];
                    SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);
                    float[] inv = new float[16];
                    android.opengl.Matrix.invertM(inv, 0, R, 0);
                    android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                    earthAccX = earthAcc[0] + "";
                    earthAccY = earthAcc[1] + "";
                    earthAccZ = earthAcc[2] + "";
                    String earthAccStr = "X:" + decimalFormat.format(earthAcc[0]) + " " + "Y:" + decimalFormat.format(earthAcc[1]) + " " + "Z:" + decimalFormat.format(earthAcc[2]);
                    smartphoneGravityAccText.setText(earthAccStr);
                    //Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");

                    float[] r = new float[9];
                    float[] result = new float[3];
                    // r从这里返回
                    SensorManager.getRotationMatrix(r, null, gravityValues, magneticValues);
                    //values从这里返回
                    SensorManager.getOrientation(r, result);
                    //提取数据
                    phoneRelativeRotationDegreeXToEarth = Math.toDegrees(event.values[0]) + "";
                    phoneRelativeRotationDegreeYToEarth = Math.toDegrees(event.values[1]) + "";
                    phoneRelativeRotationDegreeZToEarth = Math.toDegrees(event.values[2]) + "";
                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                smartphoneLinearAccX = event.values[0] + "";
                smartphoneLinearAccY = event.values[1] + "";
                smartphoneLinearAccZ = event.values[2] + "";
                String acc = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                phoneLinearAccText.setText(acc);
                if ((gravityValues != null) && (magneticValues != null)){
                    float[] deviceRelativeAcceleration = new float[4];
                    deviceRelativeAcceleration[0] = event.values[0];
                    deviceRelativeAcceleration[1] = event.values[1];
                    deviceRelativeAcceleration[2] = event.values[2];
                    deviceRelativeAcceleration[3] = 0;
                    // Change the device relative acceleration values to earth relative values
                    // X axis -> East
                    // Y axis -> North Pole
                    // Z axis -> Sky
                    float[] R = new float[16], I = new float[16], earthAcc = new float[16];
                    SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);
                    float[] inv = new float[16];
                    android.opengl.Matrix.invertM(inv, 0, R, 0);
                    android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                    earthLinearAccX = earthAcc[0] + "";
                    earthLinearAccY = earthAcc[1] + "";
                    earthLinearAccZ = earthAcc[2] + "";
                    //Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
                }
                break;
            case Sensor.TYPE_ORIENTATION:
                smartphoneOrientationX = event.values[0] + "";
                smartphoneOrientationY = event.values[1] + "";
                smartphoneOrientationZ = event.values[2] + "";
                String orientation = "X:" + decimalFormat.format(event.values[0]) + " " + "Y: " + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphoneOrientationText.setText(orientation);
                break;
            case Sensor.TYPE_GRAVITY:
                gravityValues = event.values;
                smartphoneGravityX = event.values[0] + "";
                smartphoneGravityY = event.values[1] + "";
                smartphoneGravityZ = event.values[2] + "";
                String gravity = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphonegravityText.setText(gravity);
                break;
            case Sensor.TYPE_GYROSCOPE:
                smartphoneGyroscopeX = event.values[0] + "";
                smartphoneGyroscopeY = event.values[1] + "";
                smartphoneGyroscopeZ = event.values[2] + "";
                String gyroscope = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphoneGyroscopeText.setText(gyroscope);
                if ((gravityValues != null) && (magneticValues != null)){
                    float[] deviceRelativeAcceleration = new float[4];
                    deviceRelativeAcceleration[0] = event.values[0];
                    deviceRelativeAcceleration[1] = event.values[1];
                    deviceRelativeAcceleration[2] = event.values[2];
                    deviceRelativeAcceleration[3] = 0;
                    // Change the device relative acceleration values to earth relative values
                    // X axis -> East
                    // Y axis -> North Pole
                    // Z axis -> Sky
                    float[] R = new float[16], I = new float[16], earthAcc = new float[16];
                    SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);
                    float[] inv = new float[16];
                    android.opengl.Matrix.invertM(inv, 0, R, 0);
                    android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                    earthGyroscopeX = earthAcc[0] + "";
                    earthGyroscopeY = earthAcc[1] + "";
                    earthGyroscopeZ = earthAcc[2] + "";
                    //Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                smartphoneRotationVectorX = event.values[0] + "";
                smartphoneRotationVectorY = event.values[1] + "";
                smartphoneRotationVectorZ = event.values[2] + "";
                String rotation = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphoneRotationVectorText.setText(rotation);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values;
                magneticX = event.values[0] + "";
                magneticY = event.values[1] + "";
                magneticZ = event.values[2] + "";
                String magnetic = "X:" + decimalFormat.format(event.values[0]) + " " + "Y:" + decimalFormat.format(event.values[1]) + " " + "Z:" + decimalFormat.format(event.values[2]);
                smartphoneMagneticText.setText(magnetic);
                break;
            case Sensor.TYPE_PRESSURE:
                pressure = event.values[0] +"";
                pressureText.setText(pressure);
                break;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void checkMQTTAvailable(){
        API.checkMQTTAvailable(new API.TaskListener() {
            @Override
            public void postExecute(API.Response result) throws JsonSyntaxException {
                if(result != null && result.getStatusCode() == 200){
                    boolean available = result.getBody().get("available").getAsBoolean();
                    if(!available){
                        protocol = HTTP;
                    }
                    //protocolSwitch.setVisibility(available ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    public String getPresentTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(System.currentTimeMillis());

        return simpleDateFormat.format(date).toString();
    }


    private void startApp() {
        //initAppServer();
        if(vehicleDevice == null || !vehicleDevice.hasValidAccessInfo()){
            final String publishProtocol = getPreference(SettingsFragment.PROTOCOL, HTTP.name());
            this.protocol = Protocol.valueOf(publishProtocol.toUpperCase());
            //protocolSwitch.setChecked(protocol == Protocol.MQTT);
            final String vendor = getPreference(protocol.prefName(SettingsFragment.VENDOR), null);
            final String mo_id = getPreference(protocol.prefName(SettingsFragment.MO_ID), null);
            final String endpoint = getPreference(protocol.prefName(SettingsFragment.ENDPOINT), null);
            final String tenant_id = getPreference(protocol.prefName(SettingsFragment.TENANT_ID), "public");
            final String username = getPreference(protocol.prefName(SettingsFragment.USERNAME), null);
            final String password = getPreference(protocol.prefName(SettingsFragment.PASSWORD), null);
            final String userAgent = getPreference(protocol.prefName(SettingsFragment.USER_AGENT), null);
            if(endpoint != null && !endpoint.isEmpty() && vendor != null && !vendor.isEmpty() && mo_id != null && !mo_id.isEmpty()){
                AccessInfo accessInfo = AbstractVehicleDevice.createAccessInfo(endpoint, tenant_id, vendor, mo_id, username, password);
                if(userAgent != null){
                    accessInfo.put(AccessInfo.ParamName.USER_AGENT, userAgent);
                }
                try{
                    vehicleDevice = protocol.createDevice(accessInfo);
                    initialized = true;
                }catch(Exception e){
                    Toast.makeText(Home.this, "Cannot create device to connect to !", Toast.LENGTH_LONG).show();
                    Home.this.finishAffinity();
                }
            }
        }
        startApp2();
    }

    private void initAppServer() {
        String appServer = getPreference(SettingsFragment.APP_SERVER_URL, null);
        if(appServer == null){
            startSpecifyServerActivity();
        }
    }

    private void startApp2() {
        // this has to be called in the UI thread

        if (setLocationInformation() == false) {
            // GPS and/or Network is disabled
            return;
        }
        if (!obdBridgeBluetooth.setupBluetooth()) {
            // when bluetooth is not available (e.g. Android Studio simulator)
            final boolean doNotRunSimulationWithoutBluetooth = false; // testing purpose
            if (doNotRunSimulationWithoutBluetooth) {
                // terminate the app
                Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth!", Toast.LENGTH_LONG).show();
                setChangeNetworkEnabled(false);
                setChangeFrequencyEnabled(false);
                showStatus("Bluetooth Failed");
            } else {
                // force to run in simulation mode for testing purpose
                Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth! Will be running in Simulation Mode", Toast.LENGTH_LONG).show();
                runSimulatedObdScan();
                showStatus("Simulated OBD Scan");
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final String[] permissionsArray = getPermissionsNeeded();
                if (permissionsArray != null) {
                    requestPermissions(permissionsArray, INITIAL_PERMISSIONS);
                } else {
                    permissionsGranted();
                }

            } else {
                if (!wasWarningShown()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setTitle("Warning")
                            .setMessage("This app requires permissions to your Locations, Bluetooth and Storage settings.\n\n" +
                                    "If you are running the application to your phone from Android Studio, you will not be able to allow these permissions.\n\n" +
                                    "If that is the case, please install the app through the provided APK file."
                            )
                            .setPositiveButton("Ok", null)
                            .show();
                }
                permissionsGranted();
            }
        }
    }

    private String[] getPermissionsNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Map<String, String> permissions = new HashMap<>();
            final ArrayList<String> permissionNeeded = new ArrayList<>();

//            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
//                permissions.put("internet", Manifest.permission.INTERNET);

//            if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
//                permissions.put("networkState", Manifest.permission.ACCESS_NETWORK_STATE);

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.put("coarseLocation", Manifest.permission.ACCESS_COARSE_LOCATION);

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.put("fineLocation", Manifest.permission.ACCESS_FINE_LOCATION);

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                permissions.put("bluetooth", Manifest.permission.BLUETOOTH_ADMIN);

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissions.put("write", Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissions.put("read", Manifest.permission.READ_EXTERNAL_STORAGE);

            for (Map.Entry<String, String> entry : permissions.entrySet()) {
                permissionNeeded.add(entry.getValue());
            }
            if (permissionNeeded.size() > 0) {
                Object[] tempObjectArray = permissionNeeded.toArray();
                String[] permissionsArray = Arrays.copyOf(tempObjectArray, tempObjectArray.length, String[].class);

                return permissionsArray;

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void startSettingsActivity() {
        final Intent intent = new Intent(this, AppSettingsActivity.class);
        String appUrl = getPreference(SettingsFragment.APP_SERVER_URL, null);
        intent.putExtra(SettingsFragment.APP_SERVER_URL, appUrl);
        String appUser = getPreference(SettingsFragment.APP_SERVER_USERNAME, null);
        intent.putExtra(SettingsFragment.APP_SERVER_USERNAME, appUser);
        String appPass = getPreference(SettingsFragment.APP_SERVER_PASSWORD, null);
        intent.putExtra(SettingsFragment.APP_SERVER_PASSWORD, appPass);
        String device_id = "";
        try {
            device_id = obdBridge.getDeviceId(obdBridge.isSimulation(), getUUID());
        } catch (DeviceNotConnectedException e) {
            device_id = "";
        }
        intent.putExtra(SettingsFragment.BLUETOOTH_DEVICE_ID, device_id);
        intent.putExtra(SettingsFragment.BLUETOOTH_DEVICE_ADDRESS, obdBridgeBluetooth.getUserDeviceAddress());
        intent.putExtra(SettingsFragment.BLUETOOTH_DEVICE_NAME, obdBridgeBluetooth.getUserDeviceName());
        intent.putExtra(SettingsFragment.UPLOAD_FREQUENCY, "" + getUploadFrequencySec());
        intent.putExtra(SettingsFragment.OBD_TIMEOUT_MS, "" + getObdTimeoutMs());
        intent.putExtra(SettingsFragment.OBD_PROTOCOL, "" + getObdProtocol());

        startActivityForResult(intent, SETTINGS_INTENT);
    }

    private void startSpecifyServerActivity(){
        final Intent intent = new Intent(Home.this, SpecifyServer.class);
        startActivityForResult(intent, SPECIFY_SERVER_INTENT);
    }

    private void checkSettingsOnResume() {
        final String endpoint = getPreference(protocol.prefName(SettingsFragment.ENDPOINT), null);
        final String vendor = getPreference(protocol.prefName(SettingsFragment.VENDOR), null);
        final String mo_id = getPreference(protocol.prefName(SettingsFragment.MO_ID), null);
        final String username = getPreference(protocol.prefName(SettingsFragment.USERNAME), null);
        final String password = getPreference(protocol.prefName(SettingsFragment.PASSWORD), null);
        AccessInfo accessInfo = vehicleDevice != null ? vehicleDevice.getAccessInfo() : null;
        String currentEndpoint = accessInfo != null ? accessInfo.get(AccessInfo.ParamName.ENDPOINT) : null;
        if (currentEndpoint == null || !currentEndpoint.equals(endpoint)) {
//            restartApp(endpoint, vendor, mo_id, username, password);
        } else if (!obdBridge.isSimulation()) {
            final int obd_timeout_ms = Integer.parseInt(getPreference(SettingsFragment.OBD_TIMEOUT_MS, "" + ObdBridge.DEFAULT_OBD_TIMEOUT_MS));
            final ObdProtocols obd_protocol = ObdProtocols.valueOf(getPreference(SettingsFragment.OBD_PROTOCOL, ObdBridge.DEFAULT_OBD_PROTOCOL.name()));
            if (!obdBridge.isCurrentObdTimeoutSameAs(obd_timeout_ms) || !obdBridge.isCurrentObdProtocolSameAs(obd_protocol)) {
                runRealObdScan(obd_timeout_ms, obd_protocol);
            }
        }
    }

    private void showStatus(final String msg) {
        if (supportActionBar == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                supportActionBar.setTitle(msg);
            }
        });
    }

    private void setChangeNetworkEnabled(final boolean enableChangeNetwork) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeNetwork.setEnabled(enableChangeNetwork);
            }
        });
    }

    private void setChangeFrequencyEnabled(final boolean enableChangeFrequncy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeFrequency.setEnabled(enableChangeFrequncy);
            }
        });
    }

    private void showStatus(final String msg, final int progressBarVisibility) {
        if (progressBar == null || supportActionBar == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                supportActionBar.setTitle(msg);
                progressBar.setVisibility(progressBarVisibility);
            }
        });
    }

    private void showToastText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Home.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenu_1:
                startSettingsActivity();
                return true;
            case R.id.specifyServerMenu:
                startSpecifyServerActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if(vehicleDevice != null){
            vehicleDevice.stopPublishing();
        }
        if(obdBridge != null){
            obdBridge.stopObdScan();
        }
        super.onDestroy();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        switch (requestCode) {
            case INITIAL_PERMISSIONS:
                if (results[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted();
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case INITIAL_LOCATION_PERMISSIONS:
                if (results[0] == PackageManager.PERMISSION_GRANTED) {
                    if (provider == null)
                        provider = locationManager.getBestProvider(new Criteria(), false);
                    setChangeNetworkEnabled(false);
                    checkDeviceRegistry(true);
                    setChangeFrequencyEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, results);
        }
    }
    final Context context = this;
    private void permissionsGranted() {
        System.out.println("PERMISSIONS GRANTED");
        showObdScanModeDialog();
    }

    private void showObdScanModeDialog() {
        // allows the user to select real OBD Scan mode or Simulation mode
        if(realOrSimuChecking){
            return;
        }
        if(realOrSimuSelected){
            if(obdBridge.isSimulation()){
                runSimulatedObdScan();
            }else{
                runRealObdScan();
            }
        }else{
            realOrSimuChecking = true;
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.prompts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //et_ip_address.setText(userInput.getText());
                                    serverIPAddress = userInput.getText().toString().replace(" ","");
                                    dataUploadPath = "http://"+serverIPAddress+":8080/OBDServer/receiveOBDData";
                                    dialog.dismiss();
                                    dataSelectfrequencySelect(1);
                                }
                            })
                    .setNegativeButton("No, I have a real OBDII Dongle",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //et_ip_address.setText(userInput.getText());
                                    serverIPAddress = userInput.getText().toString().replace(" ","");
                                    dataUploadPath = "http://"+serverIPAddress+":8080/OBDServer/receiveOBDData";
                                    dialog.dismiss();
                                    dataSelectfrequencySelect(2);
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }
    }


    public void showDialog(){
        final AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        alertDialog1.setCancelable(false);
        alertDialog1.setTitle("NEW TRIP.\nWanna try out a simulated version?");
        alertDialog1.setPositiveButton("Yes", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dataSelectfrequencySelect(1);
            }
        });
        alertDialog1.setNegativeButton("No, I have a real OBDII Dongle", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dataSelectfrequencySelect(2);
            }
        });
        alertDialog1.show();
    }

    private void dataSelectfrequencySelect(final int realModeOrSimulatedMode){
        AlertDialog.Builder dataSelectFrequencyBuilder = new AlertDialog.Builder(this);
        dataSelectFrequencyBuilder.setTitle("Please select data select frequency(second)");
        final String[] items = new String[]{"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1"};
        dataSelectFrequencyBuilder.setSingleChoiceItems(items, -1, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(realModeOrSimulatedMode == 1){
                    obdBridge.setIsSimulation(true);
                    realOrSimuSelected = true;
                    realOrSimuChecking = false;
                }else{
                    obdBridge.setIsSimulation(false);
                    realOrSimuSelected = true;
                    realOrSimuChecking = false;
                }
                currentDataSelectFrequency = items[which];

                if( currentCSVFileName == "" || currentCSVFileName == null){
                    currentCSVFileName = getPresentTimeForFileName();
                    currentCSVFileNameText.setText("Trip csv: "+currentCSVFileName+".csv");
                }
                tripId = getPresentTimeForFileName();
                Toast.makeText(getApplicationContext(), "Start recording new trip data into:"+currentCSVFileName+".csv", Toast.LENGTH_SHORT).show();
                if(realModeOrSimulatedMode == 1){
                    runSimulatedObdScan();
                }else{
                    runRealObdScan();
                }
            }
        });
        dataSelectFrequencyBuilder.show();
    }


    private void runSimulatedObdScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, INITIAL_LOCATION_PERMISSIONS);
                return;
            }
        }
        setChangeNetworkEnabled(false);
        checkDeviceRegistry(true);
        setChangeFrequencyEnabled(true);
        startDataOperation();
    }

    private void startDataOperation(){
        initSensorTv();
        float selectFre = Float.parseFloat(currentDataSelectFrequency);
        final int frequency = (int)(selectFre*1000);
        syncTask = new Thread(new Runnable() {
            @Override
            public void run() {
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // (1) 使用handler发送消息
                        Message message=new Message();
                        message.what=0;
                        mHandler.sendMessage(message);
                    }
                    //},0,Integer.parseInt(frequency*1000+""));
                },0,frequency);
            }
        });
        syncTask.start();
        writeCSVFileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                csvWwritingTimer=new Timer();
                csvWwritingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // (1) 使用handler发送消息
                        Message message=new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                },0,1*60*1000);
            }
        });
        writeCSVFileThread.start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    updateDatalistForWritingCSV();
                    break;
                case 1:
                    if (dataListForWritingCSV!=null){
                        secondaryDataListForWritingCSV = new ArrayList<>();
                        secondaryDataListForWritingCSV = dataListForWritingCSV;
                        dataListForWritingCSV = null;
                        CSVOperation.writeDataIntoCSV2(secondaryDataListForWritingCSV,currentCSVFileName);
                        //Toast.makeText(getApplicationContext(), "Writing data in to file:"+currentCSVFileName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void runRealObdScan() {
        final int obd_timeout_ms = getObdTimeoutMs();
        final ObdProtocols obd_protocol = getObdProtocol();
        runRealObdScan(obd_timeout_ms, obd_protocol);
    }

    private void runRealObdScan(final int obd_timeout_ms, final ObdProtocols obd_protocol) {
        obdBridge.stopObdScan();

        if (obdBridge instanceof ObdBridgeBluetooth) {
            runRealObdBluetoothScan(obd_timeout_ms, obd_protocol);
        } else if (obdBridge instanceof ObdBridgeWifi) {
            runRealObdWifiScan(obd_timeout_ms, obd_protocol);
        }
    }

    private void runRealObdWifiScan(final int obd_imeout_ms, final ObdProtocols obd_protocol) {
        final String address = "192.168.0.10";
        final int port = 35000;
        final boolean connected = obdBridgeWifi.connectSocket(address, port, obd_imeout_ms, obd_protocol);
        System.out.println("WI-FI CONNECTED " + connected);
        if (connected) {
            showStatus("Connected to " + address + ":" + port, View.GONE);
            startObdScan(false);
            //checkDeviceRegistry(false);
        } else {
            showStatus("Connection Failed for " + address + ":" + port, View.GONE);
        }
    }

    private void runRealObdBluetoothScan(final int obd_timeout_ms, final ObdProtocols obd_protocol) {
        if (!obdBridgeBluetooth.isBluetoothEnabled()) {
            final Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, BLUETOOTH_REQUEST);
            return;
        }
        final Set<BluetoothDevice> pairedDevicesSet = obdBridgeBluetooth.getPairedDeviceSet();

        // In case user clicks on Change Network, need to repopulate the devices list
        final ArrayList<String> deviceNames = new ArrayList<>();
        final ArrayList<String> deviceAddresses = new ArrayList<>();
        if (pairedDevicesSet != null && pairedDevicesSet.size() > 0) {
            for (BluetoothDevice device : pairedDevicesSet) {
                deviceNames.add(device.getName());
                deviceAddresses.add(device.getAddress());
            }
            final String preferredName = getPreference(SettingsFragment.BLUETOOTH_DEVICE_NAME, "obd").toLowerCase();
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
            final ArrayAdapter adapter = new ArrayAdapter(Home.this, android.R.layout.select_dialog_singlechoice, deviceNames.toArray(new String[deviceNames.size()]));
            int selectedDevice = -1;
            for (int i = 0; i < deviceNames.size(); i++) {
                if (deviceNames.get(i).toLowerCase().contains(preferredName)) {
                    selectedDevice = i;
                }
            }
            alertDialog
                    .setCancelable(false)
                    .setSingleChoiceItems(adapter, selectedDevice, null)
                    .setTitle("Please Choose the OBDII Bluetooth Device")
                    .setPositiveButton("Ok", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            final String deviceAddress = deviceAddresses.get(position);
                            final String deviceName = deviceNames.get(position);
                            startConnectingBluetoothDevice(deviceAddress, deviceName, obd_timeout_ms, obd_protocol);
                            setPreference(SettingsFragment.BLUETOOTH_DEVICE_NAME, deviceName);
                        }
                    })
                    .show();
        } else {
            Toast.makeText(getApplicationContext(), "Please pair with your OBDII device and restart the application!", Toast.LENGTH_SHORT).show();
        }
    }

    private synchronized void startConnectingBluetoothDevice(final String userDeviceAddress, final String userDeviceName, final int obd_timeout_ms, final ObdProtocols obd_protocol) {
        completeConnectingBluetoothDevice(); // clean up previous try

        // save for reconnection later
        this.userDeviceAddress = userDeviceAddress;
        this.userDeviceName = userDeviceName;
        this.obd_timeout_ms = obd_timeout_ms;
        this.obd_protocol = obd_protocol;

        final int[] retryCount = new int[1];
        retryCount[0] = 0;
        Log.i("BT Connection Task", "STARTED");
        System.out.println("BT Connection Task: STARTED");
        showStatus("Connecting to \"" + userDeviceName + "\"", View.VISIBLE);
        bluetoothConnectorHandle = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (obdBridgeBluetooth.connectBluetoothSocket(userDeviceAddress, userDeviceName, obd_timeout_ms, obd_protocol)) {
                    showStatus("Connected to \"" + userDeviceName + "\"", View.GONE);
                    checkDeviceRegistry(false);
                    completeConnectingBluetoothDevice();
                    startDataOperation();
                } else if (++retryCount[0] > MAX_RETRY) {
                    showToastText("Unable to connect to the device, please make sure to choose the right network");
                    showStatus("Connection Failed", View.GONE);
                    completeConnectingBluetoothDevice();
                } else {
                    showStatus("Retry [" + retryCount[0] + "] Connecting to \"" + userDeviceName + "\"", View.VISIBLE);
                }
            }
        }, BLUETOOTH_CONNECTION_RETRY_DELAY_MS, BLUETOOTH_CONNECTION_RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private synchronized void completeConnectingBluetoothDevice() {
        if (bluetoothConnectorHandle != null) {
            bluetoothConnectorHandle.cancel(true);
            bluetoothConnectorHandle = null;
            Log.i("BT Connection Task", "ENDED");
            System.out.println("BT Connection Task: ENDED");
        }
    }

    private void checkDeviceRegistry(final boolean simulation) {
//        if(deviceRegistryChecking){
//            return;
//        }
        deviceRegistryChecking = true;
        setLocationInformation();

        try {
            showStatus("Checking Device Registration", View.VISIBLE);

            final String uuid = getUUID();
            final String device_id = obdBridge.getDeviceId(simulation, uuid);
            if(vehicleDevice == null || !vehicleDevice.hasValidAccessInfo()){
                API.getDeviceAccessInfo(uuid, protocol, new API.TaskListener() {
                    @Override
                    public void postExecute(API.Response result) throws JsonSyntaxException {
                        onDeviceRegistrationChecked(result.getStatusCode(), result.getBody(), simulation);
                    }
                });
            }else{
                deviceRegistered(obdBridge.isSimulation());
            }
        } catch (DeviceNotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void onDeviceRegistrationChecked(final int statusCode, final JsonObject deviceDefinition, final boolean simulation) {
        // run in UI thread
        switch (statusCode) {
            case 200: {
                Log.d("Check Device Registry", "***Already Registered***");
                showStatus("Device Already Registered", View.GONE);

                JsonElement obj = deviceDefinition.get("endpoint");
                final String endpoint = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("tenant_id");
                final String tenant_id = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("vendor");
                final String vendor = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("mo_id");
                final String mo_id = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("username");
                final String username = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("password");
                final String password = obj != null ? obj.getAsString() : null;
                obj = deviceDefinition.get("userAgent");
                final String userAgent = obj != null ? obj.getAsString() : null;

                AccessInfo accessInfo = AbstractVehicleDevice.createAccessInfo(endpoint, tenant_id, vendor, mo_id, username, password);
                if(userAgent != null){
                    accessInfo.put(AccessInfo.ParamName.USER_AGENT, userAgent);
                }
                if(vehicleDevice == null){
                    vehicleDevice = protocol.createDevice(accessInfo);
                }else {
                    vehicleDevice.setAccessInfo(accessInfo);
                }
                setPreference(protocol.prefName(SettingsFragment.ENDPOINT), endpoint);
                setPreference(protocol.prefName(SettingsFragment.VENDOR), vendor);
                setPreference(protocol.prefName(SettingsFragment.MO_ID), mo_id);
                setPreference(protocol.prefName(SettingsFragment.USERNAME), username);
                setPreference(protocol.prefName(SettingsFragment.PASSWORD), password);

                deviceRegistered(simulation);
                break;
            }
            case 404:
            case 405: {
                Log.d("Check Device Registry", "***Not Registered***");
                progressBar.setVisibility(View.GONE);
                registerDevice(simulation);
//                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
//                alertDialog
//                        .setCancelable(false)
//                        .setTitle("Your Device is NOT Registered!")
//                        .setMessage("In order to use this application, we need to register your device to the IBM IoT Platform")
//                        .setPositiveButton("Ok", new OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int which) {
//                                registerDevice(simulation);
//                            }
//                        })
//                        .setNegativeButton("Exit", new OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int which) {
//                                Toast.makeText(Home.this, "Cannot continue without registering your device!", Toast.LENGTH_LONG).show();
//                                Home.this.finishAffinity();
//                            }
//                        })
//                        .show();
                break;
            }
            default: {
                Log.d("Check Device Registry", "Failed to connect Fleet Management Server: statusCode=" + statusCode);
                progressBar.setVisibility(View.GONE);

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
                alertDialog
                        .setCancelable(false)
                        .setTitle("Failed to connect to Fleet Management Server")
                        .setMessage("Check endpoint of your fleet management server. statusCode:" + statusCode)
                        .setPositiveButton("Ok", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                showStatus("Failed to connect to Fleet Management Server");
                                deviceNotRegistered(simulation);
                            }
                        })
                        .setNegativeButton("Exit", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Toast.makeText(Home.this, "Cannot continue without connecting to Fleet Management Server!", Toast.LENGTH_LONG).show();
                                Home.this.finishAffinity();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    private void registerDevice(final boolean simulation) {
        try {
            showStatus("Registering Your Device", View.VISIBLE);

            final String uuid = getUUID();
            final String device_id = obdBridge.getDeviceId(simulation, uuid);
            API.registerDevice(uuid, protocol, new API.TaskListener() {
                @Override
                public void postExecute(API.Response result) {
                    onDeviceRegistration(result.getStatusCode(), result.getBody(), simulation);
                }
            });
        } catch (DeviceNotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void onDeviceRegistration(int statusCode, final JsonObject deviceDefinition, final boolean simulation) {
        // run in UI thread
        switch (statusCode) {
            case 200:
            case 201:
            case 202:
                final String endpoint = deviceDefinition.get(SettingsFragment.ENDPOINT).getAsString();
                JsonElement obj = deviceDefinition.get(SettingsFragment.TENANT_ID);
                final String tenant_id = obj != null ? obj.getAsString() : null;
                final String vendor = deviceDefinition.get(SettingsFragment.VENDOR).getAsString();
                final String mo_id = deviceDefinition.get(SettingsFragment.MO_ID).getAsString();
                final String username = deviceDefinition.get(SettingsFragment.USERNAME).getAsString();
                final String password = deviceDefinition.get(SettingsFragment.PASSWORD).getAsString();
                obj = deviceDefinition.get(SettingsFragment.USER_AGENT);
                final String userAgent = obj != null ? obj.getAsString() : null;
                AccessInfo accessInfo = AbstractVehicleDevice.createAccessInfo(endpoint, tenant_id, vendor, mo_id, username, password);
                if(userAgent != null){
                    accessInfo.put(AccessInfo.ParamName.USER_AGENT, userAgent);
                }
                if(vehicleDevice == null) {
                    vehicleDevice = protocol.createDevice(accessInfo);
                }else{
                    vehicleDevice.setAccessInfo(accessInfo);
                }

                setPreference(protocol.prefName(SettingsFragment.ENDPOINT), endpoint);
                setPreference(protocol.prefName(SettingsFragment.TENANT_ID), tenant_id);
                setPreference(protocol.prefName(SettingsFragment.VENDOR), vendor);
                setPreference(protocol.prefName(SettingsFragment.MO_ID), mo_id);
                setPreference(protocol.prefName(SettingsFragment.USER_AGENT), userAgent);
                setPreference(protocol.prefName(SettingsFragment.USERNAME), username);
                setPreference(protocol.prefName(SettingsFragment.PASSWORD), password);

//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
//                alertDialog
//                        .setCancelable(false)
//                        .setTitle("Your Device is Now Registered!")
//                        .setMessage("")
//                        .setPositiveButton("Close", new OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int which) {
//                                deviceRegistered(simulation);
//                            }
//                        })
//                        .show();
                //deviceRegistered(simulation);
                break;
            case 400:
            case 404:
            case 500:
//                alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
//                alertDialog.setCancelable(false)
//                        .setTitle("Device registration is failed.")
//                        .setMessage("Your device is not registered. Contact to the application administrator.")
//                        .setNeutralButton("Close", new OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int which) {
//                                //Close
//                            }
//                        }).show();
            default:
                break;
        }
        progressBar.setVisibility(View.GONE);
    }

    private void deviceRegistered(final boolean simulation) {
        deviceRegistryChecking = false;
        // starts OBD scan and data transmission process here
        trip_id = createTripId();
        try {
            startObdScan(simulation);
            startPublishingProbeData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deviceNotRegistered(final boolean simulation) {
        deviceRegistryChecking = false;
        // starts OBD scan without data transmission
        try {
            // go settings for correct server connection
            // startSettingsActivity();

            // obd2 scan without registration
            startObdScan(simulation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String createTripId() {
        String tid = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tid += "-" + UUID.randomUUID();
        return tid;
    }

    private void startPublishingProbeData() {
        final int uploadIntervalMS = getUploadFrequencySec() * 1000;
        final String tenant_id = vehicleDevice.getAccessInfo().get(AccessInfo.ParamName.TENANT_ID);
        final String mo_id = vehicleDevice.getAccessInfo().get(AccessInfo.ParamName.MO_ID);
        moIdText.setText(String.format("Vehicle ID: %s", mo_id));
        vehicleDevice.startPublishing(new EventDataGenerator() {
            @Override
            public String generateData() {
                if (location != null) {
                    JsonObject data = obdBridge.generateEvent(location, trip_id);
                    data.addProperty("mo_id", mo_id);
                    data.addProperty("trip_id", trip_id);
                    data.addProperty("tenant_id", tenant_id);
                    String payload = protocol.defaultFormat().format(data);
                    return payload;
                } else {
                    showStatus("Waiting to Find Location", View.VISIBLE);
                    return null;
                }
            }
        }, uploadIntervalMS, new NotificationHandler() {
            @Override
            public void notifyPostResult(boolean success, Notification notification) {
                if(success){
                    showStatus(obdBridge.isSimulation() ? "Simulated Data is Being Sent" : "Live Data is being Sent", View.VISIBLE);
                    if(notification != null) {
                        Log.d("notification", notification.getMessage());
                    }
                }else{
                    showStatus(notification.getMessage(), View.INVISIBLE);
                }
            }
        });

        sendButton.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseButton.setChecked(false);
    }

    private void stopPublishingProbeData() {
        if(vehicleDevice != null){
            vehicleDevice.stopPublishing();
        }
        pauseButton.setEnabled(false);
        sendButton.setEnabled(true);
        sendButton.setChecked(false);
        showStatus("Your trip is finished", View.GONE);
    }
    public void send(View view){
        startPublishingProbeData();
    }
    public void pause(View view){
        stopPublishingProbeData();
    }

    private void startObdScan() {
        startObdScan(obdBridge.isSimulation());
    }

    private void startObdScan(final boolean simulation) {
        final int scanIntervalMS = getUploadFrequencySec() * 1000;
        obdBridge.startObdScan(simulation, OBD_SCAN_DELAY_MS, scanIntervalMS, new Runnable() {
            @Override
            public void run() {
                System.out.println("Obd Scan Thread: Terminating");
                obdBridge.stopObdScan();
                // reconnect
                if (!simulation) {
                    stopPublishingProbeData();
                    startConnectingBluetoothDevice(userDeviceAddress, userDeviceName, obd_timeout_ms, obd_protocol);
                    startPublishingProbeData();
                }
            }
        });
    }

    public void changeFrequency(View view) {
        // called from UI panel
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this, R.style.AppCompatAlertDialogStyle);
        final View changeFrequencyAlert = getLayoutInflater().inflate(R.layout.activity_home_changefrequency, null, false);

        final NumberPicker numberPicker = (NumberPicker) changeFrequencyAlert.findViewById(R.id.numberPicker);
        final int frequency_sec = getUploadFrequencySec();
        numberPicker.setMinValue(MIN_FREQUENCY_SEC);
        numberPicker.setMaxValue(MAX_FREQUENCY_SEC);
        numberPicker.setValue(frequency_sec);

        alertDialog.setView(changeFrequencyAlert);
        alertDialog
                .setCancelable(false)
                .setTitle("Change the Frequency of Data Being Sent (in Seconds)")
                .setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        final int value = numberPicker.getValue();
                        if (value != frequency_sec) {
                            setUploadFrequencySec(value);
                            startObdScan();
                            startPublishingProbeData();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void changeNetwork(View view) {
        // called from UI panel
        // do the following async as it may take time
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                obdBridge.stopObdScan();
            }
        }, 0, TimeUnit.MILLISECONDS);

        permissionsGranted();
    }

    public void clearDataAfterCSVFileSaving(){
        tripId="";
        //currentCSVFileName = "";
        dataListForWritingCSV = null;
        smartphoneAccX = "-";
        smartphoneAccY = "-";
        smartphoneAccZ = "-";
        smartphoneGravityX = "-";
        smartphoneGravityY = "-";
        smartphoneGravityZ = "-";
        smartphoneOrientationX = "-";
        smartphoneOrientationY = "-";
        smartphoneOrientationZ = "-";
        smartphoneGyroscopeX = "-";
        smartphoneGyroscopeY = "-";
        smartphoneGyroscopeZ = "-";
        smartphoneRotationVectorX = "-";
        smartphoneRotationVectorY = "-";
        smartphoneRotationVectorZ = "-";
        earthAccX = "";
        earthAccY = "";
        earthAccZ = "";
        magneticX = "";
        magneticY = "";
        magneticZ = "";
        pressure = "";
        smartphoneLinearAccX = "";
        smartphoneLinearAccY = "";
        smartphoneLinearAccZ = "";
        vehicleHeadingText.setText("-");
        gpsLongtitudeText.setText("-");
        gpsLatitudeText.setText("-");
        rpmText.setText("-");
        vehicleSpeedText.setText("-");
        engineOilText.setText("-");
        engineCoolantText.setText("-");
        fuelLevelText.setText("-");
        smartphoneAccText.setText("-");
        smartphonegravityText.setText("-");
        smartphoneOrientationText.setText("-");
        smartphoneGyroscopeText.setText("-");
        smartphoneRotationVectorText.setText("-");
        smartphoneGravityAccText.setText("-");
        smartphoneMagneticText.setText("-");
        pressureText.setText("-");
        phoneLinearAccText.setText("-");
        phoneRelativeRotationDegreeXToEarth = "";
        phoneRelativeRotationDegreeYToEarth = "";
        phoneRelativeRotationDegreeZToEarth = "";
        earthLinearAccX = "";
        earthLinearAccY = "";
        earthLinearAccZ = "";
        earthGyroscopeX = "";
        earthGyroscopeY = "";
        earthGyroscopeZ = "";
        currentCSVFileNameText.setText("Trip csv:");
    }

    //upload dataList to server
    public String postSubmit(View view){
        List<List<String>> dataListFromSCV = null;
        dataListFromSCV = CSVOperation.readCSVFile(currentCSVFileName);
        String jsonString = CSVOperation.converToJson(dataListFromSCV,"HuiqunHuang","password");
//        Looper.prepare();
//        Toast.makeText(Home.this, jsonString, Toast.LENGTH_SHORT).show();
//        Looper.loop();
        dataListFromSCV.clear();
        return jsonString;
    }

    public void uploadDataIntoServerByJson2(String jsonString){
        try {
            // 创建url资源
            URL url = new URL(dataUploadPath);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            //转换为字节数组
            byte[] data = (jsonString).getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");
            // 开始连接请求
            conn.connect();
            OutputStream  out = conn.getOutputStream();
            // 写入请求的字符串
            out.write((jsonString).getBytes());
            out.flush();
            out.close();
            currentCSVFileName = "";
            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                Looper.prepare();
                Toast.makeText(Home.this, "Successfully uploading trip data into server.", Toast.LENGTH_SHORT).show();
                Looper.loop();
//                // 请求返回的数据
//                InputStream in = conn.getInputStream();
//                String a = null;
//                try {
//                    byte[] data1 = new byte[in.available()];
//                    in.read(data1);
//                    // 转成字符串
//                    a = new String(data1);
//                    System.out.println(a);
//                } catch (Exception e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
            } else {
                Looper.prepare();
                Toast.makeText(Home.this, "Data Upload Failed~", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch(ProtocolException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    public void endSession(final View view) {
        //CSVOperation.writeDataIntoCSV(secondaryDataListForWritingCSV, currentCSVFileName);
        AlertDialog.Builder dataSavingSelectionBuilder = new AlertDialog.Builder(this);
        dataSavingSelectionBuilder.setMessage("Wanna upload the data to the server?");
        dataSavingSelectionBuilder.setPositiveButton("Yes", new OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                syncTask.interrupt();
                writeCSVFileThread.interrupt();
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        //postSubmit(view);
                        if(dataListForWritingCSV!=null){
                            secondaryDataListForWritingCSV = new ArrayList<>();
                            secondaryDataListForWritingCSV = dataListForWritingCSV;
                            dataListForWritingCSV = null;
                            CSVOperation.writeDataIntoCSV2(secondaryDataListForWritingCSV,currentCSVFileName);
                        }
                        uploadDataIntoServerByJson2(postSubmit(view));
                    }
                }).start();
                //Toast.makeText(Home.this, "ggg", Toast.LENGTH_LONG).show();
                clearDataAfterCSVFileSaving();
                currentCSVFileName = "";
                startNewTrip();
                realOrSimuChecking = false;
                realOrSimuSelected = false;
                showObdScanModeDialog();
//                scheduler.schedule(new Runnable() {
//                    @Override
//                    public void run() {
//                        clean();
//                    }
//                }, 0, TimeUnit.MILLISECONDS);
//                Home.this.finishAffinity();
            }
        });
        dataSavingSelectionBuilder.setNegativeButton("No", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                syncTask.interrupt();
                writeCSVFileThread.interrupt();
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
                sensorManager.unregisterListener(Home.this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

                clearDataAfterCSVFileSaving();
                currentCSVFileName = "";
                realOrSimuChecking = false;
                realOrSimuSelected = false;
                startNewTrip();
                showObdScanModeDialog();
//                scheduler.schedule(new Runnable() {
//                    @Override
//                    public void run() {
//                        clean();
//                    }
//                }, 0, TimeUnit.MILLISECONDS);
//                Home.this.finishAffinity();
            }
        });
        AlertDialog b = dataSavingSelectionBuilder.create();
        b.show();
        // do the following async as it may take time
    }

    public void setUploadFrequencySec(final int sec) {
        setPreference(SettingsFragment.UPLOAD_FREQUENCY, "" + sec);
    }

    public int getUploadFrequencySec() {
        final String value = getPreference(SettingsFragment.UPLOAD_FREQUENCY, "" + DEFAULT_FREQUENCY_SEC);
        return Integer.parseInt(value);
    }

    public int getObdTimeoutMs() {
        final String value = getPreference(SettingsFragment.OBD_TIMEOUT_MS, "" + ObdBridge.DEFAULT_OBD_TIMEOUT_MS);
        return Integer.parseInt(value);
    }

    public ObdProtocols getObdProtocol() {
        final String value = getPreference(SettingsFragment.OBD_PROTOCOL, "" + ObdBridge.DEFAULT_OBD_PROTOCOL);
        return ObdProtocols.valueOf(value);
    }

    private boolean wasWarningShown() {
        final boolean warningShown = getPreferenceBoolean("iota-starter-obdii-warning-message", false);
        if (warningShown) {
            return warningShown;
        } else {
            setPreferenceBoolean("iota-starter-obdii-warning-message", true);
            return false;
        }
    }

    private String getUUID() {
        String uuidString = getPreference("iota-starter-obdii-uuid", DOESNOTEXIST);
        if (!DOESNOTEXIST.equals(uuidString)) {
            return uuidString;
        } else {
            uuidString = UUID.randomUUID().toString();
            setPreference("iota-starter-obdii-uuid", uuidString);
            return uuidString;
        }
    }

    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
        //sensorManager.registerListener(listener, accsensor, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(listener, gravitysensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 500, 1, this);
        }

        if (initialized) {
            checkSettingsOnResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocationInformation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public Location getLocation() {
        return location;
    }

    private boolean setLocationInformation() {
        // returns false if GPS and Network settings are needed
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){// && (networkInfo != null && networkInfo.isConnected())) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            final List<String> providers = locationManager.getProviders(true);
            Location finalLocation = null;

            for (String provider : providers) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }

                final Location lastKnown = locationManager.getLastKnownLocation(provider);

                if (lastKnown == null) {
                    continue;
                }
                if (finalLocation == null || (lastKnown.getAccuracy() < finalLocation.getAccuracy())) {
                    finalLocation = lastKnown;
                }
            }

            if (finalLocation == null) {
                Log.e("Location Data", "Not Working!");
            } else {
                Log.d("Location Data", finalLocation.getLatitude() + " " + finalLocation.getLongitude() + "");
                location = finalLocation;
            }

            return true;
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "Please turn on your GPS", Toast.LENGTH_LONG).show();

                final Intent gpsIntent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(gpsIntent, GPS_INTENT);

//                if (networkInfo == null) {
//                    networkIntentNeeded = true;
//                }
                return false;
            }else {
//                if (networkInfo == null) {
//                    Toast.makeText(getApplicationContext(), "Please turn on Mobile Data or WIFI", Toast.LENGTH_LONG).show();
//
//                    final Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
//                    startActivityForResult(settingsIntent, SETTINGS_INTENT);
//                    return false;
//                } else {
                    return true;
               }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case GPS_INTENT:
                if (networkIntentNeeded) {
                    Toast.makeText(getApplicationContext(), "Please connect to a network", Toast.LENGTH_LONG).show();
                    final Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivityForResult(settingsIntent, SETTINGS_INTENT);
                } else {
                    setLocationInformation();
                    startApp2();
                }
                break;
            case SETTINGS_INTENT:
                if(data.getBooleanExtra("appServerChanged", true)){
                    networkIntentNeeded = false;
                    setLocationInformation();
                    startApp2();
                }
                break;
            case BLUETOOTH_REQUEST:
                startApp2();
                break;
            case SPECIFY_SERVER_INTENT:
                if(data == null) {
                    // App Server is not changed
                    break;
                }else if(data.getBooleanExtra("useDefault", false)){
                    setPreference(SettingsFragment.APP_SERVER_URL, API.getDefaultAppURL());
                    setPreference(SettingsFragment.APP_SERVER_USERNAME, API.getDefaultAppUser());
                    setPreference(SettingsFragment.APP_SERVER_PASSWORD, API.getDefaultAppPassword());
                    API.useDefault();
                }else if(data.getBooleanExtra("appServerChanged", false)) {
                    String appUrl = getPreference(SettingsFragment.APP_SERVER_URL, null);
                    String appUser = getPreference(SettingsFragment.APP_SERVER_USERNAME, null);
                    String appPass = getPreference(SettingsFragment.APP_SERVER_PASSWORD, null);
                    API.doInitialize(appUrl, appUser, appPass);
                    networkIntentNeeded = false;
                    setLocationInformation();
                    startApp2();
                    break;
                }else{
                    String appUrl = data.getStringExtra(SettingsFragment.APP_SERVER_URL);
                    String appUsername = data.getStringExtra(SettingsFragment.APP_SERVER_USERNAME);
                    String appPassword = data.getStringExtra(SettingsFragment.APP_SERVER_PASSWORD);
                    setPreference(SettingsFragment.APP_SERVER_URL, appUrl);
                    setPreference(SettingsFragment.APP_SERVER_USERNAME, appUsername);
                    setPreference(SettingsFragment.APP_SERVER_PASSWORD, appPassword);
                    API.doInitialize(appUrl, appUsername, appPassword);
                }
                checkMQTTAvailable();
                if(vehicleDevice != null){
                    vehicleDevice.clean();
                    vehicleDevice = null;
                }
                startApp2();
                break;
            default:
                break;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Home Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public int getPreferenceInt(final String prefKey, final int defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            return preferences.getInt(prefKey, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public boolean getPreferenceBoolean(final String prefKey, final boolean defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            return preferences.getBoolean(prefKey, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public String getPreference(final String prefKey, final String defaultValue) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            return preferences.getString(prefKey, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public void setPreferenceInt(final String prefKey, final int value) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putInt(prefKey, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreferenceBoolean(final String prefKey, final boolean value) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putBoolean(prefKey, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreference(final String prefKey, final String value) {
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString(prefKey, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void removePreference(final String prefKey){
        try{
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().remove(prefKey).apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
