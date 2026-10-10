package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validator.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator validator;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this(warehouseStore, locationResolver, new WarehouseValidator());
  }

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore,
      LocationResolver locationResolver,
      WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.debugf("Validating replacement for warehouse %s.", newWarehouse.businessUnitCode);
    validator.validateRequiredFields(newWarehouse);
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
    validator.validateLocationAndCapacity(newWarehouse, location, currentWarehouse, warehouseStore.getAll());

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(currentWarehouse);

    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
    LOGGER.infof(
        "Replaced warehouse %s at location %s.", newWarehouse.businessUnitCode, newWarehouse.location);
  }
}
