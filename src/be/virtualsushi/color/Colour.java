package be.virtualsushi.color;


import java.text.DecimalFormat;

/**
 * The type Colour.
 */
public class Colour extends Color {

	//Color Scheme Enumeration (for color scheme generation)
	public enum ColorScheme {
		ColorSchemeAnalagous, ColorSchemeMonochromatic, ColorSchemeTriad, ColorSchemeComplementary
	}

	public enum ColorDistanceFormula {
		ColorDistanceFormulaCIE76, ColorDistanceFormulaCIE94, ColorDistanceFormulaCIE2000
	}

	public static int[] fromHex(String hex) {
		if (hex.indexOf('#') != 0) hex = "#" + hex;
		return new int[]{
				Integer.valueOf(hex.substring(1, 3), 16),
				Integer.valueOf(hex.substring(3, 5), 16),
				Integer.valueOf(hex.substring(5, 7), 16)
		};
	}

	public static String toHex(int[] rgb){
		return "#"+Integer.toHexString(rgb[0])+Integer.toHexString(rgb[1])+Integer.toHexString(rgb[2]);
	}

	public static String toHex(String r, String g, String b){
		return toHex(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
	}

	public static String toHex(int r, int g, int b){
		DecimalFormat formatter = new DecimalFormat("00");
		return "#"+String.format("%02x", r)+String.format("%02x", g)+String.format("%02x", b);
	}

	// ///////////////////////////////////
	// 4 Color Scheme from Color
	// ///////////////////////////////////

	/**
	 * Creates an int[] of 4 Colors that complement the Color.
	 *
	 * @param type ColorSchemeAnalagous, ColorSchemeMonochromatic,
	 *             ColorSchemeTriad, ColorSchemeComplementary
	 * @return ArrayList<Integer>
	 */
	public static int[] colorSchemeOfType(int color, ColorScheme type) {
		float[] hsv = new float[3];
		colorToHSV(color, hsv);

		switch (type) {
			case ColorSchemeAnalagous:
				return Colour.analagousColors(hsv);
			case ColorSchemeMonochromatic:
				return Colour.monochromaticColors(hsv);
			case ColorSchemeTriad:
				return Colour.triadColors(hsv);
			case ColorSchemeComplementary:
				return Colour.complementaryColors(hsv);
			default:
				return null;
		}
	}

	public static int[] analagousColors(float[] hsv) {
		float[] CA1 = {Colour.addDegrees(hsv[0], 15),
				(float) (hsv[1] - 0.05), (float) (hsv[2] - 0.05)};
		float[] CA2 = {Colour.addDegrees(hsv[0], 30),
				(float) (hsv[1] - 0.05), (float) (hsv[2] - 0.1)};
		float[] CB1 = {Colour.addDegrees(hsv[0], -15),
				(float) (hsv[1] - 0.05), (float) (hsv[2] - 0.05)};
		float[] CB2 = {Colour.addDegrees(hsv[0], -30),
				(float) (hsv[1] - 0.05), (float) (hsv[2] - 0.1)};

		return new int[]{HSVToColor(CA1), HSVToColor(CA2),
				HSVToColor(CB1), HSVToColor(CB2)};
	}

	public static int[] monochromaticColors(float[] hsv) {
		float[] CA1 = {hsv[0], (float) (hsv[1]), (float) (hsv[2] / 2)};
		float[] CA2 = {hsv[0], (float) (hsv[1] / 2), (float) (hsv[2] / 3)};
		float[] CB1 = {hsv[0], (float) (hsv[1] / 3), (float) (hsv[2] * 2 / 3)};
		float[] CB2 = {hsv[0], (float) (hsv[1]), (float) (hsv[2] * 4 / 5)};

		return new int[]{HSVToColor(CA1), HSVToColor(CA2),
				HSVToColor(CB1), HSVToColor(CB2)};
	}

	public static int[] triadColors(float[] hsv) {

		float[] CA1 = {Colour.addDegrees(hsv[0], 120), (float) (hsv[1]),
				(float) (hsv[2])};
		float[] CA2 = {Colour.addDegrees(hsv[0], 120),
				(float) (hsv[1] * 7 / 6), (float) (hsv[2] - 0.05)};
		float[] CB1 = {Colour.addDegrees(hsv[0], 240), (float) (hsv[1]),
				(float) (hsv[2])};
		float[] CB2 = {Colour.addDegrees(hsv[0], 240),
				(float) (hsv[1] * 7 / 6), (float) (hsv[2] - 0.05)};

		return new int[]{HSVToColor(CA1), HSVToColor(CA2),
				HSVToColor(CB1), HSVToColor(CB2)};
	}

	public static int[] complementaryColors(float[] hsv) {
		float[] CA1 = {hsv[0], (float) (hsv[1] * 5 / 7), (float) (hsv[2])};
		float[] CA2 = {hsv[0], (float) (hsv[1]), (float) (hsv[2] * 4 / 5)};
		float[] CB1 = {Colour.addDegrees(hsv[0], 180), (float) (hsv[1]),
				(float) (hsv[2])};
		float[] CB2 = {Colour.addDegrees(hsv[0], 180),
				(float) (hsv[1] * 5 / 7), (float) (hsv[2])};

		return new int[]{HSVToColor(CA1), HSVToColor(CA2),
				HSVToColor(CB1), HSVToColor(CB2)};
	}

	public static float addDegrees(float addDeg, float staticDeg) {
		staticDeg += addDeg;
		if (staticDeg > 360) {
			float offset = staticDeg - 360;
			return offset;
		} else if (staticDeg < 0) {
			return -1 * staticDeg;
		} else {
			return staticDeg;
		}
	}

	/**
	 * Returns black or white, depending on which color would contrast best with the provided color.
	 *
	 * @param color (Color)
	 * @return int
	 */
	public static int blackOrWhiteContrastingColor(int color) {
		int[] rgbaArray = new int[]{Colour.red(color), Colour.green(color), Colour.blue(color)};
		double a = 1 - ((0.00299 * (double) rgbaArray[0]) + (0.00587 * (double) rgbaArray[1]) + (0.00114 * (double) rgbaArray[2]));
		return a < 0.5 ? Colour.BLACK : Colour.WHITE;
	}


	/**
	 * This method will create a color instance that is the exact opposite color from another color on the color wheel. The same saturation and brightness are preserved, just the hue is changed.
	 *
	 * @param color (Color)
	 * @return int
	 */
	public static int complementaryColor(int color) {
		float[] hsv = new float[3];
		Colour.colorToHSV(color, hsv);
		float newH = Colour.addDegrees(180, hsv[0]);
		hsv[0] = newH;

		return Colour.HSVToColor(hsv);
	}

	// CMYK

	/**
	 * Color to cMYK.
	 *
	 * @return float [ ]
	 */
	public static float[] colorToCMYK(int r, int g, int b) {
		float c = 1 - r / 255;
		float m = 1 - g / 255;
		float y = 1 - b / 255;
		float k = Math.min(1, Math.min(c, Math.min(m, y)));
		if (k == 1) {
			c = 0;
			m = 0;
			y = 0;
		} else {
			c = (c - k) / (1 - k);
			m = (m - k) / (1 - k);
			y = (y - k) / (1 - k);
		}

		return new float[]{c, m, y, k};
	}


	/**
	 * CMYK to color.
	 *
	 * @param cmyk the cmyk array
	 * @return color
	 */
	public static int CMYKtoColor(float[] cmyk) {
		float c = cmyk[0] * (1 - cmyk[3]) + cmyk[3];
		float m = cmyk[1] * (1 - cmyk[3]) + cmyk[3];
		float y = cmyk[2] * (1 - cmyk[3]) + cmyk[3];
		return Colour.rgb((int) ((1 - c) * 255), (int) ((1 - m) * 255), (int) ((1 - y) * 255));
	}

	/**
	 * Color to cIE _ lAB.
	 *
	 * @return double[]
	 */
	public static double[] colorToCIE_LAB(int rint, int gint, int bint) {
		// Convert Color to XYZ format first
		double r = rint / 255.0;
		double g = gint / 255.0;
		double b = bint / 255.0;

		// Create deltaRGB
		r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.40) : (r / 12.92);
		g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.40) : (g / 12.92);
		b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.40) : (b / 12.92);


		// Create XYZ
		double X = r * 41.24 + g * 35.76 + b * 18.05;
		double Y = r * 21.26 + g * 71.52 + b * 7.22;
		double Z = r * 1.93 + g * 11.92 + b * 95.05;

		// Convert XYZ to L*a*b*
		X = X / 95.047;
		Y = Y / 100.000;
		Z = Z / 108.883;
		X = (X > Math.pow((6.0 / 29.0), 3.0)) ? Math.pow(X, 1.0 / 3.0) : (1 / 3) * Math.pow((29.0 / 6.0), 2.0) * X + 4 / 29.0;
		Y = (Y > Math.pow((6.0 / 29.0), 3.0)) ? Math.pow(Y, 1.0 / 3.0) : (1 / 3) * Math.pow((29.0 / 6.0), 2.0) * Y + 4 / 29.0;
		Z = (Z > Math.pow((6.0 / 29.0), 3.0)) ? Math.pow(Z, 1.0 / 3.0) : (1 / 3) * Math.pow((29.0 / 6.0), 2.0) * Z + 4 / 29.0;
		double CIE_L = 116 * Y - 16;
		double CIE_a = 500 * (X - Y);
		double CIE_b = 200 * (Y - Z);
		return new double[]{CIE_L, CIE_a, CIE_b};
	}

	/**
	 * CIE _ lab to color.
	 *
	 * @param cie_lab the double[]
	 * @return color
	 */
	public static int[] CIE_LabToColor(double[] cie_lab) {
		double L = cie_lab[0];
		double A = cie_lab[1];
		double B = cie_lab[2];
		double Y = (L + 16.0) / 116.0;
		double X = A / 500 + Y;
		double Z = Y - B / 200;
		X = (Math.pow(X, 3.0) > 0.008856) ? Math.pow(X, 3.0) : (X - 4 / 29.0) / 7.787;
		Y = (Math.pow(Y, 3.0) > 0.008856) ? Math.pow(Y, 3.0) : (Y - 4 / 29.0) / 7.787;
		Z = (Math.pow(Z, 3.0) > 0.008856) ? Math.pow(Z, 3.0) : (Z - 4 / 29.0) / 7.787;
		X = X * .95047;
		Y = Y * 1.00000;
		Z = Z * 1.08883;

		// Convert XYZ to RGB
		double R = X * 3.2406 + Y * -1.5372 + Z * -0.4986;
		double G = X * -0.9689 + Y * 1.8758 + Z * 0.0415;
		double _B = X * 0.0557 + Y * -0.2040 + Z * 1.0570;
		R = (R > 0.0031308) ? 1.055 * (Math.pow(R, (1 / 2.4))) - 0.055 : R * 12.92;
		G = (G > 0.0031308) ? 1.055 * (Math.pow(G, (1 / 2.4))) - 0.055 : G * 12.92;
		_B = (_B > 0.0031308) ? 1.055 * (Math.pow(_B, (1 / 2.4))) - 0.055 : _B * 12.92;
		return new int[]{(int) (R * 255), (int) (G * 255), (int) (_B * 255)};
	}

	public static double distanceBetweenColors(int[] colorA, int[] colorB) {
		return distanceBetweenColorsWithFormula(colorA, colorB, ColorDistanceFormula.ColorDistanceFormulaCIE94);
	}

	public static double distanceBetweenColorsWithFormula(int[] colorA, int[] colorB, ColorDistanceFormula formula) {
		double[] lab1 = Colour.colorToCIE_LAB(colorA[0], colorA[1], colorA[2]);
		double[] lab2 = Colour.colorToCIE_LAB(colorB[0], colorB[1], colorB[2]);
		double L1 = lab1[0];
		double A1 = lab1[1];
		double B1 = lab1[2];
		double L2 = lab2[0];
		double A2 = lab2[1];
		double B2 = lab2[2];

		// CIE76 first
		if (formula == ColorDistanceFormula.ColorDistanceFormulaCIE76) {
			double distance = Math.sqrt(Math.pow((L1 - L2), 2) + Math.pow((A1 - A2), 2) + Math.pow((B1 - B2), 2));
			return distance;
		}

		// More Common Variables
		double kL = 1;
		double kC = 1;
		double kH = 1;
		double k1 = 0.045;
		double k2 = 0.015;
		double deltaL = L1 - L2;
		double C1 = Math.sqrt((A1 * A1) + (B1 * B1));
		double C2 = Math.sqrt((A2 * A2) + (B2 * B2));
		double deltaC = C1 - C2;
		double deltaH_sq = Math.pow((A1 - A2), 2.0) + Math.pow((B1 - B2), 2.0) - Math.pow(deltaC, 2.0);
		double deltaH = deltaH_sq > 0 ? Math.sqrt(deltaH_sq) : 0;
		double sL = 1;
		double sC = 1 + k1 * (Math.sqrt((A1 * A1) + (B1 * B1)));
		double sH = 1 + k2 * (Math.sqrt((A1 * A1) + (B1 * B1)));

		// CIE94
		if (formula == ColorDistanceFormula.ColorDistanceFormulaCIE94) {
			return Math.sqrt(Math.pow((deltaL / (kL * sL)), 2.0) + Math.pow((deltaC / (kC * sC)), 2.0) + Math.pow((deltaH / (kH * sH)), 2.0));
		}

		// CIE2000
		// More variables
		double deltaLPrime = L2 - L1;
		double meanL = (L1 + L2) / 2;
		double meanC = (C1 + C2) / 2;
		double aPrime1 = A1 + A1 / 2 * (1 - Math.sqrt(Math.pow(meanC, 7.0) / (Math.pow(meanC, 7.0) + Math.pow(25.0, 7.0))));
		double aPrime2 = A2 + A2 / 2 * (1 - Math.sqrt(Math.pow(meanC, 7.0) / (Math.pow(meanC, 7.0) + Math.pow(25.0, 7.0))));
		double cPrime1 = Math.sqrt((aPrime1 * aPrime1) + (B1 * B1));
		double cPrime2 = Math.sqrt((aPrime2 * aPrime2) + (B2 * B2));
		double cMeanPrime = (cPrime1 + cPrime2) / 2;
		double deltaCPrime = cPrime1 - cPrime2;
		double hPrime1 = Math.atan2(B1, aPrime1);
		double hPrime2 = Math.atan2(B2, aPrime2);
		hPrime1 = hPrime1 % RAD(360.0);
		hPrime2 = hPrime2 % RAD(360.0);
		double deltahPrime = 0;
		if (Math.abs(hPrime1 - hPrime2) <= RAD(180.0)) {
			deltahPrime = hPrime2 - hPrime1;
		} else {
			deltahPrime = (hPrime2 <= hPrime1) ? hPrime2 - hPrime1 + RAD(360.0) : hPrime2 - hPrime1 - RAD(360.0);
		}
		double deltaHPrime = 2 * Math.sqrt(cPrime1 * cPrime2) * Math.sin(deltahPrime / 2);
		double meanHPrime = (Math.abs(hPrime1 - hPrime2) <= RAD(180.0)) ? (hPrime1 + hPrime2) / 2 : (hPrime1 + hPrime2 + RAD(360.0)) / 2;
		double T = 1 - 0.17 * Math.cos(meanHPrime - RAD(30.0)) + 0.24 * Math.cos(2 * meanHPrime) + 0.32 * Math.cos(3 * meanHPrime + RAD(6.0)) - 0.20 * Math.cos(4 * meanHPrime - RAD(63.0));
		sL = 1 + (0.015 * Math.pow((meanL - 50), 2)) / Math.sqrt(20 + Math.pow((meanL - 50), 2));
		sC = 1 + 0.045 * cMeanPrime;
		sH = 1 + 0.015 * cMeanPrime * T;
		double Rt = -2 * Math.sqrt(Math.pow(cMeanPrime, 7) / (Math.pow(cMeanPrime, 7) + Math.pow(25.0, 7))) * Math.sin(RAD(60.0) * Math.exp(-1 * Math.pow((meanHPrime - RAD(275.0)) / RAD(25.0), 2)));

		// Finally return CIE2000 distance
		return Math.sqrt(Math.pow((deltaLPrime / (kL * sL)), 2) + Math.pow((deltaCPrime / (kC * sC)), 2) + Math.pow((deltaHPrime / (kH * sH)), Rt * (deltaC / (kC * sC)) * (deltaHPrime / (kH * sH))));
	}

	public static int getBrightness(int[] rgb) {
		return (int) Math.sqrt(
				rgb[0] * rgb[0] * .241 +
						rgb[1] * rgb[1] * .691 +
						rgb[2] * rgb[2] * .068);
	}

	public static int getBrightness(String hex) {
		int[] rgb = fromHex(hex);
		System.out.println("Brightness => " + hex + " - " + getBrightness(rgb));
		return getBrightness(rgb);
	}

	private static double RAD(double degree) {
		return degree * Math.PI / 180;
	}

	//*******************************************
	// From here, all code has been added by Jef
	//*******************************************

	public static boolean isClose(String color1, String color2, int REFERENCE_DISTANCE) {
		//grey colors can only be close to other grey colors
		if ((isGrey(color1) && !isGrey(color2)) || (!isGrey(color1) && isGrey(color2)))
			return false;
		if (isGrey(color1) && isGrey(color2))
			return true;
		double distance = Colour.distanceBetweenColorsWithFormula(
				Colour.fromHex(color1),
				Colour.fromHex(color2),
				Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		System.out.println(String.format("distance between %s and %s = %f", color1, color2, distance));

		return Colour.distanceBetweenColorsWithFormula(
				Colour.fromHex(color1),
				Colour.fromHex(color2),
				Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94) < REFERENCE_DISTANCE;
	}

	public static boolean isGrey(String color) {
		if (color.indexOf("#") == 0) color = color.substring(1);
		return color.substring(0, 2).equalsIgnoreCase(color.substring(2, 4)) && color.substring(0, 2).equalsIgnoreCase(color.substring(4, 6));
	}

	public static boolean isGreyish(String color) {
		int[] rgb = Colour.fromHex(color);
		return isGreyish(rgb);
	}

	public static boolean isGreyish(int[] rgb) {
		if(Math.abs(rgb[0] - rgb[1]) > 20) return false;
		if(Math.abs(rgb[1] - rgb[2]) > 20) return false;
		if(Math.abs(rgb[2] - rgb[0]) > 20) return false;
		return true;
	}

	public static boolean isYellow(String color) {
		int[] rgb = Colour.fromHex(color);
		if(isGreyish(rgb)) return false;
		int iColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		float[] hsv = new float[3];
		Colour.colorToHSV(iColor, hsv);
		return hsv[0] > 51 && hsv[0] <= 60;
	}

	public static boolean isYG(String color) {
		int[] rgb = Colour.fromHex(color);
		if(isGreyish(rgb)) return false;
		int iColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		float[] hsv = new float[3];
		Colour.colorToHSV(iColor, hsv);
		return hsv[0] > 61 && hsv[0] <= 80;
	}

	public static boolean isGreen(String color) {
		int[] rgb = Colour.fromHex(color);
		if(isGreyish(rgb)) return false;
		int iColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		float[] hsv = new float[3];
		Colour.colorToHSV(iColor, hsv);
		System.out.printf("Hue for %s is %s\n", color, hsv[0]+"");
		return hsv[0] > 81 && hsv[0] <= 140;
	}

	public static boolean isGC(String color) {
		int[] rgb = Colour.fromHex(color);
		if(isGreyish(rgb)) return false;
		int iColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		float[] hsv = new float[3];
		Colour.colorToHSV(iColor, hsv);
		return hsv[0] > 141 && hsv[0] <= 169;
	}

	public static boolean isYellowOrGreen(String color) {
		int[] rgb = Colour.fromHex(color);
		if(isGreyish(rgb)) return false;
		int iColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
		float[] hsv = new float[3];
		Colour.colorToHSV(iColor, hsv);
		return hsv[0] > 51 && hsv[0] <= 169;
	}


}