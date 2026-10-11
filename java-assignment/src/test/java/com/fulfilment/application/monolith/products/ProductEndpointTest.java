package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    given().when().delete(path + "/1").then().statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testCreateAndUpdateProduct() {
    String name = "BILLY-" + System.nanoTime();
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"name\":\""
                    + name
                    + "\",\"description\":\"Adjustable bookcase\",\"price\":129.99,\"stock\":12}")
            .when()
            .post("product")
            .then()
            .statusCode(201)
            .body("name", equalTo(name), "stock", equalTo(12))
            .extract()
            .path("id");

    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"name\":\""
                + name
                + "\",\"description\":\"Updated case\",\"price\":149.99,\"stock\":18}")
        .when()
        .put("product/{id}", id)
        .then()
        .statusCode(200)
        .body("description", equalTo("Updated case"), "stock", equalTo(18));

    given().when().get("product/{id}", id).then().statusCode(200).body("name", equalTo(name));
  }

  @Test
  public void testRejectsInvalidProductUpdate() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"description\":\"missing name\"}")
        .when()
        .put("product/1")
        .then()
        .statusCode(422);
  }
}
