package tekknowlogical.thecomicbooker;

import android.Manifest;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends AppCompatActivity
    implements ZXingScannerView.ResultHandler{

    private ZXingScannerView zXingScannerView;

    private static final String FLASH_STATE = "flash_state";
    private static final String AUTO_FOCUS_STATE = "auto_focus_state";
    private static final String SELECTED_FORMATS = "selected_formats";
    private static final String CAMERA_ID = "camera_id";

    private boolean flash;
    private boolean autoFocus;

    private int cameraID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);


        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        zXingScannerView = new ZXingScannerView(this);

        Button button = (Button)findViewById(R.id.open_barcode);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, 1);
        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeScanner(v);
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner();
    }

    @Override
    public void handleResult(Result result) {

        try{
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 200);
           // toneGenerator.startTone(ToneGenerator., 200);
        }catch (Exception e){

            Log.v("UMMMM", "YAAHHHHS");
        }
    }

    public void barcodeScanner(){

        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
        zXingScannerView.setFlash(false);
        zXingScannerView.setAutoFocus(true);


    }


}
