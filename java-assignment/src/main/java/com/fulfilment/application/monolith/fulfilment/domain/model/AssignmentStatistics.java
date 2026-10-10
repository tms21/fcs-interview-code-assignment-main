package com.fulfilment.application.monolith.fulfilment.domain.model;

public class AssignmentStatistics {

  public final long warehousesForStoreAndProduct;
  public final long warehousesForStore;
  public final long productsForWarehouse;
  public final boolean warehouseAlreadyServesStore;
  public final boolean productAlreadyStoredInWarehouse;

  public AssignmentStatistics(
      long warehousesForStoreAndProduct,
      long warehousesForStore,
      long productsForWarehouse,
      boolean warehouseAlreadyServesStore,
      boolean productAlreadyStoredInWarehouse) {
    this.warehousesForStoreAndProduct = warehousesForStoreAndProduct;
    this.warehousesForStore = warehousesForStore;
    this.productsForWarehouse = productsForWarehouse;
    this.warehouseAlreadyServesStore = warehouseAlreadyServesStore;
    this.productAlreadyStoredInWarehouse = productAlreadyStoredInWarehouse;
  }
}
