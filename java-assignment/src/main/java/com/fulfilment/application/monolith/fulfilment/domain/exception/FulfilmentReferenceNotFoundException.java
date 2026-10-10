package com.fulfilment.application.monolith.fulfilment.domain.exception;

public class FulfilmentReferenceNotFoundException extends RuntimeException {

  public FulfilmentReferenceNotFoundException(String message) {
    super(message);
  }
}
