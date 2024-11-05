package com.test;

import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.Resolutions;
import com.honeywell.barcode.Symbology;
import com.honeywell.dl.DLDecoder;
import com.honeywell.dl.DLScanType;
import com.honeywell.license.ActivationManager;
import com.honeywell.license.ActivationResult;
import com.honeywell.plugins.PluginResultListener;
import com.honeywell.plugins.decode.DecodeResultListener;
import com.test.plugin.MyCustomPlugin;
import com.test.plugin.MyCustomPluginResultListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import java.util.ArrayList;

/* The purpose of this sample code is to show the multiple different ways you can use bar code scanning functionality in your application.
 * Bar code scanning can be achieved in the following 3 ways:
 * 1) You can let HSMDecoder handle everything for you by simply calling hsmDecoder.scanBarcode();
 * 2) You can embed an HSMDecodeComponent into your own activity and size it how you see fit
 * 3) You can create a custom SwiftPlugin to completely control the look and operation of a scan event.  
 *    This custom plug-in must be registered with HSMDecoder and can be used with either method 1 or 2 mentioned above.
 */
public class MainActivity extends Activity implements DecodeResultListener, MyCustomPluginResultListener
{		
    private HSMDecoder hsmDecoder;
    private TextView  tvsdk, tvDeviceId, tvdl;

    private static MyCustomPlugin customPlugin;
	public static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
	public  String entitlementID = "Insert-your-key-here";


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

			LinearLayout mainLayout = findViewById(R.id.main_layout);
            //activate the API with your license key
            ActivationResult activationResult = ActivationManager.activateEntitlement(this, entitlementID);
	        Toast.makeText(this, "Activation Result: " + activationResult, Toast.LENGTH_LONG).show();


	        if(activationResult.getValue() == 1){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mainLayout.setBackground(getApplicationContext().getDrawable(R.drawable.gradient_green));
				//	DLDecoder.getInstance(getApplicationContext()).setDLScanFeature(true,getApplicationContext(), DLScanType.FRONT_SCAN.getValue());
				}
				else{
					mainLayout.setBackgroundDrawable(getApplicationContext().getDrawable(R.drawable.gradient_green));
				}
			}
	        //get the singleton instance of the decoder
        	hsmDecoder = HSMDecoder.getInstance(this);
        	
	        //set all decoder related settings

            hsmDecoder.enableFlashOnDecode(false);
            hsmDecoder.enableSound(true);
            hsmDecoder.enableAimer(true);
            hsmDecoder.setAimerColor(Color.RED);
            hsmDecoder.setOverlayText("Place barcode completely inside viewfinder!");
            hsmDecoder.setOverlayTextColor(Color.WHITE); 
            hsmDecoder.addResultListener(this);
            
            //instantiate custom plug-in and set this activity as a listener
            customPlugin = new MyCustomPlugin(getApplicationContext());
            customPlugin.addResultListener(this);

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
    	
    	//dispose of you custom plug-in
    	customPlugin.dispose();
    }



    

    
	@Override
	public void onHSMDecodeResult(HSMDecodeResult[] barcodeData)
	{
		//handle results from the default decoding functionality
		displayBarcodeData(barcodeData);
	}
	
	@Override
	public void onCustomPluginResult(HSMDecodeResult[] barcodeData) 
	{
		//handle results from your custom plug-in
		displayBarcodeData(barcodeData);
	}

	private void displayBarcodeData(final HSMDecodeResult[] barcodeData)
	{


	}
	
	private void initGuiElements()
	{
		//stop the device from going to sleep and hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        
        //inflate the base UI layer
        setContentView(R.layout.main);


		Button buttonDLScan = (Button)findViewById(R.id.buttonEnableDl);
		buttonDLScan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

						//create the DecodeComponentActivity intent
						Intent decActivityIntent = new Intent(MainActivity.this, DLActivity.class);

						startActivity(decActivityIntent);
					}
				});

			//	DLDecoder.getInstance(getApplicationContext()).setDLScanFeature(true,getApplicationContext(), DLScanType.FRONT_SCAN.getValue());
		LinearLayout resolutionLayout = findViewById(R.id.layout_resolution);
		Button setResolution = findViewById(R.id.setresolution);

		ArrayList<Resolutions> camera_resolutions = HSMDecoder.getInstance(getApplicationContext()).getSupportedCameraResolutions();
		if(camera_resolutions != null && camera_resolutions.size() >0){
			RadioGroup resolutionRadioGroup = new RadioGroup(this);
			resolutionRadioGroup.setOrientation(RadioGroup.VERTICAL);
			for(Resolutions resolutions : camera_resolutions){
				RadioButton radioButtonresolution = new RadioButton(this);
				radioButtonresolution.setText(resolutions.toString());
				radioButtonresolution.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
						if(checked){


							setResolution.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									System.out.println("compoundButton.getText().toString() = " + compoundButton.getText().toString());
									Resolutions resolution =  resolutions;
									HSMDecoder.getInstance(getApplicationContext()).setCameraResolutions(resolutions);
								}
							});
						}
					}
				});
				resolutionRadioGroup.addView(radioButtonresolution);

			}
			resolutionLayout.addView(resolutionRadioGroup);
		}

    

        tvsdk = (TextView) findViewById(R.id.textViewsdk);
		tvDeviceId = (TextView)findViewById(R.id.textViewdeviceId);
		tvDeviceId.setText("Device Id: "+getDeviceID());
		tvdl = (TextView) findViewById(R.id.textViewsdkdl);
		tvsdk.setText("SDK: "+hsmDecoder.getAPIVersion());
		tvdl.setText("DL: "+DLDecoder.getInstance(this).getDLVersion());

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
	
	public static void addCustomPluginListener(PluginResultListener listener)
	{
		customPlugin.addResultListener(listener);
	}
	
	public static void removeCustomPluginListener(PluginResultListener listener)
	{
		customPlugin.removeResultListener(listener);
	}

	public String getDeviceID(){
		return ActivationManager.getDeviceId(this,entitlementID);
	}
}