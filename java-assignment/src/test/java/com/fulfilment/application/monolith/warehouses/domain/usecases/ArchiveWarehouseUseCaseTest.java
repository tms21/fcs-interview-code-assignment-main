package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  public void archivesWarehouseWithoutRemovingItsHistory() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    store.create(warehouse);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    assertNotNull(store.allRows().get(0));
  }
}
