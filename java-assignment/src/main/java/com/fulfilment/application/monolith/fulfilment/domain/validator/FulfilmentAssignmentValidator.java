package com.fulfilment.application.monolith.fulfilment.domain.validator;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentAssignmentConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.model.AssignmentStatistics;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfilmentAssignmentValidator {

  public void validate(AssignmentStatistics statistics) {
    if (statistics.warehousesForStoreAndProduct >= 2) {
      throw new FulfilmentAssignmentConflictException(
          "A product can be fulfilled by at most two warehouses per store.");
    }
    if (!statistics.warehouseAlreadyServesStore && statistics.warehousesForStore >= 3) {
      throw new FulfilmentAssignmentConflictException(
          "A store can be fulfilled by at most three different warehouses.");
    }
    if (!statistics.productAlreadyStoredInWarehouse && statistics.productsForWarehouse >= 5) {
      throw new FulfilmentAssignmentConflictException(
          "A warehouse can store at most five different products.");
    }
  }
}
