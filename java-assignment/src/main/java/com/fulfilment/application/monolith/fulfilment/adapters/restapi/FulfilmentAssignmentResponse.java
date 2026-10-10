package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;

public class FulfilmentAssignmentResponse {

  public Long id;
  public Long storeId;
  public Long productId;
  public Long warehouseId;

  public static FulfilmentAssignmentResponse from(FulfilmentAssignment assignment) {
    FulfilmentAssignmentResponse response = new FulfilmentAssignmentResponse();
    response.id = assignment.id;
    response.storeId = assignment.storeId;
    response.productId = assignment.productId;
    response.warehouseId = assignment.warehouseId;
    return response;
  }
}
