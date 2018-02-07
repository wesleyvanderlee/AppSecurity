package com.bunq.learner.alphabet;

/**
 * Interface for the parsing of XML elements of
 * Android and iOS window dumps.
 * 
 * @author michel
 *
 */
public interface UINode {

  public String toAlphabetWord();

  public boolean isInteractive();
}
