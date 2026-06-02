FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

RUN apk add --no-cache maven

COPY pom.xml ./
COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

COPY --from=build /workspace/target/clients-1.0.0.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
