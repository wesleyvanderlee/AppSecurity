package com.bunq.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.bunq.apk.APK;
import com.bunq.apk.apkinfo;
import com.bunq.learner.FsmLearner;
import com.bunq.learner.alphabet.AlphabetFileWriter;
import com.bunq.util.Property;
import com.wesley.ResultProcessor.ResultProcessor;
import com.wesley.emulator.AndroidEmulatorInstrumentator;
import com.wesley.emulator.Emulator;

import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import net.automatalib.incremental.ConflictException;
import net.automatalib.visualization.Visualization;

/**
 * The main class of the fsm-learner application.
 * 
 * @author tom
 *
 */
public class Main {

	private String command;
	private List<String> arguments;
	private apkinfo apkInfo;

	/**
	 * Constructor of the main class. Uses reflection to execute the given
	 * commands.
	 * 
	 * @param commandLineArgs
	 *            the commandline arguments
	 */
	public Main(String[] commandLineArgs) {
		int claLength = commandLineArgs.length;
		if (claLength == 0) {
			help();
		} else {
			command = commandLineArgs[0];
			if (claLength >= 2) {
				arguments = Arrays.asList(commandLineArgs).subList(1, commandLineArgs.length - 1);
			}
		}
	}

	/**
	 * Prints the help message.
	 */
	public void help() {
		String format = "%-20s%s%n";
		String[] commands = new String[5];
		commands[0] = "help";
		commands[1] = "learn";
		commands[2] = "alphabet:create";
		commands[3] = "alphabet:destroy";
		commands[4] = "alphabet:compose";

		String[] explanation = new String[5];
		explanation[0] = "display this message";
		explanation[1] = "starts a learning process. Note: with a large alphabet, this\ncan take a "
				+ "very long time!";
		explanation[2] = "creates an alphabet. Currently only working for Android, the\n"
				+ "creation of an iOS alphabet has to be done more or less manually";
		explanation[3] = "deletes an existing alphabet";
		explanation[4] = "composes an alphabet from existing window dumps.";

		for (int i = 0; i < commands.length; i++) {
			System.out.printf(format, commands[i], explanation[i]);
		}
	}

	public void emulator() {
		System.out.println("In emulator");
		Emulator em = new Emulator("Nexus5X");
		em.start();
	}

	/**
	 * Starts a learning process.
	 */
	public void learn() throws FileNotFoundException {
		printConfig();
		askConfirm("Are these your settings (y/n)?");
		System.out.println("Start Learning Process");
		FsmLearner learner = new FsmLearner();
		try {
			learner.setUpLearner();
		} catch (FileNotFoundException fnfe) {
			System.out.println("The alphabet as specified in the config file was not found. Please check "
					+ "if the alphabet exists. If not, use 'alphabet:create' (for Android) to create a new " + "one.");
		}
		boolean learning_done = false;
		while (!learning_done) {
			try {
				learner.runExperiment();
				learning_done = true;
			} catch (ConflictException ce) {

				System.out.println("ConflictException");
				learner.instantiateSulsReset();
				ce.printStackTrace();
				// learner.setUpLearner();

			} catch (Exception e) {
				System.out.println("Something unforseen happened while learning\nPrinting StackTrace:");
				e.printStackTrace();
				learning_done = true;
			}
		}

		try {
			learner.printResults();
		} catch (IOException e) {
			System.out.println("An error occurred while printing the results.");
			e.printStackTrace();
		}
		learner.stopExperiment();
		System.out.println("Query Cache Statistics:");
		System.out.println(learner.sul.getSum());
		System.out.println(learner.sul.getNum());
		
		
		System.out.println("Finished Learning, doing the postresults");
//		this.postLearning(learner, em.getInstrumentator());
		System.out.println("Results are in: " + learner.getStamp());
	}


	/*
		This method performs the vulnerability identification part. Automatically executed by the learn() method.
		When starting to learn DFAs (i.e. setting up the project) enable this method at the very end.

	*/
	private void postLearning(FsmLearner learner, AndroidEmulatorInstrumentator aei) {
		Emulator em = new Emulator("Nexus5X");		// Make sure you have setup a correct emulator
		em.start();

		apkConstruct(); // fills apkinfo
		ResultProcessor rp = new ResultProcessor(learner.getStamp(), aei, this.apkInfo);
		rp.run();
		rp.print();

	}

	/*
		This method can be used for manually postprocessing learned graphs, such that learning does not
		need to take place. Provide the correct stamp of the learned graph.
	*/
	public void post(){
		String stmp = "graphs/graph_1510816061185";
		apkConstruct(); // fills apkinfo
//		Emulator em = new Emulator("Nexus5X");
//		em.start();
		AndroidEmulatorInstrumentator aei = null;
//		AndroidEmulatorInstrumentator aei = em.getInstrumentator();
		
		ResultProcessor rp = new ResultProcessor(stmp, aei, this.apkInfo);
		
		rp.run();
		rp.print();
	}
	


	/**
	 * Creates a new alphabet from the window_dumps are made using this
	 * function.
	 */
	public void createAlphabet() {
		System.out.println("This currently only works for Android. Connect your phone to the PC/Mac"
				+ " and make sure that the bunq app is open and 'debug options' are enabled.\n");
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a name for your alphabet (please do use only " + "characters and underscores): ");
		String alphabetName = scanner.nextLine();

		System.out.println("\n\nRepeat the following steps for each screen you want to include in the " + "alphabet.\n"
				+ "1. Move to the screen you want to include on your phone.\n"
				+ "2. Type a filename here (e.g. login.xml) and hit enter. \n\n"
				+ "When you are done, type 'stop'  instead of a filename.\n\n");

		System.out.println("Enter filename (including extension): ");
		String input;
		while (scanner.hasNext()) {
			if ((input = scanner.nextLine()).equals("stop")) {
				break;
			}

			if (runAlphabetScript(alphabetName + "/" + input) != 0) {
				System.out.println("An error occurred. Please repeat the last action.");
			}
			System.out.println("Enter filename (including extension): ");
		}
		scanner.close();
		composeAlphabet(alphabetName, "alphabet/window_dumps/" + alphabetName + "/");
		// composeActivities(alphabetName);
	}

	private void composeActivities(String alphaname) {
		this.apkConstruct();
		try {
			PrintWriter out = new PrintWriter(
					new BufferedWriter(new FileWriter("alphabet/" + alphaname + ".txt", true)));
			APK apk = this.apkInfo.getPublicAPK();
			ArrayList<String> _callableActivities = apk.getCallableActivities();
			for (String activity : _callableActivities)
				out.println("activity%" + apk.APKname + "/" + activity + "\n");
			out.close();
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}
	}

	/**
	 * Guides you through the composing alphabet process.
	 */
	public void composeAlphabet() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter a name for this alphabet: ");
		String alphabetName = scanner.nextLine();
		System.out.println("Please enter the location of the window dumps " + "(relative to the current folder): ");
		String alphabetLoc = scanner.nextLine();
		scanner.close();
		composeAlphabet(alphabetName, alphabetLoc);
	}

	/**
	 * Composes the alphabet from previously made windowdumps.
	 * 
	 * @param alphabetName
	 *            the name of the alphabet that should be composed
	 * @param alphabetLoc
	 *            the location of the window_dumps (.xml) / .plist files of
	 *            which the alphabet will be made
	 */
	private void composeAlphabet(String alphabetName, String alphabetLoc) {
		AlphabetFileWriter afw = new AlphabetFileWriter("alphabet/" + alphabetName + ".txt");
		try {
			afw.collectAlphabet(new File(alphabetLoc));
			System.out.println("An alphabet was created with name " + alphabetName);
		} catch (Exception e) {
			System.out.println(
					"An error occurred. Please try again. " + "Make sure you did not misspell the filelocation.");
		}
	}


	/**
	 * Runs the scripts/make_dump.sh, which makes a window dump of the current
	 * screen and saves it to alphabet/window_dumps/fileName
	 * 
	 * @param fileName
	 *            the filename of the window dump
	 * @return -1 for an error, else the ouput from the process that runs the
	 *         script
	 */
	private int runAlphabetScript(String fileName) {
		System.out.println("Executing script");
		String out = "alphabet/window_dumps/" + fileName;

		Process alphabetScript;
		try {
			System.out.println("In try");
			Runtime.getRuntime().exec("adb shell uiautomator dump").waitFor();
			System.out.println("Done dump");
			// Runtime.getRuntime().exec("adb pull /sdcard/window_dump.xml
			// alphabet/window_dumps/" + fileName).waitFor();
			Runtime.getRuntime()
//					.exec("adb pull /storage/emulated/legacy/window_dump.xml alphabet/window_dumps/" + fileName)
					.exec("adb pull /sdcard/window_dump.xml alphabet/window_dumps/" + fileName)

					.waitFor();

			System.out.println("Done pull");
			return 0;

		} catch (Exception e) {
			// System.out.println("Please repeat your last action: there was a
			// problem executing " + "the script.");
			e.printStackTrace();
		}
		System.out.println("Ending badly");
		return -1;
	}
	/**
	 * Asks and waits for a confirmation of the given config settings. Expects y
	 * or n.
	 * 
	 * @param question
	 *            the question to ask the user.
	 */
	private void askConfirm(String question) {
		Scanner scanner = new Scanner(System.in);
		String confirm = "y";
		while (!confirm.equals("y") && !confirm.equals("n")) {
			System.out.println(question);
			confirm = scanner.nextLine();
			if (confirm.equals("y")) {
				System.out.println("Thank you.");
			} else if (confirm.equals("n")) {
				System.out.println("You can edit the config settings in src/main/config");
				System.exit(1);
			} else {
				System.out.println("Really funny. Try again.");
			}

		}
		scanner.close();
	}

	/**
	 * Executes the method given on the start of the application.
	 */
	public void execute() {
		try {
			Method method = this.getClass().getMethod(parseMethod(), new Class[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException | SecurityException e) {
			System.out.println("Unknown option: " + command);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			System.out.println(
					"Invalid usage of: " + command + ", the following error " + "was given: " + e.getMessage());
		} catch (InvocationTargetException e) {
			System.out.println("An error occurred while executing '" + command + "'. The "
					+ "following error was given: " + e.getCause());
			e.printStackTrace();
		}
	}

	/**
	 * Prints the most important config settings so that the user can verify if
	 * they are correct or not. Prints: os, alphabetFile, resetMethod,
	 * dumpFileExtension.
	 */
	public void printConfig() {
		Property props = Property.getInstance();
		System.out.println("You are using the following settings. If this is correct, "
				+ "you do not have to do anything. If they are not correct, please alter " + "your config files.");
		String[] importantSettings = new String[] { "apkname", "alphabetFile", "learningAlgorithm", "EquivMethod","deviceName" };
		for (int i = 0; i < importantSettings.length; i++) {
			System.out.printf("%-25s%s%n", importantSettings[i], " = " + props.get(importantSettings[i]));
		}
	}

	/**
	 * Deletes an alphabet with the given name.
	 */
	public void destroyAlphabet() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter the name of the alphabet: ");
		String alphabetName = scanner.nextLine();
		File alphaFile = new File("alphabet/" + alphabetName + ".txt");
		if (alphaFile.exists()) {
			alphaFile.delete();
			System.out.println("Alphabet file was deleted successfully.");
		} else {
			System.out.println("The file containing " + alphabetName + " could not be found.");
		}
		scanner.close();
	}

	// To be called from commandline for testing
	public void apkk() {
		this.apkInfo = new apkinfo();
		this.apkInfo.selectAPKInput();
	}

	public void apkConstruct() {
		this.apkInfo = new apkinfo();
		this.apkInfo.selectAPKConfig();
	}

	/**
	 * parses the methods given. When the method is of the format object:action,
	 * this will translate to actionObject
	 * 
	 * @return the result of the parsing process
	 */
	private String parseMethod() {
		String[] splitted = command.split(":", 2);
		if (splitted.length == 1) {
			return splitted[0];
		} else {
			return splitted[1] + splitted[0].substring(0, 1).toUpperCase() + splitted[0].substring(1);
		}
	}

	/**
	 * The main method of the application. Creates a Main instance and executes
	 * all the assignments it gets.
	 * 
	 * @param args
	 *            the commandline arguments given on the start of the
	 *            application
	 */
	public static void main(String[] args) {
		Main main = new Main(args);
		main.execute();
	}
}
