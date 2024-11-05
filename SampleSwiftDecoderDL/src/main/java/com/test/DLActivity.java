package com.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.barcode.DecodeManager;
import com.honeywell.barcode.HSMDecodeComponent;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.OCRActiveTemplate;
import com.honeywell.barcode.Symbology;

import com.honeywell.dl.DLDecoder;
import com.honeywell.dl.DLDecoderErrorCode;
import com.honeywell.dl.DLDecoderStatus;
import com.honeywell.dl.DLResultListener;
import com.honeywell.dl.DLScanType;
import com.honeywell.misc.HSMLog;
import com.honeywell.parser.LicenseData;
import com.honeywell.parser.LicenseParser;
import com.honeywell.plugins.decode.DecodeResultListener;

import java.util.ArrayList;

public class DLActivity extends Activity implements DecodeResultListener , DLResultListener {

    private View mScanResultView, mDoneBigBtn;

    private Button front_scan, back_scan;
    private ListView mListViewScanResult;
    private HSMDecoder mHsmDecoder;
    private HSMDecodeComponent mDecodeComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
         //   this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_dlactivity);


        View.OnClickListener doneListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLDecoder.destroyInstance();
                finish();
                setScanResultShowing(false);
            }
        };
        mDoneBigBtn = findViewById(R.id.view_scan_result_done);
        front_scan = findViewById(R.id.frontscan);
        back_scan = findViewById(R.id.backscan);

        enableDLScanning(true, DLScanType.FRONT_SCAN);

        back_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("LicenseParser.getVersion() = " + LicenseParser.getVersion());
                mHsmDecoder.enableSymbology(Symbology.PDF417);
                enableDLScanning(true,DLScanType.BACK_SCAN);
                mHsmDecoder.setOverlayText(" Place the back of your DL completely within the preview");


            }
        });

        front_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHsmDecoder.disableSymbology(Symbology.PDF417);
                enableDLScanning(true,DLScanType.FRONT_SCAN);

                mHsmDecoder.setOverlayText(" Place the front of your DL completely within the preview");

            }
        });

        mDoneBigBtn.setOnClickListener(doneListener);
        mHsmDecoder = HSMDecoder.getInstance(this.getApplicationContext());
        mListViewScanResult = (ListView) findViewById(R.id.listview_scan_result);
        mDecodeComponent = (HSMDecodeComponent) findViewById(R.id.hsm_decode_component);
        mScanResultView = findViewById(R.id.card_scan_result);
       // mHsmDecoder.enableSymbology(Symbology.OCR);
       // mHsmDecoder.setOCRActiveTemplate(OCRActiveTemplate.DRIVER_LICENSE);
        mHsmDecoder.addResultListener(this);
        mHsmDecoder.setOverlayText(" Place the front of your DL completely within the preview");
        setScanResultShowing(false);
    }

    private void enableDLScanning(boolean isDL,DLScanType dlScanType) {
      DLDecoderStatus status =  DLDecoder.getInstance(getApplicationContext()).setDLScanFeature(isDL,getApplicationContext(), dlScanType);
        System.out.println("status = " + status);
        if(status == DLDecoderStatus.ENABLE_SUCCESS) {
            DLDecoder.getInstance(getApplicationContext()).addResultListener(this);
        }else{
            Toast.makeText(this, ""+status, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        HSMLog.trace();
        DLDecoder.destroyInstance();
        mHsmDecoder.removeResultListener(this);
        mHsmDecoder.enableDecoding(false);
        mDecodeComponent.dispose();

       // setScanResultShowing(false);
        finish();

    }
    @Override
    public void onStop()
    {
        super.onStop();
        DLDecoder.getInstance(this).dispose();
        mHsmDecoder.removeResultListener(this);
        mDecodeComponent.dispose();
      //  setScanResultShowing(false);
        finish();
    }
//    @Override
//    public void onHSMDecodeResult( LicenseData licenseData) {
//        showResultView(licenseData);
//    }
//
    private void showResultView(LicenseData data){
        setScanResultShowing(true);

        if(data != null){
            ArrayList<IDDataAdapter.IDData> idArrayList = new ArrayList<IDDataAdapter.IDData>();
            updateDLDataList(data, idArrayList);
            IDDataAdapter adapter = new IDDataAdapter(getApplicationContext(), idArrayList);
            mListViewScanResult.setAdapter(adapter);
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        mDecodeComponent.enableScanning(true);
    }

    private void showErrorToast(String Message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setScanResultShowing(true);
                Toast.makeText(getApplicationContext(), ""+Message, Toast.LENGTH_SHORT).show();
               // finish();
            }

        });

    }

    private void showLicenseErrorToast(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Parsing Failed as EZDL License is NOT Enabled", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void setScanResultShowing(final boolean showing) {

        mHsmDecoder.enableDecoding(!showing);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mScanResultView.setVisibility(showing ? View.VISIBLE : View.GONE);
               mDoneBigBtn.setVisibility(!showing ? View.GONE : View.VISIBLE);
            }
        });

    }

    @Override
    public void onHSMDecodeResult(HSMDecodeResult[] results) {

    }

    @Override
    public void onDLDecodeResult(LicenseData licensedata) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showResultView(licensedata);
            }
        });

    }

    @Override
    public void onDLDecodeError(DLDecoderErrorCode error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showErrorToast(error.toString());
            }
        });
    }



    public static void updateDLDataList(LicenseData license, ArrayList<IDDataAdapter.IDData> dlDataList){

        //Just to add a spacing at start of list item added with empty spaces
        dlDataList.add(new IDDataAdapter.IDData("     ","      "));
       // dlDataList.add(new IDDataAdapter.IDData("DL IN:",license.DLIIN != null ?license.DLIIN :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Name Last:",license.DLNameLast!= null ?license.DLNameLast :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Name First:", license.DLNameFirst!= null ?license.DLNameFirst :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Address1:", license.DLAddress1!= null ?license.DLAddress1 :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL IDNumber:", license.DLIDNumber!= null ?license.DLIDNumber :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Class:", license.DLClass!= null ?license.DLClass :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Restrictions:", license.DLRestrictions!= null ?license.DLRestrictions :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Endorsements:", license.DLEndorsements!= null ?license.DLEndorsements :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL BirthDate:", license.DLBirthDate!= null ?license.DLBirthDate :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Expires:", license.DLExpires!= null ?license.DLExpires :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Sex:", license.DLSex!= null ?license.DLSex :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL IssueDate:", license.DLIssueDate!= null ?license.DLIssueDate :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL Country:", license.DLCountry!= null ?license.DLCountry :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL PlaceOfBirth:", license.DLPlaceOfBirth!= null ?license.DLPlaceOfBirth :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL StdVehicleClass:", license.DLStdVehicleClass!= null ?license.DLStdVehicleClass :"NA"));
        dlDataList.add(new IDDataAdapter.IDData("DL IssuingAuthority:", license.DLIssuingAuthority!= null ?license.DLIssuingAuthority :"NA"));

    }
}