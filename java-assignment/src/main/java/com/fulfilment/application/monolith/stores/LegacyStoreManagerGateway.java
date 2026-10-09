package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);

  public void createStoreOnLegacySystem(Store store) {
    LOGGER.debugf("Emulating legacy-system create for store %s.", store.name);
    writeToFile(store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    LOGGER.debugf("Emulating legacy-system update for store %s.", store.name);
    writeToFile(store);
  }

  private void writeToFile(Store store) {
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile(store.name, ".txt");
      String content =
          "Store created. [ name ="
              + store.name
              + " ] [ items on stock ="
              + store.quantityProductsInStock
              + "]";
      Files.writeString(tempFile, content);
      LOGGER.debugf("Wrote legacy-system payload to temporary file %s.", tempFile);
    } catch (IOException e) {
      LOGGER.errorf(e, "Failed to emulate legacy-system synchronization for store %s.", store.name);
      throw new IllegalStateException("Legacy store synchronization failed.", e);
    } finally {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        } catch (IOException e) {
          LOGGER.errorf(e, "Failed to remove temporary legacy-sync file %s.", tempFile);
          throw new IllegalStateException("Failed to clean up legacy-sync temporary file.", e);
        }
      }
    }
  }
}
