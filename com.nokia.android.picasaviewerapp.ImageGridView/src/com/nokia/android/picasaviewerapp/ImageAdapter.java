package com.nokia.android.picasaviewerapp;

import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.futurice.tantalum3.Closure;
import com.nokia.example.picasa.common.ImageObject;
import com.nokia.example.picasa.common.Storage;

/**
 * ImageAdapter class for setting the image inside the grid-layout cells.
 * 
 * 
 */
public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private Vector<ImageObject> imageObjects;
	private HashMap<Integer, ImageView> views;

	public ImageAdapter(Context c, Vector<ImageObject> objects) {
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

		if (views.containsKey(position)) {
			return views.get(position);
		} else {
			ImageView imageView = new ImageView(mContext);
			
			// Get the thumbnail from cache or web
			Storage.thumbCache.get(imageObjects.elementAt(position).getThumbUrl(),
					new ImageResult(imageView));
			
			//imageView.setImageResource(R.drawable.ic_action_search); // TODO: Loading gfx
			imageView.setMinimumHeight(Storage.getWidth());
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			views.put(position, imageView);
			return imageView;
		}

	}

	/**
	 * Runnable class that sets the result in the background, and then runs in
	 * the UI thread. This enables editing the UI in the run() method.
	 * 
	 * @author oehn
	 * 
	 */
	private final class ImageResult extends Closure {
		private ImageView view;

		public ImageResult(ImageView view) {
			this.view = view;
		}

		public void run() {
			try {
				if (get() != null) {
					view.setImageBitmap((Bitmap) get());
					notifyDataSetChanged();
				}
			} catch (Exception e) {
				Log.e(ImageGridView.LOG_MAIN_ACTIVITY, "Can not get imageresult", e);
			}
		}
	}

}
