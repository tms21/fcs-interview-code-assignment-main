package com.fulfilment.application.monolith.stores;

public class StoreSyncEvent {

  public enum Operation {
    CREATE,
    UPDATE
  }

  public final Operation operation;
  public final Long id;
  public final String name;
  public final int quantityProductsInStock;

  public StoreSyncEvent(
      Operation operation, Long id, String name, int quantityProductsInStock) {
    this.operation = operation;
    this.id = id;
    this.name = name;
    this.quantityProductsInStock = quantityProductsInStock;
  }

  public Store toStore() {
    Store store = new Store(name);
    store.id = id;
    store.quantityProductsInStock = quantityProductsInStock;
    return store;
  }
}
