package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    copyToEntity(warehouse, entity);
    persist(entity);
    warehouse.id = entity.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse entity = warehouse.id == null ? null : findEntityById(warehouse.id);
    if (entity == null && warehouse.businessUnitCode != null) {
      entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
          .firstResult();
    }
    if (entity == null) {
      throw new IllegalArgumentException("Warehouse does not exist.");
    }
    copyToEntity(warehouse, entity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse.id != null) {
      deleteById(warehouse.id);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity =
        find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse findActiveById(Long id) {
    DbWarehouse entity = find("id = ?1 and archivedAt is null", id).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  private DbWarehouse findEntityById(Long id) {
    return find("id = ?1", id).firstResult();
  }

  private void copyToEntity(Warehouse warehouse, DbWarehouse entity) {
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
  }
}
