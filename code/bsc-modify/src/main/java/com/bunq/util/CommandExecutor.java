package com.bunq.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandExecutor {
	
	private static String executeCommandNonBlocking(String command) {
		String info = "";
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	private static String executeCommand(String command) {
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
