# Gallery (Spring Boot)

이미지를 업로드하고 목록에서 보거나 삭제할 수 있는 간단한 웹 갤러리 애플리케이션이다. 업로드 파일은 **로컬 디스크**, **Amazon S3**, 또는 **Azure Blob Storage**에 저장하도록 선택할 수 있다.

---

## 기능

- 갤러리 목록 조회
- 이미지 파일 업로드(선택적으로 코멘트 입력)
- 항목 삭제(저장소의 파일과 DB 레코드 정리)
- 로컬 / Amazon S3 / Azure Blob 스토리지 전환(`app.storage.type`)

---

## 기술 스택

| 구분 | 사용 |
|------|------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x |
| View | Thymeleaf |
| Persistence | MyBatis, H2(기본), MariaDB JDBC 포함 |
| Cloud SDK | AWS SDK v2 (S3), Azure SDK (Blob Storage, Identity) — 선택 시 |
| Build | Maven |

---

## 사전 요구 사항

- JDK 21
- (S3 사용 시) AWS 자격 증명 및 버킷 — SDK 기본 제공 체인(환경 변수, 프로파일 등)을 사용한다.
- (Azure Blob 사용 시) Storage Account·컨테이너 및 자격 증명 — `DefaultAzureCredential` 기본 체인(Managed Identity, 환경 변수 등)을 사용한다.

---

## 빌드

프로젝트 루트에서 Maven Wrapper로 패키징한다. 테스트를 포함한 전체 빌드는 아래 명령을 사용한다.

```bash
./mvnw clean package
```

Windows:

```bash
mvnw.cmd clean package
```

- **산출물**: `target/cloud-workload-gallery-0.0.1-SNAPSHOT.jar`
- **테스트만** 실행: `./mvnw test`
- **테스트 생략** 후 패키지: `./mvnw clean package -DskipTests`

`pom.xml`의 `finalName`은 프로퍼티 **`build.finalName`** 으로 정의되어 있으므로, 빌드 시 아래처럼 산출 파일 이름을 `gallery.jar`로 고정할 때는 예를 들어 다음과 같다.

```bash
./mvnw clean package -Dbuild.finalName=gallery
```

Windows:

```bash
mvnw.cmd clean package -Dbuild.finalName=gallery
```

이때 산출물은 **`target/gallery.jar`** 이다. 아래 JAR 실행 예시도 이 파일명을 기준으로 한다. 기본 `clean package` 만 쓸 때는 **`target/cloud-workload-gallery-0.0.1-SNAPSHOT.jar`** 가 생성되므로, 실행 시 경로만 맞추면 된다.

---

## 실행 방법

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

활성 프로파일 **기본(default)은 `dev`** 이며, 애플리케이션은 **http://localhost:8080** 에서 동작한다.

---

## 설정

주요 설정은 `src/main/resources/application.yaml` 및 프로파일(`dev`, `prod`)에서 다룬다.

| 항목                             | 설명 |
|--------------------------------|------|
| `spring.profiles.active`       | 활성 프로파일. **기본(default)은 `dev`** 이며, 별도로 넘기지 않아도 된다. `prod` 를 쓸 때만 명시한다. |
| `spring.datasource.*`          | DB 연결. 기본은 인메모리 H2(MySQL 모드). MariaDB 등으로 교체 가능하다. |
| `app.storage.type`             | `local`, `s3`, 또는 `azure-blob` |
| `app.storage.local.path`       | 로컬 저장 디렉터리 (상대 경로는 프로세스 작업 디렉터리 기준). **기본값은 `uploads`** (`application.yaml` 의 default 프로파일과 동일) |
| `app.storage.local.url.prefix` | 브라우저에서 접근하는 업로드 파일 URL prefix |
| `app.storage.s3.bucket`        | S3 버킷 이름 (`app.storage.type=s3` 일 때) |
| `app.storage.blob.account`     | Storage Account 이름 (`app.storage.type=azure-blob` 일 때). 환경변수 `AZURE_STORAGE_ACCOUNT` 로도 주입 |
| `app.storage.blob.container`   | Blob 컨테이너 이름 (`app.storage.type=azure-blob` 일 때) |

---

## HTTP 엔드포인트 (요약)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 화면 |
| POST | `/new` | `multipart/form-data` — `file`, `comment` |
| GET | `/{id}/delete` | 항목 삭제 |

---

## dev와 prod 프로파일

`application.yaml` 에서 **`spring.profiles.active`** 기본값은 **`dev`** 이다. 인자를 넘기지 않으면 **기본(default)으로 `dev`** 가 적용된다. **`prod`** 는 `--spring.profiles.active=prod` 로만 전환한다.

| 구분 | `dev` (default) | `prod` |
|------|--------|--------|
| **HTTP 포트** | `8080` (`application.yaml` 의 `server.port`) | `8080` (동일. 프로파일과 무관하게 **8080 고정**) |
| **로깅** | 기본 콘솔 패턴 (`application.yaml` 의 `logging`) | `logback-prod.xml` 사용, 구조화·운영 로그에 맞춘 설정 |
| **기타** | Thymeleaf 캐시 끔, DevTools(핫 리로드 등) 사용 | Thymeleaf 캐시 켬, `logging.ops.level` 등 운영용 레벨 |

포트를 바꾸려면 실행 시 **`--server.port=...`** 또는 환경 변수 **`SERVER_PORT`** 로 덮어쓸 수 있다. 기본 구성은 **항상 8080** 이다.

---

## JAR 실행과 환경 조합

Spring Boot 는 **`--속성=값`** 으로 설정을 덮어쓴다. 비밀번호 등은 명령줄에 노출되기 쉬우므로, 운영에서는 환경 변수나 외부 설정 파일 사용을 권장한다.

### 자주 쓰는 파라미터

| 목적               | 예시                                                                                  |
|------------------|-------------------------------------------------------------------------------------|
| 프로파일             | `prod` 를 쓸 때만 `--spring.profiles.active=prod` (기본 `dev` 는 생략 가능) |
| 포트               | 기본 `8080`. 다른 포트는 `--server.port=9090` 등 (선택) |
| 스토리지             | `--app.storage.type=s3` 또는 `--app.storage.type=azure-blob`                          |
| S3 버킷            | `--app.storage.s3.bucket=버킷이름`                                                      |
| Azure Blob        | `--app.storage.blob.account=계정명` , `--app.storage.blob.container=컨테이너명`             |
| 외부 DB(MariaDB 등) | `--spring.datasource.url=...` , `--spring.datasource.username=...` , `--spring.datasource.password=...` |

### 조합 예시

**1) 프로파일(dev, 기본) + H2 + 스토리지(local) + 포트(8080, 기본)**

```bash
java -jar target/gallery.jar
```

**2) 프로파일(dev, 기본) + MariaDB(local) + 스토리지(S3) + 포트(8080)**

```bash
java -jar target/gallery.jar \
--spring.datasource.url=jdbc:mariadb://localhost:3306/gallery \
--spring.datasource.username=gallery \
--spring.datasource.password='비밀번호' \
--server.port=8080
--app.storage.type=s3 \
--app.storage.s3.bucket=gallery \
```

(AWS 자격 증명은 환경 변수·프로파일 등으로 제공한다.)

**3) 프로파일(prod) + H2 + 스토리지(local) + 포트(8080, 기본)**

```bash
java -jar target/gallery.jar \
--spring.profiles.active=prod \
```

**4) 프로파일(prod) + H2 + 스토리지(S3) + 포트(8080)**

```bash
java -jar target/gallery.jar \
--spring.profiles.active=prod \
--app.storage.type=s3 \
--app.storage.s3.bucket=gallery \
--server.port=8080
```



**5) 프로파일(prod) + 스토리지(Azure Blob) + 포트(8080)**

```bash
java -jar target/gallery.jar \
--spring.profiles.active=prod \
--app.storage.type=azure-blob \
--app.storage.blob.account=stgallery \
--app.storage.blob.container=gallery
```

(Azure 자격 증명은 Managed Identity·환경 변수 등 `DefaultAzureCredential` 체인으로 제공한다. 반환되는 이미지 URL이 브라우저에서 바로 열리려면 컨테이너에 익명 읽기가 허용돼야 한다.)

---

## Docker: 이미지 빌드와 컨테이너 실행

컨테이너 이미지는 **`Dockerfile`** 의 `ENV` 로 **기본적으로 dev·H2·로컬 스토리지** 를 쓴다(포트는 이미지에서 **8080**). 실행 시 **`-e`** 로 JAR 의 `--속성=값` 과 같은 설정을 덮어쓴다. 비밀번호 등은 명령줄·환경 변수 노출을 피하기 위해 운영에서는 Secret·외부 설정을 권장한다.

**로컬 스토리지 경로(컨테이너):** 이미지는 **`WORKDIR`(`/gallery`) 기준 상대 경로 `uploads`** 로 고정한다(`ENTRYPOINT` 에서 `--app.storage.local.path=uploads`). 호스트에서 절대경로로 바꾸는 설정은 컨테이너 샌드박스와 맞지 않아 **의도적으로 우선 적용하기 어렵게** 두었다. 업로드를 호스트에 남기려면 **볼륨으로 `/gallery/uploads` 에 마운트**하면 된다 (예: `-v "$(pwd)/gallery-uploads:/gallery/uploads"`).

이미지 빌드 시 **`--build-arg application=gallery` 는 필수** 이다(`ARG application` 기본값 없음).

### 빌드

프로젝트 루트(`gallery-spring-boot/`)에서 실행한다.

```bash
docker build --build-arg application=gallery -t gallery:local .
```

Windows:

```bash
docker build --build-arg application=gallery -t gallery:local .
```

### 환경 변수 (컨테이너)

| 목적 | 예시 |
|------|------|
| 프로파일 | `prod` 를 쓸 때만 `SPRING_PROFILES_ACTIVE=prod` (이미지 기본은 `dev`) |
| 포트 | 기본 **8080**. 다른 포트가 필요할 때만 `SERVER_PORT=...` (선택) |
| 스토리지 | `APP_STORAGE_TYPE=s3` |
| S3 버킷 | `APP_STORAGE_S3_BUCKET=버킷이름` |
| 외부 DB(MariaDB 등) | `SPRING_DATASOURCE_URL=...`, `SPRING_DATASOURCE_USERNAME=...`, `SPRING_DATASOURCE_PASSWORD=...` |

S3 사용 시 AWS 자격 증명은 **사전 요구 사항** 과 같이 SDK 기본 체인(예: `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` / `AWS_REGION` 등)을 따른다.

### 조합 예시

**1) 기본 프로파일(dev, 기본) + H2 + 스토리지(local)**

```bash
docker run --rm -p 8080:8080 gallery:local
```

**2) 프로파일(dev, 기본) + MariaDB(local) + 스토리지(S3) + 포트(8080)**

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/gallery \
  -e SPRING_DATASOURCE_USERNAME=gallery \
  -e SPRING_DATASOURCE_PASSWORD='비밀번호' \
  -e SERVER_PORT=8080 \
  -e APP_STORAGE_TYPE=s3 \
  -e APP_STORAGE_S3_BUCKET=gallery \
  gallery:local
```

(AWS 자격 증명은 환경 변수·프로파일 등으로 제공한다.)

**3) 프로파일(prod) + H2 + 스토리지(local) + 포트(8080)**

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8080 \
  gallery:prod
```

**4) 프로파일(prod) + H2 + 스토리지(S3) + 포트(8080)**

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8080 \
  -e APP_STORAGE_TYPE=s3 \
  -e APP_STORAGE_S3_BUCKET=gallery \
  gallery:local
```

(AWS 자격 증명은 환경 변수·프로파일 등으로 제공한다.)



---

## 라이선스

상위 저장소의 라이선스·이용 조건을 따른다.
