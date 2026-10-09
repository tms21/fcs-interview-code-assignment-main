package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class WarehouseCreationStatusFilter implements ContainerResponseFilter {

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    String path = requestContext.getUriInfo().getPath();
    if ("POST".equals(requestContext.getMethod())
        && "warehouse".equals(path.replaceFirst("^/+", ""))) {
      responseContext.setStatus(201);
    }
  }
}
