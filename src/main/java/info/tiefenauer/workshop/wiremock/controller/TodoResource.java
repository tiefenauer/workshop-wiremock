package info.tiefenauer.workshop.wiremock.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TodoResource {

	final RestTemplate restTemplate;

	@Value("${external.api.url}")
	String externalApiUrl;

	public TodoResource(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/todos/count")
	@Operation(summary = "gets the number of todos")
	public int countTodos() {
		List<String> todos = restTemplate.exchange(externalApiUrl + "/todos", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
		}).getBody();
		return todos.size();
	}

	@GetMapping("/todos")
	@Operation(summary = "gets the number of todos")
	public List<String> getTodos() {
		List<String> todos = restTemplate.exchange(externalApiUrl + "/todos", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
		}).getBody();
		return todos.stream().sorted().toList();
	}

	@PostMapping("/todo")
	@Operation(summary = "adds a todo to the list")
	@ResponseStatus(HttpStatus.CREATED)
	public void addTodo(String todo) {
		restTemplate.postForEntity(externalApiUrl + "/todo", todo, String.class);
	}
}
