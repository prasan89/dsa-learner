FROM eclipse-temurin:21-jdk-alpine

WORKDIR /sandbox

# Create a non-root user for running submitted code
RUN addgroup -S sandbox && adduser -S sandbox -G sandbox

# Copy the runner script
COPY runner.sh /runner.sh
RUN chmod +x /runner.sh

EXPOSE 8081

ENTRYPOINT ["/runner.sh"]
