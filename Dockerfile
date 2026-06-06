FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
COPY target/ledger-valuation-engine-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseCompactObjectHeaders", "-jar", "app.jar"]
