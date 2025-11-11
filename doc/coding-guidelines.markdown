# Coding Guidelines for Hyper-Flexible Business Simulator

## Overview
- **Purpose**: This document outlines the coding conventions, best practices, and testing strategies for the Hyper-Flexible Business Simulator project. It ensures consistency, readability, maintainability, and high-quality code across the Java-based simulator. The project uses IntelliJ IDEA as the IDE, Maven for build management, and focuses on unit and integration tests to validate dynamic entity loading from Google Spreadsheets, lifecycles, events, and financial outputs.
- **Scope**: Applies to all Java code, including core components (e.g., BaseEntity, SheetsLoader, Simulator), utilities (e.g., ExpressionEvaluator), and tests. Follow Java 17+ features where appropriate (e.g., records for data classes, sealed classes for hierarchies if needed).
- **References**: Base on Google Java Style Guide for formatting, with customizations for this project. Use SonarLint in IntelliJ for static analysis.

## Libraries, Frameworks, and Components
- **Core Language and Runtime**: Java 17+ (leverage features like records for immutable data classes such as Event or Attribute, sealed classes for controlled entity hierarchies if extended, and pattern matching for switch in expression evaluation).
- **Build and Dependency Management**: Maven (use pom.xml to manage dependencies, builds, and plugins like JaCoCo for coverage reporting).
- **External APIs and Integrations**:
  - Google Sheets API and Google Drive API (for discovering and loading spreadsheets dynamically; use the official Google APIs Client Library for Java to handle authentication and data retrieval).
  - javax.script (built-in Java library for evaluating dynamic expressions in JS-like syntax within the ExpressionEvaluator component).
- **Data Processing and Utilities**:
  - Jackson (for JSON parsing and serialization, e.g., handling Outputs in CaptureDeletion tab or exporting financial reports as JSON/CSV).
  - SLF4J (for logging warnings, errors, and debug info across components like SheetsLoader and Simulator; pair with Logback as the implementation for configurable logging).
- **Testing Frameworks**:
  - JUnit 5 (for writing and running unit and integration tests, including parameterized tests for expression variants and edge cases).
  - Mockito (for mocking dependencies such as Google APIs in SheetsLoader tests or Simulator interactions in Event tests).
- **Other Components**:
  - java.time (built-in for handling dates and time-based logic in events, lifecycles, and simulations, e.g., calculating cycles or due dates).
  - java.util.concurrent (for thread-safe structures like PriorityQueue in Simulator for event handling, and ConcurrentHashMap for entity maps if multi-threading is introduced).
- **Dependency Guidelines**: Pin specific versions in pom.xml (e.g., com.google.api-client:google-api-client:2.2.0, com.fasterxml.jackson.core:jackson-databind:2.15.0, org.junit.jupiter:junit-jupiter:5.10.0, org.mockito:mockito-core:5.5.0). Avoid snapshot versions in production builds. Use Maven's dependency management to resolve conflicts and ensure compatibility.

## Coding Conventions
- **Naming Conventions**:
  - **Classes**: PascalCase (e.g., `BaseEntity`, `SheetsLoader`).
  - **Methods**: camelCase (e.g., `updateAttribute`, `enqueueEvent`).
  - **Variables/Fields**: camelCase (e.g., `currentTime`, `eventQueue`). Use descriptive names; avoid abbreviations unless common (e.g., `maxCapacity` not `maxCap`).
  - **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_CAPACITY_DEFAULT`).
  - **Packages**: Lowercase, dot-separated (e.g., `com.simulator.entities`, `com.simulator.utils`).
  - **Tests**: Class names end with `Test` (e.g., `SheetsLoaderTest`); methods describe behavior (e.g., `testMergeWithExtends`).

- **Formatting and Style**:
  - **Indentation**: 4 spaces (no tabs). Configure IntelliJ to enforce this.
  - **Line Length**: 100 characters max; wrap logically.
  - **Braces**: K&R style (opening brace on same line). Always use braces for if/loop bodies, even single-line.
  - **Imports**: Sort alphabetically; avoid wildcard (*) imports. Use IntelliJ's Optimize Imports.
  - **Comments**: Javadoc for public methods/classes (e.g., `@param`, `@return`). Inline // for explanations. No TODO without issue tracker reference.
  - **Exceptions**: Use checked exceptions for recoverable errors (e.g., IOException in loader); unchecked for programming errors (e.g., IllegalStateException for cycle in extends).
  - **Java Features**: Prefer streams/lambdas for collections (e.g., `entities.values().stream().filter(...)`). Use records for immutable data (e.g., Event if simple).

- **Error Handling and Logging**:
  - Log warnings/errors using SLF4J (add dep if needed); e.g., `logger.warn("Skipping invalid sheet: {}", sheetId)`.
  - Handle exceptions gracefully (e.g., in evaluator, catch ScriptException and default to null/log).
  - Validate inputs in loader (e.g., check for cyclic extends).

- **Best Practices**:
  - **Immutability**: Prefer immutable objects (e.g., defensive copies for maps: `new HashMap<>(attributes)`).
  - **Concurrency**: Use thread-safe structures (e.g., ConcurrentHashMap if multi-threaded sim in future).
  - **Performance**: Profile with IntelliJ tools; batch events for large sims