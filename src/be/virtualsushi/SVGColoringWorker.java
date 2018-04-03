package be.virtualsushi;

import be.virtualsushi.color.Colour;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

	//private static String sourcefolder = "svg_orig/";
	//private static String destfolder = "svg_created/";
	private static String name = "";
	private static String gender = "";
	private static String folder = "";

	//private static int REFERENCE_DISTANCE = 20;
	private static final int VERTICAL = 0;
	private static final int HORIZONTAL = 1;

	private Set<String> allColors = new HashSet<>();
	private Set<String> remainingColors = new HashSet<>();
	private List<Set<String>> matchingColors;
	private Map<String, TinaColor> tinaColors;

	private int reference_distance;
	private String coloringMethod;


	public SVGColoringWorker(int reference_distance, String coloringMethod) {
		this.reference_distance = reference_distance;
		this.coloringMethod = coloringMethod;
	}

	/*public static void main(String[] args) {
		SVGColoringWorker worker = new SVGColoringWorker();
		worker.start(args[0]);


		System.out.println("test.size() = " + worker.allColors.size());
	}*/

	public void start(String folder, String gender, String name) {
		this.name = name;
		this.gender = gender;
		this.folder = folder;
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
		replaceIDs();
		cleanup();
	}

	private void replaceRGB() {
		int[] stopCounter = {0};
		String[] lastStop = new String[1];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, name, "_norgb_1.svg"))));
			String uri = String.format("%s%s/%s%s",folder, gender, name, ".svg");
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, name, "_nostyle_2.svg"))));
			String uri = String.format("%s%s/%s%s",folder, gender, name, "_norgb_1.svg");
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
		//int[] stopCounter = {0};
		String[] lastStop = new String[1];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, name, "_coloured_3.svg"))));
			String uri = String.format("%s%s/%s%s",folder, gender, name, "_nostyle_2.svg");
			Stream<String> stream = Files.lines(Paths.get(uri));
			String[] gradientLine = new String[1];
			String[] darkest = new String[1];
			String[] brightest = new String[1];
			String[] darkestLine = new String[1];
			String[] brightestLine = new String[1];
			stream.forEach(e -> {
				try {
					if (e.contains("<linearGradient ")|| e.contains("<radialGradient ")) {
						gradientLine[0] = e;
					} else if (e.contains("<stop ")) {
						String currentColor = findColor(e);
						if(isDarker(currentColor, darkest[0])){
							darkest[0] = currentColor;
							darkestLine[0] = e;
						}
						if(isBrighter(currentColor, brightest[0])){
							brightest[0] = currentColor;
							brightestLine[0] = e;
						}

						/*if (stopCounter[0] == 0) {
							writer.append(e.replaceAll("(offset=\"[0-9\\.e-]+\")", "offset=\"0\"")
									.replaceAll("style=\"stop-color:\\s*#([0-9a-fA-F]{6});?\\s*([^\"]*)", "stop-color=\"#$1\" style=\"$2"));
							writer.append(System.lineSeparator());
						} else {
							lastStop[0] = e.replaceAll("style=\"stop-color:\\s*#([0-9a-fA-F]{6});?\\s*([^\"]*)", "stop-color=\"#$1\" style=\"$2");
						}*/
						//stopCounter[0]++;
					} else if (e.contains("</linearGradient") || e.contains("</radialGradient")) {
						double darkStop = findOffset(darkestLine[0]);
						double brightStop = findOffset(brightestLine[0]);
						writer.append(gradientLine[0]);
						writer.append(System.lineSeparator());
						if(darkStop>brightStop){
							writer.append(String.format("<stop offset=\"0\" stop-color=\"#%s\" />", brightest));
							writer.append(System.lineSeparator());
							writer.append(String.format("<stop offset=\"1\" stop-color=\"#%s\" />", darkest));
						}
						else{
							writer.append(String.format("<stop offset=\"0\" stop-color=\"#%s\" />", darkest));
							writer.append(System.lineSeparator());
							writer.append(String.format("<stop offset=\"1\" stop-color=\"#%s\" />", brightest));
						}


						//writer.append(lastStop[0].replaceAll("(offset=\"[0-9\\.]+\")", "offset=\"1\""));
						writer.append(System.lineSeparator());
						writer.append(e);
						writer.append(System.lineSeparator());
						//stopCounter[0] = 0;
						darkest[0] = null;
						brightest[0] = null;
						darkestLine[0]= null;
						brightestLine[0] = null;
						gradientLine[0] = null;
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

	private boolean isBrighter(String color, String brightestColor){
		if(brightestColor==null) return true;
		if(Colour.getBrightness(color) > Colour.getBrightness(brightestColor))
			System.out.println(color+" is brighter then"+brightestColor);
		return Colour.getBrightness(color) > Colour.getBrightness(brightestColor);
	}

	private boolean isDarker(String color, String darkestColor){
		if(darkestColor==null) return true;
		if(Colour.getBrightness(color) < Colour.getBrightness(darkestColor))
			System.out.println(color+" is darker then"+darkestColor);
		return Colour.getBrightness(color) < Colour.getBrightness(darkestColor);
	}

	private void findAllColors() {
		try {
			String uri = String.format("%s%s/%s%s",folder, gender, name, "_coloured_3.svg");
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

		/*if (allColors.size() > 100) REFERENCE_DISTANCE = 40;
		else if (allColors.size() > 80) REFERENCE_DISTANCE = 36;
		else if (allColors.size() > 60) REFERENCE_DISTANCE = 32;
		else if (allColors.size() > 40) REFERENCE_DISTANCE = 28;
		else if (allColors.size() > 20) REFERENCE_DISTANCE = 24;
		else
			REFERENCE_DISTANCE = 20;*/
		/*else if (allColors.size() > 10) REFERENCE_DISTANCE = 20;
		else if (allColors.size() <= 5) REFERENCE_DISTANCE = 16;*/
	}

	//Create a container with very different colors (depends on the color distance)
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

	//Add aColor to the container if it's very different from the container's main color(s)
	private void addMatchersToContainer(String aColor, Set<String> container) {
		for (String newColor : allColors) {
			if (remainingColors.contains(newColor)) {
				if (Colour.isClose(aColor, newColor, this.reference_distance)) {
					remainingColors.remove(newColor);
					addMatchersToContainer(newColor, container);
					container.add(newColor);
				}
			}
		}
	}

	private void makeTinaColors() {
		tinaColors = new HashMap<>();

		List<TinaColor> orderedTinaColors = new ArrayList<>();
		for (Set<String> matchingColor : matchingColors) {
			//Map<String, Integer> brightnesses = new HashMap<>();
			for (String aColor : matchingColor) {
				TinaColor tinaColor = new TinaColor();
				tinaColor.setColor(aColor);
				tinaColor.setMatchSet(getMatchSet(aColor));
				tinaColor.setBrightness(Colour.getBrightness(aColor));
				TinaColor tinaColor2 = new TinaColor();
				tinaColor2.setColor(aColor);
				tinaColor2.setMatchSet(getMatchSet(aColor));
				tinaColor2.setBrightness(Colour.getBrightness(aColor));
				orderedTinaColors.add(tinaColor);
				tinaColors.put(tinaColor.getColor(), tinaColor);
				//brightnesses.put(aColor, tinaColor.getBrightness());
			}

			//Order by brightness
			Collections.sort(orderedTinaColors, new Comparator<TinaColor>() {
				public int compare(TinaColor tc1, TinaColor tc2) {
					return tc1.getBrightness() - tc2.getBrightness();
				}
			});
			if("BRIGHTEST".equals(this.coloringMethod)) {
				//calculate the brightest of all brightnesses
				int brightest = 0;
				String brightestId = null;
				for (String aColor : matchingColor) {
					TinaColor current = tinaColors.get(aColor);

					if (current.getBrightness() >= brightest) {
						brightest = current.getBrightness();
						brightestId = current.getColor();
					}
				}
				//int mean = brightnessSum / matchingColor.size();
				//find the tina whose brightness value is closest to the mean and point this tina as the median
				int brightnessDiff = 1000;
				for (String aColor : matchingColor) {
					TinaColor current = tinaColors.get(aColor);
					if (current.getBrightness() > 0 && Math.abs(current.getBrightness() - brightest) < brightnessDiff) {
						//medianId = current.getColor();
						brightnessDiff = Math.abs(current.getBrightness() - brightest);
					}
				}

				//now calculate the relative brightnesses
				for (String aColor : matchingColor) {
					TinaColor current = tinaColors.get(aColor);
					if(tinaColors.get(brightestId)==null)
						System.out.println("current = " + current);
					current.setRelativeBrightness(calculateRelativeBrightness(current.getBrightness(), tinaColors.get(brightestId).getBrightness(), brightestId));
				}
			}
			else if("AVERAGE".equals(this.coloringMethod)){

				//Calculate the reference color which is defined by the smallest brightness diff between the upper and lower color
				String referenceColour = null;
				int sumOfBrightnessDifs = 1000;

				if(orderedTinaColors.size()<3) referenceColour = orderedTinaColors.get(0).getColor();
				else {
					for (int i = 1; i < orderedTinaColors.size() - 1; i++) {
						int newSum = orderedTinaColors.get(i).brightness - orderedTinaColors.get(i - 1).brightness + orderedTinaColors.get(i + 1).brightness - orderedTinaColors.get(i).brightness;
						if (newSum < sumOfBrightnessDifs) {
							sumOfBrightnessDifs = newSum;
							referenceColour = orderedTinaColors.get(i).getColor();
						}
					}
				}

				for (String aColor : matchingColor) {
					TinaColor current = tinaColors.get(aColor);
					current.setRelativeBrightness(calculateRelativeBrightness(current.getBrightness(), tinaColors.get(referenceColour).getBrightness(), referenceColour));
					int test = current.getRelativeBrightness();
				}
			}
		}
	}

	/*private void orderTinaColors(){
		// not yet sorted
		List<TinaColor> orderedTinaColors = new ArrayList<TinaColor>(tinaColors.values());

		Collections.sort(orderedTinaColors, new Comparator<TinaColor>() {

			public int compare(TinaColor tc1, TinaColor tc2) {
				return tc1.getBrightness() - tc2.getBrightness();
			}
		});



		tinaColors = new HashMap<String, TinaColor>();
		for (TinaColor orderedTinaColor : orderedTinaColors) {
			tinaColors.put(orderedTinaColor.getColor(), orderedTinaColor);
		}

	}*/

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

		/*//default, if brighter then ref color => use a big brightness factor
		int brightnessFactor = 75;
		//if darker then ref color => use a little smaller brightness factor

		if(Colour.isYellowOrGreen(referenceColor)) //@todo then add 'yellow class' else add 'blue' class
			return referenceBrightness==0 ? 0 : ((currentBrightness - referenceBrightness)*brightnessFactor)/referenceBrightness;
		else
			return referenceBrightness==0 ? 0 : ((currentBrightness - referenceBrightness)*brightnessFactor)/referenceBrightness;*/
		//int test = (100*(currentBrightness - referenceBrightness))/referenceBrightness;

		//System.out.println("test = " + test);

		return referenceBrightness == 0 ? 0 : (100 * (currentBrightness - referenceBrightness)) / referenceBrightness;
	}

	private void replaceSimpleColors() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, name, "_all_grads_4.svg"))));
			String uri = String.format("%s%s/%s%s",folder, gender, name, "_coloured_3.svg");
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
					}
					else if (isPath(e) && e.matches(".*stroke=\"#.*")) {
						String simpleColor = getSimpleColor(e);
						String gradientId = UUID.randomUUID().toString();
						String simpleGradient = getSimpleGradient(gradientId, simpleColor);
						writer.append(simpleGradient);
						writer.append(System.lineSeparator());
						writer.append(e.replaceAll("stroke=\"\\s*(#[0-9a-fA-F]{6})", "stroke=\"url(#" + gradientId + ")"));
						writer.append(System.lineSeparator());
					}
					else if (isPath(e) && e.matches(".*fill:\\s*#.*")) {
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, name, "_all_grads_5.svg"))));
			String uri = String.format("%s%s/%s%s",folder, gender, name, "_all_grads_4.svg");
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
								e = e.replace("<stop ", "<stop data-target=\"" + ((float)(currentColor.getRelativeBrightness()))/100 + "\" ");
								if (stopCounter[0] == 0) {
									writer.append(gradients[0].replaceAll("id=", "class=\"color" + currentColor.getMatchSet() + "\" id="));
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

	private void replaceIDs() {
		Charset charset = StandardCharsets.UTF_8;
		String uri = String.format("%s%s/%s%s",folder, gender, name, "_all_grads_5.svg");
		String content = null;

		try {
			//Find all non-uuid ids and store them in a set
			Pattern patt = Pattern.compile("id=\"([^\"]+)\"");
			Set<String> ids = new HashSet<>();
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				if (e.contains("<linearGradient ") || e.contains("<radialGradient ")) {
					Matcher matcher = patt.matcher(e);
					while (matcher.find()) {
						String id = matcher.group(1);
						if(id.length() < 36)
							ids.add(id);
					}
				}
			});

			//Loop over the set and create a uuid for every id
			Map<String, String> idMap = new HashMap<>();
			for (String id : ids) {
				idMap.put(id, UUID.randomUUID().toString());
			}

			//Replace all instances of "id" with its corresponding uuid
			String shortName = name.substring(0, name.indexOf("_cleaned"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",folder, gender, shortName, ".svg"))));
			stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					String tmp = null;
					for (String id : idMap.keySet()) {
						if(e.contains(String.format("id=\"%s\"", id))) {
							tmp = e.replaceAll(String.format("id=\"%s\"", id), String.format("id=\"%s\"", idMap.get(id)));
						}
						else if(e.contains(String.format("url(#%s)", id))) {
							tmp = e.replaceAll(String.format("url\\(#%s\\)", id), String.format("url\\(#%s\\)", idMap.get(id)));
							writer.append(System.lineSeparator());
						}
					}
					if(tmp!=null)
						writer.append(tmp);
					else
						writer.append(e);
					writer.append(System.lineSeparator());

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();


		} catch (IOException e) {
			e.printStackTrace();
		}


		/*try {
			content = new String(Files.readAllBytes(Paths.get(uri)), charset);


			Pattern patt = Pattern.compile("id=([^\"]+)");
			Matcher matcher = patt.matcher(content);
			Set<String> ids = new HashSet<>();
			while (matcher.find()) {
				String id = matcher.group(1);
				if(id.length() < 36)
					ids.add(id);
			}
			Map<String, String> idMap = new HashMap<>();
			for (String id : ids) {
				idMap.put(id, UUID.randomUUID().toString());
			}
			for (String id : idMap.keySet()) {
				content = content.replaceAll(id, idMap.get(id));
			}
			name = name.substring(0, name.indexOf("_cleaned"));
			Files.write(Paths.get(String.format("%s%s/%s%s",folder, gender, name, ".svg")), content.getBytes(charset));

		} catch (IOException e) {
			e.printStackTrace();
		}*/
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

	private String findColor(String line) {
		Pattern patt = Pattern.compile("#([0-9a-fA-F]{6})");
		Matcher mat = patt.matcher(line);
		if (mat.find()) {
			return mat.group(1);
		}
		return null;
	}

	private double findOffset(String line) {
		Pattern patt = Pattern.compile("offset=\"([0-9\\.e\\-\\+]+)\"");
		Matcher mat = patt.matcher(line);
		if (mat.find()) {
			return Double.parseDouble(mat.group(1));
		}
		return -1;
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
	}

	public void test() {
		Colour.getBrightness("#FFFF00");
		Colour.getBrightness("#FF00FF");
		Colour.getBrightness("#00FFFF");
		Colour.getBrightness("#FF0000");
		Colour.getBrightness("#00FF00");
		Colour.getBrightness("#0000FF");
	}





	public void cleanup(){
		deleteFile("_temp_1.svg");
		deleteFile("_norgb_1.svg");
		deleteFile("_nostyle_2.svg");
		deleteFile("_coloured_3.svg");
		deleteFile("_all_grads_4.svg");
		deleteFile("_all_grads_5.svg");
	}

	private void deleteFile(String extension){
		File aFile = new File(String.format("%s%s/%s%s",this.folder, this.gender, this.name, extension));
		if(aFile!=null) aFile.delete();
	}
}
