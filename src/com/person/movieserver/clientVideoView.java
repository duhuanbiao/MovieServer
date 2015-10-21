package com.person.movieserver;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class clientVideoView extends VideoView {
	public clientVideoView(Context context) {
		super(context);
	}
	public clientVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public clientVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
}
