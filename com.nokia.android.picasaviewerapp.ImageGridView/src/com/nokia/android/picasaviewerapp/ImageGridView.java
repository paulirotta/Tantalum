package com.nokia.android.picasaviewerapp;

import java.util.Vector;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.futurice.tantalum3.Closure;
import com.futurice.tantalum3.TantalumActivity;
import com.nokia.example.picasa.common.ImageObject;
import com.nokia.example.picasa.common.Storage;

/**
 * Class for displaying the grid of images in the main view of the application.
 * 
 * 
 */
public class ImageGridView extends TantalumActivity implements
		OnItemClickListener {
	public static final String LOG_MAIN_ACTIVITY = "MainActivity";
	
	private Vector<ImageObject> imageObjects;
	private OnItemClickListener clickListener;
	private boolean updating; // To prevent updating again while still updating
	private GridView gridView;
	private int scrollY;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// AndroidDatabase.setContext(getApplicationContext());
		Log.i(LOG_MAIN_ACTIVITY, "onCreate");
		
		Storage.init();
		updating = false;
		clickListener = this;
		scrollY = 0;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(LOG_MAIN_ACTIVITY, "onResume");
		updating = false;
		this.refresh(false, false);
		try {
			((GridView) findViewById(R.id.grid_view)).setSelection(scrollY);
		} catch (ArrayIndexOutOfBoundsException e) {
			// Catch the error when the grid is not yet ready.
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.featured_view, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(LOG_MAIN_ACTIVITY, "onPause");
		scrollY = ((GridView) findViewById(R.id.grid_view))
				.getFirstVisiblePosition();
	}

	public void refresh(final boolean fromWeb, boolean resetFeatured) {
		Log.i(LOG_MAIN_ACTIVITY, "refresh");
		if (updating) {
			return;
		} else {
			updating = true;
		}

		if (fromWeb) {
			// Notify the user if it was a manual refresh
			showToast("Updating feed.");
		} else {

		}
		setContentView(R.layout.featured_view);
		gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setOnItemClickListener(clickListener);

		if (imageObjects == null || fromWeb || resetFeatured) {
			Storage.getImageObjects(new Closure() {

				/*
				 * The run() method runs in the UI thread, and allows for UI
				 * changes.
				 */
				@SuppressWarnings("unchecked")
				public void run() {
					try {
						imageObjects = (Vector<ImageObject>) get();
						if (imageObjects != null) {

							gridView.setAdapter(new ImageAdapter(
									getApplicationContext(), imageObjects));
							if (fromWeb) {
								showToast("Feed updated.");
							}
						} else {
							showToast("Failed to retrieve feed.");
						}
					} catch (Exception e) {
						Log.e(LOG_MAIN_ACTIVITY, "Can not get imageobjects", e);
					}
					updating = false;
				}
			}, fromWeb);
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
		Log.i(LOG_MAIN_ACTIVITY, "onItemClick");
		Intent intent = new Intent(this, DetailedView.class);
		intent.putExtra("url", imageObjects.get(thumbId).getImageUrl());
		intent.putExtra("title", imageObjects.get(thumbId).getTitle());
		intent.putExtra("author", imageObjects.get(thumbId).getAuthor());
		startActivity(intent);
	}

	/**
	 * Item from options menu selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(LOG_MAIN_ACTIVITY, "onOptionsItemSelected");
		if (item.getItemId() == R.id.refresh) {
			this.refresh(true, false);
		} else if (item.getItemId() == R.id.search) {
			this.searchDialog();
		} else if (item.getItemId() == R.id.featured) {
			this.refresh(false, true);
		}
		return false;
	}

	/**
	 * Show a toast message.
	 * 
	 * @param text
	 */
	private void showToast(String text) {
		Log.i(LOG_MAIN_ACTIVITY, "showToast");
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * Open the search dialog.
	 */
	private void searchDialog() {
		Log.i(LOG_MAIN_ACTIVITY, "searchDialog");
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.search_dialog);
		dialog.setTitle(R.string.search);
		dialog.setCancelable(true);

		Button searchButton = (Button) dialog.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				search(((EditText) dialog.findViewById(R.id.search_box))
						.getText().toString());
				dialog.cancel();
			}
		});

		dialog.show();
	}

	/**
	 * Do a search with the given string. The result is set as the imageObjects
	 * Vector and the UI updated when the query has finished.
	 * 
	 * @param searchString
	 */
	private void search(String searchString) {
		Log.i(LOG_MAIN_ACTIVITY, "search");
		showToast("Searching for: " + searchString);
		setContentView(R.layout.featured_view);

		Storage.getImageObjects(new Closure() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					imageObjects = (Vector<ImageObject>) get();
					if (imageObjects != null) {
						final GridView gridView = (GridView) findViewById(R.id.grid_view);
						gridView.setOnItemClickListener(clickListener);
						gridView.setAdapter(new ImageAdapter(
								getApplicationContext(), imageObjects));
					}
				} catch (Exception e) {
					Log.e(LOG_MAIN_ACTIVITY, "Can not get imageObjects", e);
				}
			}
		}, searchString);
	}
}
