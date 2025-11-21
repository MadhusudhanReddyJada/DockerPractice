package restvsgraphql;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.Assertions;

@Epic("Characters")
@Feature("REST vs GraphQL")
@Owner("madhusudhan_reddy")
public class RestVSGraphQL {

    record Query(String query, Map<String, Object> variables) {
    }

    private static final String BASE = System.getProperty("baseUrl", "https://rickandmortyapi.com");

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @Story("Cross-Verification: REST vs GraphQL")
    void compareRestAndGraphqlById(int id) {
        //REST
        Response restResp = given().baseUri(BASE).basePath("/api/character/{id}").pathParam("id", id).when().get().then().statusCode(200).extract().response();

        Allure.addAttachment("REST response for id=" + id, "application/json", restResp.asString(), ".json");

        String restName = restResp.path("name");
        String restStatus = restResp.path("status");
        List<String> restEpisodes = restResp.path("episode");

        //GraphQL
        String q = """
                    query($id:ID!){
                      character(id:$id){ id name status episode{ id } }
                    }
                """;
        var body = new Query(q, Map.of("id", id));

        Response gqlResp = given().baseUri(BASE).basePath("/graphql").contentType("application/json").body(body).when().post().then().statusCode(200).extract().response();

        Allure.addAttachment("GraphQL response for id=" + id, "application/json", gqlResp.asString(), ".json");

        Object errors = gqlResp.path("errors");
        Assertions.assertThat(errors).isNull();

        String graphqlName = gqlResp.path("data.character.name");
        String graphqlStatus = gqlResp.path("data.character.status");
        @SuppressWarnings("unchecked") List<Map<String, Object>> graphqlEpisodes = gqlResp.path("data.character.episode");

        int restEpisodeCount = restEpisodes == null ? 0 : restEpisodes.size();
        int graphqlEpisodeCount = graphqlEpisodes == null ? 0 : graphqlEpisodes.size();
        System.out.println(graphqlEpisodeCount + " " + restEpisodeCount);
        assertAll(
                () ->assertThat(graphqlName).isEqualTo(restName),
                () ->assertThat(graphqlStatus).isEqualTo(restStatus),
                () ->assertThat(graphqlEpisodeCount).isEqualTo(restEpisodeCount)
        );

    }
}
