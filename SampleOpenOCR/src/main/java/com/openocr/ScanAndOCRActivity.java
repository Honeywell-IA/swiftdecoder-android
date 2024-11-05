package com.openocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.honeywell.barcode.HSMDecodeComponent;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.Symbology;
import com.honeywell.misc.HSMLog;
import com.honeywell.plugins.decode.DecodeResultListener;
import com.honeywell.swiftocr.SwiftOCRDecoder;
import com.honeywell.swiftocr.SwiftOCRResult;
import com.honeywell.swiftocr.SwiftOCRResultListener;
import com.honeywell.swiftocr.SwiftOCRStatus;
import com.honeywell.swiftocr.SwiftOCRTemplateResult;
import com.honeywell.swiftocr.TargetedSingleROIConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScanAndOCRActivity extends Activity implements DecodeResultListener, SwiftOCRResultListener {

    private View mScanResultView, mDoneBigBtn;
    private ListView mListViewScanResult;
    private HSMDecoder mHsmDecoder;
    private HSMDecodeComponent mDecodeComponent;
    private Button textZoomButton,OCRScanROIOrientation,OCRScanArea;
    boolean addTemplateIntent =  false;
    boolean finishCalled = false;
    Switch OCRSwitch;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scanocr);
        context = this;
        View.OnClickListener doneListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScanResultShowing(false);
            }
        };
        mDoneBigBtn = findViewById(R.id.view_scan_result_done);
        mDoneBigBtn.setOnClickListener(doneListener);
        mHsmDecoder = HSMDecoder.getInstance(this.getApplicationContext());
        mListViewScanResult = (ListView) findViewById(R.id.listview_scan_result);
        mDecodeComponent = (HSMDecodeComponent) findViewById(R.id.hsm_decode_component);
        mScanResultView = findViewById(R.id.card_scan_result);
        textZoomButton = findViewById(R.id.OCRScanZoom);
        textZoomButton.setVisibility(View.VISIBLE);

        textZoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double zoomratio = HSMDecoder.getInstance(context).getZoomRatio();
                if(zoomratio == 1.0){
                    HSMDecoder.getInstance(context).setZoomRatio(2.0);
                    textZoomButton.setText("3x");
                }else if(zoomratio == 2.0){
                    HSMDecoder.getInstance(context).setZoomRatio(3.0);
                    textZoomButton.setText("1x");
                }else if(zoomratio == 3.0){
                    HSMDecoder.getInstance(context).setZoomRatio(1.0);
                    textZoomButton.setText("2x");
                }
            }
        });

        //Handle on first display
        double zoomratio = HSMDecoder.getInstance(context).getZoomRatio();
        if(zoomratio == 1.0){
            textZoomButton.setText("2x");
        }else if(zoomratio == 2.0){
            textZoomButton.setText("3x");
        }else if(zoomratio == 3.0){
            textZoomButton.setText("1x");
        }

        OCRSwitch = (Switch) findViewById(R.id.OCRSwitch);
        OCRSwitch.setChecked(false);
        configureScan();
        OCRSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                        configureGenericOCR();
                        configureButtons();
                             }
                else
                {
                    configureScan();
                    configureButtons();
                }

            }
        });

        setScanResultShowing(false);
    }


    @Override
    public void onBackPressed() {
        HSMLog.trace();

        SwiftOCRDecoder.getInstance(context).enableSwiftOCRFeature(false);

        SwiftOCRDecoder.getInstance(context).removeResultListener(this);
        mHsmDecoder.removeResultListener(this);
        mDecodeComponent.dispose();
        setScanResultShowing(false);

        finish();

    }
    @Override
    public void onStop()
    {
        super.onStop();
        mDecodeComponent.enableScanning(false);
        SwiftOCRDecoder.getInstance(context).removeResultListener(this);
        mHsmDecoder.removeResultListener(this);
        setScanResultShowing(false);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        mDecodeComponent.enableScanning(true);
    }


    private void setScanResultShowing(final boolean showing) {
        //Specifically calling Deocdemanager nor HSmDecoder as decodeManager instance is destroyed during license activation
        HSMDecoder.getInstance(getApplicationContext()).enableDecoding(!showing);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!addTemplateIntent) {
                    mScanResultView.setVisibility(showing ? View.VISIBLE : View.GONE);
                    mDoneBigBtn.setVisibility(!showing ? View.GONE : View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onHSMDecodeResult(HSMDecodeResult[] results) {

        showResultView(results);
    }

   public static void updateDataList(HSMDecodeResult result, ArrayList<IDDataAdapter.IDData> dlDataList){
        dlDataList.add(new IDDataAdapter.IDData(result.getBarcodeData(), ""));
       dlDataList.add(new IDDataAdapter.IDData("Decode Time : ", String.valueOf(result.getDecodeTime()) + " ms"));
       dlDataList.add(new IDDataAdapter.IDData("     ","      "));
    }
private void showResultView(final HSMDecodeResult[] results)
{
    runOnUiThread(new Runnable() {
        @Override
        public void run() {


            try {
                if (results != null && results.length > 0)
                {
                    setScanResultShowing(true);

                    ArrayList<IDDataAdapter.IDData> idArrayList = new ArrayList<IDDataAdapter.IDData>();
                    int ocr_box_index = 0;
                    for (HSMDecodeResult res : results) {
                           updateDataList( res, idArrayList);
                    }

                    IDDataAdapter adapter = new IDDataAdapter(getApplicationContext(), idArrayList);
                    mListViewScanResult.setAdapter(adapter);
                }

            }  catch (Exception e)
            {
                HSMLog.e(e);
            }
        }
    });
}
public void configureGenericOCR()
{
    mHsmDecoder.removeResultListener(this);
    SwiftOCRDecoder.getInstance(this.getApplicationContext()).setSwiftOCRScanArea(SwiftOCRDecoder.SwiftOCRScanArea.TARGETED_SINGLE_ROI);
    SwiftOCRDecoder.getInstance(this.getApplicationContext()).setOCRDetectionMode(SwiftOCRDecoder.SwiftOCRDetectionMode.OPEN_OCR);
    TargetedSingleROIConfig targetedSingleROIConfig = new TargetedSingleROIConfig();
    targetedSingleROIConfig.targetedSingleROIPosition= TargetedSingleROIConfig.TargetedSingleROIPosition.CENTER;
    targetedSingleROIConfig.targetedSingleROIOrientation= TargetedSingleROIConfig.TargetedSingleROIOrientation.TARGETED_SINGLE_ROI_ORIENTATION_HORIZONTAL;
    Map<String,String> ocrFormat = new HashMap<>();
    ocrFormat.put("regex","");
    ocrFormat.put("whitelist","");
    targetedSingleROIConfig.setDetectedOCRFormat(ocrFormat);
    SwiftOCRDecoder.getInstance(this).setTargetedSingleROIConfig(targetedSingleROIConfig);
    SwiftOCRStatus enableStatus = SwiftOCRDecoder.getInstance(this.getApplicationContext()).enableSwiftOCRFeature(true);
    if(enableStatus != SwiftOCRStatus.SUCCESS){
        Toast.makeText(getApplicationContext(), "enableStatus Result: " + enableStatus, Toast.LENGTH_LONG).show();
        return;
    }

    SwiftOCRDecoder.getInstance(context).addResultListener(this);
}

public void configureScan()
{
    SwiftOCRDecoder.getInstance(this.getApplicationContext()).setOCRDetectionMode(SwiftOCRDecoder.SwiftOCRDetectionMode.TEMPLATE_OCR);
    SwiftOCRDecoder.getInstance(context).removeResultListener(this);
    SwiftOCRDecoder.getInstance(this.getApplicationContext()).enableSwiftOCRFeature(false);
    mHsmDecoder.enableSymbology(Symbology.UPCA);
    mHsmDecoder.enableSymbology(Symbology.CODE128);
    mHsmDecoder.enableSymbology(Symbology.CODE39);
    mHsmDecoder.enableSymbology(Symbology.QR);
    mHsmDecoder.enableSymbology(Symbology.DATAMATRIX);
    mHsmDecoder.enableFlashOnDecode(false);
    mHsmDecoder.enableSound(true);
    mHsmDecoder.enableAimer(true);
    mHsmDecoder.setAimerColor(Color.RED);
    mHsmDecoder.setOverlayText("Place barcode completely inside viewfinder!");
    mHsmDecoder.setOverlayTextColor(Color.WHITE);
    mHsmDecoder.addResultListener(this);
}
    public void configureButtons()
    {

        OCRScanROIOrientation = findViewById(R.id.OCRScanROIOrientation);
        OCRScanArea=findViewById(R.id.OCRScanArea);
        if(SwiftOCRDecoder.getInstance(context).getOCRDetectionMode()== SwiftOCRDecoder.SwiftOCRDetectionMode.OPEN_OCR)
        {
            OCRScanArea.setVisibility(View.VISIBLE);
            OCRScanArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SwiftOCRDecoder.getInstance(context).getSwiftOCRScanArea() == SwiftOCRDecoder.SwiftOCRScanArea.TARGETED_SINGLE_ROI) {
                        OCRScanROIOrientation.setVisibility(View.GONE);
                        OCRScanArea.setText("SINGLE-ROI");
                        SwiftOCRDecoder.getInstance(context).setSwiftOCRScanArea(SwiftOCRDecoder.SwiftOCRScanArea.FULL_PREVIEW);
                    } else if  (SwiftOCRDecoder.getInstance(context).getSwiftOCRScanArea() == SwiftOCRDecoder.SwiftOCRScanArea.FULL_PREVIEW) {
                        OCRScanROIOrientation.setVisibility(View.VISIBLE);
                        OCRScanArea.setText("FULL-PREVIEW");
                        SwiftOCRDecoder.getInstance(context).setSwiftOCRScanArea(SwiftOCRDecoder.SwiftOCRScanArea.TARGETED_SINGLE_ROI);
                    }
                }
            });
            if((SwiftOCRDecoder.getInstance(context).getSwiftOCRScanArea()== SwiftOCRDecoder.SwiftOCRScanArea.TARGETED_SINGLE_ROI)&&(SwiftOCRDecoder.getInstance(context).getTargetedSingleROIConfig().targetedSingleROIPosition== TargetedSingleROIConfig.TargetedSingleROIPosition.CENTER)) {

                OCRScanROIOrientation.setVisibility(View.VISIBLE);
                OCRScanROIOrientation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (SwiftOCRDecoder.getInstance(context).getTargetedSingleROIConfig().targetedSingleROIOrientation == TargetedSingleROIConfig.TargetedSingleROIOrientation.TARGETED_SINGLE_ROI_ORIENTATION_HORIZONTAL) {
                            OCRScanROIOrientation.setText("ROI-H");
                            SwiftOCRDecoder.getInstance(context).switchTargetedSingleROIOrientation();
                        } else if (SwiftOCRDecoder.getInstance(context).getTargetedSingleROIConfig().targetedSingleROIOrientation == TargetedSingleROIConfig.TargetedSingleROIOrientation.TARGETED_SINGLE_ROI_ORIENTATION_VERTICAL) {
                            OCRScanROIOrientation.setText("ROI-V");
                            SwiftOCRDecoder.getInstance(context).switchTargetedSingleROIOrientation();
                        }
                    }
                });

                if (SwiftOCRDecoder.getInstance(context).getTargetedSingleROIConfig().targetedSingleROIOrientation == TargetedSingleROIConfig.TargetedSingleROIOrientation.TARGETED_SINGLE_ROI_ORIENTATION_HORIZONTAL) {
                    OCRScanROIOrientation.setText("ROI-V");
                } else if (SwiftOCRDecoder.getInstance(context).getTargetedSingleROIConfig().targetedSingleROIOrientation == TargetedSingleROIConfig.TargetedSingleROIOrientation.TARGETED_SINGLE_ROI_ORIENTATION_VERTICAL) {
                    OCRScanROIOrientation.setText("ROI-H");
                }
            }

        }
        else {
            OCRScanArea.setVisibility(View.GONE);
            OCRScanROIOrientation.setVisibility(View.GONE);
        }
    }
    public static void updateAdvOcrDataList(String tag, SwiftOCRResult result, ArrayList<IDDataAdapter.IDData> dlDataList){
        dlDataList.add(new IDDataAdapter.IDData(tag, ""));
        dlDataList.add(new IDDataAdapter.IDData(result.getOCRData(), ""));
        dlDataList.add(new IDDataAdapter.IDData("Decode Time : ", String.valueOf(result.getOCRRecognitionTime()) + " ms"));
        dlDataList.add(new IDDataAdapter.IDData("     ","      "));
    }

    @Override
    public void onSwiftOCRResult(SwiftOCRResult[] swiftOCRResults) {
        showGenericOCRResult(swiftOCRResults);
    }

    @Override
    public void onSwiftOCRTemplateResult(SwiftOCRTemplateResult[] swiftOCRTemplateResults) {

    }

    private void showGenericOCRResult(final SwiftOCRResult[] swiftOCRResults)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (swiftOCRResults != null && swiftOCRResults.length > 0)
                    {
                        setScanResultShowing(true);

                            ArrayList<IDDataAdapter.IDData> idArrayList = new ArrayList<IDDataAdapter.IDData>();
                            int ocr_box_index = 0;
                            for (SwiftOCRResult ocrRes : swiftOCRResults) {
                                String ocr_tag =  "OCR-" + String.valueOf(++ocr_box_index);
                                updateAdvOcrDataList(ocr_tag, ocrRes, idArrayList);
                            }

                            IDDataAdapter adapter = new IDDataAdapter(getApplicationContext(), idArrayList);
                            mListViewScanResult.setAdapter(adapter);
                    }

                }  catch (Exception e)
                {
                    HSMLog.e(e);
                }
            }
        });
    }


}

