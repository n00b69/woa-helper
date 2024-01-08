package id.kuato.verncopyright;

/*
 * Copyright (C) 2013 Vern Kuato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import id.kuato.woahelper.R;
import java.lang.ref.WeakReference;

public class BlurLayout extends CardView {

	public static final float DEFAULT_DOWNSCALE_FACTOR = 0.12f;
	public static final int DEFAULT_BLUR_RADIUS = 12;
	public static final int DEFAULT_FPS = 60;
	private float mDownscaleFactor;
	private int mBlurRadius;
	private int mFPS;
	private WeakReference<View> mActivityView;

	public BlurLayout(Context context) {
		super(context, null);
	}

	public BlurLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		BlurKit.init(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BlurLayout, 0, 0);

		try {
			mDownscaleFactor = a.getFloat(R.styleable.BlurLayout_downscaleFactor, DEFAULT_DOWNSCALE_FACTOR);
			mBlurRadius = a.getInteger(R.styleable.BlurLayout_blurRadius, DEFAULT_BLUR_RADIUS);
			mFPS = a.getInteger(R.styleable.BlurLayout_fps, DEFAULT_FPS);
		} finally {
			a.recycle();
		}

		if (mFPS > 0) {
			Choreographer.getInstance().postFrameCallback(invalidationLoop);
		}
	}

	private final Choreographer.FrameCallback invalidationLoop = new Choreographer.FrameCallback() {
		@Override
		public void doFrame(long frameTimeNanos) {
			invalidate();
			Choreographer.getInstance().postFrameCallbackDelayed(this, 1000 / mFPS);
		}
	};

	@Override
	public void invalidate() {
		super.invalidate();
		Bitmap bitmap = blur();
		if (bitmap != null) {
			setBackground(new BitmapDrawable(bitmap));
		}
	}

	private Bitmap blur() {
		if (getContext() == null) {
			return null;
		}

		if (mActivityView == null || mActivityView.get() == null) {
			mActivityView = new WeakReference<>(getActivityView());
			if (mActivityView.get() == null) {
				return null;
			}
		}

		Point pointRelativeToActivityView = getPositionInScreen();

		setAlpha(0);

		int screenWidth = mActivityView.get().getWidth();
		int screenHeight = mActivityView.get().getHeight();

		int width = (int) (getWidth() * mDownscaleFactor);
		int height = (int) (getHeight() * mDownscaleFactor);

		int x = (int) (pointRelativeToActivityView.x * mDownscaleFactor);
		int y = (int) (pointRelativeToActivityView.y * mDownscaleFactor);

		int xPadding = getWidth() / 8;
		int yPadding = getHeight() / 8;

		int leftOffset = -xPadding;
		leftOffset = x + leftOffset >= 0 ? leftOffset : 0;

		int rightOffset = xPadding;
		rightOffset = x + getWidth() + rightOffset <= screenWidth ? rightOffset : screenWidth - getWidth() - x;

		int topOffset = -yPadding;
		topOffset = y + topOffset >= 0 ? topOffset : 0;

		int bottomOffset = yPadding;
		bottomOffset = y + height + bottomOffset <= screenHeight ? bottomOffset : 0;

		Bitmap bitmap;
		try {
			bitmap = getDownscaledBitmapForView(mActivityView.get(),
					new Rect(pointRelativeToActivityView.x + leftOffset, pointRelativeToActivityView.y + topOffset,
							pointRelativeToActivityView.x + getWidth() + Math.abs(leftOffset) + rightOffset,
							pointRelativeToActivityView.y + getHeight() + Math.abs(topOffset) + bottomOffset),
					mDownscaleFactor);
		} catch (NullPointerException e) {
			return null;
		}
		bitmap = BlurKit.getInstance().blur(bitmap, mBlurRadius);
		bitmap = Bitmap.createBitmap(bitmap, (int) (Math.abs(leftOffset) * mDownscaleFactor),
				(int) (Math.abs(topOffset) * mDownscaleFactor), width, height);
		setAlpha(1);

		return bitmap;
	}

	private View getActivityView() {
		Activity activity;
		try {
			activity = (Activity) getContext();
		} catch (ClassCastException e) {
			return null;
		}

		return activity.getWindow().getDecorView().findViewById(android.R.id.content);
	}

	private Point getPositionInScreen() {
		return getPositionInScreen(this);
	}

	private Point getPositionInScreen(View view) {
		if (getParent() == null) {
			return new Point();
		}

		ViewGroup parent;
		try {
			parent = (ViewGroup) view.getParent();
		} catch (Exception e) {
			return new Point();
		}

		if (parent == null) {
			return new Point();
		}

		Point point = getPositionInScreen(parent);
		point.offset((int) view.getX(), (int) view.getY());
		return point;
	}

	private Bitmap getDownscaledBitmapForView(View view, Rect crop, float downscaleFactor) throws NullPointerException {
		View screenView = view.getRootView();

		int width = (int) (crop.width() * downscaleFactor);
		int height = (int) (crop.height() * downscaleFactor);

		if (screenView.getWidth() <= 0 || screenView.getHeight() <= 0 || width <= 0 || height <= 0) {
			throw new NullPointerException();
		}

		float dx = -crop.left * downscaleFactor;
		float dy = -crop.top * downscaleFactor;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		Matrix matrix = new Matrix();
		matrix.preScale(downscaleFactor, downscaleFactor);
		matrix.postTranslate(dx, dy);
		canvas.setMatrix(matrix);
		screenView.draw(canvas);

		return bitmap;
	}

	public void setDownscaleFactor(float downscaleFactor) {
		this.mDownscaleFactor = downscaleFactor;
		invalidate();
	}

	public void setBlurRadius(int blurRadius) {
		this.mBlurRadius = blurRadius;
		invalidate();
	}

	public void setFPS(int fps) {
		this.mFPS = fps;
	}
}
