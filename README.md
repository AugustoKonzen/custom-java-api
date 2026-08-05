# Custom Java API

A lightweight, Spring-inspired Java framework built from scratch as a learning project.

**Custom Java API** is not a wrapper around Spring. It is a custom abstraction that recreates core framework ideas — annotation-driven configuration, classpath scanning, dependency injection, REST controllers, and declarative HTTP clients — using reflection, dynamic proxies, and Apache HttpComponents.

The goal was to understand *how* frameworks like Spring Boot and OpenFeign work under the hood by implementing a simplified version of those concepts myself.

---

## Why this project exists

Spring is powerful, but much of its magic can feel opaque. This project was created as a hands-on study object to answer questions such as:

- How does annotation scanning discover controllers and services?
- How can constructor-based dependency injection be implemented without a full IoC container?
- How do declarative HTTP clients (like Feign) turn an interface into real HTTP calls?
- How are request mappings, interceptors, and configuration wired together at runtime?

By building these pieces manually, the project turns framework “magic” into explicit, readable code.

---

## What it does

| Feature | Inspired by | How it works here |
|---------|-------------|-------------------|
| REST controllers | Spring MVC | Classes annotated with `@Controller` expose HTTP endpoints |
| Dependency injection | Spring IoC | Constructor injection via typed registries |
| Declarative HTTP clients | OpenFeign / `@FeignClient` | Interfaces annotated with `@HttpClient` become JDK dynamic proxies |
| Property injection | `@Value` | Fields and client base URLs resolve from `application.properties` / env vars |
| Request mapping | `@GetMapping`, `@PostMapping`, … | Meta-annotations over a shared `@RequestMapping` model |
| Client interceptors | Feign interceptors | Per-client configuration can attach request/response interceptors |

The included demo API wires a controller → service → HTTP clients against [ReqRes](https://reqres.in) and [Visual Crossing Weather](https://www.visualcrossing.com/).

---

## How it works

### Boot sequence

```
Main
 └─ ApiCreationFactory.start()
      ├─ Load application.properties (classpath)
      ├─ Scan application classes (ClassFinder)
      ├─ Create @HttpClient proxies          → ClientRegistry
      ├─ Instantiate @Service / @Controller  → ServiceRegistry / ControllerRegistry
      ├─ Register endpoint handlers
      └─ Start Apache HttpCore5 server
```

HTTP clients are created **before** dependency injection so services can receive client proxies through constructors.

### Dependency injection

Beans are discovered by annotation (`@Component`, `@Service`, `@Controller`) and registered as singletons.

Injection is **constructor-based**:

1. Resolve each constructor parameter from the matching registry (`@HttpClient`, `@Service`, `@Controller`, `@Component`)
2. Instantiate the class
3. Apply `@Value` field injection for configuration properties

### Controllers and routing

URL composition:

```text
{context-path} + {@ControllerMapping} + {method mapping}
```

Example:

```text
/api + /generic + /weather  →  /api/generic/weather
```

Each mapped method is bound to a `GenericHttpRequestHandler`, which:

- Validates the HTTP method
- Binds `@RequestBody` (JSON) and `@RequestParam`
- Serializes the return value as JSON

### Declarative HTTP clients

An interface like this:

```java
@HttpClient(
    name = "ReqRes Client",
    baseUrl = "${reqres.base-url}",
    configuration = ReqResConfig.class
)
public interface ReqresClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") int id);
}
```

is turned into a proxy that:

1. Resolves the base URL from properties
2. Builds the path with `@PathVariable` / `@RequestParam` / `@RequestBody`
3. Runs configured interceptors (auth headers, query params, etc.)
4. Executes the call with Apache HttpClient 5
5. Deserializes the JSON response into the method return type

This mirrors the Feign client model: declare the contract, configure interceptors, inject the client as a dependency.

---

## Core annotations

| Annotation | Purpose |
|------------|---------|
| `@Controller` | Marks a REST controller |
| `@ControllerMapping` | Class-level path prefix |
| `@Service` / `@Component` | Injectable application beans |
| `@HttpClient` | Declares a declarative HTTP client interface |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, … | HTTP verb + path |
| `@RequestBody`, `@RequestParam`, `@PathVariable` | Argument binding |
| `@Value` | Injects a property (supports `${ENV_VAR}` indirection) |

---

## Quick example

```java
@Controller
@ControllerMapping("/generic")
@RequiredArgsConstructor
public class GenericController {

    private final GenericService service;

    @GetMapping("/weather")
    public VisualCrossingResponse weather(@RequestParam(name = "location") String location) {
        return service.weather(location);
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class GenericService {

    private final VisualCrossingClient visualCrossingClient;

    public VisualCrossingResponse weather(String location) {
        return visualCrossingClient.getWeather(location, "today");
    }
}
```

From the outside it feels like Spring. Underneath, every step — scanning, registration, proxying, routing — is implemented in this repository.

---

## Getting started

### Requirements

- **Java 17+**
- **Maven 3.9+**

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/custom-java-api.jar
```

The server starts on port **8080** with context path `/api` (configurable via `application.properties`).

### Optional environment variables

Some demo clients require credentials:

| Variable | Used by |
|----------|---------|
| `REQRES_AUTH_TOKEN` | ReqRes client interceptor |
| `VISUAL_CROSSING_TOKEN` | Visual Crossing client interceptor |

---

## Sample endpoints

With the default context path `/api`:

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/generic/find_by_email` | Demo endpoint with JSON body |
| `GET` | `/api/generic/search_by_email` | Demo endpoint with query params |
| `GET` | `/api/generic/reqres` | Calls the ReqRes HTTP client |
| `GET` | `/api/generic/weather?location=London` | Calls the Visual Crossing HTTP client |

---

## Configuration

`application.properties` (packaged on the classpath):

```properties
server.servlet.context-path=/api
reqres.base-url=https://reqres.in
reqres.token=${REQRES_AUTH_TOKEN}
visualcrossing.api.base-url=https://weather.visualcrossing.com
visualcrossing.api.key=${VISUAL_CROSSING_TOKEN}
```

`server.port` defaults to `8080` when omitted.

Property values can reference environment variables using `${ENV_NAME}` syntax.

---

## Tech stack

- **Java 17**
- **Apache HttpComponents Core 5** — embedded HTTP server
- **Apache HttpClient 5** — outbound HTTP calls
- **Jackson** — JSON serialization / deserialization
- **Lombok** — constructor generation for demo beans
- **Maven Shade Plugin** — executable fat JAR

No Spring dependencies are used.

---

## Project structure

```text
src/
├── Main.java                 # Application entry point
├── annotations/              # Framework annotations
├── factory/                  # Boot, DI, routing, and HTTP client factories
├── handlers/                 # Inbound HTTP request handling
├── registers/                # Singleton registries (Controller, Service, Client, Component)
├── config/                   # HTTP client configuration SPI
├── interceptors/             # Default client interceptors
├── utils/                    # Classpath scanning, properties, casting helpers
├── logger/                   # Lightweight custom logger
└── api/                      # Demo application (controllers, services, clients)
application.properties        # Runtime configuration
```

---

## Design notes

- **Classpath scanning** works both from exploded class directories (IDE / `target/classes`) and from the packaged JAR.
- **Registries** keep one singleton instance per bean type, similar to a minimal application context.
- **Meta-annotations** allow verb-specific mappings (`@GetMapping`, etc.) while sharing one `@RequestMapping` model.
- This is intentionally a **learning-oriented subset** of Spring-like behavior — not a production replacement for Spring Boot.

---

## Disclaimer

Custom Java API is an educational portfolio project. It was built to explore framework internals, not to compete with mature ecosystems. APIs and capabilities are simplified on purpose so the control flow stays understandable.

If you are studying how Spring-style frameworks bootstrap applications, feel free to explore the `factory`, `registers`, and `handlers` packages — that is where most of the learning lives.
