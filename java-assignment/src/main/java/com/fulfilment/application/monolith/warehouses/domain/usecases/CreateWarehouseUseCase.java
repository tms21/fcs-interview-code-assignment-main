package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateRequiredFields(warehouse);
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw badRequest("Business unit code already exists.");
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    validateLocationAndCapacity(warehouse, location, null, warehouseStore.getAll());

    warehouse.id = null;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }

  static void validateRequiredFields(Warehouse warehouse) {
    if (warehouse == null
        || warehouse.businessUnitCode == null
        || warehouse.businessUnitCode.isBlank()
        || warehouse.location == null
        || warehouse.location.isBlank()
        || warehouse.capacity == null
        || warehouse.stock == null) {
      throw badRequest("Business unit code, location, capacity, and stock are required.");
    }
    if (warehouse.capacity <= 0 || warehouse.stock < 0 || warehouse.stock > warehouse.capacity) {
      throw badRequest("Capacity must be positive and stock must be between zero and capacity.");
    }
  }

  static void validateLocationAndCapacity(
      Warehouse warehouse,
      Location location,
      Warehouse excludedWarehouse,
      List<Warehouse> existingWarehouses) {
    if (location == null) {
      throw badRequest("Warehouse location does not exist.");
    }
    if (warehouse.capacity > location.maxCapacity) {
      throw badRequest("Warehouse capacity exceeds the location maximum capacity.");
    }

    long activeCount = 0;
    long activeCapacity = 0;
    for (Warehouse existing : existingWarehouses) {
      if (excludedWarehouse != null
          && (excludedWarehouse == existing
              || (excludedWarehouse.id != null && excludedWarehouse.id.equals(existing.id)))) {
        continue;
      }
      if (existing.location.equals(warehouse.location)) {
        activeCount++;
        activeCapacity += existing.capacity;
      }
    }
    if (activeCount >= location.maxNumberOfWarehouses) {
      throw badRequest("Maximum number of warehouses for this location has been reached.");
    }
    if (activeCapacity + warehouse.capacity > location.maxCapacity) {
      throw badRequest("Total warehouse capacity exceeds the location maximum capacity.");
    }
  }

  private static WebApplicationException badRequest(String message) {
    return new WebApplicationException(message, 400);
  }
}
