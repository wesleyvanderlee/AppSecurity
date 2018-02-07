package com.bunq.learner;

/**
 * Exception for the case in which the querycache does not have the given entry.
 * @author tom
 *
 */
public class QueryCacheMissException extends Exception {
  
  public QueryCacheMissException(String message) {
    super(message);
  }
}
