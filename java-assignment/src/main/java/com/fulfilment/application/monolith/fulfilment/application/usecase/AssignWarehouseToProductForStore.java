package com.fulfilment.application.monolith.fulfilment.application.usecase;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentAssignmentConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentReferenceNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentRequestException;
import com.fulfilment.application.monolith.fulfilment.domain.port.FulfilmentAssignmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.validator.FulfilmentAssignmentValidator;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AssignWarehouseToProductForStore {

  private final FulfilmentAssignmentStore assignmentStore;
  private final FulfilmentAssignmentValidator validator;

  public AssignWarehouseToProductForStore(
      FulfilmentAssignmentStore assignmentStore, FulfilmentAssignmentValidator validator) {
    this.assignmentStore = assignmentStore;
    this.validator = validator;
  }

  public FulfilmentAssignment assign(FulfilmentAssignment assignment) {
    validateRequest(assignment);

    // Lock shared rows in a consistent order to protect the aggregate limits from concurrent writes.
    if (!assignmentStore.lockStore(assignment.storeId)) {
      throw new FulfilmentReferenceNotFoundException("Store does not exist.");
    }
    if (!assignmentStore.lockProduct(assignment.productId)) {
      throw new FulfilmentReferenceNotFoundException("Product does not exist.");
    }
    if (!assignmentStore.lockActiveWarehouse(assignment.warehouseId)) {
      throw new FulfilmentReferenceNotFoundException("Active warehouse does not exist.");
    }

    if (assignmentStore.exists(
        assignment.storeId, assignment.productId, assignment.warehouseId)) {
      throw new FulfilmentAssignmentConflictException(
          "This fulfilment assignment already exists.");
    }

    validator.validate(
        assignmentStore.getStatistics(
            assignment.storeId, assignment.productId, assignment.warehouseId));
    assignmentStore.create(assignment);
    return assignment;
  }

  public List<FulfilmentAssignment> getAll() {
    return assignmentStore.getAll();
  }

  private void validateRequest(FulfilmentAssignment assignment) {
    if (assignment == null
        || !isPositive(assignment.storeId)
        || !isPositive(assignment.productId)
        || !isPositive(assignment.warehouseId)) {
      throw new FulfilmentRequestException("Store, product, and warehouse ids must be positive.");
    }
  }

  private boolean isPositive(Long id) {
    return id != null && id > 0;
  }
}
