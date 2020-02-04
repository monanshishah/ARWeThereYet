package com.example.arwethereyet_se4450;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
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
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
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

public class ARPage extends AppCompatActivity implements SensorEventListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ar_page);

//        //sceneform version
//        if (!checkIsSupportedDeviceOrFinish(this)) {
//            return;
//        }

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

//        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_FASTEST);
//
//        Timer timer = new Timer();
//        //Set the schedule function
//        timer.scheduleAtFixedRate(new TimerTask() {
//                                      @Override
//                                      public void run() {
//
//                                          Frame frame = arFragment.getArSceneView().getArFrame();
//
//                                          Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
//
//                                          for(Plane plane : planes){
//                                              if(plane.getTrackingState() == TrackingState.TRACKING){
//
//                                                  Anchor anchor = plane.createAnchor(plane.getCenterPose());
//
//                                                  ModelRenderable.builder()
//                                                          .setSource(ARPage.this, R.raw.arrow)
//                                                          .build()
//                                                          .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
//                                                          .exceptionally(throwable -> {
//                                                              Toast toast =
//                                                                      Toast.makeText(ARPage.this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                                                              toast.setGravity(Gravity.CENTER, 0, 0);
//                                                              toast.show();
//                                                              return null;
//                                                          });
//                                                  break;
//                                              }
//                                          }
//                                      }
//                                  },
//                0, 5000);

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
    }

    private void onUpdate(FrameTime frameTime) {
//
//        if(isModelPlaced)
//            return;

        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);

        for(Plane plane : planes){
            if(plane.getTrackingState() == TrackingState.TRACKING){

                Anchor anchor = plane.createAnchor(plane.getCenterPose());

                ModelRenderable.builder()
                    .setSource(this, R.raw.arrow)
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                    .exceptionally(throwable -> {
                        Toast toast =
                                Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return null;
                    });
                break;
            }
        }
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        isModelPlaced = true;
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        transformableNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 227f));
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        System.out.print("Hello world");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
