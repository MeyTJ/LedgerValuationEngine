FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -B -q package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S lve && adduser -S lve -G lve
USER lve
WORKDIR /app
COPY --from=build /src/target/ledger-valuation-engine-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-XX:+UseCompactObjectHeaders", "-jar", "app.jar"]
