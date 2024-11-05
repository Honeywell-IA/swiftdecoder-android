package com.sample;

import com.honeywell.barcode.CaptureRequestBuilderListener;
import com.honeywell.barcode.HSMDecodeComponent;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.plugins.decode.DecodeResultListener;
import com.sample.plugin.MyCustomPluginResultListener;
import com.honeywell.misc.HSMLog;
import android.hardware.camera2.CaptureRequest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.os.Build;

public class DecodeComponentActivity extends Activity implements DecodeResultListener, MyCustomPluginResultListener
{
	private EditText editTextDisplay;
	private HSMDecoder hsmDecoder;
	private HSMDecodeComponent decCom;
	private int scanCount = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	
    	//stop the device from going to sleep and hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
        
    	setContentView(R.layout.mydecodeactivity);

		decCom = (HSMDecodeComponent)findViewById(R.id.hsm_decodeComponent);

    	editTextDisplay = (EditText)findViewById(R.id.editTextDisplay);
    	editTextDisplay.setEnabled(false);
    	editTextDisplay.setTextColor(Color.WHITE);
    	
    	//get the singleton instance to HSMDecoder
    	hsmDecoder = HSMDecoder.getInstance(this.getApplicationContext());

		hsmDecoder.setCaptureRequestBuilderListener(new CaptureRequestBuilderListener() {
			@Override
			public void OnCaptureRequestBuilderAvailable(CaptureRequest.Builder captureRequestBuilder) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

					//Un-comment this code , if manual camera control is required
					/*
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 400);
                    captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long) 41666666);
                    */

				}
			}
		});
    }

    @Override
    public void onStart() 
    {
    	super.onStart();
		decCom.enableScanning(true);
		scanCount = 0;

    	if( this.getIntent().getBooleanExtra("DEFAULT_ENABLED", false) )
    		hsmDecoder.addResultListener(this);
    	
    	if( this.getIntent().getBooleanExtra("CUSTOM_ENABLED", false) )
    		MainActivity.addCustomPluginListener(this);
    }
    
    @Override
    public void onStop() 
    {
    	super.onStop();
		decCom.enableScanning(false);
		hsmDecoder.setCaptureRequestBuilderListener(null);
		try {
			finish();
		} catch (Exception e) {
			HSMLog.e(e);
		}

		//we need to remove this activity as a listener each time we stop it, because our main activity can't disable default decoding if there are any active listeners

    }

	@Override
	public void onBackPressed() {
		HSMLog.trace();
		System.out.println("editTextDisplay = " + editTextDisplay);
		hsmDecoder.removeResultListener(this);
		MainActivity.removeCustomPluginListener(this);
		hsmDecoder.setCaptureRequestBuilderListener(null);

		try {
			finish();
		} catch (Exception e) {
			HSMLog.e(e);
		}
	}
	private void displayBarcodeData(final HSMDecodeResult[] barcodeData)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				{
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							HSMDecodeResult firstResult = barcodeData[0];
							String msg = "Scan Count: " + ++scanCount + "\n\n" +
									"onHSMDecodeResult\n" +
									"Data: " + firstResult.getBarcodeData() + "\n" +
									"Symbology: " + firstResult.getSymbology() + "\n" +
									"Length: " + firstResult.getBarcodeDataLength()  + "\n" +
									"Decode Time: " + firstResult.getDecodeTime() + "ms";
							editTextDisplay.setText(msg);
						}
					});
				}
			}
		});
	}
	@Override
	public void onHSMDecodeResult(HSMDecodeResult[] barcodeData){
		displayBarcodeData(barcodeData);
		// do something (application specific task)
	}

	@Override
	public void onCustomPluginResult(HSMDecodeResult[] barcodeData)
	{
		displayBarcodeData(barcodeData);
	}
}