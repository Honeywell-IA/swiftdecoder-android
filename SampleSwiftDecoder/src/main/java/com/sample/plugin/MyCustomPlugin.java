package com.sample.plugin;

import java.util.List;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.plugins.PluginResultListener;
import com.honeywell.plugins.SwiftPlugin;
import com.sample.R;

/* 
 * This plug-in does nothing more than render text in the middle of the screen and silently return decode results to its listeners 
 */
public class MyCustomPlugin extends SwiftPlugin
{
	private TextView tvMessage;
	private int clickCount = 0;

	public MyCustomPlugin(Context context)
	{
		super(context);
		
		//inflate the base UI layer
		View.inflate(context, R.layout.my_custom_plugin, this);
		
		tvMessage = (TextView)findViewById(R.id.textViewMsg);
		
		Button buttonHello = (Button)findViewById(R.id.buttonHello);
		buttonHello.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				tvMessage.setText("Custom Plugin: UI button clicked " + ++clickCount + " times");
			}
		});
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public void onDecode(HSMDecodeResult[] results)
	{
		super.onDecode(results);
		
		notifyListeners(results);
	}
	
	@Override
	protected void onDecodeFailed()
	{
		super.onDecodeFailed();
	}
	
	@Override
	protected void onImage(byte[] image, int width, int height)
	{
		super.onImage(image, width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	} 
	
	private void notifyListeners(HSMDecodeResult[] results)
	{
		//tells all plug-in monitor event listeners we have a result (used by the system)
		this.finish();
				
		//notify all plug-in listeners we have a result
		List<PluginResultListener> listeners = this.getResultListeners();
		for(PluginResultListener listener : listeners)
			((MyCustomPluginResultListener)listener).onCustomPluginResult(results);
	}
}
