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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

public class BlurKit {

    private static BlurKit instance;

    private RenderScript rs;

    public static void init(Context context) {
        if (null != instance) {
            return;
        }

        instance = new BlurKit();
        instance.rs = RenderScript.create(context);
    }

    public Bitmap blur(Bitmap src, int radius) {
        final Allocation input = Allocation.createFromBitmap(rs, src);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script;
        if (android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 <= android.os.Build.VERSION.SDK_INT) {
            script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
        }
        output.copyTo(src);
        return src;
    }

    public Bitmap blur(View src, int radius) {
        Bitmap bitmap = getBitmapForView(src, 1.0f);
        return blur(bitmap, radius);
    }

    public Bitmap fastBlur(View src, int radius, float downscaleFactor) {
        Bitmap bitmap = getBitmapForView(src, downscaleFactor);
        return blur(bitmap, radius);
    }

    private Bitmap getBitmapForView(View src, float downscaleFactor) {
        Bitmap bitmap =
                Bitmap.createBitmap(
                        (int) (src.getWidth() * downscaleFactor),
                        (int) (src.getHeight() * downscaleFactor),
                        Bitmap.Config.ARGB_4444);

        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(downscaleFactor, downscaleFactor);
        canvas.setMatrix(matrix);
        src.draw(canvas);

        return bitmap;
    }

    public static BlurKit getInstance() {
        if (null == instance) {
            throw new RuntimeException("BlurKit not initialized!");
        }

        return BlurKit.instance;
    }
}
