# syntax=docker/dockerfile:1

# Stage01: Build
# eclipse-temurin:*-focal 태그는 레지스트리에 없을 수 있음 → 21-jdk / 21-jre 사용
FROM eclipse-temurin:21-jdk AS build
ARG workspace=workspace
ARG application

WORKDIR /${workspace}
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

RUN --mount=type=cache,target=/root/.m2 ./mvnw -B dependency:go-offline
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -Dbuild.finalName=${application} -DskipTests package

# Stage02: Extract layered jar
FROM build AS prepare-application
ARG workspace=workspace
ARG temp=temp
ARG application

WORKDIR /${workspace}/${temp}
COPY --from=build /${workspace}/target/${application}.jar ${application}.jar
RUN java -Djarmode=tools -jar ${application}.jar extract --layers --launcher

# Stage03: Runtime
FROM eclipse-temurin:21-jre
ARG workspace=workspace
ARG temp=temp
ARG application

WORKDIR /${application}

COPY --from=prepare-application /${workspace}/${temp}/${application}/dependencies/ ./
COPY --from=prepare-application /${workspace}/${temp}/${application}/snapshot-dependencies/ ./
COPY --from=prepare-application /${workspace}/${temp}/${application}/spring-boot-loader/ ./
COPY --from=prepare-application /${workspace}/${temp}/${application}/application/ ./

RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p uploads \
    && chown -R spring:spring /${application}

EXPOSE 8080
USER spring:spring

ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_DATASOURCE_URL=jdbc:h2:mem:gallery;DB_CLOSE_DELAY=-1;MODE=MYSQL
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=
ENV APP_STORAGE_TYPE=local
ENV APP_STORAGE_LOCAL_URL_PREFIX=/assets/images/uploads
ENV APP_STORAGE_S3_BUCKET=gallery

# 로컬 저장소는 WORKDIR 기준 상대 경로 `uploads` 로 고정 (명령줄 인자가 env·yaml보다 우선 → 호스트 절대경로 주입 완화)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher", "--app.storage.local.path=uploads"]
