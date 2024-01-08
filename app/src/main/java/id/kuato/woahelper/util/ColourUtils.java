package id.kuato.woahelper.vayu.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import id.kuato.woahelper.R;

public class ColourUtils {
	public static int getDominantColor(Context context, Drawable drawable) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
		List<Palette.Swatch> swatches = new ArrayList<Palette.Swatch>(swatchesTemp);
		Collections.sort(swatches, new Comparator<Palette.Swatch>() {
			@Override
			public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
				return swatch2.getPopulation() - swatch1.getPopulation();
			}
		});
		return swatches.size() > 0 ? swatches.get(0).getRgb() : context.getColor(R.color.colorPrimary);
	}

	@ColorInt
	public static int getColorAttr(Context context, int colorRes) {
		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		theme.resolveAttribute(colorRes, typedValue, true);
		return typedValue.data;
	}

	/**
	 * Darkens a color by a given factor.
	 *
	 * @param color The color to darken
	 * @param factor The factor to darken the color.
	 * @return darker version of specified color.
	 */
	@ColorInt
	public static int darker(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
		// factor = 0.85f
		return Color.argb(Color.alpha(color), Math.max((Color.red(color) * factor), 0),
				Math.max((Color.red(color) * factor), 0), Math.max((Color.red(color) * factor), 0));
	}

	/**
	 * Lightens a color by a given factor.
	 *
	 * @param color The color to lighten
	 * @param factor The factor to lighten the color. 0 will make the color unchanged. 1 will make the color white.
	 * @return lighter version of the specified color.
	 */
	@ColorInt
	public static int lighter(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
		// factor = 0.15f
		int alpha = Color.alpha(color);
		float red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
		float green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
		float blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
		return Color.argb(alpha, red, green, blue);
	}

	/**
	 * Returns `true` if the luminance of the color is less than or equal to 0.5
	 *
	 * @param color The color to calculate the luminance.
	 * @return `true` if the color is dark
	 */

	public static boolean isDarkColor(@ColorInt int color) {
		return isDarkColor(color, 0.5);
	}

	/**
	 * Returns `true` if the luminance of the color is less than or equal to the luminance factor
	 *
	 * @param color The color to calculate the luminance.
	 * @param luminance Value from 0-1. 1 = white. 0 = black.
	 * @return `true` if the color is dark
	 */

	public static boolean isDarkColor(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) double luminance) {
		return ColorUtils.calculateLuminance(color) <= luminance;
	}

	/**
	 * Returns `true` if the luminance of the color is greater than or equal to 0.5
	 *
	 * @param color The color to calculate the luminance.
	 * @return `true` if the color is light
	 */

	public static boolean isLightColor(@ColorInt int color) {
		return isLightColor(color, 0.5);
	}

	/**
	 * Returns `true` if the luminance of the color is less than or equal to the luminance factor
	 *
	 * @param color The color to calculate the luminance.
	 * @param luminance Value from 0-1. 1 = white. 0 = black.
	 * @return `true` if the color is light
	 */

	public static boolean isLightColor(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) double luminance) {
		// luminance = 0.5
		return ColorUtils.calculateLuminance(color) >= luminance;
	}

	/**
	 * Manipulate the alpha bytes of a color
	 *
	 * @param color The color to adjust the alpha on
	 * @param factor 0.0f - 1.0f
	 * @return The new color value
	 */

	@ColorInt
	public static int adjustAlpha(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
		int alpha = Math.round(Color.alpha(color) * factor);
		float red = Color.red(color);
		float green = Color.green(color);
		float blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}

	/**
	 * Remove alpha from a color
	 *
	 * @param color The color to modify
	 * @return The color without any transparency
	 */
	@ColorInt
	public static int stripAlpha(@ColorInt int color) {
		return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
	}

	public static String toHex(@ColorInt int color, boolean alpha) {
		if (alpha) {
			return "#" + String.format("%08X", color);
		} else {
			return "#" + String.format("%06X", color);
		}
	}

	/**
	 * Parse the color string, and return the corresponding color-int.
	 * If the string cannot be parsed, throws an IllegalArgumentException
	 * exception.
	 */
	@ColorInt
	public static int parseColor(String colorString) {
		try {
			if (colorString.startsWith("#")) {
				return parseColor(colorString.substring(1));
			}
			int length = colorString.length();
			int a;
			int r;
			int g;
			int b = 0;
			if (length == 0) {
				r = 0;
				a = 255;
				g = 0;
			} else if (length <= 2) {
				a = 255;
				r = 0;
				b = Integer.parseInt(colorString, 16);
				g = 0;
			} else if (length == 3) {
				a = 255;
				r = Integer.parseInt(colorString.substring(0, 1), 16);
				g = Integer.parseInt(colorString.substring(1, 2), 16);
				b = Integer.parseInt(colorString.substring(2, 3), 16);
			} else if (length == 4) {
				a = 255;
				r = Integer.parseInt(colorString.substring(0, 2), 16);
				g = r;
				r = 0;
				b = Integer.parseInt(colorString.substring(2, 4), 16);
			} else if (length == 5) {
				a = 255;
				r = Integer.parseInt(colorString.substring(0, 1), 16);
				g = Integer.parseInt(colorString.substring(1, 3), 16);
				b = Integer.parseInt(colorString.substring(3, 5), 16);
			} else if (length == 6) {
				a = 255;
				r = Integer.parseInt(colorString.substring(0, 2), 16);
				g = Integer.parseInt(colorString.substring(2, 4), 16);
				b = Integer.parseInt(colorString.substring(4, 6), 16);
			} else if (length == 7) {
				a = Integer.parseInt(colorString.substring(0, 1), 16);
				r = Integer.parseInt(colorString.substring(1, 3), 16);
				g = Integer.parseInt(colorString.substring(3, 5), 16);
				b = Integer.parseInt(colorString.substring(5, 7), 16);
			} else if (length == 8) {
				a = Integer.parseInt(colorString.substring(0, 2), 16);
				r = Integer.parseInt(colorString.substring(2, 4), 16);
				g = Integer.parseInt(colorString.substring(4, 6), 16);
				b = Integer.parseInt(colorString.substring(6, 8), 16);
			} else {
				b = -1;
				g = -1;
				r = -1;
				a = -1;
			}
			return Color.argb(a, r, g, b);
		} catch (NumberFormatException e) {
			return Color.parseColor(colorString);
		}
	}
}