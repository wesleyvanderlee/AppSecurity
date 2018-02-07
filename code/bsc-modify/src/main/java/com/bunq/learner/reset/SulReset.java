package com.bunq.learner.reset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Abstract super class for all the different Sul-reset classes.
 * @author tom
 *
 */
public abstract class SulReset {
  protected String[] command;
  private Runtime runtime;

  public SulReset(Runtime execEnv) {
    runtime = execEnv;
  }

  /**
   * Resets the SUL and returns the exit code of the commandline (0 for correct execution, != 0 for
   * errors.
   * 
   * @param verbose
   *          when true, generates output on the commandline, else does everything silent
   * @return 0 for a succesful reset, >0 when problens arised.
   */
  public int reset(boolean verbose) {
    try {
      Process process = runtime.exec(command);
      if (verbose) {
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = input.readLine()) != null) {
          System.out.println(line);
        }
        input.close();
      }
      return process.waitFor();

    } catch (IOException | InterruptedException err) {
      System.out.println("PROBLEM: executing command failed: " + err.getMessage());
      return 100;
    }
  }
}
