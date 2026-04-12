package com.example.usermanagement;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class UserControllerTest extends IntegrationTest {

    private int registerUser(String username) {
        return given()
            .body(Map.of("username", username, "pwd", "123456"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    // === Task 1: POST /api/users ===

    @Test
    void should_register_user_successfully() {
        given()
            .body(Map.of("username", "john", "pwd", "123456"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("username", equalTo("john"))
            .body("pwd", equalTo(null));
    }

    @Test
    void should_return_400_when_username_is_blank() {
        given()
            .body(Map.of("username", "", "pwd", "123456"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(400);
    }

    @Test
    void should_return_400_when_pwd_is_blank() {
        given()
            .body(Map.of("username", "nopwd", "pwd", ""))
        .when()
            .post("/api/users")
        .then()
            .statusCode(400);
    }

    @Test
    void should_return_400_when_pwd_too_short() {
        given()
            .body(Map.of("username", "short1", "pwd", "123"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(400);
    }

    @Test
    void should_return_409_when_username_exists() {
        given()
            .body(Map.of("username", "duplicate", "pwd", "123456"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(201);

        given()
            .body(Map.of("username", "duplicate", "pwd", "654321"))
        .when()
            .post("/api/users")
        .then()
            .statusCode(409);
    }

    // === Task 2: GET /api/users/{id} ===

    @Test
    void should_get_user_by_id() {
        int id = registerUser("getme");
        given()
        .when()
            .get("/api/users/{id}", id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("username", equalTo("getme"))
            .body("pwd", equalTo(null));
    }

    @Test
    void should_return_404_when_user_not_found() {
        given()
        .when()
            .get("/api/users/{id}", 99999)
        .then()
            .statusCode(404);
    }

    // === Task 3: GET /api/users ===

    @Test
    void should_list_all_users() {
        registerUser("list1");
        registerUser("list2");
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    // === Task 4: PUT /api/users/{id} ===

    @Test
    void should_update_pwd_successfully() {
        int id = registerUser("updateme");
        given()
            .body(Map.of("pwd", "newpwd123"))
        .when()
            .put("/api/users/{id}", id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("username", equalTo("updateme"));
    }

    @Test
    void should_return_400_when_new_pwd_too_short() {
        int id = registerUser("short2");
        given()
            .body(Map.of("pwd", "123"))
        .when()
            .put("/api/users/{id}", id)
        .then()
            .statusCode(400);
    }

    @Test
    void should_return_404_when_updating_nonexistent_user() {
        given()
            .body(Map.of("pwd", "newpwd123"))
        .when()
            .put("/api/users/{id}", 99999)
        .then()
            .statusCode(404);
    }

    // === Task 5: DELETE /api/users/{id} ===

    @Test
    void should_delete_user_successfully() {
        int id = registerUser("deleteme");
        given()
        .when()
            .delete("/api/users/{id}", id)
        .then()
            .statusCode(204);
    }

    @Test
    void should_return_404_when_deleting_nonexistent_user() {
        given()
        .when()
            .delete("/api/users/{id}", 99999)
        .then()
            .statusCode(404);
    }
}
