# workshop-wiremock
Repository containing code samples for my workshop on Wiremock.

## Code samples
Coming Soon

## Exercises
This repository contains a simple application ([TodoApplication.java](src/main/java/info/tiefenauer/workshop/wiremock/TodoApplication.java)) that simulates a REST-API that could be used for a basic todo list application.

To keep things simple and provide a generic example for integration testing with wiremock, the TodoApplication only contains minimal logic itself - it simply relays requests made to its API to an imaginary external REST API. The [TodoResourceIT.java](src/test/java/info/tiefenauer/workshop/wiremock/controller/TodoResourceIT.java) contains integration test for the TodoApplication. In order to work, the external API needs to be mocked using Wiremock. Your goal is to make all the integration tests pass.

THe `solutions` branch contains example solutions. If you need help, read the [Wiremock Docs](https://wiremock.org/docs/).
 

## Slides
Coming soon

## Demo application

The Swagger doc for the API can be found here: http://localhost:8080/swagger-ui.html
