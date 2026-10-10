package com.fulfilment.application.monolith.fulfilment.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentAssignmentConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentReferenceNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentRequestException;
import com.fulfilment.application.monolith.fulfilment.domain.model.AssignmentStatistics;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.port.FulfilmentAssignmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.validator.FulfilmentAssignmentValidator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssignWarehouseToProductForStoreTest {

  private final FulfilmentAssignmentValidator validator = new FulfilmentAssignmentValidator();

  @Test
  void assignsWarehouseWhenReferencesAndLimitsAreValid() {
    StubAssignmentStore store = new StubAssignmentStore();
    AssignWarehouseToProductForStore useCase = new AssignWarehouseToProductForStore(store, validator);
    FulfilmentAssignment assignment = validAssignment();

    FulfilmentAssignment result = useCase.assign(assignment);

    assertSame(assignment, result);
    assertEquals(42L, result.id);
    assertEquals(1, store.created.size());
    assertEquals(List.of("store", "product", "warehouse"), store.lockOrder);
    assertEquals(assignment.storeId, store.statisticsStoreId);
    assertEquals(assignment.productId, store.statisticsProductId);
    assertEquals(assignment.warehouseId, store.statisticsWarehouseId);
  }

  @Test
  void rejectsMissingOrNonPositiveIdsBeforeAccessingStore() {
    StubAssignmentStore store = new StubAssignmentStore();
    AssignWarehouseToProductForStore useCase = new AssignWarehouseToProductForStore(store, validator);

    assertThrows(FulfilmentRequestException.class, () -> useCase.assign(null));
    assertThrows(
        FulfilmentRequestException.class,
        () -> useCase.assign(new FulfilmentAssignment()));
    FulfilmentAssignment assignment = validAssignment();
    assignment.productId = 0L;
    assertThrows(FulfilmentRequestException.class, () -> useCase.assign(assignment));

    assertEquals(List.of(), store.lockOrder);
    assertEquals(List.of(), store.created);
  }

  @Test
  void rejectsMissingStoreProductOrActiveWarehouse() {
    StubAssignmentStore missingStore = new StubAssignmentStore();
    missingStore.storeExists = false;
    assertThrows(
        FulfilmentReferenceNotFoundException.class,
        () -> useCase(missingStore).assign(validAssignment()));
    assertEquals(List.of("store"), missingStore.lockOrder);

    StubAssignmentStore missingProduct = new StubAssignmentStore();
    missingProduct.productExists = false;
    assertThrows(
        FulfilmentReferenceNotFoundException.class,
        () -> useCase(missingProduct).assign(validAssignment()));
    assertEquals(List.of("store", "product"), missingProduct.lockOrder);

    StubAssignmentStore missingWarehouse = new StubAssignmentStore();
    missingWarehouse.warehouseExists = false;
    assertThrows(
        FulfilmentReferenceNotFoundException.class,
        () -> useCase(missingWarehouse).assign(validAssignment()));
    assertEquals(List.of("store", "product", "warehouse"), missingWarehouse.lockOrder);
  }

  @Test
  void rejectsDuplicateAssignmentWithoutCreatingIt() {
    StubAssignmentStore store = new StubAssignmentStore();
    store.assignmentExists = true;

    assertThrows(
        FulfilmentAssignmentConflictException.class,
        () -> useCase(store).assign(validAssignment()));

    assertEquals(List.of(), store.created);
    assertEquals(0, store.statisticsCalls);
  }

  @Test
  void rejectsAssignmentsThatWouldExceedAnyFulfilmentLimit() {
    List<AssignmentStatistics> limitViolations =
        List.of(
            new AssignmentStatistics(2, 2, 4, false, false),
            new AssignmentStatistics(1, 3, 4, false, false),
            new AssignmentStatistics(0, 1, 5, true, false));

    for (AssignmentStatistics statistics : limitViolations) {
      StubAssignmentStore store = new StubAssignmentStore();
      store.statistics = statistics;

      assertThrows(
          FulfilmentAssignmentConflictException.class,
          () -> useCase(store).assign(validAssignment()));
      assertEquals(List.of(), store.created);
    }
  }

  @Test
  void returnsAssignmentsFromTheStorePort() {
    StubAssignmentStore store = new StubAssignmentStore();
    FulfilmentAssignment existing = validAssignment();
    store.assignments = List.of(existing);

    assertSame(store.assignments, useCase(store).getAll());
  }

  private AssignWarehouseToProductForStore useCase(StubAssignmentStore store) {
    return new AssignWarehouseToProductForStore(store, validator);
  }

  private FulfilmentAssignment validAssignment() {
    FulfilmentAssignment assignment = new FulfilmentAssignment();
    assignment.storeId = 1L;
    assignment.productId = 2L;
    assignment.warehouseId = 3L;
    return assignment;
  }

  private static class StubAssignmentStore implements FulfilmentAssignmentStore {

    private boolean storeExists = true;
    private boolean productExists = true;
    private boolean warehouseExists = true;
    private boolean assignmentExists;
    private int statisticsCalls;
    private Long statisticsStoreId;
    private Long statisticsProductId;
    private Long statisticsWarehouseId;
    private AssignmentStatistics statistics = new AssignmentStatistics(0, 0, 0, false, false);
    private List<FulfilmentAssignment> assignments = List.of();
    private final List<String> lockOrder = new ArrayList<>();
    private final List<FulfilmentAssignment> created = new ArrayList<>();

    @Override
    public boolean lockStore(Long storeId) {
      lockOrder.add("store");
      return storeExists;
    }

    @Override
    public boolean lockProduct(Long productId) {
      lockOrder.add("product");
      return productExists;
    }

    @Override
    public boolean lockActiveWarehouse(Long warehouseId) {
      lockOrder.add("warehouse");
      return warehouseExists;
    }

    @Override
    public boolean exists(Long storeId, Long productId, Long warehouseId) {
      return assignmentExists;
    }

    @Override
    public AssignmentStatistics getStatistics(Long storeId, Long productId, Long warehouseId) {
      statisticsCalls++;
      statisticsStoreId = storeId;
      statisticsProductId = productId;
      statisticsWarehouseId = warehouseId;
      return statistics;
    }

    @Override
    public void create(FulfilmentAssignment assignment) {
      assignment.id = 42L;
      created.add(assignment);
    }

    @Override
    public List<FulfilmentAssignment> getAll() {
      return assignments;
    }
  }
}
