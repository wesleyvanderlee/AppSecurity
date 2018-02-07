package com.wesley.emulator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Emulator {

	// emulator @Nexus5X -http-proxy 127.0.0.1:8080
	private final static String startCMD = "emulator -avd %s -http-proxy 127.0.0.1:8889";
	AndroidEmulatorInstrumentator aei;
	String name;
	boolean loggedIn;

	public Emulator(String _name) {
		this.name = _name;
		this.loggedIn = false;
	}

	public void start() {
		System.out.println("Launching emulator now");
		String devices = executeCommand("adb devices");
		while (!devices.contains("emulator")) {
//			this.needForStart();
			devices = executeCommand("adb devices");
		}
		System.out.println(devices);

		this.aei = new AndroidEmulatorInstrumentator();
		this.aei.startApp();
	}

	public AndroidEmulatorInstrumentator getInstrumentator(){
		return this.aei;
	}
	
	private void needForStart() {
		String comm = String.format(startCMD, this.name);
		executeCommandNonBlocking(comm);
		System.out.println(
				"Emulator hasn't started yet, start the emulator with the following command: \"" + comm + "\"");

		System.out.println("Waiting 3 minutes for the emulator to start.");

		for (int i = 0; i < 18; i++) {
			System.out.print("=");
		}
		System.out.println();
		for (int i = 0; i < 18; i++) {
			if (!this.loggedIn)
				try {
					Thread.sleep(10000);
				} catch (Exception e) {

				}
			if (i > 12) {
				ensureLogin();
			}
			System.out.print("=");

		}
		System.out.println();
	}

	private void ensureLogin() {
		if (this.loggedIn) {
			return;
		}
		String c = "adb -s emulator-5554 shell dumpsys window windows | grep -E 'mCurrentFocus'";
		if (!executeCommand(c).contains("Launcher")) { // indicates not logged
														// in
			executeCommand("scripts/loginEmma.sh");
			// executeCommand("scripts/loginEmma.sh");
		} else { // indicates logged in
			this.loggedIn = true;
		}

	}


	
	private void startProxy(){
		List<String> commandLine = new ArrayList<String>();
		commandLine.add("scripts/mitmdump");
		commandLine.add("-s scripts/customScript.py");
		commandLine.add("-p 8889");
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		builder.redirectErrorStream(true);
		builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		
		try {
			Process process = builder.start();
			process.waitFor(60, TimeUnit.SECONDS);
			
			Thread.sleep(20000);
			process.destroy();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String executeCommandNonBlocking(String command) {
		String info = "";
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	private String executeCommand(String command) {
		String info = "";
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(command);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = b.readLine()) != null) {
				info += line + "\n";
			}
			b.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
}
