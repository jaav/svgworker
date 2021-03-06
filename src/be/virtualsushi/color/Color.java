package be.virtualsushi.color;


import java.util.HashMap;
import java.util.Locale;

/**
 * The Color class defines methods for creating and converting color ints.
 * Colors are represented as packed ints, made up of 4 bytes: alpha, red,
 * green, blue. The values are unpremultiplied, meaning any transparency is
 * stored solely in the alpha component, and not in the color components. The
 * components are stored as follows (alpha << 24) | (red << 16) |
 * (green << 8) | blue. Each component ranges between 0..255 with 0
 * meaning no contribution for that component, and 255 meaning 100%
 * contribution. Thus opaque-black would be 0xFF000000 (100% opaque but
 * no contributions from red, green, or blue), and opaque-white would be
 * 0xFFFFFFFF
 */
public class Color {
	public static final int BLACK = 0xFF000000;
	public static final int DKGRAY = 0xFF444444;
	public static final int GRAY = 0xFF888888;
	public static final int LTGRAY = 0xFFCCCCCC;
	public static final int WHITE = 0xFFFFFFFF;
	public static final int RED = 0xFFFF0000;
	public static final int GREEN = 0xFF00FF00;
	public static final int BLUE = 0xFF0000FF;
	public static final int YELLOW = 0xFFFFFF00;
	public static final int CYAN = 0xFF00FFFF;
	public static final int MAGENTA = 0xFFFF00FF;
	public static final int TRANSPARENT = 0;

	/**
	 * Return the alpha component of a color int. This is the same as saying
	 * color >>> 24
	 */
	public static int alpha(int color) {
		return color >>> 24;
	}

	/**
	 * Return the red component of a color int. This is the same as saying
	 * (color >> 16) & 0xFF
	 */
	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	/**
	 * Return the green component of a color int. This is the same as saying
	 * (color >> 8) & 0xFF
	 */
	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	/**
	 * Return the blue component of a color int. This is the same as saying
	 * color & 0xFF
	 */
	public static int blue(int color) {
		return color & 0xFF;
	}

	/**
	 * Return a color-int from red, green, blue components.
	 * The alpha component is implicity 255 (fully opaque).
	 * These component values should be [0..255], but there is no
	 * range check performed, so if they are out of range, the
	 * returned color is undefined.
	 * red  Red component [0..255] of the color
	 * green Green component [0..255] of the color
	 * blue  Blue component [0..255] of the color
	 */
	public static int rgb(int red, int green, int blue) {
		return (0xFF << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * Return a color-int from alpha, red, green, blue components.
	 * These component values should be [0..255], but there is no
	 * range check performed, so if they are out of range, the
	 * returned color is undefined.
	 * alpha Alpha component [0..255] of the color
	 * red   Red component [0..255] of the color
	 * green Green component [0..255] of the color
	 * blue  Blue component [0..255] of the color
	 */
	public static int argb(int alpha, int red, int green, int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * Returns the relative luminance of a color.
	 * <p>
	 * Assumes sRGB encoding. Based on the formula for relative luminance
	 * defined in WCAG 2.0, W3C Recommendation 11 December 2008.
	 * <p>
	 * a value between 0 (darkest black) and 1 (lightest white)
	 */
	public static float luminance(int color) {
		double red = Color.red(color) / 255.0;
		red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
		double green = Color.green(color) / 255.0;
		green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
		double blue = Color.blue(color) / 255.0;
		blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
		return (float) ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue));
	}

	/**
	 * Parse the color string, and return the corresponding color-int.
	 * If the string cannot be parsed, throws an IllegalArgumentException
	 * exception. Supported formats are:
	 * #RRGGBB
	 * #AARRGGBB
	 * or one of the following names:
	 * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
	 * 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey',
	 * 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple',
	 * 'silver', 'teal'.
	 */
	public static int parseColor(String colorString) {
		if (colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color");
			}
			return (int) color;
		} else {
			Integer color = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
			if (color != null) {
				return color;
			}
		}
		throw new IllegalArgumentException("Unknown color");
	}

	/**
	 * Convert RGB components to HSV.
	 * hsv[0] is Hue [0 .. 360)
	 * hsv[1] is Saturation [0...1]
	 * hsv[2] is Value [0...1]
	 * red  red component value [0..255]
	 * green  green component value [0..255]
	 * blue  blue component value [0..255]
	 * hsv  3 element array which holds the resulting HSV components.
	 */
	public static void RGBToHSV(int red, int green, int blue, float hsv[]) {
		if (hsv.length < 3) {
			throw new RuntimeException("3 components required for hsv");
		}
		nativeRGBToHSV(red, green, blue, hsv);
	}

	/**
	 * Convert the argb color to its HSV components.
	 * hsv[0] is Hue [0 .. 360)
	 * hsv[1] is Saturation [0...1]
	 * hsv[2] is Value [0...1]
	 * color the argb color to convert. The alpha component is ignored.
	 * hsv  3 element array which holds the resulting HSV components.
	 */
	public static void colorToHSV(int color, float hsv[]) {
		RGBToHSV((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
	}

	/**
	 * Convert HSV components to an ARGB color. Alpha set to 0xFF.
	 * hsv[0] is Hue [0 .. 360)
	 * hsv[1] is Saturation [0...1]
	 * hsv[2] is Value [0...1]
	 * If hsv values are out of range, they are pinned.
	 * hsv  3 element array which holds the input HSV components.
	 * the resulting argb color
	 */
	public static int HSVToColor(float hsv[]) {
		return HSVToColor(0xFF, hsv);
	}

	/**
	 * Convert HSV components to an ARGB color. The alpha component is passed
	 * through unchanged.
	 * hsv[0] is Hue [0 .. 360)
	 * hsv[1] is Saturation [0...1]
	 * hsv[2] is Value [0...1]
	 * If hsv values are out of range, they are pinned.
	 * alpha the alpha component of the returned argb color.
	 * hsv  3 element array which holds the input HSV components.
	 * the resulting argb color
	 */
	public static int HSVToColor(int alpha, float hsv[]) {
		if (hsv.length < 3) {
			throw new RuntimeException("3 components required for hsv");
		}
		return nativeHSVToColor(alpha, hsv);
	}


	public static void nativeRGBToHSV(int r, int g, int b, float[] hsv) {
		if(r==g && g==b) return;
		float min, max, delta;
		min = (float)Math.min(Math.min(r, g), b);
		max = (float)Math.max(Math.max(r, g), b);
		// V
		hsv[2] = max;
		delta = max - min;
		// S
		if (max != 0)
			hsv[1] = delta / max;
		else {
			hsv[1] = 0;
			hsv[0] = -1;
		}
		// H
		if (r == max)
			hsv[0] = (g - b) / delta; // between yellow & magenta
		else if (g == max)
			hsv[0] = 2 + (b - r) / delta; // between cyan & yellow
		else
			hsv[0] = 4 + (r - g) / delta; // between magenta & cyan
		hsv[0] *= 60;    // degrees
		if (hsv[0] < 0)
			hsv[0] += 360;
	}

	public static native int nativeHSVToColor(int alpha, float hsv[]);

	/**
	 * Converts an HTML color (named or numeric) to an integer RGB value.
	 * <p>
	 * color Non-null color string.
	 * <p>
	 * A color value, or {-1} if the color string could not be interpreted.
	 *
	 * @hide
	 */
	public static int getHtmlColor(String color) {
		Integer i = sColorNameMap.get(color.toLowerCase(Locale.ROOT));
		if (i != null) {
			return i;
		} else {
			try {
				return convertValueToInt(color, -1);
			} catch (NumberFormatException nfe) {
				return -1;
			}
		}
	}

	private static final HashMap<String, Integer> sColorNameMap;

	static {
		sColorNameMap = new HashMap<String, Integer>();
		sColorNameMap.put("black", BLACK);
		sColorNameMap.put("darkgray", DKGRAY);
		sColorNameMap.put("gray", GRAY);
		sColorNameMap.put("lightgray", LTGRAY);
		sColorNameMap.put("white", WHITE);
		sColorNameMap.put("red", RED);
		sColorNameMap.put("green", GREEN);
		sColorNameMap.put("blue", BLUE);
		sColorNameMap.put("yellow", YELLOW);
		sColorNameMap.put("cyan", CYAN);
		sColorNameMap.put("magenta", MAGENTA);
		sColorNameMap.put("aqua", 0xFF00FFFF);
		sColorNameMap.put("fuchsia", 0xFFFF00FF);
		sColorNameMap.put("darkgrey", DKGRAY);
		sColorNameMap.put("grey", GRAY);
		sColorNameMap.put("lightgrey", LTGRAY);
		sColorNameMap.put("lime", 0xFF00FF00);
		sColorNameMap.put("maroon", 0xFF800000);
		sColorNameMap.put("navy", 0xFF000080);
		sColorNameMap.put("olive", 0xFF808000);
		sColorNameMap.put("purple", 0xFF800080);
		sColorNameMap.put("silver", 0xFFC0C0C0);
		sColorNameMap.put("teal", 0xFF008080);

	}

	public static final int convertValueToInt(CharSequence charSeq, int defaultValue) {
		if (null == charSeq)
			return defaultValue;

		String nm = charSeq.toString();

		// XXX This code is copied from Integer.decode() so we don't
		// have to instantiate an Integer!

		int value;
		int sign = 1;
		int index = 0;
		int len = nm.length();
		int base = 10;

		if ('-' == nm.charAt(0)) {
			sign = -1;
			index++;
		}

		if ('0' == nm.charAt(index)) {
			//  Quick check for a zero by itself
			if (index == (len - 1))
				return 0;

			char c = nm.charAt(index + 1);

			if ('x' == c || 'X' == c) {
				index += 2;
				base = 16;
			} else {
				index++;
				base = 8;
			}
		} else if ('#' == nm.charAt(index)) {
			index++;
			base = 16;
		}

		return Integer.parseInt(nm.substring(index), base) * sign;
	}
}
