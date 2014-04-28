/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */
package com.nokia.android.picasaviewerapp;

import java.util.Vector;

import org.tantalum.UITask;
import org.tantalum.Worker;
import org.tantalum.android.TantalumActivity;
import org.tantalum.net.StaticWebCache;
import org.tantalum.util.L;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.nokia.example.picasa.common.PicasaImageObject;
import com.nokia.example.picasa.common.PicasaStorage;

/**
 * Class for displaying the grid of images in the main view of the application.
 * 
 * 
 */
public class ImageGridView extends TantalumActivity implements
		OnItemClickListener {
	public static final String LOG_MAIN_ACTIVITY = "ImageGridView";

	private Vector<PicasaImageObject> imageObjects;
	private OnItemClickListener clickListener;
	private boolean updating; // To prevent updating again while still updating
	private GridView gridView;
	private int scrollY;
	private String search = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// AndroidDatabase.setContext(getApplicationContext());
		L.i(LOG_MAIN_ACTIVITY, "onCreate");

		PicasaStorage.init(512);
		updating = false;
		clickListener = this;
		scrollY = 0;
		L.i(LOG_MAIN_ACTIVITY, "end onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		L.i(LOG_MAIN_ACTIVITY, "onResume");
		updating = false;
		this.refresh(search, StaticWebCache.GET_ANYWHERE);
		try {
			((GridView) findViewById(R.id.grid_view)).setSelection(scrollY);
		} catch (ArrayIndexOutOfBoundsException e) {
			// Catch the error when the grid is not yet ready.
			L.e(LOG_MAIN_ACTIVITY, "Grid out of bounds", e);
		}
		L.i(LOG_MAIN_ACTIVITY, "end onResume");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.featured_view, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		L.i(LOG_MAIN_ACTIVITY, "onPause");
		scrollY = ((GridView) findViewById(R.id.grid_view))
				.getFirstVisiblePosition();
	}

	public void refresh(final String search, final int getType) {
		L.i(LOG_MAIN_ACTIVITY, "refresh");
		if (updating) {
			return;
		}
		updating = true;
		this.search = search;

		if (getType == StaticWebCache.GET_WEB) {
			// Notify the user if it was a manual refresh
			showToast("Updating feed");
		}
		setContentView(R.layout.featured_view);
		gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setOnItemClickListener(clickListener);

		if (imageObjects == null || getType == StaticWebCache.GET_WEB) {
			L.i(LOG_MAIN_ACTIVITY, "getImageObjects: type=" + getType
					+ " search=" + search);
			
			
			PicasaStorage.getImageObjects(new UITask() {
				public Object doInBackground(Object in) {
					L.i(LOG_MAIN_ACTIVITY,
							"doInBackground getImageObjects: type="
									+ getType + " search=" + search);

					return in;
				}

				// UI thread allows UI changes.
				@SuppressWarnings("unchecked")
				public void onPostExecute(final Object result) {
					L.i(LOG_MAIN_ACTIVITY,
							"onPostExecute getImageObjects: type="
									+ getType + " search=" + search);
					try {
						imageObjects = (Vector<PicasaImageObject>) result;
						if (imageObjects != null) {

							gridView.setAdapter(new ImageAdapter(
									getApplicationContext(),
									imageObjects));
							if (getType == StaticWebCache.GET_WEB) {
								showToast("Feed updated.");
							}
						} else {
							showToast("Failed to retrieve feed.");
						}
					} catch (Exception e) {
						L.e(LOG_MAIN_ACTIVITY,
								"Can not get imageobjects", e);
					}
					updating = false;
				}
			}, search, Worker.HIGH_PRIORITY, getType);

		} else {
			// ImageObjects is still the same, so no need to update from cache.
			gridView.setAdapter(new ImageAdapter(getApplicationContext(),
					imageObjects));
			updating = false;
		}
	}

	/**
	 * Thumb was tapped, open detailed view.
	 */
	public void onItemClick(AdapterView<?> arg0, View v, int thumbId, long arg3) {
		L.i(LOG_MAIN_ACTIVITY, "onItemClick");
		Intent intent = new Intent(this, DetailedView.class);
		intent.putExtra("url", imageObjects.get(thumbId).imageUrl);
		intent.putExtra("thumbUrl", imageObjects.get(thumbId).thumbUrl);
		intent.putExtra("title", imageObjects.get(thumbId).title);
		intent.putExtra("author", imageObjects.get(thumbId).author);
		startActivity(intent);
	}

	/**
	 * Item from options menu selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		L.i(LOG_MAIN_ACTIVITY, "onOptionsItemSelected");
		if (item.getItemId() == R.id.refresh) {
			this.refresh(null, StaticWebCache.GET_WEB);
		} else if (item.getItemId() == R.id.search) {
			this.searchDialog();
		} else if (item.getItemId() == R.id.featured) {
			this.refresh(null, StaticWebCache.GET_ANYWHERE);
		}
		return false;
	}

	/**
	 * Show a toast message.
	 * 
	 * @param text
	 */
	private void showToast(String text) {
		L.i(LOG_MAIN_ACTIVITY, "showToast " + text);
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * Open the search dialog.
	 */
	private void searchDialog() {
		L.i(LOG_MAIN_ACTIVITY, "searchDialog");
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.search_dialog);
		dialog.setTitle(R.string.search);
		dialog.setCancelable(true);

		Button searchButton = (Button) dialog.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refresh(((EditText) dialog.findViewById(R.id.search_box))
						.getText().toString(), StaticWebCache.GET_WEB);
				dialog.cancel();
			}
		});

		dialog.show();
	}
}
