/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.android.picasaviewerapp;

import java.util.HashMap;
import java.util.Vector;

import org.tantalum.UITask;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nokia.example.picasa.common.PicasaImageObject;
import com.nokia.example.picasa.common.PicasaStorage;

/**
 * ImageAdapter class for setting the image inside the grid-layout cells.
 * 
 * 
 */
public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private Vector<PicasaImageObject> imageObjects;
	private HashMap<Integer, ImageView> views;

	public ImageAdapter(Context c, Vector<PicasaImageObject> objects) {
		mContext = c;
		imageObjects = objects;
		views = new HashMap<Integer, ImageView>();
	}

	public int getCount() {
		return imageObjects.size();
	}

	public Object getItem(int position) {
		return imageObjects.elementAt(position);
	}

	// TODO: fix?
	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ImageView imageView;
		
		if (views.containsKey(position)) {
			imageView = views.get(position);
		} else {
			imageView = new ImageView(mContext) {
				@Override
				public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
				}
				
			};

			// Get the thumbnail from cache or web
			PicasaStorage.imageCache.get(
					imageObjects.elementAt(position).thumbUrl, Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE,
						new ImageResult(imageView, position));

			// imageView.setImageResource(R.drawable.ic_action_search); // TODO:
			// Loading gfx
			imageView.setMinimumHeight(PicasaStorage.getScreenWidth());
			imageView.setMinimumWidth(PicasaStorage.getScreenWidth());
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			views.put(position, imageView);
		}

		return imageView;
	}

	/**
	 * Runnable class that sets the result in the background, and then runs in
	 * the UI thread. This enables editing the UI in the run() method.
	 * 
	 * @author oehn
	 * 
	 */
	private final class ImageResult extends UITask {
		private final ImageView view;
		private final int position;

		public ImageResult(ImageView view, int position) {
			this.view = view;
			this.position = position;
		}
		
		@Override
		protected void onPostExecute(final Object in) {
			try {
				if (in != null) {
					L.i(ImageGridView.LOG_MAIN_ACTIVITY,
							"imageresult: position=" + position);
					view.setImageBitmap((Bitmap) in);
					notifyDataSetChanged();
				}
			} catch (Exception e) {
				L.e(ImageGridView.LOG_MAIN_ACTIVITY,
						"Can not get imageresult", e);
			}
		}
		
		public void onCancelled() {
			L.i(ImageGridView.LOG_MAIN_ACTIVITY,
					"imageresult CANCELLED: position=" + position);			
		}
	}
}
