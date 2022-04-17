FROM openjdk:19-jdk AS builder
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar

FROM openjdk:19-slim
ARG application
ARG port
ENV SERVER_PORT $port
COPY --from=builder application/$application/build/libs/*.jar application.jar
EXPOSE $port
ENTRYPOINT ["java", "-jar", "/application.jar"]
