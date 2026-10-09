package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  @Test
  public void createsWarehouseWhenLocationAndCapacityAreValid() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, this::resolveLocation);
    Warehouse warehouse = warehouse("MWH.100", 60, 40);

    useCase.create(warehouse);

    assertEquals(1, store.getAll().size());
    assertEquals("MWH.100", store.getAll().get(0).businessUnitCode);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  public void rejectsWarehouseWhenLocationIsUnknown() {
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(new WarehouseStoreStub(), identifier -> null);

    WebApplicationException error =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse("MWH.101", 10, 2)));

    assertEquals(400, error.getResponse().getStatus());
  }

  @Test
  public void rejectsWarehouseWhenLocationCapacityWouldBeExceeded() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    store.create(warehouse("MWH.102", 70, 10));
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, this::resolveLocation);

    WebApplicationException error =
        assertThrows(
            WebApplicationException.class,
            () -> useCase.create(warehouse("MWH.103", 40, 10)));

    assertEquals(400, error.getResponse().getStatus());
  }

  @Test
  public void rejectsWarehouseWhenLocationWarehouseLimitIsReached() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    store.create(warehouse("MWH.104", 30, 10));
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(
            store, identifier -> new Location("LOC-1", 1, 100));

    WebApplicationException error =
        assertThrows(
            WebApplicationException.class,
            () -> useCase.create(warehouse("MWH.105", 30, 10)));

    assertEquals(400, error.getResponse().getStatus());
  }

  @Test
  public void rejectsDuplicateActiveBusinessUnitCode() {
    WarehouseStoreStub store = new WarehouseStoreStub();
    store.create(warehouse("MWH.106", 20, 10));
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, this::resolveLocation);

    WebApplicationException error =
        assertThrows(
            WebApplicationException.class,
            () -> useCase.create(warehouse("MWH.106", 20, 10)));

    assertEquals(400, error.getResponse().getStatus());
  }

  private Location resolveLocation(String identifier) {
    return "LOC-1".equals(identifier) ? new Location("LOC-1", 2, 100) : null;
  }

  private Warehouse warehouse(String code, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = "LOC-1";
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
