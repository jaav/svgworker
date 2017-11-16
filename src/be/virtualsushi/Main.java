package be.virtualsushi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

	private static String destfolder = "/home/jefw/virtualsushi/svgworker/www/";
	private static String sourcefolder = "/home/jefw/virtualsushi/svgworker/svg_created/";

	//For colour distance algorithm, check https://github.com/MatthewYork/Colours/blob/master/ColoursLibrary/src/com/mattyork/colours/Colour.java

	public static void main(String[] args) {
		try {
			String command = "svgo --config config.yml svg_orig/" + args[0] + ".svg svg_orig/" + args[0] + "_prep.svg";
			List<String> commandParts = new ArrayList<>();
			commandParts.add("svgo");
			ProcessBuilder builder = new ProcessBuilder(command.split(" "));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			process.waitFor();


			SVGScaler scaler = new SVGScaler();
			scaler.start(args[0]);
			SVGColoringWorker worker = new SVGColoringWorker();
			worker.start(args[0]);

			makeHtml(args[0]);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void makeHtml(String name) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(destfolder + name + ".html")));
			String uri = destfolder + "blank.html";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {

					if (e.matches(".*_REPLACE_.*")) {
						writer.append(getSVG(name));
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

	private static String getSVG(String name) {
		try {
			StringWriter sw = new StringWriter();
			BufferedWriter writer = new BufferedWriter(sw);
			String uri = sourcefolder + name + "_full_grads_5.svg";
			Stream<String> stream = Files.lines(Paths.get(uri));
			boolean[] inSVG = {false};
			stream.forEach(e -> {
				try {

					if (e.contains("<svg ")) {
						inSVG[0] = true;
						writer.append(e);
						writer.append(System.lineSeparator());
					} else if (e.contains("</svg")) {
						inSVG[0] = false;
						writer.append(e);
						writer.append(System.lineSeparator());
					} else if (inSVG[0]) {
						writer.append(e);
						writer.append(System.lineSeparator());
					}

				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}

			});
			writer.flush();
			writer.close();
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
