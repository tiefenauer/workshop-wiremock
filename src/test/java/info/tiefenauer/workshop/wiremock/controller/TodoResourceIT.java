package info.tiefenauer.workshop.wiremock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(
        @ConfigureWireMock(port = 8888, httpsPort = 9999)
)
@TestPropertySource(properties = {
        "external.api.url=http://localhost:8888" // re-wire external calls to Wiremock
})
class TodoResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturnTodos() throws Exception {
        // given
        List<String> todos = List.of("Buy flowers for my wife", "Call Mum", "Clean the car");

        /*
        EXERCISE 1: Stubbing a GET request
        Using Wiremock, stub the external APIso it returns above list of Todos
        see https://wiremock.org/docs/stubbing/
         */
        stubFor(WireMock.get(urlEqualTo("/todos"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(todos))
                )
        );
        /*
        END
         */
        // when
        mockMvc.perform(get("/api/todos"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", contains(todos.toArray())));

        // verify WireMock was actually called!
        verify(exactly(1), getRequestedFor(urlEqualTo("/todos")));
    }

    @Test
    void shouldReturnTodosForWorkCategory() throws Exception {
        // given
        List<String> workTodos = List.of("Send E-Mail to boss", "Prepare meeting", "Business Lunch");
        /*
        EXERCISE 2: Stubbing a GET request with query parameter
        Using Wiremock, stub the external API so it returns above list of unsorted Todos when the query param 'category' is 'work'
        Attention: you need a regex to match the URL! See https://wiremock.org/docs/request-matching/ for details
         */
        stubFor(WireMock.get(urlMatching("/todos.*"))
                .withQueryParam("category", equalTo("work"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(workTodos))
                )
        );
        /*
        END
         */
        // when
        mockMvc.perform(get("/api/todos/work"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", contains(workTodos.toArray())))
        ;

        // verify WireMock was actually called!
        verify(exactly(1), getRequestedFor(urlMatching("/todos.*")));
    }

    @Test
    void shouldHandleClientErrorsGracefully() throws Exception {
        // given
        /*
        EXERCISE 3: Simulating a server error
        Using Wiremock, stub the external API so it returns an HTTP Status 5xx if the category is 'holiday'
         */
        stubFor(WireMock.get(urlMatching("/todos.*"))
                .withQueryParam("category", equalTo("holiday"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                )
        );
        /*
        END
         */
        // when
        mockMvc.perform(get("/api/todos/holiday"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
        ;

        // verify WireMock was actually called!
        verify(exactly(1), getRequestedFor(urlMatching("/todos.*")));
    }

    @Test
    void shouldAddHolidayCategory() throws Exception {
        // given
        Category holidayCategory = new Category("holiday");
        /*
        EXERCISE 4: Stubbing a POST request using header and body matching
        Using Wiremock, stub the external API so it expects above category as JSON and returns its name
        Hint: Visit https://wiremock.org/docs/request-matching/ and read the sections about header matching and JSON Path
         */
        stubFor(WireMock.post("/todos/category") // note: urlEqualTo can be omitted
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(matchingJsonPath("$.name", equalTo(holidayCategory.name())))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withBody(holidayCategory.name())
                )
        );
        /*
        END
         */
        // when
        mockMvc.perform(post("/api/todos/category")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(holidayCategory))
                )
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(holidayCategory.name()));

        // verify WireMock was actually called!
        verify(exactly(1), postRequestedFor(urlEqualTo("/todos/category")));
    }

    @Test
    void shouldReturnTheIdOfTheAddedTodo() throws Exception {
        // given
        String scenarioName = "Round trip: Adding an item";
        /*
        Exercise 5: Stateful behavior
        Using Wiremock, simulate the state of the external API so it returns an empty list when getting all Todos initially,
        but a list containing one item after adding a todo.
        Hint: read https://wiremock.org/docs/stateful-behaviour/
         */
        // Step 1: return an empty list for for GET /todos when the scenario state is Scenario.STARTED for above scenario
        stubFor(WireMock.get(urlEqualTo("/todos"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(Collections.emptyList()))
                )
        );
        // Step 2: set scenario state to something else when POST endpoint of the external API is called
        stubFor(WireMock.post(urlEqualTo("/todo"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("todo item added")
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.CREATED.value())
                )
        );
        // Step 3: return a list with one item for GET /todos if Wiremock is in the scenario you set in step 2
        stubFor(WireMock.get(urlEqualTo("/todos"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("todo item added")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of("Mow the lawn")))
                )
        );
        /*
        END
         */

        // when
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(post("/api/todo")
                        .content("Mow the lawn"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Mow the lawn"));

        verify(exactly(1), postRequestedFor(urlEqualTo("/todo")));
        verify(exactly(2), getRequestedFor(urlEqualTo("/todos")));
    }

}