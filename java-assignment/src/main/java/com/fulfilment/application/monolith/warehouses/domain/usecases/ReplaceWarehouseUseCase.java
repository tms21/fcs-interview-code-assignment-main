package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    CreateWarehouseUseCase.validateRequiredFields(newWarehouse);
    Warehouse currentWarehouse =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw new WebApplicationException("Active warehouse does not exist.", 404);
    }
    if (!currentWarehouse.location.equals(newWarehouse.location)) {
      throw new WebApplicationException(
          "Replacement warehouse must use the existing warehouse location.", 400);
    }
    if (!currentWarehouse.stock.equals(newWarehouse.stock)) {
      throw new WebApplicationException(
          "Replacement warehouse stock must match the existing warehouse stock.", 400);
    }
    if (newWarehouse.capacity < currentWarehouse.stock) {
      throw new WebApplicationException(
          "Replacement capacity must accommodate the existing warehouse stock.", 400);
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    CreateWarehouseUseCase.validateLocationAndCapacity(
        newWarehouse, location, currentWarehouse, warehouseStore.getAll());

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(currentWarehouse);

    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
