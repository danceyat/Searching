package com.android.searching;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.searching.engines.Engine;

public class Utils {

	private static final int K = 1024;
	private static final int M = 1024 * 1024;

	public static View formatResult(View view, final Engine.IResult result,
			final Context context) {
		// set action
		view.findViewById(R.id.linearLayout_directAction_resultsItem)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						result.onClick(context);
					}
				});
		// set name
		TextView textView = (TextView) view
				.findViewById(R.id.textView_itemName_resultsItem);
		textView.setText(result.getText());
		// set description
		textView = (TextView) view
				.findViewById(R.id.textView_itemDesc_resultsItem);
		textView.setText(result.getDesc());
		// set icon
		ImageView imageView = (ImageView) view
				.findViewById(R.id.imageView_itemImage_resultsItem);
		LayoutParams layoutParams = imageView.getLayoutParams();
		layoutParams.height = ConfigManager.getResultItemDrawableSize();
		layoutParams.width = ConfigManager.getResultItemDrawableSize();
		imageView.setLayoutParams(layoutParams);
		imageView.setImageDrawable(result.getIcon());
		// set extended actions
		final LinearLayout extendedActionsLayout = (LinearLayout) view
				.findViewById(R.id.linearLayout_extendedAction_resultsItem);
		// TODO remember state of extended actions when getView(...) is called
		extendedActionsLayout.setVisibility(View.GONE);
		// set button
		final ImageButton imageButton = (ImageButton) view
				.findViewById(R.id.imageButton_extend_resultsItem);
		layoutParams = imageButton.getLayoutParams();
		layoutParams.width = ConfigManager.getResultItemDrawableSize();
		layoutParams.height = ConfigManager.getResultItemDrawableSize();
		imageButton.setLayoutParams(layoutParams);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (extendedActionsLayout.getVisibility()) {
				case View.VISIBLE:
					imageButton.setImageDrawable(context.getResources()
							.getDrawable(R.drawable.arrow_normal));
					extendedActionsLayout.setVisibility(View.GONE);
					break;
				case View.GONE:
					imageButton.setImageDrawable(context.getResources()
							.getDrawable(R.drawable.arrow_expanded_normal));
					extendedActionsLayout.setVisibility(View.VISIBLE);
					break;
				default:
					break;
				}
			}
		});
		// set favorite
		final TextView textViewFavorite = (TextView) view
				.findViewById(R.id.textView_favorite_resultsItem);
		// TODO initial state
		Drawable drawable = context.getResources().getDrawable(
				R.drawable.favorite);
		drawable.setBounds(0, 0, 160, 60);
		textViewFavorite.setCompoundDrawables(null, drawable, null, null);
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO add favorite logic
			}
		});
		// TODO more info
		return view;
	}

	public static String getReadableSizeString(String size) {
		String ret;
		int sizeVal = Integer.parseInt(size);
		if (sizeVal < K) {
			ret = size + "B";
		} else if (sizeVal < M) {
			ret = String.valueOf(sizeVal / K) + "KB";
		} else {
			double newSize = (double) sizeVal / (double) M;
			newSize = Math.round(newSize * 100) / 100.0;
			ret = String.valueOf(newSize) + "MB";
		}
		return ret;
	}
}
