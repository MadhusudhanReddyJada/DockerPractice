package rest;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import pojo.CharacterPojo;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Epic("Characters")
@Feature("REST")
@Owner("madhusudhan_reddy")
public class RestTests {
    private final String actualName ="Rick Sanchez";

    @Test
    @Story("Get character by ID")
    void getCharacterById() {
        String base = System.getProperty("baseUrl", "https://rickandmortyapi.com");

        Response resp = given()
                .baseUri(base)
                .basePath("/api/character/{id}")
                .pathParam("id", 1)
                .when().get()
                .then().statusCode(HttpURLConnection.HTTP_OK)
                .extract().response();

        Allure.addAttachment("Request", "GET /api/character/1");
        Allure.addAttachment("Response JSON", "application/json", resp.asString(), ".json");

        String name = resp.path("name");
        List<String> episodes = resp.path("episode");

        assertThat(name).isEqualTo(actualName);
        assertThat(episodes).isNotEmpty();
    }

    @Test
    @Story("Filter and pagination")
    void filterCharactersAlivePage2() {
        String base = System.getProperty("baseUrl", "https://rickandmortyapi.com");

        Response resp = given()
                .baseUri(base)
                .basePath("/api/character")
                .queryParam("status", "alive")
                .queryParam("page", 3)
                .when().get()
                .then().statusCode(HttpURLConnection.HTTP_OK)
                .extract().response();

        Allure.addAttachment("Filtered Response", "application/json", resp.asString(), ".json");

        List<String> statuses = resp.path("results.status");
        assertThat(statuses).allMatch(status -> status.equalsIgnoreCase("Alive"));

        Map<String, Object> info = resp.path("info");
        assertThat(info).isNotNull();

        String nextPage = (String) info.get("next");
        assertThat(info).isNotNull();
        assertThat(nextPage).isNotNull();
    }

    @Test
    @Story("Negative scenario")
    void getCharacterInvalidId() {
        String base = System.getProperty("baseUrl", "https://rickandmortyapi.com");

        Response resp = given()
                .baseUri(base)
                .basePath("/api/character/{id}")
                .pathParam("id", 999999)
                .when().get()
                .then().statusCode(HttpURLConnection.HTTP_NOT_FOUND)
                .extract().response();

        Allure.addAttachment("Negative Response", "application/json", resp.asString(), ".json");

        String errorMessage = resp.path("error");
        assertThat(errorMessage).isNotEmpty();
    }

    @Test
    @Story("Contract and Schema check (basic) - Lombok POJO")
    void verifyCharacterSchemaUsingPOJO() {
        String base = System.getProperty("baseUrl", "https://rickandmortyapi.com");

        Response resp = given()
                .baseUri(base)
                .basePath("/api/character/{id}")
                .pathParam("id", 1)
                .when().get()
                .then().statusCode(HttpURLConnection.HTTP_OK)
                .extract().response();

        Allure.addAttachment("Schema Validation Response", "application/json", resp.asString(), ".json");

        // Deserialize into Lombok POJO
        CharacterPojo pojo = resp.as(CharacterPojo.class);

        // Attach the Lombok-generated toString() for quick debug
        Allure.addAttachment("Deserialized POJO", pojo.toString());


        assertAll(
                () -> assertThat(pojo.getId()).isGreaterThan(0),
                () -> assertThat(pojo.getName()).isNotEmpty(),
                () -> assertThat(pojo.getStatus()).isNotEmpty(),
                () -> assertThat(pojo.getSpecies()).isNotEmpty(),
                () -> assertThat(pojo.getGender()).isNotEmpty(),
                () -> assertThat(pojo.getOrigin()).isNotNull(),
                () ->assertThat(pojo.getOrigin().getName()).isNotEmpty(),
                () -> assertThat(pojo.getLocation()).isNotNull(),
                () -> assertThat(pojo.getLocation().getName()).isNotEmpty(),
                () ->assertThat(pojo.getEpisode()).isNotEmpty(),
                () -> assertThat(pojo.getImage()).isNotEmpty(),
                () -> assertThat(pojo.getUrl()).isNotEmpty(),
                () -> assertThat(pojo.getCreated()).isNotEmpty()
        );
    }
}
