package info.tiefenauer.workshop.wiremock.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TodoResource {

    private final RestClient restClient;

    @Value("${external.api.url}")
    private String externalApiUrl;

    @GetMapping("/todos")
    @Operation(summary = "gets a list of todos")
    public List<String> getTodos() {
        return restClient.get()
                .uri(externalApiUrl + "/todos")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<String>>() {
                })
                .getBody();
    }

    @GetMapping("/todos/{category}")
    @Operation(summary = "Gets the todos for a given category")
    public List<String> getTodosByCategory(@PathVariable String category) {
        try {
            return restClient.get()
                    .uri(externalApiUrl + "/todos?category=" + category)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<String>>() {
                    })
                    .getBody()
                    .stream()
                    .toList();
        } catch (
                RestClientResponseException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                // default to empty string if service is unavailable
                return Collections.emptyList();
            }
            throw ex; // rethrow other errors (like 4xx)
        }
    }

    @PostMapping("/todos/category")
    @Operation(summary = "add a new category")
    public String addCategory(@RequestBody Category category) {
        return restClient.post()
                .uri(externalApiUrl + "/todos/category")
                .body(category)
                .retrieve()
                .toEntity(String.class)
                .getBody();
    }

    @PostMapping("/todo")
    @Operation(summary = "adds a todo to the list")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTodo(@RequestBody String todo) {
        restClient.post()
                .uri(externalApiUrl + "/todo")
                .body(todo).retrieve().toBodilessEntity();
    }
}
