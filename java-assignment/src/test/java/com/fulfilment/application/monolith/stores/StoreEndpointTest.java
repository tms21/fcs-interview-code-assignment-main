package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  @Test
  public void createsUpdatesAndDeletesStore() {
    String name = "MALM-" + System.nanoTime();
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + name + "\",\"quantityProductsInStock\":4}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(name), "quantityProductsInStock", equalTo(4))
            .extract()
            .path("id");

    given().when().get("/store/{id}", id).then().statusCode(200).body("name", equalTo(name));

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"" + name + "-updated\",\"quantityProductsInStock\":7}")
        .when()
        .put("/store/{id}", id)
        .then()
        .statusCode(200)
        .body("name", equalTo(name + "-updated"), "quantityProductsInStock", equalTo(7));

    given().when().delete("/store/{id}", id).then().statusCode(204);
  }

  @Test
  public void rejectsStoreNameMissingOnUpdate() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\": 1}")
        .when()
        .put("/store/1")
        .then()
        .statusCode(422);
  }
}
