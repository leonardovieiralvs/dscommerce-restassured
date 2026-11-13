package com.devlsouza.dscommerce_restassured.controllers;

import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProductControllerTest {

    private Long existingProductId, nonExistingProductId;
    private String productName;
    private String adminToken;
    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setUp() {
        baseURI = "http://localhost:8080";

        productName = "Macbook";
        adminToken = "eyJraWQiOiIwODU5MTMzNi1mZTMwLTQ3MzQtOGE5Ny0xZDIzNzI5Mzc1ODMiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJteWNsaWVudGlkIiwiYXVkIjoibXljbGllbnRpZCIsIm5iZiI6MTc5NDQ3OTEwNSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiZXhwIjoxNzk0NTY1NTA1LCJpYXQiOjE3OTQ0NzkxMDUsImp0aSI6IjJiNThmZTEyLWJiMWYtNDJjNy04ZGE3LTMxY2YwOGVkMDM4ZCIsImF1dGhvcml0aWVzIjpbIlJPTEVfQ0xJRU5UIl0sInVzZXJuYW1lIjoibWFyaWFAZ21haWwuY29tIn0.dh_eVsCtnklPsbMMAZKy7j5rSgii__P1-z7UUA0FCfOlRHNikJa9w4jk97R0tLBso5Fh6ujU5g5hXOq2BGJWUQQd4RFF3kSIrTY6M84tmYGuUHr14MSj0jnKmkHoEYq51_hwzgTGKfY-z3DEgK1HHOwlZO1wcNPggkfFHC25NtTDAvIYbE3ABJacLlFtWzGwo_UoBFa7nMqwGxneiI3xV6iuoV8_SyXvF5TIKqAEOyavXtDCUmBwzPx1YSK5dzSLh9WyrmLNsTZZSt6FiBFhAmgurKU0ldrc4SOGEN_OyjHb_8YvjEsw5SBCU-8y_L9f4ryHmKTrmaYE4SO9NpPILA";
        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("price", 50.0);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> categories1 = new HashMap<>();
        categories1.put("id", 2);
        Map<String, Object> categories2 = new HashMap<>();
        categories2.put("id", 3);

        categories.add(categories1);
        categories.add(categories2);

        postProductInstance.put("categories", categories);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {

        existingProductId = 2L;
        nonExistingProductId = 100L;

        given()
                .get("/products/{id}", existingProductId)
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0F))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"));

    }

    @Test
    public void findAllShouldReturnPageProductWhenProductNameIsEmpty() {

        existingProductId = 2L;
        nonExistingProductId = 100L;


        given()
                .get("/products")
                .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
    }

    @Test
    public void findAllShouldReturnPageProductWhenProductNameIsNotEmpty() {

        given()
                .get("/products?name={productName}", productName)
                .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0F))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
    }

    @Test
    public void findAllShouldReturnPageProductsWithPriceGreaterThan2000() {
        given()
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll { it.price > 2000 }.name", hasItems("Smart TV", "PC Gamer Weed"));
    }

    @Test
    public void insertShouldReturnProductCreatedWhenAdminLogged() {
        JSONObject newObject = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newObject)
                .contentType("application/json")
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Meu Produto"))
                .body("price", is(50.0))
                .body("imgUrl", equalTo("Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim"))
                .body("categories", hasItems(2, 3));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {

        postProductInstance.put("description", "lorem ipsum");
        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newObject)
                .contentType("application/json")
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsNegative() {

        postProductInstance.put("price", -50.0);
        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newObject)
                .contentType("application/json")
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {

        postProductInstance.put("categories", null);
        JSONObject newObject = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newObject)
                .contentType("application/json")
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }
}
