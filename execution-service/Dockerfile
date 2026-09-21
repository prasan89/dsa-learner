FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jdk-alpine
RUN addgroup -S sandbox && adduser -S sandbox -G sandbox
RUN mkdir -p /tmp/sandbox && chown sandbox:sandbox /tmp/sandbox
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
USER sandbox
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
