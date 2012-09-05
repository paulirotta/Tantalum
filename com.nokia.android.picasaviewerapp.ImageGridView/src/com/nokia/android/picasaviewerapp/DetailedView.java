package com.nokia.android.picasaviewerapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.futurice.tantalum3.Closure;
import com.nokia.example.picasa.common.Storage;

/**
 * View for displaying the bigger image and infomation of an image.
 * 
 *
 */
public class DetailedView extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailed_view);
		
		// Get the information from the intent
		String title = getIntent().getExtras().getString("title");
		if (title != null) {
			((TextView) findViewById(R.id.imageTitle)).setText(title);
		}
		
		String author = getIntent().getExtras().getString("author");
		if (author != null) {
			((TextView) findViewById(R.id.authorName)).setText(author);
		}

		String url = getIntent().getExtras().getString("url");
		if (url != null) {
			ImageView iv = (ImageView) findViewById(R.id.bigImage);
			
			// Get the image from storage (or the web)
			Storage.imageCache.get(url, new ImageResult(iv));
		}

	}

	
	/** 
	 * A runnable result that is passed to storage. The result is set in the background thread,
	 * but the run method is ran in the UI thread, enabling UI changes depending on the result.
	 */
	protected class ImageResult extends Closure {
		private ImageView view;

		public ImageResult(ImageView iv) {
			this.view = iv;
		}

		public void run(){
			try {
				view.setImageBitmap((Bitmap) get());
			} catch (Exception e) {
				Log.e(ImageGridView.LOG_MAIN_ACTIVITY, "Can not setImageBitmap", e);
			}
		}
	}
}
