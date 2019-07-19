# syntax=docker/dockerfile:experimental
FROM oracle/graalvm-ce:19.0.2 as native
RUN gu install native-image

FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app
ARG HOME=/root
RUN apk add protobuf
COPY mvnw .
COPY .mvn .mvn
RUN --mount=type=cache,target=${HOME}/.m2 ./mvnw dependency:get -Dartifact=org.springframework.boot.experimental:spring-boot-thin-launcher:1.0.22.RELEASE:jar:exec -Dtransitive=false
COPY * ./
RUN --mount=type=cache,target=${HOME}/.m2 ./mvnw install -DskipTests
VOLUME ${HOME}/.m2

FROM native
WORKDIR /workspace/app
ARG LISTENER=netty
ARG HOME
# can't use ${HOME} here
ARG THINJAR=/root/.m2/repository/org/springframework/boot/experimental/spring-boot-thin-launcher/1.0.22.RELEASE/spring-boot-thin-launcher-1.0.22.RELEASE-exec.jar
COPY --from=build /root/.m2 /root/.m2
COPY --from=build /workspace/app/listener/${LISTENER}/target/*.jar target/
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
RUN native-image --no-server -Dio.netty.noUnsafe=true -H:+ReportExceptionStackTraces --no-fallback --allow-incomplete-classpath --report-unsupported-elements-at-runtime --shared -H:Name=target/lib${LISTENER}listener -cp `java -jar ${THINJAR} --thin.archive=target/dependency --thin.classpath`
