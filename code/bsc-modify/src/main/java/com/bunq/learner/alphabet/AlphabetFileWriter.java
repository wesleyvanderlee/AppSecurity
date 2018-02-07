package com.bunq.learner.alphabet;

import com.bunq.util.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class collects the partial alphabets and writes them to a file for further use.
 * 
 * @author michel
 *
 */
public class AlphabetFileWriter {

  private String filePath = "";
  private Property properties;
  

  /**
   * This constructor sets the given filePath in its field.
   * @Constructor Creates a new AlphabetFileWriter
   * @param filePath
   *          The filepath where the alphabet will be written to
   */
  public AlphabetFileWriter(String filePath) {
    this.filePath = filePath;
    properties = Property.getInstance();
  }

  /**
   * Writes all the collected partial alphabets from the given input folder to the specified
   * filepath that was given in the constructor.
   * 
   * @param inputFolder
   *          The input folder with xml/plist view files
   * @throws FileNotFoundException 
   *          When the fikle cannot be found.
   */
  public void collectAlphabet(File inputFolder) throws FileNotFoundException, Exception {
	System.out.println("In AlphabetFileWriter:collectAlphabet");
    FilenameFilter filter = createFilter(properties.get("dumpFileExtension"));
    HashSet<String> words = new HashSet<String>();
    boolean a = inputFolder.exists();
    boolean b = inputFolder.isDirectory();
    System.out.println(a + " || " + b);
    if (a && b) {
    	System.out.println("In AlphabetFileWriter:collectAlphabet  A");
      for (File file : inputFolder.listFiles(filter)) {
    	  System.out.println("In AlphabetFileWriter:collectAlphabet " + file.getName());
        XMLParser parser = new XMLParser(file, properties.get("rootTag"), 
            properties.get("nodeParserClass"));
        words.addAll(parser.parseDocument());
      }
      writeToFile(words);
    } else {
      throw new FileNotFoundException("Inputfolder (" + inputFolder
          + ") could not be found or is not a folder");
    }
  }

  /**
   * Creates a filename filter based on the extension provided.
   * 
   * @param extension
   *          A string representation of the file extension to be filtered
   * @return the filename filter that was created.
   */
  private FilenameFilter createFilter(final String extension) {
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(extension);
      }
    };
    return filter;
  }

  /**
   * Writes the given (hash)set to the filepath that is
   * in the filepath field.
   * 
   * @param words
   *          The alphabet that should be written to file
   * @throws FileNotFoundException when the filepath is non-existing
   */
  public void writeToFile(Set<String> words) throws FileNotFoundException {
    PrintWriter writer;
    try {
      writer = new PrintWriter(this.filePath, "UTF-8");
      Iterator<String> it = words.iterator();
      while (it.hasNext()) {
        writer.println(it.next());
      }
      writer.close();

    } catch (UnsupportedEncodingException e) {
      System.err.println("Unknown Encoding: " + e.getMessage());
    }
  }
}
