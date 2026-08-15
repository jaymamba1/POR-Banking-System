# POR Banking System - Complete Manual Setup Guide

**POR:** Palaging Overtime si Rodney  
**Tagline:** “Kung walang resibo, baka drawing lang ang budget.”

This document explains how to recreate, run, test, and package this Spring Boot
project manually on Windows. The project is a Java 21 REST API built with Maven
and Spring Boot 4.1.0.

## 1. What the finished setup contains

- Java 21
- Spring Boot 4.1.0
- Maven Wrapper, so a separate Maven installation is not required
- Spring Web MVC for REST endpoints
- Spring Validation for request validation
- Spring Boot Actuator for application health information
- A sample `GET /api` endpoint
- A `GET /` Hello World page
- Automated context and controller tests

## 2. Prerequisites

Install the following software:

1. A Java 21 JDK, such as Eclipse Temurin 21.
2. Visual Studio Code or IntelliJ IDEA.
3. For VS Code, install the **Extension Pack for Java** and **Spring Boot
   Extension Pack** extensions.

Verify Java from PowerShell:

```powershell
java -version
javac -version
```

Both commands should report version 21. If `java` is not recognized, configure
the `JAVA_HOME` environment variable and add `%JAVA_HOME%\bin` to `Path`, then
restart the terminal.

## 3. Generate the base project with Spring Initializr

1. Open <https://start.spring.io/>.
2. Configure the project with these values:

| Setting | Value |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.0 |
| Group | `com.tesda` |
| Artifact | `banking-app` |
| Name | `POR Banking System` |
| Description | `POR Banking System - Palaging Overtime si Rodney` |
| Package name | `com.tesda.banking` |
| Packaging | Jar |
| Java | 21 |

3. Add these dependencies:

   - Spring Web
   - Validation
   - Spring Boot Actuator

4. Select **Generate** to download the ZIP archive.
5. Create a folder named `TESDA-Banking-app`, then extract the generated project
   into a `backend` folder inside it.
6. Open `TESDA-Banking-app` in VS Code. The Spring Boot `pom.xml` will be inside
   `backend`, while the React application will be inside `frontend`.

The generated Maven Wrapper files, `mvnw` and `mvnw.cmd`, should remain in the
project. They allow every developer to use the project's Maven version.

## 4. Expected project structure

```text
TESDA-Banking-app/
├── backend/
│   ├── .mvn/wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tesda/banking/
│   │   │   └── resources/application.properties
│   │   └── test/java/com/tesda/banking/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── .gitignore
├── package.json
└── README.md
```

Java package names and directories must match. For example, the
`com.tesda.banking.api` package belongs under
`backend/src/main/java/com/tesda/banking/api`.

## 5. Configure Maven

The generated `pom.xml` should use Spring Boot 4.1.0, Java 21, and contain the
Web MVC, Validation, and Actuator dependencies. The important parts are:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
    <relativePath/>
</parent>

<groupId>com.tesda</groupId>
<artifactId>banking-app</artifactId>
<version>0.0.1-SNAPSHOT</version>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Spring Initializr may generate extra metadata elements such as `licenses`,
`developers`, and `scm`. They can remain in the file.

## 6. Create the application entry point

Create
`backend/src/main/java/com/tesda/banking/TesdaBankingAppApplication.java`:

```java
package com.tesda.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TesdaBankingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesdaBankingAppApplication.class, args);
    }
}
```

`@SpringBootApplication` enables Spring Boot configuration and scans this
package and its subpackages for controllers and other Spring components.

## 7. Create the sample REST endpoint

Create `backend/src/main/java/com/tesda/banking/api/HomeController.java`:

```java
package com.tesda.banking.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public Map<String, String> home() {
        return Map.of(
                "application", "POR Banking System",
                "status", "running");
    }
}
```

Spring automatically converts the returned map into JSON.

## 8. Configure the application

Put the following in `backend/src/main/resources/application.properties`:

```properties
spring.application.name=por-banking-system

management.endpoints.web.exposure.include=health,info
```

The application uses port `8080` by default. To use a different port, add:

```properties
server.port=8081
```

## 9. Add automated tests

Create the context test at
`backend/src/test/java/com/tesda/banking/TesdaBankingAppApplicationTests.java`:

```java
package com.tesda.banking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TesdaBankingAppApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Create the controller test at
`backend/src/test/java/com/tesda/banking/api/HomeControllerTests.java`:

```java
package com.tesda.banking.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsApplicationStatus() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("POR Banking System"))
                .andExpect(jsonPath("$.status").value("running"));
    }
}
```

## 10. Run the project

Open a PowerShell terminal at the repository root and run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The first execution downloads Maven and the dependencies, so it can take a few
minutes. Wait for a message indicating that the application has started.

On macOS or Linux, use:

```bash
cd backend
./mvnw spring-boot:run
```

Stop the application with `Ctrl+C`.

## 11. Verify the API

Open <http://localhost:8080/api> in a browser. The response should be:

```json
{
  "application": "POR Banking System",
  "status": "running"
}
```

Check the Actuator health endpoint at
<http://localhost:8080/actuator/health>. It should return a response containing:

```json
{
  "status": "UP"
}
```

The same checks can be made from PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api
Invoke-RestMethod http://localhost:8080/actuator/health
```

## 12. Run the tests

On Windows:

```powershell
cd backend
.\mvnw.cmd test
```

A successful result ends with `BUILD SUCCESS` and reports two tests with no
failures or errors.

## 13. Build and run an executable JAR

Build the application:

```powershell
cd backend
.\mvnw.cmd clean package
```

Run the generated JAR:

```powershell
java -jar .\target\banking-app-0.0.1-SNAPSHOT.jar
```

## 14. Common problems

### `java` is not recognized

Install JDK 21, configure `JAVA_HOME`, add `%JAVA_HOME%\bin` to `Path`, and open
a new terminal.

### `mvn` is not recognized

Use `.\mvnw.cmd` on Windows. The Maven Wrapper means that a global `mvn`
installation is unnecessary.

### Port 8080 is already in use

Stop the other application, or set a different port in
`application.properties`, such as `server.port=8081`.

### PowerShell cannot execute a command

Use the Windows wrapper exactly as shown:

```powershell
cd backend
.\mvnw.cmd test
```

### The endpoint returns 404

Confirm that `HomeController` is inside the `com.tesda.banking` package tree.
Spring scans the main application's package and its children automatically.

### Dependencies do not download

Check the internet connection, proxy, firewall, and access to Maven Central.
Then retry the wrapper command.

## 15. Current setup status

This repository is already configured. The application compiles successfully,
and both the application-context test and `/api` controller test pass.

## 16. React frontend

The React application is in the `frontend` directory. It uses Vite and Tailwind
CSS, and proxies requests beginning with `/api` to the Spring Boot backend on
port 8080.

Open two PowerShell terminals. Start Spring Boot in the first terminal:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Start React from the project root in the second terminal:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Then open <http://localhost:5173/>. The page displays the connection status of
the Spring Boot API. Stop either development server by pressing `Ctrl+C` in its
terminal.

To create a production frontend build:

```powershell
cd frontend
npm.cmd run build
```

The generated static files are placed in `frontend/dist`.

## 17. Run the backend and frontend together

Install the root development helper once from the project root:

```powershell
npm.cmd install
```

Then start Spring Boot and React with one command:

```powershell
npm.cmd run dev
```

Spring Boot runs at <http://localhost:8080/> and React runs at
<http://localhost:5173/>. Both ports are fixed; startup fails with a clear error
if either port is already occupied. Press `Ctrl+C` once to stop both
applications.

## 18. XAMPP MariaDB database

XAMPP is installed separately from this repository. Open the XAMPP Control
Panel and start **MySQL**, then open **Shell** and create the development
database:

```sql
mysql -u root
CREATE DATABASE banking_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
EXIT;
```

The root `.env` file is loaded automatically by `npm.cmd run dev`. The default
local configuration connects to `localhost:3306`, database `banking_db`,
with XAMPP's default `root` user and blank password. This default is only for
local development. For a shared or production environment, create a dedicated
database user and update the `.env` values before starting the backend:

```powershell
$env:DB_URL="jdbc:mariadb://localhost:3306/banking_db"
$env:DB_USERNAME="tesda_app"
$env:DB_PASSWORD="your-strong-password"
npm.cmd run dev
```

Spring Data JPA handles persistence. The single database definition is
`backend/src/main/resources/db/migration/schema.sql`; run it once when setting
up a new database. Automated tests use an isolated H2 in-memory database and do
not modify the XAMPP database.

### Banking schema

The project follows the supplied banking schema and extends it with connected
registration tables:

| Table | Purpose |
| --- | --- |
| `customers` | Stores registration profile information. |
| `customer_credentials` | Stores password hashes, roles, terms acceptance, and login security data. |
| `accounts` | Stores account number, account-holder name, and current balance. |
| `transactions` | Stores deposits, withdrawals, transfer entries, balances, references, and remarks. |

`accounts.customer_id` links each banking account to its registered customer.
Any future table must continue to connect to this schema rather than
introducing a separate banking schema.
