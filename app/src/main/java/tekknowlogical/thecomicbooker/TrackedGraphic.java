package tekknowlogical.thecomicbooker;

import android.graphics.Canvas;

/**
 * Created by Tim on 1/3/2017.
 */

abstract class TrackedGraphic<T> extends GraphicOverlay.Graphic {


    /**
     * Common base class for defining graphics for a particular item type.  This along with
     * {@link GraphicTracker} avoids the need to duplicate this code for both the face and barcode
     * instances.
     */

    private int mId;

    TrackedGraphic(GraphicOverlay overlay) {
        super(overlay);
    }

    void setId(int id) {
        mId = id;
    }

    protected int getId() {
        return mId;
    }

    abstract void updateItem(T item);
}

