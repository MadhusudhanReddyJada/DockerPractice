package graphql;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

@Epic("Characters")
@Feature("GraphQL")
@Owner("madhusudhan_reddy")
public class GraphqlTests {
    private final String actualName ="Rick Sanchez";
    record Query(String query, Map<String, Object> variables) {}

    @Test
    @Story("Character by ID")
    void characterByIdGraphQL() {
        record Query(String query, Map<String,Object> variables) {}

        String q = """ 
        query($id:ID!){ 
          character(id:$id){ id name status episode{ id } } 
        } 
    """;

        var body = new Query(q, Map.of("id", 1));

        var resp = given()
                .baseUri("https://rickandmortyapi.com/graphql")
                .contentType("application/json")
                .body(body)
                .when().post()
                .then().statusCode(200)
                .extract().response();

        Allure.addAttachment("GraphQL response", "application/json", resp.asString(), ".json");
        Object errors = resp.path("errors");
        assertThat(errors).isNull();

        String name = resp.path("data.character.name");
        List<Object> episodes = resp.path("data.character.episode");
        assertThat(name).isEqualTo(actualName);
        assertThat(episodes).isNotEmpty();

    }

    @Test
    @Story("Filtered list query")
    void filterCharactersGraphQL() {
        String q = """
        query($page:Int,$status:String){
          characters(page:$page, filter:{status:$status}) {
            info { next }
            results { id name status }
          }
        }
    """;

        var body = new Query(q, Map.of("page", 2, "status", "alive"));

        Response resp = given()
                .baseUri("https://rickandmortyapi.com")
                .basePath("/graphql")
                .contentType("application/json")
                .body(body)
                .when().post()
                .then().statusCode(HttpURLConnection.HTTP_OK)
                .extract().response();

        Allure.addAttachment("Filtered GraphQL Response", "application/json", resp.asString(), ".json");

        Object errors = resp.path("errors");
        Assertions.assertThat(errors).isNull();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = resp.path("data.characters.results");
        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isNotEmpty();

        for (Map<String, Object> r : results) {
            Object statusObj = r.get("status");
            Assertions.assertThat(statusObj).isNotNull();
            Assertions.assertThat(statusObj.toString()).matches(s -> s.equalsIgnoreCase("Alive"));
        }

        Object infoNode = resp.path("data.characters.info");
        Assertions.assertThat(infoNode).isNotNull();
        Object nextPage = resp.path("data.characters.info.next");
//        Object pages = resp.path("data.characters.info.pages");
        Assertions.assertThat(nextPage).isNotNull();
    }

    @Test
    @Story("Negative GraphQL scenario")
    void negativeCharacterIdGraphQL() {
        String q = """
        query($id:ID!){
          character(id:$id){ id name status episode{ id } }
        }
    """;

        var body = new Query(q, Map.of("id", 999999));

        Response resp = given()
                .baseUri("https://rickandmortyapi.com")
                .basePath("/graphql")
                .contentType("application/json")
                .body(body)
                .when().post()
                .then().statusCode(HttpURLConnection.HTTP_OK)
                .extract().response();

        Allure.addAttachment("Negative GraphQL Response", "application/json", resp.asString(), ".json");

        Object errorsNode = resp.path("errors");
        Object characterNode = resp.path("data.character");

        if (errorsNode != null) {
            @SuppressWarnings("unchecked")
            List<Object> errors = (List<Object>) errorsNode;
            Assertions.assertThat(errors).isNotEmpty();
        } else {
            Assertions.assertThat(characterNode).isNull();
        }
    }

}
