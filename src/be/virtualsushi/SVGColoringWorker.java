package be.virtualsushi;

import be.virtualsushi.color.Colour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created on IntelliJ
 * User: Jef Waumans for Virtual Sushi
 * Date: 06/09/16
 * Time: 20:14
 */
public class SVGColoringWorker {

	private static String sourcefolder = "/home/jefw/virtualsushi/svgworker/svg_orig/";
	private static String destfolder = "/home/jefw/virtualsushi/svgworker/svg_created/";
	private static String name = "";

	private static int REFERENCE_DISTANCE = 20;
	private static final int VERTICAL = 0;
	private static final int HORIZONTAL = 1;

	private Set<String> allColors = new HashSet<>();
	private Set<String> remainingColors = new HashSet<>();
	private List<Set<String>> matchingColors;
	private Map<String, TinaColor> tinaColors;

	public static void main(String[] args) {
		SVGColoringWorker worker = new SVGColoringWorker();
		worker.start(args[0]);


		System.out.println("test.size() = " + worker.allColors.size());
	}

	public void start(String name) {
		this.name = name;
		doStart();
	}

	private void doStart() {
		replaceRGB();
		replaceStyles();
		simplifyGradients();
		findAllColors();
		matchColors();
		//Create a map of TinaColors
		makeTinaColors();
		//Replace simple colors by uni-color gradients
		replaceSimpleColors();
		reworkGradients();
	}

	private void replaceRGB() {
		int[] stopCounter = {0};
		String[] lastStop = new String[1];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + "_norgb_1.svg")));
			String uri = destfolder + name + ".svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {

					if (e.contains("rgb(")) {
						Pattern patt = Pattern.compile("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
						Matcher matcher = patt.matcher(e);
						StringBuffer sb = new StringBuffer();
						while (matcher.find()) {
							String r = matcher.group(1);
							String g = matcher.group(2);
							String b = matcher.group(3);
							matcher.appendReplacement(sb, Colour.toHex(r, g, b));
							matcher.appendTail(sb);
							//s = s.replace(s_number, multiply(s_number));
							//s = s.replaceAll("(\\D)"+s_number+"(\\D)", "$1"+multiply(s_number)+"$2");
						}
						writer.append(sb);
						writer.append(System.lineSeparator());
					} else {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// change style="fill: url(#XMLID_63_) #000000;" into fill="url(#XMLID_63)"
	private void replaceStyles() {
		int[] stopCounter = {0};
		String[] lastStop = new String[1];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + "_nostyle_2.svg")));
			String uri = destfolder + name + "_norgb_1.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {

					if (isPath(e) && e.matches(".*style=\"[^\"]*fill:.*")) {
						Pattern patt = Pattern.compile("style=\"([^\"]*)fill:\\s*(url\\(#[^\\)]+\\))?(\\s*)(#[a-zA-Z0-9]{6}|none)?;?([^\"]*)");//@TODO OR 'none' color !!!!!
						Matcher matcher = patt.matcher(e);
						StringBuffer sb = new StringBuffer();
						while (matcher.find()) {
							String url = matcher.group(2);
							url = (url==null||url.equals(""))?"":url+" ";
							String color = (matcher.group(4)==null || "none".equals(matcher.group(4))) ? "" : matcher.group(4);
							matcher.appendReplacement(sb, "fill=\""+url+color+"\" style=\"$1$3$5");
							matcher.appendTail(sb);
							//s = s.replace(s_number, multiply(s_number));
							//s = s.replaceAll("(\\D)"+s_number+"(\\D)", "$1"+multiply(s_number)+"$2");
						}
						writer.append(sb);
						writer.append(System.lineSeparator());
					} else {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void simplifyGradients() {
		int[] stopCounter = {0};
		String[] lastStop = new String[1];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + "_coloured_3.svg")));
			String uri = destfolder + name + "_nostyle_2.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					boolean inGradient = false;

					if (e.contains("<linearGradient ")|| e.contains("<radialGradient ")) {
						inGradient = true;
						writer.append(e);
						writer.append(System.lineSeparator());
					} else if (e.contains("<stop ")) {
						if (stopCounter[0] == 0) {
							writer.append(e.replaceAll("(offset=\"[0-9\\.e-]+\")", "offset=\"0\"")
									.replaceAll("style=\"stop-color:\\s*#([0-9a-fA-F]{6});?\\s*([^\"]*)", "stop-color=\"#$1\" style=\"$2"));
							writer.append(System.lineSeparator());
						} else {
							lastStop[0] = e.replaceAll("style=\"stop-color:\\s*#([0-9a-fA-F]{6});?\\s*([^\"]*)", "stop-color=\"#$1\" style=\"$2");
						}
						stopCounter[0]++;
					} else if (e.contains("</linearGradient") || e.contains("</radialGradient")) {
						writer.append(lastStop[0].replaceAll("(offset=\"[0-9\\.]+\")", "offset=\"1\""));
						writer.append(System.lineSeparator());
						writer.append(e);
						writer.append(System.lineSeparator());
						stopCounter[0] = 0;
					} else {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void findAllColors() {
		try {
			String uri = destfolder + name + "_coloured_3.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					allColors.addAll(findColors(e));
					remainingColors.addAll(findColors(e));
				} catch (RuntimeException e1) {
					e1.printStackTrace();
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (allColors.size() > 100) REFERENCE_DISTANCE = 8;
		else if (allColors.size() > 80) REFERENCE_DISTANCE = 10;
		else if (allColors.size() > 60) REFERENCE_DISTANCE = 12;
		else if (allColors.size() > 40) REFERENCE_DISTANCE = 14;
		else if (allColors.size() > 20) REFERENCE_DISTANCE = 16;
		else if (allColors.size() > 10) REFERENCE_DISTANCE = 18;
	}

	private void matchColors() {
		matchingColors = new ArrayList<>();
		for (String aColor : allColors) {
			if (remainingColors.contains(aColor)) {
				remainingColors.remove(aColor);
				//if (!Colour.isGrey(aColor)) {
					Set<String> container = getMatchedSet(aColor, matchingColors);
					if (container == null) {
						container = new HashSet<>();
						container.add(aColor);
						matchingColors.add(container);
					}
					addMatchersToContainer(aColor, container);
				//}
			}
		}
	}

	private void addMatchersToContainer(String aColor, Set<String> container) {
		for (String newColor : allColors) {
			if (remainingColors.contains(newColor)) {
				if (Colour.isClose(aColor, newColor, REFERENCE_DISTANCE)) {
					remainingColors.remove(newColor);
					addMatchersToContainer(newColor, container);
					container.add(newColor);
				}
			}
		}
	}

	private void makeTinaColors() {
		tinaColors = new HashMap<>();
		for (Set<String> matchingColor : matchingColors) {
			Map<String, Integer> brightnesses = new HashMap<>();
			for (String aColor : matchingColor) {
				TinaColor tinaColor = new TinaColor();
				tinaColor.setColor(aColor);
				tinaColor.setMatchSet(getMatchSet(aColor));
				tinaColor.setBrightness(Colour.getBrightness(aColor));
				tinaColors.put(aColor, tinaColor);
				brightnesses.put(aColor, tinaColor.getBrightness());
			}
			//calculate the mean of all brightnesses
			int brightnessSum = 0;
			for (String aColor : matchingColor) {
				TinaColor current = tinaColors.get(aColor);
				brightnessSum += current.getBrightness();
			}
			int mean = brightnessSum / matchingColor.size();
			//find the tina whose brightness value is closest to the mean and point this tina as the median
			int brightnessDiff = 1000;
			String medianId = null;
			for (String aColor : matchingColor) {
				TinaColor current = tinaColors.get(aColor);
				if (current.getBrightness() > 0 && Math.abs(current.getBrightness() - mean) < brightnessDiff) {
					medianId = current.getColor();
					brightnessDiff = Math.abs(current.getBrightness() - mean);
				}
			}

			//now calculate the relative brightnesses
			for (String aColor : matchingColor) {
				TinaColor current = tinaColors.get(aColor);
				current.setRelativeBrightness(calculateRelativeBrightness(current.getBrightness(), tinaColors.get(medianId).getBrightness(), medianId));
			}
		}
	}

	private int getMatchSet(String color) {
		int counter = 1;
		for (Set<String> matchingColor : matchingColors) {
			for (String aColor : matchingColor) {
				if (color.equals(aColor))
					return counter;
			}
			counter++;
		}
		return -1;
	}

	private int calculateRelativeBrightness(int currentBrightness, int referenceBrightness, String referenceColor) {
		if (Colour.isGreen(referenceColor)) System.out.printf("Color %s is green\n", referenceColor);
		if (Colour.isYellow(referenceColor)) System.out.printf("Color %s is yellow\n", referenceColor);
		if (Colour.isYG(referenceColor)) System.out.printf("Color %s is yg\n", referenceColor);
		if (Colour.isGC(referenceColor)) System.out.printf("Color %s is gc\n", referenceColor);

		/*//default, if brighter then ref color => use a big brightness factor
		int brightnessFactor = 75;
		//if darker then ref color => use a little smaller brightness factor
		if(Colour.isYellowOrGreen(referenceColor)) //@todo then add 'yellow class' else add 'blue' class
			return referenceBrightness==0 ? 0 : ((currentBrightness - referenceBrightness)*brightnessFactor)/referenceBrightness;
		else
			return referenceBrightness==0 ? 0 : ((currentBrightness - referenceBrightness)*brightnessFactor)/referenceBrightness;*/

		return referenceBrightness == 0 ? 0 : (100 * (currentBrightness - referenceBrightness)) / referenceBrightness;
	}

	private void replaceSimpleColors() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + "_all_grads_4.svg")));
			String uri = destfolder + name + "_coloured_3.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {

					if (isPath(e) && e.matches(".*fill=\"#.*")) {
						String simpleColor = getSimpleColor(e);
						String gradientId = UUID.randomUUID().toString();
						String simpleGradient = getSimpleGradient(gradientId, simpleColor);
						writer.append(simpleGradient);
						writer.append(System.lineSeparator());
						writer.append(e.replaceAll("fill=\"\\s*(#[0-9a-fA-F]{6}|none)", "fill=\"url(#" + gradientId + ")"));
						writer.append(System.lineSeparator());
					} else if (isPath(e) && e.matches(".*fill:\\s*#.*")) {
						String simpleColor = getSimpleColor(e);
						String gradientId = UUID.randomUUID().toString();
						String simpleGradient = getSimpleGradient(gradientId, simpleColor);
						writer.append(simpleGradient);
						writer.append(System.lineSeparator());
						writer.append(e
								.replaceAll("fill:\\s*#[0-9a-fA-F]{6};", "")
								.replaceAll("style", "fill=\"url(#" + gradientId + ")\" style"));
						writer.append(System.lineSeparator());
					} else {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isPath(String line){
		if(line.contains("<path ")) return true;
		else if(line.contains("<ellipse ")) return true;
		else return false;
	}

	private String getSimpleColor(String line) {
		String color = null;
		Pattern patt = Pattern.compile("(#[0-9a-fA-F]{6})");
		Matcher matcher = patt.matcher(line);
		if (matcher.find()) {
			color = matcher.group(1);
		}
		//Is it rgb()
		if (color != null)
			return color;
		patt = Pattern.compile("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
		matcher = patt.matcher(line);
		if (matcher.find()) {
			color = getSimpleColor(Colour.toHex(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))));
		}
		return color;
	}

	private String getSimpleGradient(String gradientId, String simpleColor) {
		StringBuilder builder = new StringBuilder();
		return builder.append("<linearGradient id=\"").append(gradientId).append("\"").append(" >")
				.append(System.lineSeparator())
				.append("<stop stop-color=\"" + simpleColor + "\"></stop>")
				.append(System.lineSeparator())
				.append("</linearGradient>").toString();
	}

	private void reworkGradients() {
		try {
			String[] gradients = new String[1];
			int[] stopCounter = {0};
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + "_full_grads_5.svg")));
			String uri = destfolder + name + "_all_grads_4.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if (e.contains("<linearGradient ") || e.contains("<radialGradient ")) {
						gradients[0] = e;
					} else if (e.contains("<stop ")) {
						String simpleColor = getSimpleColor(e);
						if (simpleColor != null) {
							TinaColor currentColor = tinaColors.get(simpleColor.substring(1));
							if (currentColor != null) {
								e = e.replace("<stop ", "<stop data-target=\"" + currentColor.getRelativeBrightness() + "\" ");
								if (stopCounter[0] == 0) {
									writer.append(gradients[0].replaceAll("id=", "class=\"color_" + currentColor.getMatchSet() + " " + "yellowOrBlue" + "\" id="));
									writer.append(System.lineSeparator());
								}
							} else {
								writer.append(gradients[0]);
								writer.append(System.lineSeparator());
							}
						} else {
							writer.append(gradients[0]);
							writer.append(System.lineSeparator());
						}
						stopCounter[0]++;
						writer.append(e);
						writer.append(System.lineSeparator());
					} else if (e.contains("</linearGradient") || e.contains("</radialGradient")) {
						writer.append(e);
						writer.append(System.lineSeparator());
						stopCounter[0] = 0;
					} else {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Set<String> getMatchedSet(String aColor, List<Set<String>> matchingColors) {
		for (Set<String> matchingColorSet : matchingColors) {
			if (matchingColorSet.contains(aColor))
				return matchingColorSet;
		}
		return null;
	}

	private Set<String> findColors(String line) {
		Set<String> colors = new HashSet<>();
		Pattern patt = Pattern.compile("#([0-9a-fA-F]{6})");
		Matcher mat = patt.matcher(line);
		while (mat.find()) {
			colors.add(mat.group(1));
		}
		return colors;
	}

	private static class TinaColor {
		private String color;
		private int matchSet;
		private int brightness;
		private int relativeBrightness;

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public int getMatchSet() {
			return matchSet;
		}

		public void setMatchSet(int matchSet) {
			this.matchSet = matchSet;
		}

		public int getBrightness() {
			return brightness;
		}

		public void setBrightness(int brightness) {
			this.brightness = brightness;
		}

		public int getRelativeBrightness() {
			return relativeBrightness;
		}

		public void setRelativeBrightness(int relativeBrightness) {
			this.relativeBrightness = relativeBrightness;
		}
	}


	public static void colorify() {
		String color1 = "#D46A6A";//pink
		String color2 = "#AA3939";//dark pink - d1
		String color3 = "#FFAAAA";//light pink - d2
		String color4 = "#4dfe61";//fluo green - d3
		String color5 = "#4540fb";//purple blue - d4
		String color6 = "#182d63";//indigo - d5
		String color7 = "#e0232f";//red - d6
		int[] test1 = Colour.fromHex(color1);
		int[] test2 = Colour.fromHex(color2);
		int[] test3 = Colour.fromHex(color3);
		int[] test4 = Colour.fromHex(color4);
		int[] test5 = Colour.fromHex(color5);
		int[] test6 = Colour.fromHex(color6);
		int[] test7 = Colour.fromHex(color7);
		double distance1 = Colour.distanceBetweenColorsWithFormula(test1, test2, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		double distance2 = Colour.distanceBetweenColorsWithFormula(test1, test3, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		double distance3 = Colour.distanceBetweenColorsWithFormula(test1, test4, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		double distance4 = Colour.distanceBetweenColorsWithFormula(test1, test5, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		double distance5 = Colour.distanceBetweenColorsWithFormula(test1, test6, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		double distance6 = Colour.distanceBetweenColorsWithFormula(test1, test7, Colour.ColorDistanceFormula.ColorDistanceFormulaCIE94);
		System.out.println("distance1 = " + distance1);
		System.out.println("distance2 = " + distance2);
		System.out.println("distance3 = " + distance3);
		System.out.println("distance4 = " + distance4);
		System.out.println("distance5 = " + distance5);
		System.out.println("distance6 = " + distance6);
	}

	public void test() {
		Colour.getBrightness("#FFFF00");
		Colour.getBrightness("#FF00FF");
		Colour.getBrightness("#00FFFF");
		Colour.getBrightness("#FF0000");
		Colour.getBrightness("#00FF00");
		Colour.getBrightness("#0000FF");
	}
}
