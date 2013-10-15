/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.android.picasaviewerapp;

import org.tantalum.UITask;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.nokia.example.picasa.common.PicasaStorage;

/**
 * View for displaying the bigger image and information of an image.
 * 
 *
 */
public class DetailedView extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailed_view);
		
		// Get the information from the intent
		final String title = getIntent().getExtras().getString("title");
		if (title != null) {
			((TextView) findViewById(R.id.imageTitle)).setText(title);
		}
		
		final String author = getIntent().getExtras().getString("author");
		if (author != null) {
			((TextView) findViewById(R.id.authorName)).setText(author);
		}

		final String url = getIntent().getExtras().getString("url");
		if (url != null) {
			ImageView iv = (ImageView) findViewById(R.id.bigImage);
			
			// Get the image from storage (or the web)
			PicasaStorage.imageCache.get(url, Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, new ImageResult(iv, title));
		}
	}
	
	/** 
	 * A runnable result that is passed to storage. The result is set in the background thread,
	 * but the run method is ran in the UI thread, enabling UI changes depending on the result.
	 */
	protected class ImageResult extends UITask {
		private final ImageView view;
		private final String title;

		public ImageResult(ImageView iv, String title) {
			this.view = iv;
			this.title = title;
		}

		@Override
		protected void onPostExecute(final Object in) {
			try {
				L.i(ImageGridView.LOG_MAIN_ACTIVITY,
						"DetailedView imageresult: title=" + title + " height=" + ((Bitmap) in).getHeight());	
				view.setImageBitmap((Bitmap) in);
			} catch (Exception e) {
				L.e(ImageGridView.LOG_MAIN_ACTIVITY, "Can not setImageBitmap", e);
			}			
		}
		
		public void onCancelled() {
			L.i(ImageGridView.LOG_MAIN_ACTIVITY,
					"DetailedView imageresult CANCELLED: title=" + title);			
		}
	}
}
