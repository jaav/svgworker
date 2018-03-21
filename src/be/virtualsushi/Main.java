package be.virtualsushi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

	private static String wwwfolder = "/home/jefw/Documents/virtualsushi/preventina/creation_process/www/";
	private static String sourcefolder = "/home/jefw/Documents/virtualsushi/preventina/creation_process/cleaned_source_images/";

	//For colour distance algorithm, check https://github.com/MatthewYork/Colours/blob/master/ColoursLibrary/src/com/mattyork/colours/Colour.java


	public static void main(String[] args) {
		//Fluo stuff -> BRIGHTEST 20
		//UNI Tshirts and Helmet -> BRIGHTEST 1000 (so shadows are a darker tint of the same color)
		//Do we still need AVERAGE ? (don't take the brightest color as a reference)
		String name;
		String gender;
		String coloringMethod;//AVERAGE or BRIGHTEST
		int reference_distance;
		if(args.length==0) {
			//USE DEFAULT coloringMethod AND reference_distance and loop over the DEFAULT folder
			coloringMethod = "BRIGHTEST";
			reference_distance = 20;
			File folder = new File(sourcefolder+"man");
			String[] filenames = folder.list();
			for (int i = 0; i < filenames.length; i++) {
				if(filenames[i].endsWith("_NS.svg"))
					doMain("man", filenames[i].substring(0, filenames[i].length()-4), reference_distance, coloringMethod);
			}
			folder = new File(sourcefolder+"woman");
			filenames = folder.list();
			for (int i = 0; i < filenames.length; i++) {
				if(filenames[i].endsWith("_NS.svg"))
					doMain("woman", filenames[i].substring(0, filenames[i].length()-4), reference_distance, coloringMethod);
			}
		}
		else if(args.length==4) {
			name = args[0]+"_cleaned_NS";
			gender = args[1];
			coloringMethod = args[2];
			reference_distance = Integer.parseInt(args[3]);
			doMain(gender, name, reference_distance, coloringMethod);
		}
		else
			System.out.println("Please provide 4 parameters. First is a name, second the gender, third a coloring method (AVERAGE or BRIGHTEST) and fourth a reference distance (bigger distances result in fewer colors)");
		/*try {
			//boolean absolute = Boolean.parseBoolean(args[3]);
			//svgo --config config.yml svg_orig/hand_man_01.svg svg_orig/hand_man_01_prep.svg
			//String command = "svgo --config config.yml svg_orig/" + name + ".svg svg_orig/" + name + "_prep.svg";
			//List<String> commandParts = new ArrayList<>();
			//commandParts.add("svgo");
			//ProcessBuilder builder = new ProcessBuilder(command.split(" "));
			//builder.redirectErrorStream(true);
			//Process process = builder.start();
			//process.waitFor();


			//SVGScaler scaler = new SVGScaler(absolute);
			//scaler.start(name);

			//scaler.cleanup(name);
			//worker.cleanup(name);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	private static void doMain(String gender, String name, int reference_distance, String coloringMethod){
		SVGColoringWorker worker = new SVGColoringWorker(reference_distance, coloringMethod);
		worker.start(sourcefolder, gender, name);
		makeHtml(gender, name);
	}

	private static void makeHtml(String gender, String name) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(String.format("%s%s/%s%s",wwwfolder, gender, name,".html"))));
			String uri = wwwfolder + "blank.html";
			Stream<String> stream = Files.lines(Paths.get(uri));
			stream.forEach(e -> {
				try {

					if (e.matches(".*_REPLACE_.*")) {
						writer.append(getSVG(gender, name));
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

	private static String getSVG(String gender, String name) {
		try {
			StringWriter sw = new StringWriter();
			BufferedWriter writer = new BufferedWriter(sw);
			String uri = String.format("%s%s/%s%s",sourcefolder, gender, name, "_final.svg");
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
