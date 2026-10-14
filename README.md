# Java Fulfilment Assignment

A Quarkus application that demonstrates REST APIs, persistence, domain use cases, and tests for stores, products, locations, warehouses, and fulfilment assignments.

The assignment requirements and business rules are documented in [CODE_ASSIGNMENT.md](CODE_ASSIGNMENT.md).

## Requirements

- JDK 17 or later. Use a JDK version supported by the project's Quarkus and Hibernate dependencies.
- PostgreSQL for running the application and integration tests.
- Docker is optional if you use another local PostgreSQL installation.

Set `JAVA_HOME` to the JDK installation and ensure its `bin` directory is on `PATH`.

## Database setup

The `dev` and `test` datasource profiles are configured to connect to PostgreSQL at `localhost:5432`, and Dev Services are disabled. Start a local PostgreSQL instance with a database and credentials that match `src/main/resources/application.properties`.

For example, with Docker:

```sh
docker run --rm --name fulfilment-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=local_dev_password \
  -e POSTGRES_DB=quarkus_test \
  -p 5432:5432 \
  postgres:13.3
```

Set the password for the active datasource profile in `src/main/resources/application.properties` to the same local development password, and update the settings if your database uses a different username, database name, or port. Hibernate creates the schema and loads the sample data from `src/main/resources/import.sql`.

## Build and test

From this directory, build the application with:

```sh
./mvnw package
```

On Windows PowerShell, use `.\mvnw.cmd package`.

Run all tests with:

```sh
./mvnw test
```

The fulfilment domain and use-case unit tests do not require PostgreSQL:

```sh
./mvnw -Dtest=FulfilmentAssignmentValidatorTest,AssignWarehouseToProductForStoreTest test
```

Quarkus endpoint and integration tests use the configured test datasource, so PostgreSQL must be running for those tests.

## Run locally

Start Quarkus in development mode:

```sh
./mvnw quarkus:dev
```

The application is available at <http://localhost:8080>. The generated warehouse API documentation and UI are available from the OpenAPI resources included by the project.

To run the packaged application:

```sh
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## Fulfilment assignment API

Fulfilment follows a ports-and-adapters structure under `src/main/java/com/fulfilment/application/monolith/fulfilment`:

- `domain/model`, `domain/port`, `domain/validator`, and `domain/exception` contain the assignment model, persistence boundary, business constraints, and domain errors.
- `application/usecase` coordinates assignment creation and retrieval without depending on REST or database implementations.
- `adapters/restapi` maps HTTP requests and responses to the use case.
- `adapters/database` implements the persistence port using JPA and Panache.

Create an assignment by linking an existing store, product, and active warehouse:

```http
POST /fulfilment/assignments
Content-Type: application/json

{
  "storeId": 1,
  "productId": 1,
  "warehouseId": 1
}
```

The API returns `201 Created` with the created assignment. List assignments with `GET /fulfilment/assignments`.

The API enforces these limits:

- A product can be assigned to at most two different warehouses for one store.
- A store can use at most three different warehouses.
- A warehouse can be assigned at most five different products.

Invalid IDs return `400`, missing store/product/active warehouse references return `404`, and duplicate assignments or limit violations return `409`.
