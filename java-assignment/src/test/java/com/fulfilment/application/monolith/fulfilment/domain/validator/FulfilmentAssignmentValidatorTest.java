package com.fulfilment.application.monolith.fulfilment.domain.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentAssignmentConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.model.AssignmentStatistics;
import org.junit.jupiter.api.Test;

class FulfilmentAssignmentValidatorTest {

  private final FulfilmentAssignmentValidator validator = new FulfilmentAssignmentValidator();

  @Test
  void rejectsMoreThanTwoWarehousesForAProductAtAStore() {
    assertThrows(
        FulfilmentAssignmentConflictException.class,
        () -> validator.validate(new AssignmentStatistics(2, 2, 4, false, false)));
  }

  @Test
  void permitsExistingWarehouseAndProductAtTheirAggregateLimits() {
    assertDoesNotThrow(
        () -> validator.validate(new AssignmentStatistics(1, 3, 5, true, true)));
  }

  @Test
  void rejectsANewWarehouseWhenStoreAlreadyUsesThree() {
    assertThrows(
        FulfilmentAssignmentConflictException.class,
        () -> validator.validate(new AssignmentStatistics(1, 3, 4, false, false)));
  }

  @Test
  void rejectsANewProductWhenWarehouseAlreadyStoresFive() {
    assertThrows(
        FulfilmentAssignmentConflictException.class,
        () -> validator.validate(new AssignmentStatistics(0, 1, 5, true, false)));
  }
}
