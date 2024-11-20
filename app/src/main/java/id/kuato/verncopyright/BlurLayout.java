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

import java.lang.ref.WeakReference;

import id.kuato.woahelper.R;

public class BlurLayout extends CardView {

    private static final float DEFAULT_DOWNSCALE_FACTOR = 0.12f;
    private static final int DEFAULT_BLUR_RADIUS = 12;
    private static final int DEFAULT_FPS = 60;
    private float mDownscaleFactor;
    private int mBlurRadius;
    private int mFPS;
    private WeakReference<View> mActivityView;

    public BlurLayout(final Context context) {
        super(context, null);
    }

    public BlurLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        BlurKit.init(context);

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BlurLayout, 0, 0);

        try {
            mDownscaleFactor = a.getFloat(R.styleable.BlurLayout_downscaleFactor, BlurLayout.DEFAULT_DOWNSCALE_FACTOR);
            mBlurRadius = a.getInteger(R.styleable.BlurLayout_blurRadius, BlurLayout.DEFAULT_BLUR_RADIUS);
            mFPS = a.getInteger(R.styleable.BlurLayout_fps, BlurLayout.DEFAULT_FPS);
        } finally {
            a.recycle();
        }

        if (0 < mFPS) {
            Choreographer.FrameCallback invalidationLoop = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(final long frameTimeNanos) {
                    invalidate();
                    Choreographer.getInstance().postFrameCallbackDelayed(this, 1000 / mFPS);
                }
            };
            Choreographer.getInstance().postFrameCallback(invalidationLoop);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        final Bitmap bitmap = blur();
        if (null != bitmap) {
            setBackground(new BitmapDrawable(bitmap));
        }
    }

    private Bitmap blur() {
        if (null == getContext()) {
            return null;
        }

        if (null == mActivityView || null == mActivityView.get()) {
            mActivityView = new WeakReference<>(getActivityView());
            if (null == mActivityView.get()) {
                return null;
            }
        }

        final Point pointRelativeToActivityView = getPositionInScreen();

        setAlpha(0);

        final int screenWidth = mActivityView.get().getWidth();
        final int screenHeight = mActivityView.get().getHeight();

        final int width = (int) (getWidth() * mDownscaleFactor);
        final int height = (int) (getHeight() * mDownscaleFactor);

        final int x = (int) (pointRelativeToActivityView.x * mDownscaleFactor);
        final int y = (int) (pointRelativeToActivityView.y * mDownscaleFactor);

        final int xPadding = getWidth() / 8;
        final int yPadding = getHeight() / 8;

        int leftOffset = -xPadding;
        leftOffset = 0 <= x + leftOffset ? leftOffset : 0;

        int rightOffset = xPadding;
        rightOffset = x + getWidth() + rightOffset <= screenWidth ? rightOffset : screenWidth - getWidth() - x;

        int topOffset = -yPadding;
        topOffset = 0 <= y + topOffset ? topOffset : 0;

        int bottomOffset = yPadding;
        bottomOffset = y + height + bottomOffset <= screenHeight ? bottomOffset : 0;

        Bitmap bitmap;
        try {
            bitmap = getDownscaledBitmapForView(mActivityView.get(), new Rect(pointRelativeToActivityView.x + leftOffset, pointRelativeToActivityView.y + topOffset, pointRelativeToActivityView.x + getWidth() + Math.abs(leftOffset) + rightOffset, pointRelativeToActivityView.y + getHeight() + Math.abs(topOffset) + bottomOffset), mDownscaleFactor);
        } catch (final NullPointerException e) {
            return null;
        }
        bitmap = BlurKit.getInstance().blur(bitmap, mBlurRadius);
        bitmap = Bitmap.createBitmap(bitmap, (int) (Math.abs(leftOffset) * mDownscaleFactor), (int) (Math.abs(topOffset) * mDownscaleFactor), width, height);
        setAlpha(1);

        return bitmap;
    }

    private View getActivityView() {
        final Activity activity;
        try {
            activity = (Activity) getContext();
        } catch (final ClassCastException e) {
            return null;
        }

        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    private Point getPositionInScreen() {
        return getPositionInScreen(this);
    }

    private Point getPositionInScreen(final View view) {
        if (null == getParent()) {
            return new Point();
        }

        final ViewGroup parent;
        try {
            parent = (ViewGroup) view.getParent();
        } catch (final Exception e) {
            return new Point();
        }

        if (null == parent) {
            return new Point();
        }

        final Point point = getPositionInScreen(parent);
        point.offset((int) view.getX(), (int) view.getY());
        return point;
    }

    private Bitmap getDownscaledBitmapForView(final View view, final Rect crop, final float downscaleFactor) throws NullPointerException {
        final View screenView = view.getRootView();

        final int width = (int) (crop.width() * downscaleFactor);
        final int height = (int) (crop.height() * downscaleFactor);

        if (0 >= screenView.getWidth() || 0 >= screenView.getHeight() || 0 >= width || 0 >= height) {
            throw new NullPointerException();
        }

        final float dx = -crop.left * downscaleFactor;
        final float dy = -crop.top * downscaleFactor;

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        final Canvas canvas = new Canvas(bitmap);
        final Matrix matrix = new Matrix();
        matrix.preScale(downscaleFactor, downscaleFactor);
        matrix.postTranslate(dx, dy);
        canvas.setMatrix(matrix);
        screenView.draw(canvas);

        return bitmap;
    }

    public void setDownscaleFactor(final float downscaleFactor) {
        mDownscaleFactor = downscaleFactor;
        invalidate();
    }

    public void setBlurRadius(final int blurRadius) {
        mBlurRadius = blurRadius;
        invalidate();
    }

    public void setFPS(final int fps) {
        mFPS = fps;
    }
}
