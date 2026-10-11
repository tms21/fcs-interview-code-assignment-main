package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StoreSyncObserverTest {

  @Test
  public void createEventMapsStateIntoLegacyFriendlyRecord() {
    StoreSyncEvent event =
        new StoreSyncEvent(StoreSyncEvent.Operation.CREATE, 11L, "BILLY", 16);

    Store store = event.toStore();

    assertEquals(11L, store.id);
    assertEquals("BILLY", store.name);
    assertEquals(16, store.quantityProductsInStock);
  }

  @Test
  public void synchronizeAfterCommitDoesNotFailForCreateOrUpdate() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    StoreSyncObserver observer = new StoreSyncObserver(gateway);

    assertDoesNotThrow(
        () -> observer.synchronizeAfterCommit(new StoreSyncEvent(StoreSyncEvent.Operation.CREATE, 1L, "A", 3)));
    assertDoesNotThrow(
        () -> observer.synchronizeAfterCommit(new StoreSyncEvent(StoreSyncEvent.Operation.UPDATE, 2L, "B", 5)));
  }
}
