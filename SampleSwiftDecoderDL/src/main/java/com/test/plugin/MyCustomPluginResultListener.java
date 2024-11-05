package com.test.plugin;

import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.plugins.PluginResultListener;

//this interface defines how the custom plug-in will return results to its listeners (must extend PluginResultListener)
public interface MyCustomPluginResultListener extends PluginResultListener
{
	public void onCustomPluginResult(HSMDecodeResult[] barcodeData);
}