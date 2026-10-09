package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  @Test
  public void archivesExistingAndCreatesReplacementWithMatchingStock() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    Warehouse existing = warehouse(1L, "MWH.001", 60, 20);
    store.create(existing);
    existing.id = 1L;
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, this::resolveLocation);
    Warehouse replacement = warehouse(null, "MWH.001", 80, 20);

    useCase.replace(replacement);

    assertNotNull(existing.archivedAt);
    assertNull(store.findByBusinessUnitCode("MWH.001").archivedAt);
    assertEquals(2, store.allRows().size());
    assertEquals(20, store.findByBusinessUnitCode("MWH.001").stock);
  }

  @Test
  public void rejectsReplacementWhenStockDoesNotMatch() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    Warehouse existing = warehouse(1L, "MWH.001", 60, 20);
    store.create(existing);
    existing.id = 1L;
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, this::resolveLocation);

    WebApplicationException error =
        assertThrows(
            WebApplicationException.class,
            () -> useCase.replace(warehouse(null, "MWH.001", 80, 19)));

    assertEquals(400, error.getResponse().getStatus());
    assertNull(existing.archivedAt);
    assertEquals(1, store.allRows().size());
  }

  private Location resolveLocation(String identifier) {
    return new Location("LOC-1", 2, 100);
  }

  private Warehouse warehouse(Long id, String code, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.id = id;
    warehouse.businessUnitCode = code;
    warehouse.location = "LOC-1";
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
