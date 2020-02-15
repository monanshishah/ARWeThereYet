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
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

//sceneform
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
//import com.example.arwethereyet_se4450.CameraPermissionHelper;
import java.util.ArrayList;
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

public class ARPage extends AppCompatActivity implements SensorEventListener, LocationListener {

    private static Button myArButton;
    private Session mySession;

    //from hello sample
    private static final String TAG = ARPage.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;

    //from line sample
    private AnchorNode anchorNode;
    private List<AnchorNode> anchorNodeList = new ArrayList<>();
    private Integer numberOfAnchors = 0;
    private AnchorNode currentSelectedAnchorNode = null;
    private Node nodeForLine;

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

//        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
//            Anchor anchor = hitResult.createAnchor();
//
//            ModelRenderable.builder()
//                    .setSource(this, R.raw.arrow)
//                    .build()
//                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
//                    .exceptionally(throwable -> {
//                        Toast toast =
//                                Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
//                        return null;
//                    });
//        }));

        ModelRenderable.builder()
                .setSource(ARPage.this, R.raw.arrow)
                .build()
                .thenAccept(modelRenderable -> addModelToScene(modelRenderable))
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(ARPage.this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
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
//                ModelRenderable.builder()
//                    .setSource(this, R.raw.arrow)
//                    .build()
//                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
//                    .exceptionally(throwable -> {
//                        Toast toast =
//                                Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
//                        return null;
//                    });
//                break;

//    }





    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        Node node = new Node();
        node.setParent(arFragment.getArSceneView().getScene());
        node.setRenderable(modelRenderable);
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 227f));
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Camera camera = arFragment.getArSceneView().getScene().getCamera();
            Ray ray = camera.screenPointToRay(1080/2f, 1920/2f);
            Vector3 newPosition = ray.getPoint(1f);
            node.setLocalPosition(newPosition);
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

//    public void b2(View view){
//        Frame frame = arFragment.getArSceneView().getArFrame();
//        int currentAnchorIndex = numberOfAnchors;
//        Session session = arFragment.getArSceneView().getSession();
//        Log.i(TAG, "here");
//        Anchor newMarkAnchor = session.createAnchor(
//                frame.getCamera().getPose()
//                        .compose(Pose.makeTranslation(1f, 0, -5f))
//                        .extractTranslation());
//        AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);
//        addedAnchorNode.setRenderable(andyRenderable);
//        addAnchorNode(addedAnchorNode);
//        currentSelectedAnchorNode = addedAnchorNode;
//    }

//    private void addAnchorNode(AnchorNode nodeToAdd) {
//        //Add an anchor node
//        nodeToAdd.setParent(arFragment.getArSceneView().getScene());
//        anchorNodeList.add(nodeToAdd);
//        numberOfAnchors++;
//    }

//    public void goBackClick(View view){
//        Intent myIntent = new Intent(ARPage.this, MainActivity.class);
//        startActivity(myIntent);
//        finish();
//    }


    // Set to true ensures requestInstall() triggers installation if necessary.
//    private boolean mUserRequestedInstall = true;
//
//    protected void onResume() {
//        super.onResume();
//
//        // ARCore requires camera permission to operate.
//        if (!CameraPermissionHelper.hasCameraPermission(this)) {
//            CameraPermissionHelper.requestCameraPermission(this);
//            return;
//        }
//
//        // Make sure Google Play Services for AR is installed and up to date.
//        try {
//            if (mySession == null) {
//                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
//                    case INSTALLED:
//                        // Success, create the AR session.
//                        mySession = new Session(this);
//                        break;
//                    case INSTALL_REQUESTED:
//                        // Ensures next invocation of requestInstall() will either return
//                        // INSTALLED or throw an exception.
//                        mUserRequestedInstall = false;
//                        return;
//                }
//            }
//        } catch (UnavailableUserDeclinedInstallationException e) {
//            // Display an appropriate message to the user and return gracefully.
//            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
//                    .show();
//            return;
//        } catch (UnavailableArcoreNotInstalledException e) {
//            return;
//        } catch (UnavailableDeviceNotCompatibleException e) {
//            return;
//        } catch (UnavailableSdkTooOldException e) {
//            return;
//        } catch (UnavailableApkTooOldException e) {
//            return; //e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
//        if (!CameraPermissionHelper.hasCameraPermission(this)) {
//            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
//                    .show();
//            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
//                // Permission denied with checking "Do not ask again".
//                CameraPermissionHelper.launchPermissionSettings(this);
//            }
//            finish();
//        }
//    }


    //sceneform version

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
