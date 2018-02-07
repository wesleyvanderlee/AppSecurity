package com.bunq.learner.alphabet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class implements the UINode class and creates the appropriate alphabet word for a given
 * input element for Android.
 * 
 * @author tom
 * @implements UINode
 */
public class UINodeAndroid implements UINode {

  private Element el;

  private boolean clickable;
  private boolean checkable;
  private boolean editable;
  private int width;
  private int height;
  private String xpath;

  /**
   * The constructor sets all the private attributes; it uses the xml attributes to set those.
   * 
   * @param element
   *          The element that will be used to set the attributes
   * @throws Exception when the element is corrupt
   */
  public UINodeAndroid(Element element) throws Exception {
    el = element;
    clickable = readBoolAttribute("clickable");
    checkable = readBoolAttribute("checkable");
    editable = getEditable();
    calculateSize();
    
    if(!isInteractive()){
    	this.clickable = true;
    }
    
    xpath = retrieveXPath(element, element.getAttribute("class") + "[" + getIndex(element) + "]"
        + buildAttributesSelector(element));
    
  }

  /**
   * Recursively creates the xPath according to the position of the node in the XML file.
   * 
   * @param element
   *          The element of which the xPath should be retrieved
   * @param xpath
   *          Needed for recursive calls
   * @return the xpath for the current element
   */
  private String retrieveXPath(Element element, String xpath) {
    Element parent = (Element) element.getParentNode();
    if (!parent.getNodeName().equals("hierarchy")
        && !parent.getParentNode().getNodeName().equals("hierarchy")) {
      xpath = parent.getAttribute("class") + "[" + getIndex(parent) + "]"
          + buildAttributesSelector(parent) + "/" + xpath;
      return retrieveXPath(parent, xpath);
    } else {
      xpath = "//" + xpath;
    }
    return xpath;
  }

  /**
   * Reads attr from this node and converts the "false" or "true" string to a bool value.
   * 
   * @param attr
   *          The true or false in String format
   * @return True or false as a boolean, depending on the input string
   * @throws Exception
   *           when the given attribute is not a String containing "true" or "false"
   */
  private boolean readBoolAttribute(String attr) throws Exception {
    String value = el.getAttribute(attr);
    if (value.equals("false")) {
      return false;
    } else if (value.equals("true")) {
      return true;
    } else {
      throw new Exception("Not a boolean!");
    }
  }

  /**
   * Calculates the xpath index. 
   * @note This method does not get the index attribute as defined in the XML document!
   * 
   * @param ele the element of which we are going to retrieve the index 
   * @return the value of xpathindex
   */
  private int getIndex(Element ele) {
    int index = 1;
    return index + getEqualPrevSiblings(ele);
  }

  /**
   * Returns the amount of siblings that precede the given element that have the same class and are
   * not text-nodes (which represent whitespace).
   * 
   * @param current
   *              the node of which we want to retrieve the amount of equal sibling
   * 
   * @return the number of siblings that precede the given element and do have the same class as the
   *         element
   */
  private int getEqualPrevSiblings(Element current) {
    String currentElementClass = current.getAttribute("class");
    int amount = 0;
    Node siblNode = current.getPreviousSibling();
    while (siblNode != null) {
      if (!siblNode.getNodeName().equals("#text")) {
        Element sibl = (Element) siblNode;
        if (sibl.getAttribute("class").equals(currentElementClass)) {
          amount += 1;
        }
      }
      siblNode = siblNode.getPreviousSibling();
    }
    return amount;
  }

  /**
   * The following function builds a multiple-attributes-selector for retrieving a certain element
   * using xpath selectors. The attributes that are being retrieved are: index, text,
   * resource-id and content-desc
   * 
   * @return a String in the format of: "@attr1='some_val1' and @attr2='some_val2' and ... "
   */
  private String buildAttributesSelector(Element ele) {
    String[] attrs = { "index", "resource-id", "text", "content-desc" };
    String[] translatesTo = { "index", "resource-id", "contains()", "content-desc" };
    String[] attrsSelectors = new String[4];

    for (int i = 0; i < attrs.length; i++) {
      String attr = attrs[i];
      String appEquiv = translatesTo[i];
      attrsSelectors[i] = buildAttributeSelector(ele, attr, appEquiv);
    }
    return "[" + String.join(" and ", attrsSelectors) + "]";
  }

  /**
   * The following function builds an attribute selector for retrieving a certain element using
   * xpath selectors.
   * 
   * @param ele
   *          the element on which 'attr' is going to be retrieved
   * @param attr
   *          the attribute you want to include in an xpath
   * @param appEquiv
   *          the way the given attribute is represented on the app, make null if its the same as
   *          the attr itself
   * @return a String in the format of: "@attr='some_val'" or null if the attribute has no value
   */
  private String buildAttributeSelector(Element ele, String attr, String appEquiv) {
    String selector = null;
    if (appEquiv.matches("(.*)\\(\\)")) { 
      selector = buildFunctionAttrSelector(ele, attr, appEquiv.substring(0, appEquiv.length() - 2));
    } else {
      selector = "@" + appEquiv + "=";
      String val = ele.getAttribute(attr);
      if (val == null) {
        val = "";
      }
      selector += "'" + val + "'";
    }
    return selector;
  }

  /**
   * Builds a attribute selector for a function-like selector.
   * @param ele 
   *          the element for which the attribute is
   * @param attr
   *          the attribute tag
   * @param function
   *          the function which will become the selector
   * @return the function selector as a string
   */
  private String buildFunctionAttrSelector(Element ele, String attr, String function) {
    String val = ele.getAttribute(attr);
    if (val == null) {
      val = "";
    }
    String selector = function + "(@" + attr + ", '" + val + "')";
    return selector;
  }

  /**
   * Checks whether the class of this object equals to EditText, which means that text can be
   * inserted into it.
   * 
   * @return true when this XML element is editable (that is, that there can be text entered into
   *         it)
   */
  private boolean getEditable() {
    String[] clsArr = (el.getAttribute("class")).split("\\.");
    String uiClass = clsArr[clsArr.length - 1];

    if (uiClass.equals("EditText")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Calculates the size of the element which is stored in the el field. Uses the bounds of the
   * element to set the width and the height fields. First, the bounds need to be parsed into an
   * array of numbers. The bounds ALWAYS have the following format: [leftx,topy][rightx,bottomx].
   */
  private void calculateSize() {
    String boundsStr = el.getAttribute("bounds");
    String[] boundsArr = boundsStr.substring(1, boundsStr.length() - 1).split("\\]\\[");
    String[] leftTop = boundsArr[0].split(",");
    String[] rightBottom = boundsArr[1].split(",");
    try {
      width = Integer.parseInt(rightBottom[0]) - Integer.parseInt(leftTop[0]);
      height = Integer.parseInt(rightBottom[1]) - Integer.parseInt(leftTop[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("No size calculation possible: " + nfe.getMessage());
      width = -1;
      height = -1;
    }
  }

  /**
   * Returns whether given element is interactive.
   */
  public boolean isInteractive() {
    return editable || checkable || clickable;
  }

  /**
   * Creates a word that can be used in the Learnlib alphabet.
   * 
   * @return A word in String format that can be used for the alphabet
   */
  public String toAlphabetWord() {
    String action = "";
    String arguments = xpath + "#" + width + "#" + height;

    if (editable) {
      action = "enterText";
      arguments += "#ENTER SPECIFIC ARGUMENT!";
    } else if (checkable) {
      action = "check";
    } else if (clickable) {
      action = "push";
    }
    return action + "%" + arguments;
  }
}
