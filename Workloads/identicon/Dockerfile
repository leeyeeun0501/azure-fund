ARG workspace=workspace
ARG application=app
ARG temp=tmp

# Stage01: Building Application
FROM openjdk:21 AS build
ARG workspace
ARG profile=developement

WORKDIR /${workspace}
COPY .mvn ./.mvn
COPY mvnw* ./
COPY pom.xml ./
# RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw -P ${profile} install


# Stage02: Preparing Application Stage: Extracting Executable SpringBoot Layered-Jar
FROM build AS prepare-application
ARG workspace
ARG application
ARG temp

WORKDIR /${workspace}/${temp}
COPY --from=build /${workspace}/target/*.jar ${application}.jar
RUN java -Djarmode=tools -jar ${application}.jar extract --layers --launcher


# Stage03: Building Deploy Image
FROM eclipse-temurin:17-jre-focal
ARG workspace
ARG application
ARG temp

WORKDIR /${application}
COPY --from=prepare-application /${workspace}/${temp}/${application}/dependencies/ ./
COPY --from=prepare-application /${workspace}/${temp}/${application}/application/ ./
COPY --from=prepare-application /${workspace}/${temp}/${application}/spring-boot-loader/ ./

RUN groupadd -r spring && useradd -r -g spring spring
# for checking network state
# RUN apt update && apt install -y curl && apt install -y net-tools && apt install -y dnsutils

EXPOSE 80
USER spring:spring
ENV profile=development

ENTRYPOINT ["java", "-cp", ".", "-Dspring.profiles.active=${profile}", "org.springframework.boot.loader.launch.JarLauncher"]