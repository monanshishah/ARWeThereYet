package com.example.arwethereyet_se4450;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;


//sceneform
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;


import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

public class ARPage extends AppCompatActivity implements SensorEventListener, LocationListener,
        OnNavigationReadyCallback, NavigationListener, MilestoneEventListener {


    //from hello sample
    private static final String TAG = ARPage.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;


    //detects movement
    private SensorManager sensorManager;
    private Sensor sensor;
    private TriggerEventListener triggerEventListener;
    private boolean isModelPlaced = false;

    //accelerometer
    private float xAxis = 0f;
    private float yAxis = 0f;
    private float zAxis = 0f;
    private float speed = 0f;

    //anchor
    private Anchor anchor;
    private boolean timerEnd= false;

    //AR route
    private DirectionsRoute currentRoute;
    private NavigationView navigationView;
    private Integer stepCounter = 0;

    private ModelRenderable modelRenderableGlobal;
    private float bearing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ar_page);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }
//else{
//            showGPSDisabledAlertToUser();
//        }

        /**
         * If the below hard code sceneform compatibility check is not used
         * then AndroidManifest.xml must ensure open gl.
         * The benefit to the hard coded check is that, for devices that aren't compatible
         * they can still use rest of app features just not AR.
         */
//        //sceneform version
//        if (!checkIsSupportedDeviceOrFinish(this)) {
//            return;
//        }

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

//        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
//
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_FASTEST);
//


        ModelRenderable.builder()
                .setSource(ARPage.this, R.raw.arrow)
                .build()
                .thenAccept(modelRenderable -> addModelToScene(modelRenderable))
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(ARPage.this, "Unable to load any renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });


        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);

    }

    private void navViewSetup(){
        //get route from prev activity
        currentRoute = MainActivity.currentRoute;
        NavigationViewOptions options = NavigationViewOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(true)
                .navigationListener(this)
                //.routeListener(this)
                //.feedbackListener(this)
                //.progressChangeListener(this)
                .milestoneEventListener(this)
                .build();

        Log.i(TAG,currentRoute.toString());
        Log.i(TAG,navigationView.toString());

        navigationView.startNavigation(options);

    }



//    private void renderModel() {
//        ModelRenderable.builder()
//                .setSource(ARPage.this, R.raw.arrow)
//                .build()
//                .thenAccept(modelRenderable -> addModelToScene(modelRenderable))
//                .exceptionally(throwable -> {
//                    Toast toast =
//                            Toast.makeText(ARPage.this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                    return null;
//                });
//    }

        private void onUpdate (FrameTime frameTime){
//
////        if(isModelPlaced)
////            return;
//
//            if(timerEnd == true){
//                renderModel();
//                timerEnd = false;
//            }
//
//            Frame frame = arFragment.getArSceneView().getArFrame();
//
//            Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
//
//            for(Plane plane : planes){
//                Plane lastelement;
//                lastelement = plane;
//                if (lastelement.getTrackingState() == TrackingState.TRACKING) {
//                    anchor = lastelement.createAnchor(lastelement.getCenterPose());
//                    Log.d(TAG, "this is the plane:" + lastelement);
//                }
//            }


        }
//
//    renderModel();
//    }





    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.onStart();
        Timer timer = new Timer();
        //Set the schedule function
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          timerEnd = true;
                                      }
                                  },
                0, 5000);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

    private void addModelToScene(ModelRenderable modelRenderable) {
//        isModelPlaced = true;
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//        transformableNode.setParent(anchorNode);
//        transformableNode.setRenderable(modelRenderable);
//        transformableNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 227f));
//        arFragment.getArSceneView().getScene().addChild(anchorNode);
//        transformableNode.select();

        modelRenderableGlobal = modelRenderable;

        Node node = new Node();
        node.setParent(arFragment.getArSceneView().getScene());
        node.setRenderable(modelRenderableGlobal);
        //change colour of arrow
        modelRenderable.getMaterial().setFloat4("baseColorTint", new Color(android.graphics.Color.rgb(255,89,0)));
        //remove shadow
        modelRenderable.setShadowCaster(false);

        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), bearing));
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Camera camera = arFragment.getArSceneView().getScene().getCamera();
            Ray ray = camera.screenPointToRay(1080/2f, 1920/2f);
            Vector3 newPosition = ray.getPoint(1f);
            node.setLocalPosition(newPosition);
            node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), bearing));
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Toast toast =
////                Toast.makeText(this, "Sensor works", Toast.LENGTH_LONG);
////        toast.setGravity(Gravity.CENTER, 0, 0);
////        toast.show();
        System.out.print("hello world");
        setValuesToZero();

        xAxis = event.values[0];

//        if (xAxis > 1.0) {
//            Log.d(TAG, "accelerometer detected");
//
//        }
    }

    private void setValuesToZero() {
        xAxis = 0f;
        yAxis = 0f;
        zAxis = 0f;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(@NotNull Location location) {
        speed =location.getSpeed();


        Log.d(TAG, "*********************speed=" + speed);
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



    public void goBackClick(View view){
        navigationView.stopNavigation();
        Intent myIntent = new Intent(ARPage.this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }


    //methods to implement for listeners
    @Override
    public void onCancelNavigation() {

        navigationView.stopNavigation();
    }

    @Override
    public void onNavigationFinished() {

        navigationView.stopNavigation();
    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        navViewSetup();
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
        Boolean angle = false;

        Log.i(TAG, instruction);
        //route progress info did not appear useful with emulator + simulator combo
        Log.i(TAG, routeProgress.upcomingStepPoints().toString());
        Log.i(TAG, routeProgress.currentLeg().steps().get(stepCounter).maneuver().bearingBefore().toString());
        Log.i(TAG, routeProgress.currentLeg().steps().get(stepCounter).maneuver().bearingAfter().toString());

        bearing = routeProgress.currentLeg().steps().get(stepCounter).maneuver().bearingAfter().floatValue();

        //if the instruction contains distance then its not a sharp turn
        //will have to adjust for feet or metres (change localisation settings in mapbox)
        if(instruction.contains("0") || instruction.contains ("feet") || instruction.contains("metres")){
            Log.i(TAG, "distance");
            //will have to angle arrow in corresponding direction
            angle = true;
            return;
        }
        if(instruction.contains("Continue on")){
            //no change do nothing
            return;
        }

        //roundabout has 1st exit (right angle), has 2nd exit (straight), 3rd exit (left angle)
        if(instruction.contains("roundabout")){
            Log.i(TAG, "roundabout");
            if(instruction.contains("Exit the roundabout")){
                Log.i(TAG,"roundabout exit make sharp right");
                stepCounter++;
            }
            else if (instruction.contains("1st")){
                Log.i(TAG, "round 1");
                stepCounter++;
            }
            else if (instruction.contains("2nd")){
                Log.i(TAG,"round 2");
                stepCounter++;
            }
            else if (instruction.contains("3rd")){
                Log.i(TAG, "round 3");
                stepCounter++;
            }
        }
        //e.g. of output: Turn right, then turn left
        else if (instruction.contains("then turn")){
            if (instruction.indexOf("left")< instruction.indexOf("right")){
                //turning left before right
                Log.i(TAG, "turn left first");
            }
            else{
                Log.i(TAG,"turn right first");
            }
            stepCounter++;
        }
        else if(instruction.contains("left")){
            Log.i(TAG,"left");
            Toast toast =
                    Toast.makeText(ARPage.this, "left", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            stepCounter++;
        }
        else if (instruction.contains("right")){
            Log.i(TAG,"right");
            Toast toast =
                    Toast.makeText(ARPage.this, "right", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            stepCounter++;
        }

        //e.g. output to use: In 500 feet, you will arrive at your destination
        //e.g. output: You have arrived at your destination, on the left
        //could change colour of arrow to reflect near/at destination



    }


    //AR line sample for reference
    //https://github.com/mickod/LineView/blob/master/lineview_main_app_module/src/main/java/com/amodtech/ar/lineview/LineViewMainActivity.java


/**
 * Alternative permission and device compatibility check without use of sceneform
 * refer to: https://developers.google.com/ar/develop/java/enable-arcore
 */


    //sceneform hard coded compatibility check version

//    /**
//     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
//     * on this device.
//     *
//     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
//     *
//     * <p>Finishes the activity if Sceneform can not run
//     */
//    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            Log.e(TAG, "Sceneform requires Android N or later");
//            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
//            activity.finish();
//            return false;
//        }
//        String openGlVersionString =
//                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
//                        .getDeviceConfigurationInfo()
//                        .getGlEsVersion();
//        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
//            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
//            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
//                    .show();
//            activity.finish();
//            return false;
//        }
//        return true;
//    }

}
