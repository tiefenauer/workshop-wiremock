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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
	void shouldReturnTheNumberOfTodos() throws Exception {
		// given
		List<String> todos = List.of("Buy flowers for my Wife", "Call Mum", "Clean the car");

		stubFor(WireMock.get(urlEqualTo("/todos"))
			.willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(objectMapper.writeValueAsString(todos))
			)
		);
		// when
		mockMvc.perform(get("/api/todos/count"))
			// then
			.andExpect(status().isOk())
			.andExpect(content().string(String.valueOf(todos.size())));

		// verify WireMock was actually called!
		verify(exactly(1), getRequestedFor(urlEqualTo("/todos")));
	}

	@Test
	void shouldReturnTheTodosInAlphabeticalOrder() throws Exception {
		// given
		List<String> todos = List.of("Send E-Mail to boss", "Go to sleep", "Ask Peter a favour");
		stubFor(WireMock.get(urlEqualTo("/todos"))
			.willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(objectMapper.writeValueAsString(todos))
			)
		);
		// when
		mockMvc.perform(get("/api/todos"))
			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$[0]").value("Ask Peter a favour"))
			.andExpect(jsonPath("$[1]").value("Go to sleep"))
			.andExpect(jsonPath("$[2]").value("Send E-Mail to boss"))
		;

		// verify WireMock was actually called!
		verify(exactly(1), getRequestedFor(urlEqualTo("/todos")));
	}

	@Test
	void shouldReturnTheIdOfTheAddedTodo() throws Exception {
		// given
		stubFor(WireMock.get(urlEqualTo("/todos"))
			.inScenario("Round trip: Adding an item")
			.whenScenarioStateIs(Scenario.STARTED)
			.willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(objectMapper.writeValueAsString(Collections.emptyList()))
			)
		);
		stubFor(WireMock.post(urlEqualTo("/todo"))
			.inScenario("Round trip: Adding an item")
			.whenScenarioStateIs(Scenario.STARTED)
			.willSetStateTo("todo item added")
			.willReturn(
				aResponse()
					.withStatus(HttpStatus.CREATED.value())
			)
		);
		stubFor(WireMock.get(urlEqualTo("/todos"))
			.inScenario("Round trip: Adding an item")
			.whenScenarioStateIs("todo item added")
			.willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(objectMapper.writeValueAsString(List.of("Mow the lawn")))
			)
		);

		// when
		mockMvc.perform(get("/api/todos/count"))
			.andExpect(status().isOk())
			.andExpect(content().string("0"));
		mockMvc.perform(post("/api/todo"))
			.andExpect(status().isCreated());
		mockMvc.perform(get("/api/todos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0]").value("Mow the lawn"));

		verify(exactly(1), postRequestedFor(urlEqualTo("/todo")));
		verify(exactly(2), getRequestedFor(urlEqualTo("/todos")));
	}

}