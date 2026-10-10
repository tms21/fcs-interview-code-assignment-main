package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validator.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator validator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this(warehouseStore, locationResolver, new WarehouseValidator());
  }

  @Inject
  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.debug("Validating warehouse creation request.");
    validator.validateRequiredFields(warehouse);
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw badRequest("Business unit code already exists.");
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    validator.validateLocationAndCapacity(warehouse, location, null, warehouseStore.getAll());

    warehouse.id = null;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
    LOGGER.infof("Created warehouse %s at location %s.", warehouse.businessUnitCode, warehouse.location);
  }

  static void validateRequiredFields(Warehouse warehouse) {
    new WarehouseValidator().validateRequiredFields(warehouse);
  }

  static void validateLocationAndCapacity(
      Warehouse warehouse,
      Location location,
      Warehouse excludedWarehouse,
      List<Warehouse> existingWarehouses) {
    new WarehouseValidator().validateLocationAndCapacity(
        warehouse, location, excludedWarehouse, existingWarehouses);
  }

  private static WebApplicationException badRequest(String message) {
    return new WebApplicationException(message, 400);
  }
}
