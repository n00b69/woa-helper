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

    private static final float DEFAULT_DOWNSCALE_FACTOR = 0.12f;
    private static final int DEFAULT_BLUR_RADIUS = 12;
    private static final int DEFAULT_FPS = 60;
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
            this.mDownscaleFactor = a.getFloat(R.styleable.BlurLayout_downscaleFactor, DEFAULT_DOWNSCALE_FACTOR);
            this.mBlurRadius = a.getInteger(R.styleable.BlurLayout_blurRadius, DEFAULT_BLUR_RADIUS);
            this.mFPS = a.getInteger(R.styleable.BlurLayout_fps, DEFAULT_FPS);
        } finally {
            a.recycle();
        }

        if (0 < this.mFPS) {
            final Choreographer.FrameCallback invalidationLoop = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    BlurLayout.this.invalidate();
                    Choreographer.getInstance().postFrameCallbackDelayed(this, 1000 / BlurLayout.this.mFPS);
                }
            };
            Choreographer.getInstance().postFrameCallback(invalidationLoop);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        Bitmap bitmap = this.blur();
        if (null != bitmap) {
            this.setBackground(new BitmapDrawable(bitmap));
        }
    }

    private Bitmap blur() {
        if (null == this.getContext()) {
            return null;
        }

        if (null == this.mActivityView || null == this.mActivityView.get()) {
            this.mActivityView = new WeakReference<>(this.getActivityView());
            if (null == this.mActivityView.get()) {
                return null;
            }
        }

        Point pointRelativeToActivityView = this.getPositionInScreen();

        this.setAlpha(0);

        int screenWidth = this.mActivityView.get().getWidth();
        int screenHeight = this.mActivityView.get().getHeight();

        int width = (int) (this.getWidth() * this.mDownscaleFactor);
        int height = (int) (this.getHeight() * this.mDownscaleFactor);

        int x = (int) (pointRelativeToActivityView.x * this.mDownscaleFactor);
        int y = (int) (pointRelativeToActivityView.y * this.mDownscaleFactor);

        int xPadding = this.getWidth() / 8;
        int yPadding = this.getHeight() / 8;

        int leftOffset = -xPadding;
        leftOffset = 0 <= x + leftOffset ? leftOffset : 0;

        int rightOffset = xPadding;
        rightOffset = x + this.getWidth() + rightOffset <= screenWidth ? rightOffset : screenWidth - this.getWidth() - x;

        int topOffset = -yPadding;
        topOffset = 0 <= y + topOffset ? topOffset : 0;

        int bottomOffset = yPadding;
        bottomOffset = y + height + bottomOffset <= screenHeight ? bottomOffset : 0;

        Bitmap bitmap;
        try {
            bitmap = this.getDownscaledBitmapForView(this.mActivityView.get(),
                    new Rect(pointRelativeToActivityView.x + leftOffset, pointRelativeToActivityView.y + topOffset,
                            pointRelativeToActivityView.x + this.getWidth() + Math.abs(leftOffset) + rightOffset,
                            pointRelativeToActivityView.y + this.getHeight() + Math.abs(topOffset) + bottomOffset),
                    this.mDownscaleFactor);
        } catch (NullPointerException e) {
            return null;
        }
        bitmap = BlurKit.getInstance().blur(bitmap, this.mBlurRadius);
        bitmap = Bitmap.createBitmap(bitmap, (int) (Math.abs(leftOffset) * this.mDownscaleFactor),
                (int) (Math.abs(topOffset) * this.mDownscaleFactor), width, height);
        this.setAlpha(1);

        return bitmap;
    }

    private View getActivityView() {
        Activity activity;
        try {
            activity = (Activity) this.getContext();
        } catch (ClassCastException e) {
            return null;
        }

        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    private Point getPositionInScreen() {
        return this.getPositionInScreen(this);
    }

    private Point getPositionInScreen(View view) {
        if (null == this.getParent()) {
            return new Point();
        }

        ViewGroup parent;
        try {
            parent = (ViewGroup) view.getParent();
        } catch (Exception e) {
            return new Point();
        }

        if (null == parent) {
            return new Point();
        }

        Point point = this.getPositionInScreen(parent);
        point.offset((int) view.getX(), (int) view.getY());
        return point;
    }

    private Bitmap getDownscaledBitmapForView(View view, Rect crop, float downscaleFactor) throws NullPointerException {
        View screenView = view.getRootView();

        int width = (int) (crop.width() * downscaleFactor);
        int height = (int) (crop.height() * downscaleFactor);

        if (0 >= screenView.getWidth() || 0 >= screenView.getHeight() || 0 >= width || 0 >= height) {
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
        this.invalidate();
    }

    public void setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
        this.invalidate();
    }

    public void setFPS(int fps) {
        this.mFPS = fps;
    }
}
