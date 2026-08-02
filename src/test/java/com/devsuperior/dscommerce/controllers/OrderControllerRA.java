package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private Long existingOrderId, nonExistingOrderId;
    private String clientToken, adminToken, invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken =  adminToken + "xpto"; // simulates invalid token
    }

    @Test
    public void findByIdShouldReturnOrderWhenOrderIdExistsAndAdminLogged() {
        existingOrderId = 1L;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("client.id", is(1))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items[0].name", equalTo("The Lord of the Rings"))
                .body("items[0].price", is(90.5F))
                .body("items[1].name", equalTo("Macbook Pro"))
                .body("items[1].price", is(1250.0F))
                .body("items.quantity", hasItems(1, 2))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnOrderWhenClientLoggedAndOrderBelongsToClient() {
        existingOrderId = 1L;

        given()
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("client.id", is(1))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items[0].name", equalTo("The Lord of the Rings"))
                .body("items[0].price", is(90.5F))
                .body("items[1].name", equalTo("Macbook Pro"))
                .body("items[1].price", is(1250.0F))
                .body("items.quantity", hasItems(1, 2))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenClientLoggedAndOrderDoesNotBelongToClient() {
        existingOrderId = 2L;

        given()
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(403)
                .body("status", is(403))
                .body("error", equalTo("Access denied. Should be self or admin"))
                .body("path", equalTo("/orders/2"));
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenAdminLoggedAndNonExistingOrderId() {
        nonExistingOrderId = 100L;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("status", is(404))
                .body("error", equalTo("Recurso não encontrado"))
                .body("path", equalTo("/orders/100"));
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenClientLoggedAndNonExistingOrderId() {
        nonExistingOrderId = 100L;

        given()
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("status", is(404))
                .body("error", equalTo("Recurso não encontrado"))
                .body("path", equalTo("/orders/100"));
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenInvalidToken() {
        existingOrderId = 1L;

        given()
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(401);
    }
}
