# ---------- Stage 1: build the React frontend ----------
FROM node:24-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------- Stage 2: build the Spring Boot backend ----------
FROM eclipse-temurin:25-jdk AS backend
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
COPY --from=frontend /app/frontend/dist src/main/resources/static
RUN ./mvnw package -DskipTests -B

# ---------- Stage 3: the small image that actually runs ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --create-home pocketrand
COPY --from=backend /app/target/*.jar app.jar
USER pocketrand
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]