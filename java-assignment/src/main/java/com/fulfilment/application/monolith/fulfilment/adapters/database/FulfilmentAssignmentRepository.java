package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.model.AssignmentStatistics;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.port.FulfilmentAssignmentStore;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentRepository
    implements FulfilmentAssignmentStore, PanacheRepository<DbFulfilmentAssignment> {

  @Inject ProductRepository productRepository;

  @Override
  public boolean lockStore(Long storeId) {
    return Store.findById(storeId, LockModeType.PESSIMISTIC_WRITE) != null;
  }

  @Override
  public boolean lockProduct(Long productId) {
    return productRepository
            .find("id", productId)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResult()
        != null;
  }

  @Override
  public boolean lockActiveWarehouse(Long warehouseId) {
    DbWarehouse warehouse =
        getEntityManager()
            .createQuery(
                "from DbWarehouse w where w.id = :id and w.archivedAt is null",
                DbWarehouse.class)
            .setParameter("id", warehouseId)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultStream()
            .findFirst()
            .orElse(null);
    return warehouse != null;
  }

  @Override
  public boolean exists(Long storeId, Long productId, Long warehouseId) {
    return count(
            "store.id = ?1 and product.id = ?2 and warehouse.id = ?3",
            storeId,
            productId,
            warehouseId)
        > 0;
  }

  @Override
  public AssignmentStatistics getStatistics(Long storeId, Long productId, Long warehouseId) {
    long warehousesForStoreAndProduct =
        countDistinct(
            "select count(distinct a.warehouse.id) from DbFulfilmentAssignment a "
                + "where a.store.id = ?1 and a.product.id = ?2 "
                + "and a.warehouse.archivedAt is null",
            storeId,
            productId);
    long warehousesForStore =
        countDistinct(
            "select count(distinct a.warehouse.id) from DbFulfilmentAssignment a "
                + "where a.store.id = ?1 and a.warehouse.archivedAt is null",
            storeId);
    long productsForWarehouse =
        countDistinct(
            "select count(distinct a.product.id) from DbFulfilmentAssignment a "
                + "where a.warehouse.id = ?1",
            warehouseId);
    boolean warehouseAlreadyServesStore =
        existsQuery(
            "select count(a) from DbFulfilmentAssignment a "
                + "where a.store.id = ?1 and a.warehouse.id = ?2 "
                + "and a.warehouse.archivedAt is null",
            storeId,
            warehouseId);
    boolean productAlreadyStoredInWarehouse =
        existsQuery(
            "select count(a) from DbFulfilmentAssignment a "
                + "where a.product.id = ?1 and a.warehouse.id = ?2",
            productId,
            warehouseId);
    return new AssignmentStatistics(
        warehousesForStoreAndProduct,
        warehousesForStore,
        productsForWarehouse,
        warehouseAlreadyServesStore,
        productAlreadyStoredInWarehouse);
  }

  @Override
  public void create(FulfilmentAssignment assignment) {
    DbFulfilmentAssignment entity = new DbFulfilmentAssignment();
    entity.store = Store.findById(assignment.storeId);
    entity.product = productRepository.findById(assignment.productId);
    entity.warehouse = getEntityManager().getReference(DbWarehouse.class, assignment.warehouseId);
    persist(entity);
    assignment.id = entity.id;
  }

  @Override
  public List<FulfilmentAssignment> getAll() {
    return listAll().stream().map(this::toDomain).toList();
  }

  private long countDistinct(String query, Object... parameters) {
    var typedQuery = getEntityManager().createQuery(query, Long.class);
    for (int index = 0; index < parameters.length; index++) {
      typedQuery.setParameter(index + 1, parameters[index]);
    }
    return typedQuery.getSingleResult();
  }

  private boolean existsQuery(String query, Object... parameters) {
    return countDistinct(query, parameters) > 0;
  }

  private FulfilmentAssignment toDomain(DbFulfilmentAssignment entity) {
    FulfilmentAssignment assignment = new FulfilmentAssignment();
    assignment.id = entity.id;
    assignment.storeId = entity.store.id;
    assignment.productId = entity.product.id;
    assignment.warehouseId = entity.warehouse.id;
    return assignment;
  }
}
