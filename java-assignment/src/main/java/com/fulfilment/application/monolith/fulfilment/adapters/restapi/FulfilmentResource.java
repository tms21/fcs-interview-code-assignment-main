package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.application.usecase.AssignWarehouseToProductForStore;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentAssignmentConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentReferenceNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentRequestException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment/assignments")
@ApplicationScoped
@Consumes("application/json")
@Produces("application/json")
public class FulfilmentResource {

  @Inject AssignWarehouseToProductForStore assignWarehouseToProductForStore;

  @GET
  @Transactional
  public List<FulfilmentAssignmentResponse> getAll() {
    return assignWarehouseToProductForStore.getAll().stream()
        .map(FulfilmentAssignmentResponse::from)
        .toList();
  }

  @POST
  @Transactional
  public Response assign(FulfilmentAssignmentRequest request) {
    FulfilmentAssignment assignment = new FulfilmentAssignment();
    if (request != null) {
      assignment.storeId = request.storeId;
      assignment.productId = request.productId;
      assignment.warehouseId = request.warehouseId;
    }
    try {
      FulfilmentAssignment created = assignWarehouseToProductForStore.assign(assignment);
      return Response.status(Response.Status.CREATED)
          .entity(FulfilmentAssignmentResponse.from(created))
          .build();
    } catch (FulfilmentRequestException exception) {
      throw new WebApplicationException(exception.getMessage(), 400);
    } catch (FulfilmentReferenceNotFoundException exception) {
      throw new WebApplicationException(exception.getMessage(), 404);
    } catch (FulfilmentAssignmentConflictException exception) {
      throw new WebApplicationException(exception.getMessage(), 409);
    }
  }
}
