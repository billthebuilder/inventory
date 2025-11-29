Inventory Management System (Spring Boot, Java 21)

Overview
- A web-based inventory service built with Spring Boot 3.3 and Java 21.
- Persists data in MySQL using Spring Data JPA (soft deletes on entities).
- Secured with JWT (stateless). Two roles supported: SUPER_ADMIN and USER.
- Uses Apache Kafka for:
  - Inventory domain events (create/update/delete/stock adjusted)
  - Audit trail of all HTTP requests
  - Asynchronous bulk CSV product upload

Key Features
- Products: CRUD, search, pagination, soft delete, stock adjustments
- Users: create, blacklist/unblacklist, soft delete (SUPER_ADMIN only)
- Authentication: login via username/password → JWT; role embedded in token
- Audit logging: every request is published to a Kafka audit topic
- Bulk upload: CSV file is accepted via REST and processed asynchronously via Kafka consumer

Tech Stack
- Java 21, Spring Boot 3.3.5
- Spring Web, Validation, Data JPA, Security
- MySQL (mysql-connector-j)
- Apache Kafka (spring-kafka)
- JWT (jjwt)
- OpenAPI UI via springdoc

Prerequisites
- Java 21
- Maven 3.9+
- MySQL running locally with a database/user you control
- Kafka broker available (default localhost:9092)

Configuration (application.yaml)
- Datasource: jdbc:mysql://localhost:3306/inventory (createDatabaseIfNotExist=true)
- JPA: ddl-auto=update, MySQL8 dialect
- Kafka: bootstrap-servers=localhost:9092
- JWT: security.jwt.secret and security.jwt.expiration
- Topics (defaults):
  - inventory.events
  - inventory.audit
  - inventory.bulk.upload

Build & Run
1) Configure application.yaml if needed: src/main/resources/application.yaml
2) Build
   - mvn clean package
3) Run
   - java -jar target/inventory-0.0.1-SNAPSHOT.jar

Quick Start
1) Create SUPER_ADMIN (optional bootstrap)
   - POST /api/auth/bootstrap-super-admin?username=admin&password=admin123
2) Login (get JWT)
   - POST /api/auth/login {"username":"admin","password":"admin123"}
   - Use Authorization: Bearer <token> for subsequent calls
3) Explore APIs
   - Swagger/OpenAPI UI: http://localhost:8080/swagger-ui.html

Postman Collection
- Import: postman/inventory.postman_collection.json
- The collection includes auth, product, and user endpoints and captures JWT into {{token}}.

API Highlights
- Products
  - GET /api/products?page=0&size=20
  - GET /api/products/search?q=term&page=0&size=20
  - GET /api/products/{id}
  - POST /api/products (JSON Product)
  - PUT /api/products/{id}
  - DELETE /api/products/{id} (soft delete)
  - POST /api/products/{id}/adjust?delta=5
  - POST /api/products/bulk-upload (multipart CSV: name,description,price,quantity,category,tags)
- Users (SUPER_ADMIN)
  - POST /api/users
  - POST /api/users/{id}/blacklist?value=true|false
  - DELETE /api/users/{id}

Notes
- Ensure Kafka is running; topics are auto-created by the app.
- Passwords are stored using BCrypt.
- Audit messages contain method, path, timestamp, and remote IP.