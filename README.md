# Health-Tracker Backend
This project is the backend of a health tracker application. It is built using Kotlin and Javalin, and is responsible for handling API calls and interacting with the database.

# How it works
The backend of the health tracker application was built with:
- Kotlin as the programming language
- Javalin as the web framework
- Exposed as the SQL framework
- Jackson for JSON serialization and deserialization of objects
- JUnit for unit testing
- Unirest for HTTP requests
- Maven for dependency management
- Slf4j for logging
- Swagger for API documentation
- Auth0 for authentication and authorization
- Joda Time for date and time handling

## Project Structure
The project is organized into the following packages:

`config`: Contains classes responsible for configuring the Javalin app, including authentication and authorization, database connection, and Javalin settings.

`controllers`: Contains classes that handle incoming requests and make the necessary calls to the DAO classes to retrieve or modify data in the database.

`domain`: Contains classes that represent the data models in the application (e.g. User, Activity, etc.). The db subpackage contains classes that map these models to the corresponding database tables. The repository subpackage contains the DAO classes mentioned earlier.

`ext`: Contains extension functions that add functionality to existing classes.

`utils`: Contains utility classes for various purposes, such as handling errors, authenticating and authorizing users, and working with JSON data.

## Authentication and Authorization
Authentication and authorization are managed by the AuthConfig class, which uses JWT tokens to verify the user's identity and role. The JwtProvider class is responsible for generating and validating these tokens.

## Database Interactions
The backend interacts with the database using the DAO (Data Access Object) classes located in the repository package. These classes handle the communication with the database and provide methods for retrieving and modifying data.

## Error Handling
Exceptions are handled by the ErrorExceptionMapping class, which maps specific exceptions to HTTP status codes. This allows the frontend to properly handle errors and display the appropriate message to the user

# Getting Started
## How to run
To run the application, you need to have Java 8 installed. You can then run the application by running the following command in the root directory of the project:
```
mvn clean install
```

## How to test
To run the tests, you need to have Java 8 installed. You can then run the tests by running the following command in the root directory of the project:
```
mvn test
```

# License
This project is licensed under the MIT License



