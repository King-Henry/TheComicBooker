package tekknowlogical.thecomicbooker;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;

/**
 * Created by Tim on 1/3/2017.
 */

public class GraphicTracker<T> extends Tracker<T> {

    private GraphicOverlay mOverlay;
    private TrackedGraphic<T> mGraphic;
    private Callback callback;
    private boolean doNotCallback;
    private boolean doNotCallbackNewItem;

    private ArrayList<Barcode> barcodeItems = new ArrayList<>();

    GraphicTracker(GraphicOverlay overlay, TrackedGraphic<T> graphic, Callback callback) {
        mOverlay = overlay;
        mGraphic = graphic;
        this.callback = callback;
    }

    public interface Callback {

        void onFound(String barcodeNumber);
    }

    private boolean doNotCallback(T item) {

        for (Barcode a : barcodeItems) {

            if (a.rawValue.equals(((Barcode) item).rawValue)) {

                return true;
            }

        }

        return false;

    }


    /**
     * Start tracking the detected item instance within the item overlay.
     */
    @Override
    public void onNewItem(int id, T item) {

        //doNotCallbackNewItem
        mGraphic.setId(id);
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    @Override
    public void onUpdate(Detector.Detections<T> detectionResults, T item) {

        doNotCallback = doNotCallback(item);

        if(!doNotCallback) {

            barcodeItems.add((Barcode)item);
            callback.onFound(((Barcode) item).rawValue);
        }
        mOverlay.add(mGraphic);
        mGraphic.updateItem(item);
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily, for example if the face was momentarily blocked from
     * view.
     */
    @Override
    public void onMissing(Detector.Detections<T> detectionResults) {
        mOverlay.remove(mGraphic);
    }

    /**
     * Called when the item is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }
}
