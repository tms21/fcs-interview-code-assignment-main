package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointTest {

  @Test
  public void createsRetrievesAndArchivesWarehouse() {
    String id =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "businessUnitCode": "MWH.TEST",
                  "location": "VETSBY-001",
                  "capacity": 70,
                  "stock": 20
                }
                """)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .body("businessUnitCode", equalTo("MWH.TEST"))
            .extract()
            .path("id");

    given()
        .when()
        .get("/warehouse/{id}", id)
        .then()
        .statusCode(200)
        .body("id", equalTo(id), "location", equalTo("VETSBY-001"));

    given().when().delete("/warehouse/{id}", id).then().statusCode(204);

    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"))
        .body(org.hamcrest.Matchers.not(containsString("MWH.TEST")));
  }

  @Test
  public void replacesWarehouseAndArchivesPreviousRecord() {
    String replacementId =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "location": "TILBURG-001",
                  "capacity": 35,
                  "stock": 27
                }
                """)
            .when()
            .post("/warehouse/MWH.023/replacement")
            .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("MWH.023"), "stock", equalTo(27))
            .extract()
            .path("id");

    assertNotEquals("3", replacementId);
    given().when().get("/warehouse/3").then().statusCode(404);
    given()
        .when()
        .get("/warehouse/{id}", replacementId)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.023"));
  }
}
