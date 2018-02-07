package com.bunq.apk;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.bunq.util.Property;

public class apkinfo {
	APK apk;
	public String apkname;
	ArrayList<String> choices;

	public apkinfo() {
		choices = new ArrayList<String>();
		this.apkname = "";
		this.fillAPK();
	}

	public APK getPublicAPK(){
		return this.apk;
	}
	
	public String getPublicAPKName(){
		return this.apk.APKname;
	}
	
	public void selectAPKInput() {
		printChoices();
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		in.close();
		selectAPK(choice);
		this.apk = this.getAPK();
		checkCallableActivities();

		System.out.println(apk);
	}
	
	public void selectAPKConfig() {
		this.apkname = Property.getInstance().get("apkname");
		System.out.println("\nCompiling information for " + this.apkname);
		this.apk = this.getAPK();
		checkCallableActivities();

		System.out.println(apk);
	}
	
	

	public void checkCallableActivities() {
		ArrayList<Integer> callables = new ArrayList<Integer>();
		
		if(this.apk.APKname.contains("negentwee")){ //hack around
			System.out.println("NEGENTWEE HACK USED FOR CALLABLE ACTIVITIES!!");
			callables.add(new Integer(0));
			this.apk.setCallables(callables);
			return;
		}
		
		System.out.println("Checking callable activities (" + this.apk.activities.size() + ")");
		int index = 0;
		String stopcommand = String.format("adb -s 5917c5be shell am force-stop %s",this.apk.APKname);
		for (String activity : this.apk.activities) {
			System.out.print("=");
			String command = String.format("adb  -s 5917c5be shell am start -n %s/%s",this.apk.APKname,activity);
			executeCommand(stopcommand);
			String res = executeCommand(command);
			if(!res.contains("SecurityException") && !res.contains("Error")) //Not callable -> ! -> callable
				callables.add(new Integer(index));
			index++;
		}
		System.out.println("\n");
		executeCommand(stopcommand);
		this.apk.setCallables(callables);
	}

	private void fillAPK() {
		File curDir = new File("apk/");
		for (File f : curDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".apk")) {
				choices.add(f.getName());
			}
		}
	}

	private void printChoices() {
		System.out.println("\nSelect an APK from the following list:");
		int i = 0;
		for (String apkname : choices) {
			System.out.println("[" + i + "]\t" + apkname);
			i++;
		}
	}

	private void selectAPK(int choice) {
		this.apkname = choices.get(choice);
	}

	private String getAppPackageName(String[] info) {
		for (String l : info) {
			if (l.contains("package: name=")) {
				String[] parts = l.split("\'");
				return parts[1];
			}

		}
		return "NOTFOUND";
	}

	private String getLaunchableActivityFromInfo(String[] info) {
		for (String l : info) {
			if (l.contains("launchable-activity")) {
				String[] parts = l.split("\'");
				return parts[1];
			}
		}
		return "NOTFOUND";
	}

	private ArrayList<String> getActivityFromInfo(String[] info, String packageName) {
		boolean start = false;
		ArrayList<String> activities = new ArrayList<String>();
		for (String l : info) {
			if (!start && l.contains("E: activity ")) {
				start = true;
			}
			if (start && l.contains(packageName) && l.contains("Raw")) {
				String[] parts = l.split("\"");
				activities.add(parts[3]);
			}
		}
		return activities;
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

	private APK getAPK() {
		String cmd = String.format("aapt dump badging apk/%s", this.apkname);
		String info = this.executeCommand(cmd);
		if (!info.contains("package: name=")) {
			System.out.println("Could not read info correctly, make sure aapt is installed and the file "+this.apkname+" is present in the /apk directory!");
			System.exit(0);
		}

		String[] lines = info.split("\n");
		String AppPackageName = getAppPackageName(lines);
		String launchableActivity = getLaunchableActivityFromInfo(lines);
		String manifest = this
				.executeCommand(String.format("aapt dump xmltree apk/%s AndroidManifest.xml", this.apkname));
		if (!manifest.contains("manifest")) {
			System.out.println("Could not read info correctly for manifest, make sure aapt is installed!");
			System.exit(0);
		}
		ArrayList<String> activities = getActivityFromInfo(manifest.split("\n"), AppPackageName);
		return new APK(AppPackageName, launchableActivity, activities);
	}
}
