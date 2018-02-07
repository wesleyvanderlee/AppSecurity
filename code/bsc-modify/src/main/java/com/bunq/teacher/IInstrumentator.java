package com.bunq.teacher;

/**
 * Interface for Instrumentator classes.
 * 
 * @author tom
 *
 */
public interface IInstrumentator {
  public void startApp();

  public void close();

  public String login(String pincode);

  public String reset(String method);
  
  public String tap(String tapX, String tapY);
}
