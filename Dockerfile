# 1) resolve dependencies
FROM gradle:8.14.3-jdk17 AS deps
WORKDIR /app
COPY settings.gradle build.gradle ./
RUN gradle dependencies --no-daemon

# 2) build application
FROM gradle:8.14.3-jdk17 AS build
WORKDIR /app
COPY --from=deps /home/gradle/.gradle /home/gradle/.gradle
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# 3) runtime image
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
