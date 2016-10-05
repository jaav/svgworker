package be.virtualsushi;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static be.virtualsushi.SVGUtils.transformElement;

/**
 * Created on IntelliJ
 * User: Jef Waumans for Virtual Sushi
 * Date: 24/07/16
 * Time: 16:18
 */
public class SVGScaler {

	private static String sourcefolder = "/Users/jefw/Documents/virtualsushi/projects/svgworker/svg_orig/";
	private static String destfolder = "/Users/jefw/Documents/virtualsushi/projects/svgworker/svg_created/";
	private static String name = "";
	private static int row = 0;
	private static double resizer = 1;
	private float[] minMaxes = {100000f, 100000f, 0f, 0f};
	private static float width = 0;
	private static float height = 0;
	private static float newWidth = 0;
	private static float newHeight = 0;
	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;

	public static void main(String[] args) {
		SVGScaler scaler = new SVGScaler();
		scaler.start(args[0]);
	}

	public void start(String name) {
		this.name = name;
		doStart();
	}

	private void doStart() {
		//setResizer();
		cleanSourceFile();
		//Split numbers
		splitNumbers();
		//add 'px' units where necessary
		setUnits();
		//Set absolute values
		setAbsolutePath();
		//Discover left, right, top bottom edges
		autocrop();
		//Auto crop image
		scale();
	}

	private void cleanSourceFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sourcefolder + name + "_cleaned.svg")));
			String uri = sourcefolder + name + "_prep.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					e = e.replaceAll("\\t", "").replaceAll("\\r", "").replaceAll("\\n", "").replaceAll("><", ">" + System.lineSeparator() + "<");
					writer.append(e);
					/*if (e.indexOf('>')>0) {
						writer.append(e);
						writer.append(System.lineSeparator());
					}
					else{
						writer.append(e);
					}*/

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

	private void splitNumbers() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sourcefolder + name + "_cleaned_split.svg")));
			String uri = sourcefolder + name + "_cleaned.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if(e.contains("<svg ")) {
						e = e.replaceAll("\\swidth\\s*=\\s*\"[^\"]*\"", "")
								.replaceAll("\\sheight\\s*=\\s*\"[^\"]*\"", "")
								.replaceAll("\\sviewBox\\s*=\\s*\"[^\"]*\"", "");
						writer.append(setHeaderDimensions(e));
						writer.append(System.lineSeparator());
					}
					else if(e.contains("<g ")) {
						//e = e.replaceAll("transform\\s*=\\s*\"[^\"]*\"", "");
						writer.append(e);
						writer.append(System.lineSeparator());
					}
					//c-2.007,0-4.005-0.036-6.002-0.076C
					else if(e.contains("<path ") || e.contains("<circle ") || e.contains("<ellipse ") || e.contains("<rect ")) {
						e = doSplitNumbers(e);
						writer.append(e);
						writer.append(System.lineSeparator());
						System.out.println("e = " + e);
					}
					else{
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

	private String doSplitNumbers(String path){
		String dSubstring = getDPart(path);
		if("".equals(dSubstring)) return path;
		String dKeep = getDPart(path);
		dSubstring = dSubstring
				.replaceAll("(\\d)\\s+(\\d)", "$1,$2")
				.replaceAll("(\\d)\\s+(\\d)", "$1,$2")
				.replaceAll("-", ",-")
				.replaceAll("([a-zA-Z])", ",$1,")
				.replaceAll(",\\s*,", ",")
				.replaceAll("\\s*,\\s*", ",").substring(1);
		String[] numbers = dSubstring.split(",");
		String currentMethod = null;
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for (String number : numbers) {

			if(number.matches("[a-zA-Z]")) {
				sb.append(number).append(" ");
				currentMethod = number;
				counter = 0;
			}
			else{
				if("H".equalsIgnoreCase(currentMethod)){
					sb.append(number).append(" ");
				}
				else if("V".equalsIgnoreCase(currentMethod)){
					sb.append(number).append(" ");
				}
				else if("A".equalsIgnoreCase(currentMethod)){
					if(counter==0 || counter==5)
						sb.append(number);
					else if(counter==1 || counter==6)
						sb.append(",").append(number).append(" ");
					else
						sb.append(number).append(" ");
					counter++;
				}
				else{
					sb.append(counter%2==0?number:","+number+" ");
					counter++;
				}
			}
		}

		return path.replaceAll(dKeep, sb.toString());
	}

	private String setHeaderDimensions(String s) {
		String dSubstring = getDPart(s);
		Pattern pattern = Pattern.compile("([a-zA-Z])([^a-zA-Z]+)");
		Matcher matcher = pattern.matcher(dSubstring);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String method = matcher.group(1);
			String numbers = matcher.group(2);
			System.out.println("numbers = " + numbers);
			sb.append(method);

			Pattern numberSplitPattern = Pattern.compile("(\\-?[0-9\\.]+),?");
			Matcher numberSplitMatcher = numberSplitPattern.matcher(numbers);
			int counter = 0;
			while (numberSplitMatcher.find()) {
				String replacer = counter % 2 == 0 ? "," : " ";
				String s_number = numberSplitMatcher.group(1);
				numberSplitMatcher.appendReplacement(sb, s_number + replacer);
				counter++;
			}
		}
		System.out.println("s = " + s.replace(dSubstring, sb.toString()));
		s = s.replace(dSubstring, sb.toString());
		//remove errors from single-numbered methods
		return s.replaceAll(",([a-zA-Z])", " $1");
	}

	private void setUnits() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sourcefolder + name + "_cleaned_units.svg")));
			String uri = sourcefolder + name + "_cleaned_split.svg";
			//@TODO add 'px' where necessary
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					String test = e.replaceAll("\\swidth\\s*=\\s*\"(\\d+)\"", " width=\"$1px\"");
					writer.append(e.replaceAll("\\swidth\\s*=\\s*\"(\\d+)\"", " width=\"$1px\"").replaceAll("\\sheight\\s*=\\s*\"(\\d+)\"", " height=\"$1px\""));
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
	}

	private void setAbsolutePath() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sourcefolder + name + "_cleaned_absolute.svg")));
			String uri = sourcefolder + name + "_cleaned_units.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if (e.contains("<path ")) {
						System.out.println("SPLITTING NUMBERS IN e = " + e);
						writer.append(doSetAbsolute(e));
						writer.append(System.lineSeparator());
						row++;
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


	private String doSetAbsolute(String s) {
		String dSubstring = getDPart(s);
		Pattern methodSplitPattern = Pattern.compile("([a-zA-Z]) ([^a-zA-Z]+)");
		Matcher methodSplitMatcher = methodSplitPattern.matcher(dSubstring);
		String refX = null;
		String refY = null;
		StringBuffer sb = new StringBuffer();
		while (methodSplitMatcher.find()) {
			String method = methodSplitMatcher.group(1);
			System.out.println("method = " + method);
			String[] coordinates = methodSplitMatcher.group(2).replaceAll("(\\d), ([-\\d])","$1,$2").split(" ");
			System.out.println(Arrays.toString(coordinates));
			if (method.matches("[a-z]")) {
				sb.append(method.toUpperCase());
				for (int i = 0; i < coordinates.length; i++) {
					String coordinate = coordinates[i];
					sb.append(absolify(method, coordinate, refX, refY)).append(" ");
					if (i == coordinates.length - 1) {
						String[] newRefs = absolify(method, coordinate, refX, refY).split(",");

						if ("v".equals(method)) {
							refY = newRefs[0];
						} else if ("h".equals(method)) {
							refX = newRefs[0];
						} else {
							refX = newRefs[0];
							refY = newRefs[1];
						}
					}
				}
			} else {
				sb.append(methodSplitMatcher.group());
				String endPoint = coordinates[coordinates.length - 1];
				if ("V".equals(method)) {
					refY = endPoint;
				} else if ("H".equals(method)) {
					refX = endPoint;
				} else if ("A".equals(method)) {
					refX = coordinates[coordinates.length - 2].substring(coordinates[coordinates.length - 2].indexOf(",") + 1);
					refY = endPoint;
					//endPoint = refX+","+refY;
				} else {
					refX = endPoint.substring(0, endPoint.indexOf(","));
					refY = endPoint.substring(endPoint.indexOf(",") + 1);
				}
			}
			System.out.println("refs = " + refX + " " + refY);
		}

		//System.out.println("s = " + s.replace(dSubstring, sb.toString()));
		return s.replace(dSubstring, sb.toString());
	}

	private String absolify(String method, String coordinates, String refX, String refY) {
		float fRefX = Float.parseFloat(refX);
		float fRefY = Float.parseFloat(refY);
		if ("h".equals(method))
			return "" + (fRefX + Float.parseFloat(coordinates));
		if ("v".equals(method))
			return "" + (fRefY + Float.parseFloat(coordinates));
		float coordX = Float.parseFloat(coordinates.substring(0, coordinates.indexOf(",")));
		float coordY = Float.parseFloat(coordinates.substring(coordinates.indexOf(",") + 1));
		return (fRefX + coordX) + "," + (fRefY + coordY);
	}

	private void autocrop() {
		String uri = sourcefolder + name + "_cleaned_absolute.svg";
		setMinMaxes(uri);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(sourcefolder + name + "_cleaned_cropped.svg")));
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if (e.contains("<svg ")) {
						setResizer();
						writer.append(e);
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<path ")) {
						writer.append(doAutoCropPath(e, minMaxes));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<rect ")) {
						writer.append(doAutoCropRect(e, minMaxes));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<circle ")) {
						writer.append(doAutoCropCircle(e, minMaxes));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<linearGradient ")) {
						writer.append(doAutoCropGradient(e));
						writer.append(System.lineSeparator());
						row++;
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

	private void setMinMaxes(String uri) {
		Stream<String> stream = null;
		try {
			stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				if (e.contains("<path ")) {
					String dSubstring = getDPart(e);
					dSubstring = dSubstring.replaceAll(" +-", " -");
					dSubstring = dSubstring.replaceAll("(\\d) +(\\d)", "$1 $2");
					dSubstring = dSubstring.replaceAll("(\\d) +([a-zA-Z]) +([\\d-])", "$1$2$3");
					dSubstring = dSubstring.replaceAll("M +(\\d)", "M$1");
					System.out.println("dSubstring = " + dSubstring);
					Pattern methodSplitPattern = Pattern.compile("([a-zA-Z])([^a-zA-Z]+)");
					Matcher methodSplitMatcher = methodSplitPattern.matcher(dSubstring);
					while (methodSplitMatcher.find()) {
						String method = methodSplitMatcher.group(1);
						System.out.println("method = " + method + methodSplitMatcher.group(2));
						if (!"H".equals(method) && !"V".equals(method) && !"A".equals(method)) {
							String[] coordinates = methodSplitMatcher.group(2).trim().replaceAll("(\\d), (\\d)", "$1,$2").split(" ");

							for (String coordinate : coordinates) {
								String refX = coordinate.substring(0, coordinate.indexOf(","));
								String refY = coordinate.substring(coordinate.indexOf(",") + 1);
								float iRefX = Float.parseFloat(refX);
								float iRefY = Float.parseFloat(refY);
								if (iRefX < minMaxes[0]) minMaxes[0] = iRefX;
								if (iRefY < minMaxes[1]) minMaxes[1] = iRefY;
								if (iRefX > minMaxes[2]) minMaxes[2] = iRefX;
								if (iRefY > minMaxes[3]) minMaxes[3] = iRefY;
							}
						}
					}
				}

			});
			System.out.println("minX = " + minMaxes[0]);
			System.out.println("minY = " + minMaxes[1]);
			System.out.println("maxX = " + minMaxes[2]);
			System.out.println("maxY = " + minMaxes[3]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setResizer() {
		newWidth = getNewWidth(minMaxes);
		newHeight = getNewHeight(minMaxes);
		//set resizer value
		resizer = 400 / newWidth;
	}

	private String doAutoCropPath(String s, float[] minMaxes) {
		String dSubstring = getDPart(s);

		//dSubstring = dSubstring.replaceAll(" +-", "-");
		dSubstring = dSubstring.replaceAll("([a-zA-Z])-", "$1-");
		dSubstring = dSubstring.replaceAll("(\\d) +(-?\\d)", "$1 $2");
		dSubstring = dSubstring.replaceAll("(\\d) +([a-zA-Z]) +(-?\\d)", "$1$2$3");
		dSubstring = dSubstring.replaceAll("M +(-?\\d)", "M$1");

		Pattern methodSplitPattern = Pattern.compile("([a-zA-Z])([^a-zA-Z]+)");
		Matcher methodSplitMatcher = methodSplitPattern.matcher(dSubstring);
		String refX = null;
		String refY = null;
		StringBuffer sb = new StringBuffer();
		while (methodSplitMatcher.find()) {
			String method = methodSplitMatcher.group(1);
			System.out.println("method = " + method);
			String[] coordinates = methodSplitMatcher.group(2).split(" ");
			sb.append(method);
			for (int i = 0; i < coordinates.length; i++) {
				String coordinate = coordinates[i];
				if(coordinate.indexOf(",")<0) {
					System.out.println("STOP");
					sb.append(coordinate).append(" ");
				}
				else
					sb.append(minify(method, coordinate, minMaxes)).append(" ");
			}
			System.out.println("refs = " + refX + " " + refY);
		}

		System.out.println("s = " + s.replace(dSubstring, sb.toString()));
		return s.replace(dSubstring, sb.toString());
	}

	private String doAutoCropRect(String s, float[] minMaxes) {
		Pattern patt = Pattern.compile("(\\w+)=\"([^\"]+)\"");
		Matcher mat = patt.matcher(s);
		StringBuffer buf = new StringBuffer();
		while (mat.find()) {
			if ("transform".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyTransform(mat.group(2)) + "\"");
			else if ("x".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), HORIZONTAL) + "\"");
			else if ("y".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), VERTICAL) + "\"");
		}
		mat.appendTail(buf);
		String test = buf.toString();
		return test;
	}

	private String doAutoCropCircle(String s, float[] minMaxes) {
		Pattern patt = Pattern.compile("(\\w+)=\"([^\"]+)\"");
		Matcher mat = patt.matcher(s);
		StringBuffer buf = new StringBuffer();
		while (mat.find()) {
			if ("transform".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyTransform(mat.group(2)) + "\"");
			else if ("cx".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), HORIZONTAL) + "\"");
			else if ("cy".equals(mat.group(1)))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), VERTICAL) + "\"");
		}
		mat.appendTail(buf);
		String test = buf.toString();
		return test;
	}

	private String doAutoCropGradient(String s) {

		Pattern patt = Pattern.compile("([xy]\\d)=\"([\\d\\.]+)\"");
		Matcher mat = patt.matcher(s);
		StringBuffer buf = new StringBuffer();
		while (mat.find()) {
			if (mat.group(1).contains("x"))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), HORIZONTAL) + "\"");
			else if (mat.group(1).contains("y"))
				mat.appendReplacement(buf, mat.group(1) + "=\"" + applyMove(mat.group(2), VERTICAL) + "\"");
		}
		mat.appendTail(buf);
		String test = buf.toString();
		return test;
	}

	/*private String applyMove(String xy, String size, float[] minMaxes){
		if(xy.indexOf("width")==0)
			return (Float.parseFloat(size)-minMaxes[0])+"";
		else
			return (Float.parseFloat(size)-minMaxes[1])+"";
	}*/

	private void scale() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + ".svg")));
			String uri = sourcefolder + name + "_cleaned_cropped.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if (e.contains("<svg ")) {
						e = scaleDimensions(e);
						e = scaleViewBox(e);
						writer.append(addDimensions(e));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<path ")) {
						writer.append(scaleNumbers(e));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<g ")) {
						writer.append(scaleTransformations(e));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<circle ") || e.contains("<ellipse ") || e.contains("<rect ")) {
						writer.append(scaleForm(e));
						writer.append(System.lineSeparator());
						row++;
					} else if (e.contains("<linearGradient ")) {
						writer.append(scaleGradients(e));
						writer.append(System.lineSeparator());
						row++;
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

	private float getDimension(String header, String type) {
		int start = header.indexOf(" " + type + "=");
		if (header.indexOf("px\"", start) >= 0)
			return getDimension(header, type, "px", 1f);
		if (header.indexOf("mm\"", start) >= 0)
			return getDimension(header, type, "mm", 3.5f);
		return 0f;
	}

	private float getDimension(String header, String type, String unit, float multiplicator) {
		int start = header.indexOf(" " + type + "=");
		int end = header.indexOf(unit, start);
		String test = header.substring(start + type.length() + 3, end);
		return Float.parseFloat(header.substring(start + type.length() + 3, end)) * multiplicator;
	}

	private float getNewWidth(float[] minMaxes) {
		return minMaxes[2] - minMaxes[0];
	}

	private float getNewHeight(float[] minMaxes) {
		return minMaxes[3] - minMaxes[1];
	}

	private String minify(String method, String coordinates, float[] minMaxes) {
		if ("H".equals(method)) {
			float coordX = Float.parseFloat(coordinates);
			return "" + (coordX - minMaxes[0]);
		}
		if ("V".equals(method)) {
			float coordY = Float.parseFloat(coordinates);
			return "" + (coordY - minMaxes[1]);
		}
		if ("L".equals(method)) {
			float coordX = Float.parseFloat(coordinates.substring(0, coordinates.indexOf(",")));
			float coordY = Float.parseFloat(coordinates.substring(coordinates.indexOf(",") + 1));
			return (coordX - minMaxes[0]) + "," + (coordY - minMaxes[1]);
		}
		if ("A".equals(method)) {
			float coordX = Float.parseFloat(coordinates.substring(0, coordinates.indexOf(",")));
			float coordY = Float.parseFloat(coordinates.substring(coordinates.indexOf(",") + 1));
			return (coordX - minMaxes[0]) + "," + (coordY - minMaxes[1]);
		}
		float coordX = Float.parseFloat(coordinates.substring(0, coordinates.indexOf(",")));
		float coordY = Float.parseFloat(coordinates.substring(coordinates.indexOf(",") + 1));
		return (coordX - minMaxes[0]) + "," + (coordY - minMaxes[1]);
	}

	private String scaleForm(String s) {
		Pattern pattern = Pattern.compile("([a-z]+)=\"([0-9\\.]+)");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String dimension = matcher.group(1);
			String s_number = matcher.group(2);
			if (dimension.matches("x|y|width|height|cx|cy|r|rx|ry"))
				s = s.replaceAll(s_number, multiply(s_number, false));
		}
		return scaleFormTransform(s);
	}

	private String scaleFormTransform(String s) {
		Pattern pattern = Pattern.compile("transform=\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			String formTransformer = matcher.group(1);
			String scaledFormTransformer = matcher.group(1);
			Pattern tPattern = Pattern.compile("([\\d\\.]+)");
			Matcher tMatcher = tPattern.matcher(formTransformer);
			int counter = 0;
			while (tMatcher.find()) {
				if (counter > 0)
					scaledFormTransformer = scaledFormTransformer.replace(tMatcher.group(1), multiply(tMatcher.group(1), false));
				counter++;
			}
			return s.replace(formTransformer, scaledFormTransformer);
		} else
			return s;
	}

	private String scaleNumbers(String s) {
		String dSubstring = getDPart(s);
		String reworkedSubstring = dSubstring.replaceAll("([a-zA-Z,-])(\\d+)([a-zA-Z,-])", "$1$2.0$3");
		Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(reworkedSubstring);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String s_number = matcher.group(1)+"."+matcher.group(2);
			matcher.appendReplacement(sb, multiply(s_number, true));
			//s = s.replace(s_number, multiply(s_number));
			//s = s.replaceAll("(\\D)"+s_number+"(\\D)", "$1"+multiply(s_number)+"$2");
		}
		sb.append("z");
		System.out.println("s = " + s.replace(dSubstring, sb.toString()));
		return s.replace(dSubstring, sb.toString());
	}

	//<g transform="translate(991.092 13.963)" id="g2326">
	private String scaleTransformations(String s) {
		Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String s_number = matcher.group(1)+"."+matcher.group(2);
			matcher.appendReplacement(sb, multiply(s_number, true));
			//s = s.replace(s_number, multiply(s_number));
			//s = s.replaceAll("(\\D)"+s_number+"(\\D)", "$1"+multiply(s_number)+"$2");
		}
		matcher.appendTail(sb);
		System.out.println("s = " + s.replace(s, sb.toString()));
		return s.replace(s, sb.toString());
	}

	private String scaleDimensions(String s) {
		Pattern pattern = Pattern.compile("([0-9\\.]+) ([0-9\\.]+) ([0-9\\.]+) ([0-9\\.]+)");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String s_dimension = matcher.group();
			s = s.replace(s_dimension, new StringBuilder()
					.append(multiply(matcher.group(1), false)).append(" ")
					.append(multiply(matcher.group(2), false)).append(" ")
					.append(multiply(matcher.group(3), false)).append(" ")
					.append(multiply(matcher.group(4), false)));
			System.out.println("(scaleDimensions) s in svg block = " + s);
		}
		return s;
	}

	private String scaleViewBox(String s) {
		Pattern pattern = Pattern.compile("([0-9\\.]+)px");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String s_number = matcher.group(1);
			s = s.replaceAll(s_number + "px", multiply(s_number, false) + "px");
			System.out.println("(scaleViewBox) s in svg block = " + s);
		}
		return s;
	}

	private String addDimensions(String s) {
		s = s.substring(0, s.length()-1) + " width=\"400\" height=\""+(400*((minMaxes[3]-minMaxes[1])/(minMaxes[2]-minMaxes[0])))+"\" >";
		return s;
	}

	private String scaleGradients(String s) {
		s = scaleGradientTransform(s);
		Pattern pattern = Pattern.compile("\"\\-?([0-9\\.]+)\"");//([0-9\\.]*)
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String s_number = matcher.group(1);
			s = s.replaceAll(s_number, multiply(s_number, false));
			System.out.println("(scaleGradients) s in svg block = " + s);
		}
		return s;
	}

	private String scaleGradientTransform(String s) {
		Pattern pattern = Pattern.compile("[\\( ]([0-9\\.\\-]+)[\\) ]");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String gradientTransformer = matcher.group(1);
			if (!"0".equals(gradientTransformer) && !"-1".equals(gradientTransformer) && !("1".equals(gradientTransformer))) {
				s = s.replace(gradientTransformer, multiply(gradientTransformer, false));
				System.out.println("(scaleGradientTransform) s in svg block = " + s);
			}
		}
		return s;
	}

	private String multiply(String s_number, boolean all_zeros) {
		try {
			double number = Double.parseDouble(s_number);
			number = resizer * number;
			if (all_zeros)
				return new DecimalFormat("0.000").format(number);
			else
				return new DecimalFormat("0.###").format(number);
			//return String.format("%02d", new Double(number));
		} catch (RuntimeException rte) {
			System.out.println("rte.getMessage() = " + rte.getMessage());
			rte.printStackTrace();
		}
		return s_number;
	}

	/*private void scale(String name) throws IOException, TransformerException, ParserConfigurationException {
		// Consult Batik's documentation or Google if you don't
		// know how to load a document.
		String uri = rootfolder + name + ".svg";
		Document svgDoc = createSVGDocument(uri);
		Element root = svgDoc.getDocumentElement();
		Element newRoot = (Element) root.cloneNode(true);

		svgDoc.replaceChild(newRoot, root);


		if (svgDoc.hasChildNodes()) {
			NodeList gOrigList = svgDoc.getElementsByTagName("g");
			if (gOrigList != null && gOrigList.getLength() > 0) {
				Node gOrig = gOrigList.item(0);
				if (gOrig.hasChildNodes()) {
					NodeList gChildren = gOrig.getChildNodes();
					scaleChildNodes(gChildren);
				}
			}

		}


		*//*AffineTransform translateBy5Units = AffineTransform.getTranslateInstance(5, 0);
		transformElement(newRoot, translateBy5Units);

		AffineTransform scaleByFactor10 = AffineTransform.getScaleInstance(10, 10);
		transformElement(newRoot, scaleByFactor10);*//*

		writeOut(svgDoc, name);
	}*/


	private static void scaleChildNodes(NodeList gChildren) {
		for (int i = 0; i < gChildren.getLength(); i++) {
			if ("path".equals(gChildren.item(i).getNodeName())) {
				Element child = (Element) gChildren.item(i);

				AffineTransform translateBy5Units = AffineTransform.getTranslateInstance(5, 0);
				transformElement(child, translateBy5Units);
				AffineTransform scaleByFactor10 = AffineTransform.getScaleInstance(10, 10);
				transformElement(child, scaleByFactor10);
			}
		}
	}

	private void writeOut(Document svgDoc, String name) throws FileNotFoundException, ParserConfigurationException, TransformerException {

		//DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		//Element newRoot = (Element)root.cloneNode(true);
		//Document newDoc = dBuilder.newDocument();
		//Element newRoot = newDoc.createElement("svg");
		//newDoc.adoptNode(newRoot);
		//Document ddd = newRoot.getOwnerDocument();
		//newDoc.appendChild(newRoot);
		FileOutputStream newSvgFileStream = new FileOutputStream(new File(destfolder, name + ".svg"));
		DOMSource source = new DOMSource(svgDoc);
		StreamResult fileStream = new StreamResult(newSvgFileStream);

		DOMImplementation domImpl = svgDoc.getImplementation();
		DocumentType doctype = domImpl.createDocumentType("svg",
				"-//W3C//DTD SVG 1.1//EN",
				"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd");
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		transformer.transform(source, fileStream);
	}


	private Document createSVGDocument(String uri) throws IOException {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
		return factory.createDocument(uri);
	}

	private String getDPart(String line) {
		if(line.indexOf(" d=\"")>0)
			return line.substring(line.indexOf(" d=\"") + 4, line.indexOf("\"", line.indexOf(" d=\"") + 4));
		else return "";
	}


	private String applyScale(String attributeKey, String widthKey, String heightKey, String size, float widthScaleFactor, float heightScaleFactor) {
		if (attributeKey.indexOf(widthKey) == 0)
			return (Float.parseFloat(size) * widthScaleFactor) + "";
		else if (attributeKey.indexOf(heightKey) == 0)
			return (Float.parseFloat(size) * heightScaleFactor) + "";
		else return size;
	}

	private String applyMove(String size, int axis) {
		return (Float.parseFloat(size) - minMaxes[axis]) + "";
	}

	private String applyTransform(String parameters) {
		if (parameters.indexOf("rotate") >= 0) {
			Pattern patt = Pattern.compile("([\\d\\.]+)");
			Matcher mat = patt.matcher(parameters);
			StringBuffer buf = new StringBuffer();
			int counter = 0;
			while (mat.find()) {
				if (counter > 0)
					mat.appendReplacement(buf, (Float.parseFloat(mat.group(1)) - minMaxes[counter - 1]) + "");
				counter++;
			}
			mat.appendTail(buf);
			return buf.toString();
		}
		return parameters;
	}





	/*private static void setResizer(){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + ".svg")));
			String uri = sourcefolder + name + "_cleaned.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {
					if(e.contains("<svg ")){
						String s_resizer = e.substring(e.indexOf("width")+7, e.indexOf("p", e.indexOf("width")));
						resizer = 400/Double.parseDouble(s_resizer);
						System.out.println("resizer = " + resizer);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			});
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

}
