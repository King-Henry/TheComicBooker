package tekknowlogical.thecomicbooker;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Tim on 1/3/2017.
 */

public class CameraSourcePreview extends ViewGroup {

    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
        mSurfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                cameraFocus(mCameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

            }
        });
    }

    private static boolean cameraFocus(@NonNull CameraSource cameraSource,
                                       @NonNull String focusMode){

        Field[] fields = CameraSource.class.getDeclaredFields();

        for(Field field : fields) {

            if(field.getType() == Camera.class) {

                field.setAccessible(true);
                try {

                    Camera camera = (Camera)field.get(cameraSource);
                    if(camera != null){
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFocusMode(focusMode);
                       // parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        return true;
                    }

                    return false;
                }catch (IllegalAccessException e){

                    e.printStackTrace();
                }

                break;
            }
        }

        return false;
    }

    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws IOException,SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    //Thank you to pm0733464 in GitHub issue threads for this code

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        // Computes height and width for potentially doing fit width.



        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float ratioOfWidth =  (float) viewWidth / (float) previewWidth;
        float ratioOfHeight = (float) viewHeight / (float) previewHeight;

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.



        if (ratioOfWidth > ratioOfHeight) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * ratioOfWidth);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * ratioOfHeight);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }


        // If height is too tall using fit width, does fit height instead.
        /*if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * width);
        }
*/
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(-1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
