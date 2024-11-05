package com.openocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.Resolutions;
import com.honeywell.license.ActivationManager;
import com.honeywell.license.ActivationResult;
import com.honeywell.plugins.PluginResultListener;
import com.honeywell.plugins.decode.DecodeResultListener;

import java.util.ArrayList;

/* The purpose of this sample code is to show the multiple different ways you can use bar code scanning functionality in your application.
 * Bar code scanning can be achieved in the following 3 ways:
 * 1) You can let HSMDecoder handle everything for you by simply calling hsmDecoder.scanBarcode();
 * 2) You can embed an HSMDecodeComponent into your own activity and size it how you see fit
 * 3) You can create a custom SwiftPlugin to completely control the look and operation of a scan event.
 *    This custom plug-in must be registered with HSMDecoder and can be used with either method 1 or 2 mentioned above.
 */
public class MainActivity extends Activity
{
    private HSMDecoder hsmDecoder;
    private TextView tvsdk, tvDeviceId, tvocr,tvdecoder;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    public  String entitlementID = "Insert-your-key-here";
    public EditText editEntitlementKey;
    public Button activate;
    LinearLayout mainLayout;
    ActivationResult  activationResult;
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    onBackPressed();
                }
                return;
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);

            //initialize GUI
            initGuiElements();

             mainLayout = findViewById(R.id.main_layout);
            updateMainBackground();
            //get the singleton instance of the decoder
            hsmDecoder = HSMDecoder.getInstance(this);

            //set all decoder related settings

            hsmDecoder.enableFlashOnDecode(false);
            hsmDecoder.enableSound(true);
            hsmDecoder.enableAimer(true);
            hsmDecoder.setAimerColor(Color.RED);
            hsmDecoder.setOverlayText("Place barcode completely inside viewfinder!");
            hsmDecoder.setOverlayTextColor(Color.WHITE);


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //dispose of the decoder instance, this stops the underlying camera service and releases all associated resources
        HSMDecoder.disposeInstance();


    }

    private void initGuiElements()
    {
        //stop the device from going to sleep and hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //inflate the base UI layer
        setContentView(R.layout.main);
        editEntitlementKey = findViewById(R.id.editTextForEntitlement);
        activate = findViewById(R.id.buttonActivate);
             activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String entitlementData = editEntitlementKey.getText().toString().trim();
                activationResult = ActivationManager.activateEntitlement(getApplicationContext(),entitlementData);
                updateMainBackground();
                Toast.makeText(getApplicationContext(),"activationResult = "+activationResult,Toast.LENGTH_LONG).show();

            }
        });

        Button buttonScan = (Button)findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (ActivationManager.isActivated(getApplicationContext())) {
                    Intent decActivityIntent = new Intent(MainActivity.this, ScanAndOCRActivity.class);

                    startActivity(decActivityIntent);
                } else {
Toast.makeText(getApplicationContext(),"App is Not Activated",Toast.LENGTH_SHORT).show();
                }
            }
        });




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                return;
            }
        }
    }

    public void updateMainBackground()
    {
        if(ActivationManager.isActivated(this)  ){
            tvsdk = (TextView) findViewById(R.id.textViewsdk);
            tvDeviceId = (TextView)findViewById(R.id.textViewdeviceId);
            tvDeviceId.setText("Device Id: "+getDeviceID());
            tvocr = (TextView) findViewById(R.id.textViewOCRSdk);
            tvdecoder = (TextView)findViewById(R.id.textViewDecoder);
            tvsdk.setText("SDK: "+hsmDecoder.getAPIVersion());
            tvocr.setText("SwiftOCR SDK : "+com.honeywell.BuildConfig.VERSION_NAME);
            tvdecoder.setText("Decoder : "+hsmDecoder.getDecoderVersion());
            //change the background to green if the license is activated
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainLayout.setBackground(getApplicationContext().getDrawable(R.drawable.gradient_green));
            }else{
                mainLayout.setBackgroundDrawable(getApplicationContext().getDrawable(R.drawable.gradient_green));
            }
        }else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainLayout.setBackground(getApplicationContext().getDrawable(R.drawable.gradient));
            }else{
                mainLayout.setBackgroundDrawable(getApplicationContext().getDrawable(R.drawable.gradient));
            }
        }

    }
    public String getDeviceID(){
        return ActivationManager.getDeviceId(this,entitlementID);
    }
}
