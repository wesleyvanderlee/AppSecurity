package com.bunq.learner.alphabet;

import com.bunq.util.Property;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * This class implements the UINode class and creates the appropriate alphabet word for a given
 * input element for iOS.
 * 
 * @author michel
 * @implements UINode
 */
public class UINodeiOS implements UINode {
  private Property properties = Property.getInstance();
  private ArrayList<String> clickable = properties.getList("clickableClasses");
  private ArrayList<String> checkable = properties.getList("checkableClasses");
  private ArrayList<String> editable = properties.getList("editableClasses");
  
  private String name = "";
  private String value = "";
  private boolean click = false;
  private boolean check = false;
  private boolean edit = false;

  public enum PListType {
    LOGTYPE, MESSAGE
  }

  /**
   * Sets up all fields of this UINode using the given element.
   * @Constructor Sets all appropriate properties
   * @param element the element of which we want to retrieve all the data
   */
  public UINodeiOS(Element element) {
    String[] content = element.getElementsByTagName("string").item(PListType.MESSAGE.ordinal())
        .getTextContent().split("rect:");
    String[] values = content[0].split(":");

    if (isInteractiveUI(values[0])) {
      setProperties(values);
    }
  }

  /**
   * Sets the corresponding properties that are present in the value string
   * 
   * @note concerning the input array: [0] contains the type, so skip index 0 [1] is always present,
   *       if name or value is found this means that [1] is also always present, etc., so no checks
   *       needed
   * @param values
   *          The String[] that contains the properties to be set
   */
  private void setProperties(String[] values) {
    if (values[1].contains("name")) {
      if (values[2].contains("value")) {
        this.value = values[3];
        this.name = values[2].substring(0, values[2].length() - 6);
      } else {
        this.name = values[2];
      }
    } else if (values[1].contains("value")) {
      this.value = values[2];
    }
  }

  /**
   * Checks whether given UI element supports interactivity and sets its corresponding property.
   * @param element
   *          The element to be checked on interactivity
   * @return True or false, depending on whether the element is interactive
   */
  private boolean isInteractiveUI(String element) {

    if (this.clickable.contains(element)) {
      this.click = true;
      return true;
    } else if (this.checkable.contains(element)) {
      this.check = true;
      return true;
    } else if (this.editable.contains(element)) {
      this.edit = true;
      return true;
    }
    return false;
  }

  /**
   * Returns whether given element is interactive or not.
   */
  public boolean isInteractive() {
    return !this.name.equals("");
  }

  /**
   * Converts properties to a usable alphabet entry This function should only be invoked on
   * Interactive UINodes, otherwise it will return a word without a name argument.
   * 
   * @return A String representation of a word that can be used for the alphabet
   */
  public String toAlphabetWord() {
    String action = "";
    String arguments = this.name.trim();
    String trimmedValue = this.value.trim();
    if (!trimmedValue.equals("")) {
      arguments += "#" + trimmedValue;
    }

    if (this.edit) {
      action = "enterText";
      arguments += "#ENTER SPECIFIC ARGUMENT!";
    } else if (this.check) {
      action = "check";
    } else if (this.click) {
      action = "push";
    }

    return action + "%" + arguments;
  }

}
