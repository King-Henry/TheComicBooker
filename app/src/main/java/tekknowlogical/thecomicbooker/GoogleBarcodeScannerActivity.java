package tekknowlogical.thecomicbooker;


    import android.Manifest;
    import android.app.Activity;
    import android.app.AlertDialog;
    import android.app.Dialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.support.design.widget.Snackbar;
    import android.support.v4.app.ActivityCompat;
    import android.support.v7.app.ActionBar;
    import android.support.v7.app.AppCompatActivity;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.FrameLayout;
    import android.widget.ScrollView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.common.GoogleApiAvailability;


    import com.google.android.gms.vision.CameraSource;
    import com.google.android.gms.vision.MultiProcessor;
    import com.google.android.gms.vision.barcode.BarcodeDetector;
    import com.squareup.leakcanary.LeakCanary;

    import java.io.IOException;
    import java.util.ArrayList;

/**
     * Activity for the multi-tracker app.  This app detects faces and barcodes with the rear facing
     * camera, and draws overlay graphics to indicate the position, size, and ID of each face and
     * barcode.
     */
    public final class GoogleBarcodeScannerActivity extends AppCompatActivity {


        private static final String TAG = "MultiTracker";

        private static final int RC_HANDLE_GMS = 9001;
        // permission request codes need to be < 256
        private static final int RC_HANDLE_CAMERA_PERM = 2;

        private CameraSource mCameraSource = null;
        private CameraSourcePreview mPreview;
        private GraphicOverlay mGraphicOverlay;

        private int limeGreen;
        private int scanCount = 0;
        private FrameLayout snackBarArea;
        private Button doneButton;
        private TextView listOfBarcodeIds;
        private ScrollView barcodeIdScrollview;
        private ArrayList<String> scannedBarcodeIds;



        /**
         * Initializes the UI and creates the detector pipeline.
         */
        @Override
        public void onCreate(Bundle icicle) {

            super.onCreate(icicle);
            setContentView(R.layout.scanner_layout);

            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

            configureViews();
            checkPermissions();
            scannedBarcodeIds = new ArrayList<>();


        }

        //TODO: Change denied permission message
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        public void checkPermissions(){

            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            } else {
                requestCameraPermission();
            }

        }

        public void configureViews(){

            mPreview = (CameraSourcePreview) findViewById(R.id.preview);
            mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
            barcodeIdScrollview = (ScrollView)findViewById(R.id.barcodes_list_scrollview);
            listOfBarcodeIds = (TextView)findViewById(R.id.barcode_list);
            listOfBarcodeIds.setTextColor(Color.RED);
            limeGreen = this.getResources().getColor(R.color.lime_green);
            snackBarArea = (FrameLayout)findViewById(R.id.snackbar_area);
            doneButton = (Button)findViewById(R.id.done_button);
            doneButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }

        public void showScannedBarcodeNotification(String barcodeNumber){

            Snackbar snackbar = Snackbar.make(mPreview, barcodeNumber, Snackbar.LENGTH_LONG);
            View snackBarBackground = snackbar.getView();
            snackBarBackground.setBackgroundColor(limeGreen);
            snackbar.show();
        }


        public boolean idScannedAlready(String barcodeId){

            if(scannedBarcodeIds != null) {

                for(int i = 0; i < scannedBarcodeIds.size(); i++) {

                    if (scannedBarcodeIds.get(i).equals(barcodeId)) {


                        Log.d(TAG, "idScannedAlready: true");
                        return true;
                    }
                }
            }

            scanCount++;
            scannedBarcodeIds.add(barcodeId);
            return false;
        }




        /**
         * Handles the requesting of the camera permission.  This includes
         * showing a "Snackbar" message of why the permission is needed then
         * sending the request.
         */
        private void requestCameraPermission() {
            Log.w(TAG, "Camera permission is not granted. Requesting permission");

            final String[] permissions = new String[]{Manifest.permission.CAMERA};

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
                return;
            }

            final Activity thisActivity = this;

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(thisActivity, permissions,
                            RC_HANDLE_CAMERA_PERM);
                }
            };

            Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, listener)
                    .show();
        }

        /**
         * Creates and starts the camera.  Note that this uses a higher resolution in comparison
         * to other detection examples to enable the barcode detector to detect small barcodes
         * at long distances.
         */
        private void createCameraSource() {


            Context context = getApplicationContext();


            // A barcode detector is created to track barcodes.  An associated multi-processor instance
            // is set to receive the barcode detection results, track the barcodes, and maintain
            // graphics for each barcode on screen.  The factory is used by the multi-processor to
            // create a separate tracker instance for each barcode.
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
            BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, new GraphicTracker.Callback() {
                @Override
                public void onFound(final String barcodeNumber) {


                    showScannedBarcodeNotification(barcodeNumber);


                    if(listOfBarcodeIds != null) {

                        if (!idScannedAlready(barcodeNumber)) {

                            final String pastBarcodes;
                            final String listOfBarcodeIdsText = listOfBarcodeIds.getText().toString();

                            pastBarcodes = listOfBarcodeIds.getText().toString();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (!listOfBarcodeIdsText.isEmpty()) {

                                        listOfBarcodeIds.setText(barcodeNumber + "\n" + pastBarcodes);


                                    } else {

                                        listOfBarcodeIds.setText(barcodeNumber);
                                    }

                                    doneButton.setText("Done(" + scanCount + " items)");

                                }
                            });

                        }

                    }
                }
            });
            barcodeDetector.setProcessor(
                    new MultiProcessor.Builder<>(barcodeFactory).build());


            if (!barcodeDetector.isOperational()) {
                // Note: The first time that an app using the barcode or face API is installed on a
                // device, GMS will download a native libraries to the device in order to do detection.
                // Usually this completes before the app is run for the first time.  But if that
                // download has not yet completed, then the above call will not detect any barcodes
                // and/or faces.
                //
                // isOperational() can be used to check if the required native libraries are currently
                // available.  The detectors will automatically become operational once the library
                // downloads complete on device.
                Log.w(TAG, "Detector dependencies are not yet available.");

                // Check for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                    Log.w(TAG, getString(R.string.low_storage_error));
                }
            }

            // Creates and starts the camera.  Note that this uses a higher resolution in comparison
            // to other detection examples to enable the barcode detector to detect small barcodes
            // at long distances.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1600, 1024)
                    .setRequestedFps(60.0f)
                    .build();
        }

        /**
         * Restarts the camera.
         */
        @Override
        protected void onResume() {
            super.onResume();

            startCameraSource();
        }

        /**
         * Stops the camera.
         */
        @Override
        protected void onPause() {
            super.onPause();
            mPreview.stop();

            if(scannedBarcodeIds != null){

                scannedBarcodeIds = null;
            }
        }


        /**
         * Releases the resources associated with the camera source, the associated detectors, and the
         * rest of the processing pipeline.
         */
        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mCameraSource != null) {
                mCameraSource.release();
            }
        }
        /**
         * Callback for the result from requesting permissions. This method
         * is invoked for every call on {@link #requestPermissions(String[], int)}.
         * <p>
         * <strong>Note:</strong> It is possible that the permissions request interaction
         * with the user is interrupted. In this case you will receive empty permissions
         * and results arrays which should be treated as a cancellation.
         * </p>
         *
         * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
         * @param permissions  The requested permissions. Never null.
         * @param grantResults The grant results for the corresponding permissions
         *                     which is either {@link PackageManager#PERMISSION_GRANTED}
         *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
         * @see #requestPermissions(String[], int)
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode != RC_HANDLE_CAMERA_PERM) {
                Log.d(TAG, "Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }

            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted - initialize the camera source");
                // we have permission, so create the camerasource
                createCameraSource();
                return;
            }

            Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                    " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Barcode Scanner")
                    .setMessage(R.string.no_camera_permission)
                    .setPositiveButton(R.string.ok, listener)
                    .show();
        }

        /**
         * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
         * (e.g., because onResume was called before the camera source was created), this will be called
         * again when the camera source is created.
         */
        private void startCameraSource() {

            // check that the device has play services available.
            int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                    getApplicationContext());
            if (code != ConnectionResult.SUCCESS) {
                Dialog dlg =
                        GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
                dlg.show();
            }

            if (mCameraSource != null) {
                try {
                    mPreview.start(mCameraSource, mGraphicOverlay);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    mCameraSource.release();
                    mCameraSource = null;
                }
            }
        }

    }
