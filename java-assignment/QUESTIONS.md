# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes. The current code mixes Panache's active-record style, where entities such as Store perform persistence operations themselves, with the repository pattern used for Product and Warehouse. Both approaches can work, but using several styles in one small application makes the code harder to navigate, test, and maintain.

I would favor repositories for persistence operations and keep entities focused on representing data and domain behavior. This makes database access explicit, gives services/use cases a seam that can be mocked in unit tests, and helps keep business rules out of REST resources. I would avoid a large rewrite solely for consistency: I would migrate one area at a time, add tests around existing behavior, and use the same approach for new features. The choice should remain pragmatic; Panache repositories are useful here and there is no need to add extra abstraction where it provides no value.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Generating an API from an OpenAPI specification gives clients and server code a clear contract, supports documentation and client generation, and helps keep request/response shapes consistent. It is especially useful when several teams or clients depend on the API. The trade-offs are an extra build step, generated code that can be less convenient to customize, and a need to keep the specification and implementation aligned.

Writing endpoints directly is quick and gives developers full control, which can suit a small internal API. Over time, though, the contract may be less visible, documentation can drift, and changes can become inconsistent across endpoints.

For this project, I would use OpenAPI for externally consumed or stable APIs, including Warehouse, and generate only the contract-facing types/interfaces while keeping business logic in handwritten adapters and use cases. For small internal endpoints, direct implementation is reasonable, but I would still document and test the contract. If Product and Store become public or are used by multiple clients, I would bring them under OpenAPI as well.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize tests around business rules and high-risk behavior first: warehouse creation, replacement, archive/history, location and capacity limits, and the Store legacy-system call occurring only after a successful database commit. I would cover common success cases as well as boundary and failure cases such as unknown locations, duplicate business-unit codes, insufficient capacity, missing records, and transaction rollback.

I would use unit tests for domain use cases with repository/location stubs, integration tests for database persistence and transaction behavior, and a smaller set of REST endpoint tests to verify HTTP status codes, serialization, and the API contract. Tests should be deterministic and isolated; integration tests should use a disposable database (such as the PostgreSQL service in CI) rather than depending on a developer's local setup.

To keep coverage useful, I would enforce a coverage threshold on critical business logic, publish the JaCoCo report in CI, and review uncovered branches when behavior changes. Coverage is a signal, not a substitute for meaningful assertions, so I would also require tests for new business rules and protect important regressions in code review. A fast unit-test suite can run on every push, with broader integration checks in CI before merging.
```