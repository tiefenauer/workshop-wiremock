package info.tiefenauer.workshop.wiremock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

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

        // enter code here

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

        // enter code here

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

        // enter code here

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

        // enter code here

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

        // enter code here

        // Step 2: set scenario state to something else when POST endpoint of the external API is called

        // enter code here

        // Step 3: return a list with one item for GET /todos if Wiremock is in the scenario you set in step 2

        // enter code here

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