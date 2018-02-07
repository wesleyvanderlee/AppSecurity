package com.bunq.teacher;

import com.bunq.util.Property;

import org.openqa.selenium.NoSuchElementException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Represents an Action that can be executed on an Instrumentator instance.
 *
 * @author tom
 *
 */
public class Action {
  private String methodName;
  private ArrayList<Object> parameters;
  private Class[] parameterTypes;
  private Property properties = Property.getInstance();

  /**
   * Private Constructor for the Action class. A new Action can be created by invoking the parse
   * method with a valid message;
   * 
   * @param methName
   *          the name of the method that this action represents
   * @param params
   *          the parameters that should be feeded to the method given in methName
   */
  private Action(String methName, ArrayList<Object> params) {
    methodName = methName;
    parameters = params;
    inferParameterTypes();
  }

  private Action(String methName) {
	    methodName = methName;
	    parameters = new ArrayList<Object>();
	    inferParameterTypes();
	  }
  
  /**
   * Sets the parameterTypes array with the parameters contained in the parameters member.
   */
  private void inferParameterTypes() {
    int size = parameters.size();
    parameterTypes = new Class[size];

    for (int i = 0; i < size; i++) {
      parameterTypes[i] = (parameters.get(i)).getClass();
    }
  }

  /**
   * Executes the action with the instrumentator using reflection.
   * 
   * @param instr
   *          An instance of IInstrumentator
   * @return the return value of the executed method, which reflects the output of the app on
   *         executing this action
   * @throws InvalidWordException
   *           will be thrown: if this action does not resemble a method of an Instrumentator
   *           instance, or if this action does somehow trigger an exception due to the use of
   *           reflection or if the invoked method somehow failed
   */
  public String dispatch(IInstrumentator instr) throws InvalidWordException {
    try {
//    	System.out.println("Action.dispatch:");
//      System.out.println(this);
      Method method = instr.getClass().getMethod(methodName, parameterTypes);
      return (String) method.invoke(instr, parameters.toArray());
    } catch (NoSuchMethodException e) {
      throw new InvalidWordException("Method missing: " + e.getMessage());
    } catch (SecurityException e) {
      throw new InvalidWordException("Not allowed to use this method: " + e.getMessage());
    } catch (IllegalAccessException e) {
      throw new InvalidWordException("Not allowed to use this method: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new InvalidWordException("Bad argument: " + e.getMessage());
    } catch (InvocationTargetException e) {
      if (NoSuchElementException.class.equals(e.getCause().getClass())) {
        return properties.get("failureMsg");
      } else {
        throw new InvalidWordException("Can't invocate target: " + e.getMessage());
      }
    }
  }

  /**
   * Parses the input. The input should be of one of the following formats: - method%arg -
   * method%arg1,arg_i,arg_n
   * 
   * @param input
   *          the String which should be parsed to an Action
   * @return an Action method with as attributes the values of the parsed String
   * @throws InvalidWordException
   *           when the string is not recognized
   */
  public static Action parse(String input) throws InvalidWordException {
    String[] actionParts = input.split("%");

    if (actionParts.length == 1) {
    	String method = parseMethod(actionParts[0]);
    	 return new Action(method);
    } else if(actionParts.length > 1){
      String method = parseMethod(actionParts[0]);
      ArrayList<Object> parameters = parseParameters(actionParts[1]);
      return new Action(method, parameters);
    } else{
    	throw new InvalidWordException("Contains wrong number of %'s: " + (actionParts.length - 1));
    }
  }

  /**
   * Receives the part before the colon from Action.parse.
   * 
   * @param method
   *          represents the method as a String
   * @return the trimmed version of the argument
   * @throws InvalidWordException
   *           when the given argument is empty
   */
  private static String parseMethod(String method) throws InvalidWordException {
    String res = method.trim();
    if (res.length() < 1) {
      throw new InvalidWordException("Empty method given");
    }
    return res;
  }

  /**
   * Receives the part after the colon from Action.parse
   * 
   * @param paramList
   *          is a list of the arguments as a String
   * @return the parsed parameter list
   * @throws InvalidWordException
   *           when one or more arguments are empty
   */
  private static ArrayList<Object> parseParameters(String paramList) throws InvalidWordException {
    ArrayList<Object> res = new ArrayList<Object>();
    paramList = paramList.trim();
    if (paramList.length() == 0) {
      throw new InvalidWordException("No parameters given!");
    }
    String[] paramSplitted = paramList.split("#");
    for (int i = 0; i < paramSplitted.length; i++) {
      String current = paramSplitted[i];
      if (current.length() < 1) {
        throw new InvalidWordException("Empty argument on list!");
      }
      res.add(current);
    }
    return res;
  }

  public String getMethodName() {
    return methodName;
  }

  public ArrayList<Object> getParameters() {
    return parameters;
  }

  /**
   * Creates a string representation of this Action in the following format: 
   * Action:methodName(arg1, arg2, ..., argn)
   */
  public String toString() {
    String res = "Action: ";
    res += methodName + "(";
    for (Object param : parameters) {
      res += param + ",";
    }
    res = res.substring(0, res.length() - 1);
    res += ")";
    return res;
  }
}
