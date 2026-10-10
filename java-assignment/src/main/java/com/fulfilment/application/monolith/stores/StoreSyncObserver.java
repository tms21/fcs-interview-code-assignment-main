package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StoreSyncObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreSyncObserver.class);

  private final LegacyStoreManagerGateway legacyStoreManagerGateway;

  public StoreSyncObserver(LegacyStoreManagerGateway legacyStoreManagerGateway) {
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
  }

  public void synchronizeAfterCommit(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreSyncEvent event) {
    Store store = event.toStore();
    if (event.operation == StoreSyncEvent.Operation.CREATE) {
      legacyStoreManagerGateway.createStoreOnLegacySystem(store);
      LOGGER.infof("Created store %s and synchronized it to the legacy system.", event.name);
    } else {
      legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
      LOGGER.infof("Updated store %s and synchronized it to the legacy system.", event.name);
    }
  }
}
