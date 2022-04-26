FROM openjdk:19-jdk AS builder
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

FROM openjdk:19-slim
ARG APPLICATION
ARG PORT
ENV SERVER_PORT $PORT
COPY --from=builder application/$APPLICATION/build/libs/*.jar application.jar
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "/application.jar"]
