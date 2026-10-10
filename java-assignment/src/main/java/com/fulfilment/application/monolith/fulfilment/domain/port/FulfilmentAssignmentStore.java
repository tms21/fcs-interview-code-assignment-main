package com.fulfilment.application.monolith.fulfilment.domain.port;

import com.fulfilment.application.monolith.fulfilment.domain.model.AssignmentStatistics;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import java.util.List;

public interface FulfilmentAssignmentStore {

  boolean lockStore(Long storeId);

  boolean lockProduct(Long productId);

  boolean lockActiveWarehouse(Long warehouseId);

  boolean exists(Long storeId, Long productId, Long warehouseId);

  AssignmentStatistics getStatistics(Long storeId, Long productId, Long warehouseId);

  void create(FulfilmentAssignment assignment);

  List<FulfilmentAssignment> getAll();
}
