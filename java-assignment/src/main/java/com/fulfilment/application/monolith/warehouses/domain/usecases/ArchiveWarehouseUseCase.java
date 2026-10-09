package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    LOGGER.debug("Validating warehouse archive request.");
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse does not exist.", 404);
    }
    if (warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse is already archived.", 404);
    }
    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);
    LOGGER.infof("Archived warehouse %s.", warehouse.businessUnitCode);
  }
}
