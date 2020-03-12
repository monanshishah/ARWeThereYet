package com.example.arwethereyet_se4450;


import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;


import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


//specific imports for pin query
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Marker;


import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


//AR
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;


import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener{


    private MapView mapView;
    private MapboxMap mapboxMap;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // Location engine variables
    private LocationEngine locationEngine;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    //specific var for pin query
    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";//"ck58iqryj01px2nk0t6mca66g";
//    private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID"; //for non deprecated
    private static final String CALLOUT_IMAGE_ID = "CALLOUT_IMAGE_ID";
//    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private GeoJsonSource source;

    //SEARCHING FUNCTIONALITY
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String symbolIconId = "symbolIconId";


    //pin query youtube
//    private Location ogLoc;
    private Point origindest;
    private Marker destM;

    // variables for calculating and drawing a route
    //changed private to public for current route
    public static DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    // variables needed to initialize navigation
    private Button button;
    private Button arButton;

    //info popup
    private LinearLayout linearLayoutView;
    private TextView titleTextView;
    private TextView propertiesListTextView;
    private TextView upTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        if (!isConnected()) {

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Internet Connection Alert")
                    .setMessage("Please check your internet connection")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .show();

        }


    }

    boolean isConnected() {
        // Checking internet connectivity
        ConnectivityManager connectivityMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityMgr.getActiveNetworkInfo();

        return activeNetwork!=null && activeNetwork.isConnected();
    }



    public void arClick(View view){

        Intent myIntent = new Intent(MainActivity.this, ARPage.class);
        //myIntent.putExtra("route", currentRoute); couldn't pass non customised object this way
        startActivity(myIntent);
        finish();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/aribasilone/ck5fo78du23jc1jo66i0hmm71"), new Style.OnStyleLoaded() {

                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        enableLocationComponent(style);
                        setUpData();
                        initSearchFab();

                        mapboxMap.addOnMapClickListener(MainActivity.this);
                        Toast.makeText(MainActivity.this,
                                getString(R.string.click_on_map_instruction), Toast.LENGTH_LONG).show();
//                        button = findViewById(R.id.startButton);
//                        button.setOnClickListener(v -> {
//                            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                    .directionsRoute(currentRoute)
//                                    .shouldSimulateRoute(true)
//                                    .build();
//                            // Call this method with Context from within an Activity
//                            NavigationLauncher.startNavigation(MainActivity.this, options);
//                        });

                        arButton = findViewById(R.id.button_ar_nav);
                        titleTextView = findViewById(R.id.info_title);
                        linearLayoutView = findViewById(R.id.bottom_sheet);
                        propertiesListTextView = findViewById(R.id.info_feature_properties_list);
                        upTextView = findViewById(R.id.up);
                    }
        });
    }


    //for pin query
    /**
     * Sets up all of the sources and layers needed for this example
     **/
//     public void setUpData(final FeatureCollection collection) {
    public void setUpData() {
         //featCollect = collection;
         if (mapboxMap != null) {
             mapboxMap.getStyle(style -> {
                 setupSource(style);
                 setUpInfoWindowLayer(style);
                 setupLayer(style);
             });
         }
     }



    /**
     * Adds the GeoJSON source to the map
     */
    private void setupSource(@NonNull Style loadedStyle) {
        //source = new GeoJsonSource(GEOJSON_SOURCE_ID, featCollect);
        //loadedStyle.addSource(source);

        try {
            source = new GeoJsonSource(GEOJSON_SOURCE_ID, new URI("asset://CapstoneV1.geojson"));

            loadedStyle.addSource(source);
           // Log.i(TAG,source.toString());

        } catch (URISyntaxException exception) {

            Log.d(TAG, "exception");

        }

        /**
         * Attempted to use api call and hosted geojson file
         * Issues arose with token and scope access, would not allow secret token permission
         * https://docs.mapbox.com/android/maps/overview/data-driven-styling/#geojson
         * https://docs.mapbox.com/api/maps/#tilesets
        try {
            URI geoJsonUrl = new URI(getString(R.string.tileset_api));
            GeoJsonSource geoJsonSource = new GeoJsonSource("ck58iqryj01px2nk0t6mca66g", geoJsonUrl);
            loadedStyle.addSource(geoJsonSource);
        } catch (URISyntaxException exception) {
            Log.d(TAG, "exception");
        }*/
    }


    /**
     * Needed to show the Feature properties info window.
     */
    private void refreshSource(Feature featureAtClickPoint) {

        if (source != null) {
            source.setGeoJson(featureAtClickPoint);
        }
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .bbox(-81.28432464432893,42.99455346126038,-81.2664676815399,43.014377673454874)
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    //layer for search icon
    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", GEOJSON_SOURCE_ID).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if(destM != null)
                mapboxMap.removeMarker(destM);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }

                destM = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                ((Point) selectedCarmenFeature.geometry()).longitude())));

                LatLng searchPoint = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                        ((Point) selectedCarmenFeature.geometry()).longitude());

                Point destinationPoint = Point.fromLngLat(searchPoint.getLongitude(), searchPoint.getLatitude());

                Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                        locationComponent.getLastKnownLocation().getLatitude());
                getRoute(originPoint, destinationPoint);


                // Convert LatLng coordinates to screen pixel and only query the rendered features.
                handleQueryIcon(mapboxMap.getProjection().toScreenLocation(searchPoint));

                //button.setEnabled(true);
                //button.setBackgroundResource(R.color.mapboxBlue);
                //linearLayoutView.setVisibility(View.VISIBLE);
                //arButton.setVisibility(View.VISIBLE);
                //upTextView.setVisibility(View.VISIBLE);
                //titleTextView.setVisibility(View.VISIBLE);
                //propertiesListTextView.setVisibility(View.VISIBLE);
            }
        }
    }




    /**
     * Adds a SymbolLayer to the map to show the Feature properties info window.
     */
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        // show image with id title based on the value of the name feature property
                        iconImage(CALLOUT_IMAGE_ID),

                        // set anchor of icon to bottom-left
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        // prevent the feature property window icon from being visible even
                        // if it collides with other previously drawn symbols
                        iconAllowOverlap(false),

                        // prevent other symbols from being visible even if they collide with the feature property window icon
                        iconIgnorePlacement(false),

                        // offset the info window to be above the marker
                        iconOffset(new Float[] {-2f, -28f})
                ));
    }

    /**
     * This method handles click events for SymbolLayer symbols.
     *
     * @param screenPoint the point on screen clicked
     */
    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint,"uwotest");
        return checkInCustomMap(features,true);
    }

    private void handleQueryIcon(PointF screenPoint){
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint,"uwotest");
        checkInCustomMap(features,false);
    }


    public boolean checkInCustomMap(List<Feature> features, Boolean tf){
        if (!features.isEmpty()) {
            Feature feature = features.get(0);

            if (feature.properties() != null) {

//                new GenerateViewIconTask(MainActivity.this).execute(FeatureCollection.fromFeature(feature));
                anotherFunction(FeatureCollection.fromFeature(feature));
                //arButton.setEnabled(true);
                arButton.setVisibility(View.VISIBLE);
                linearLayoutView.setVisibility(View.VISIBLE);
                upTextView.setVisibility(View.VISIBLE);
                titleTextView.setVisibility(View.VISIBLE);
                propertiesListTextView.setVisibility(View.VISIBLE);
                return true;
            }
        } else if(tf){
            Toast.makeText(this, getString(R.string.query_feature_no_properties_found), Toast.LENGTH_SHORT).show();
            //arButton.setEnabled(false);
            //arButton.setBackgroundColor(R.color.ARWeGrey);
            //titleTextView.setText("Invalid Attraction Selected");
            //propertiesListTextView.setText("");
            linearLayoutView.setVisibility(View.INVISIBLE);
        }

        else{
            linearLayoutView.setVisibility(View.VISIBLE);
            upTextView.setVisibility(View.INVISIBLE);
            titleTextView.setVisibility(View.INVISIBLE);
            propertiesListTextView.setVisibility(View.INVISIBLE);
        }
        return false;

    }

    public void anotherFunction(FeatureCollection... params){
        Feature featureAtMapClickPoint;
        if (params[0].features() != null) {
            featureAtMapClickPoint = params[0].features().get(0);

            StringBuilder stringBuilder = new StringBuilder();
            titleTextView.setText(getString(R.string.query_feature_marker_title));

            if (featureAtMapClickPoint.properties() != null) {
                String bldgCode = null, attractionType = null, entrance = null, etc = null;

                for (Map.Entry<String, JsonElement> entry : featureAtMapClickPoint.properties().entrySet()) {
                    Log.i(TAG, entry.getKey());
                    if (entry.getKey().equals("name")) {
                        String cleanName = entry.getValue().toString();
                        cleanName = cleanName.replaceAll("^\"|\"$", "");
                        titleTextView.setText(cleanName);
                    } else if (entry.getKey().equals("attractionType")) {
                        attractionType = entry.getValue().getAsString();
                        attractionType = attractionType.replaceAll("[\\[\\]]", "");
//                                cleanType = cleanType.replaceAll("^\"|\"$", "");
                        attractionType = attractionType.replaceAll("\"", "");

                    } else if (entry.getKey().equals("bldgCode")) {
                        bldgCode = entry.getValue().getAsString();

                    } else if (entry.getKey().equals("entrance")) {
                        entrance = entry.getValue().getAsString();
                    } else {
                        etc = entry.getValue().getAsString();
                    }

                }
                if (bldgCode != null) {
                    stringBuilder.append(bldgCode);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                if (attractionType != null) {
                    stringBuilder.append(attractionType);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                if (entrance != null) {
                    stringBuilder.append(entrance);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                if (etc != null) {
                    stringBuilder.append(entrance);
                }
                propertiesListTextView.setText(stringBuilder.toString());
            }
        }

    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if(destM != null){
            mapboxMap.removeMarker(destM);
        }
        destM = mapboxMap.addMarker(new MarkerOptions().position(point));
        //supposedly black icon/marker is an emulator issue, alt has to deal with deprecation of Marker

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        if(handleClickIcon(mapboxMap.getProjection().toScreenLocation(point))){
            getRoute(originPoint, destinationPoint);
        }else{
            // remove any prev route on the map
            if (navigationMapRoute != null) {
                navigationMapRoute.removeRoute();
            }
        }

        return true;

        //button.setEnabled(true);
        //button.setBackgroundResource(R.color.mapboxBlue);
//        arButton.setVisibility(View.VISIBLE);
//        linearLayoutView.setVisibility(View.VISIBLE);
    }


    /**
     * Invoked when the bitmap has been generated from a view.
     */
    public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                style.addImages(imageMap);
            });
        }
    }


    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final WeakReference<MainActivity> activityRef;
        private Feature featureAtMapClickPoint;

        GenerateViewIconTask(MainActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            MainActivity activity = activityRef.get();
            HashMap<String, Bitmap> imagesMap = new HashMap<>();
            if (activity != null) {
                LayoutInflater inflater = LayoutInflater.from(activity);

                if (params[0].features() != null) {
                    featureAtMapClickPoint = params[0].features().get(0);

                    StringBuilder stringBuilder = new StringBuilder();

                    BubbleLayout bubbleLayout = (BubbleLayout) inflater.inflate(R.layout.activity_main_win_sym, null);
                    //tried to replace bubble with android component and it crashed
                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                    titleTextView.setText(activity.getString(R.string.query_feature_marker_title));

                    if (featureAtMapClickPoint.properties() != null) {
                        //see block from anotherfunction
                        for (Map.Entry<String, JsonElement> entry : featureAtMapClickPoint.properties().entrySet()) {

                            stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
                            stringBuilder.append(System.getProperty("line.separator"));
                        }


                        TextView propertiesListTextView = bubbleLayout.findViewById(R.id.info_window_feature_properties_list);
                        propertiesListTextView.setText(stringBuilder.toString());

                        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        bubbleLayout.measure(measureSpec, measureSpec);

                        float measuredWidth = bubbleLayout.getMeasuredWidth();

                        bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                        Bitmap bitmap = MainActivity.SymbolGenerator.generate(bubbleLayout);
                        imagesMap.put(CALLOUT_IMAGE_ID, bitmap);
                    }
                }
            }

            return imagesMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            MainActivity activity = activityRef.get();
            if (activity != null && bitmapHashMap != null) {
                activity.setImageGenResults(bitmapHashMap);
                activity.refreshSource(featureAtMapClickPoint);
            }
        }

    }

    /**

     * Utility class to generate Bitmaps for Symbol.
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
//        if (locationEngine != null) {
//            locationEngine.removeLocationUpdates(callback);
//        }
        mapView.onDestroy();
    }


    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(true)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
//
//            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * End of location component
     *
     */


    /**
     * Routing functions
     *
     */

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }


                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }


}

