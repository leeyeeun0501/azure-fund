# Gallery (ASP.NET Core)

이미지를 업로드하고 목록에서 보거나 삭제하는 간단한 웹 갤러리다. **`gallery-spring-boot`(Java/Spring Boot)의 .NET 포팅**으로, 도메인·엔드포인트·화면·DB 옵션까지 대칭이 되도록 만들었다. 업로드 파일은 로컬 디스크 또는 Azure Blob에, 메타데이터는 SQLite(기본) 또는 MariaDB에 저장한다.

> Spring 배경이 있으면 아래 "Spring Boot ↔ ASP.NET Core 대응"부터 보면 빠르게 이해된다.

---

## 기술 스택

| 구분 | 사용 | Spring Boot 대응 |
|------|------|------------------|
| Runtime | **.NET 8** | Java 21 |
| Web | ASP.NET Core **MVC** + **Razor** 뷰 | Spring MVC + Thymeleaf |
| 내장 웹서버 | **Kestrel** | 내장 Tomcat |
| 영속성 | **EF Core** (SQLite 기본 / MariaDB via Pomelo) | MyBatis (H2 기본 / MariaDB) |
| 스토리지 | Local(기본) / **Azure Blob** | Local / S3 |
| 인스턴스 ID | **Azure IMDS** | EC2 Metadata |
| 헬스체크 | `/health` (Health Checks) | actuator `/actuator/health` |
| 빌드 | `dotnet` CLI → `gallery.dll` | Maven → `gallery.jar` |

**포트: 8080**(기본).

---

## 사전 요구 사항

- **.NET 8 SDK** ([dotnet.microsoft.com/download](https://dotnet.microsoft.com/download/dotnet/8.0))
  - 크로스 플랫폼이다. **Windows·Linux·macOS 모두**에서 빌드·실행된다
- (선택) MariaDB — 메타데이터를 MariaDB에 둘 때
- (선택) Azure Storage 계정 — 업로드를 Azure Blob에 둘 때

확인:
```bash
dotnet --version    # 8.0.x
```

---

## 프로젝트 구조

```text
gallery-asp-dotnet/
├── gallery.csproj              프로젝트 파일(AssemblyName=gallery)
├── Program.cs                  앱 진입점 + DI + 파이프라인 (Spring의 @SpringBootApplication + config)
├── appsettings*.json           설정 (application.yaml 대응)
├── Controllers/ItemController  GET / · POST /new · GET /{id}/delete
├── Services/ItemService
├── Repositories/ItemRepository (EF Core, item.xml 매퍼 대응)
├── Data/GalleryDbContext       테이블/컬럼 매핑 (schema.sql 대응)
├── Domain/Item                 id · url · comment
├── Storage/                    IImageStorage · LocalImageStorage · AzureBlobImageStorage
├── Runtime/AzureInstanceIdentity  Azure IMDS
├── Views/Item/Index.cshtml     Razor 뷰 (Thymeleaf index.html 대응)
└── wwwroot/assets/             css·js·images·photos (Spring Boot에서 그대로 복사)
```

---

## 설정 (appsettings.json)

| 키 | 값 | 설명 |
|----|-----|------|
| `Database:Provider` | `sqlite`(기본) / `mariadb` | DB 공급자 |
| `ConnectionStrings:Default` | 연결 문자열 | 아래 예 참고 |
| `Storage:Type` | `local`(기본) / `blob` | 업로드 저장소 |
| `Storage:Local:Path` | `uploads` | 로컬 저장 경로(작업 디렉터리 기준) |
| `Storage:Blob:ConnectionString` / `Container` | — | Azure Blob 사용 시 |

- 설정은 `appsettings.json` → `appsettings.{Environment}.json` → 환경 변수 순으로 덮어쓴다
- 환경은 `ASPNETCORE_ENVIRONMENT`(=`Development`/`Production`)로 지정한다

연결 문자열 예:
```text
SQLite : Data Source=gallery.db
MariaDB: Server=localhost;Port=3306;Database=gallery;User=gallery;Password=비밀번호;
```

---

## 빌드

```bash
# 프로젝트 루트에서
dotnet restore
dotnet build -c Release
```

배포용 게시(자기완결 산출물 생성):
```bash
dotnet publish -c Release -o publish
```
- `publish/` 에 `gallery.dll`, `web.config`(IIS용, 자동 생성), `wwwroot/`, 의존 DLL이 나온다
- 이 `publish/` 폴더 전체를 서버에 복사한다

---

## 로컬 실행 (Windows·Linux·macOS 공통)

```bash
# 기본: SQLite + 로컬 스토리지, 포트 8080
dotnet run

# 확인
curl http://localhost:8080/health     # Healthy
curl http://localhost:8080/           # 갤러리 HTML
```

브라우저에서 `http://localhost:8080` → "이미지 올리기"로 업로드. `wwwroot/assets/photos/`의 샘플 이미지(im1~15.jpg)로 테스트하면 된다.

> **Linux/macOS에서도 그대로 돈다.** .NET은 크로스 플랫폼이라 `dotnet run` 한 번이면 된다. Java의 `java -jar`와 같은 감각이다.

### MariaDB로 실행

```bash
# 환경 변수로 덮어쓰기 (또는 appsettings.Production.json)
export ASPNETCORE_ENVIRONMENT=Production
export Database__Provider=mariadb
export ConnectionStrings__Default="Server=localhost;Port=3306;Database=gallery;User=gallery;Password=비밀번호;"
dotnet run
```
- `:` 중첩 키는 환경 변수에서 `__`(밑줄 2개)로 쓴다
- 테이블(`item`)이 없으면 앱이 자동 생성한다. Spring Boot 버전과 같은 스키마라 **같은 MariaDB `item` 테이블을 공유**할 수 있다

---

## Windows Server IIS 배포

Windows Server에서 IIS + ASP.NET Core Module(ANCM)로 호스팅한다. Kestrel이 `w3wp.exe` 안에서 실행되는 **in-process** 모델이 기본이다.

### 1. 서버 준비 (winserver-vm)

```powershell
# IIS 설치 (이미 있으면 생략)
Install-WindowsFeature Web-Server -IncludeManagementTools

# ASP.NET Core Hosting Bundle 설치 (.NET Runtime + ANCM)
#   https://dotnet.microsoft.com/download/dotnet/8.0 → "Hosting Bundle"
.\dotnet-hosting-8.0-win.exe /quiet

# ANCM 모듈 반영을 위해 IIS 재시작
net stop was /y; net start w3svc
```
Hosting Bundle은 런타임과 ANCM을 함께 깐다. 서버에는 SDK가 아니라 **런타임(Hosting Bundle)만** 있으면 된다(빌드는 개발 PC에서).

### 2. 게시 산출물 배치

개발 PC에서 `dotnet publish -c Release -o publish` 한 결과를 서버로 복사한다.
```text
C:\inetpub\gallery\
├── gallery.dll
├── web.config          ← ANCM 설정(hostingModel=inprocess). publish가 자동 생성
├── wwwroot\...
└── (의존 DLL들)
```

### 3. 애플리케이션 풀 + 사이트 생성

```powershell
# ASP.NET Core 전용 앱풀: "관리 코드 없음"(자체 런타임을 쓰므로 CLR 미로드)
New-WebAppPool -Name "GalleryAppPool"
Set-ItemProperty "IIS:\AppPools\GalleryAppPool" -Name managedRuntimeVersion -Value ""

# 사이트 생성 (포트 8080)
New-Website -Name "gallery" -Port 8080 `
    -PhysicalPath "C:\inetpub\gallery" -ApplicationPool "GalleryAppPool"
```

### 4. 업로드 폴더 쓰기 권한 (로컬 스토리지)

로컬 스토리지는 앱이 `C:\inetpub\gallery\uploads`에 파일을 쓴다. 앱풀 ID에 **NTFS 쓰기 권한**을 준다.
```powershell
icacls "C:\inetpub\gallery" /grant "IIS AppPool\GalleryAppPool:(OI)(CI)(M)"
```
> 앱풀 ID(`IIS AppPool\GalleryAppPool`)에 Modify를 주는 것이 Windows의 서비스 계정 권한 부여 방식이다.

### 5. 방화벽 + MariaDB 설정

```powershell
# 외부(호스트)에서 8080 접속 허용
New-NetFirewallRule -DisplayName "Gallery-HTTP" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

MariaDB 연동 시, 환경을 Production으로 두고 연결 문자열을 준다. `web.config`의 `environmentVariables`에 넣거나 시스템 환경 변수로 설정한다.
```xml
<!-- web.config의 <aspNetCore> 안에 추가 예시 -->
<environmentVariables>
  <environmentVariable name="ASPNETCORE_ENVIRONMENT" value="Production" />
  <environmentVariable name="Database__Provider" value="mariadb" />
  <environmentVariable name="ConnectionStrings__Default"
                       value="Server=DB_HOST;Port=3306;Database=gallery;User=gallery;Password=비밀번호;" />
</environmentVariables>
```

### 6. 확인

```powershell
Invoke-WebRequest http://localhost:8080/health -UseBasicParsing | Select-Object StatusCode  # 200
```
호스트 브라우저에서 `http://192.168.56.10:8080` → 갤러리 화면.

---

## HTTP 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 화면 |
| POST | `/new` | `multipart/form-data` — `file`, `comment` |
| GET | `/{id}/delete` | 항목 삭제 |
| GET | `/health` | 헬스체크 |

---

## Spring Boot ↔ ASP.NET Core 대응 (요약)

| Spring Boot | ASP.NET Core |
|-------------|--------------|
| `@Controller` `ItemController` | `Controller` `ItemController` |
| `@Service` `ItemService` | `ItemService` (DI 등록) |
| MyBatis `ItemRepository` + `item.xml` | EF Core `ItemRepository` + `GalleryDbContext` |
| Thymeleaf `index.html` | Razor `Index.cshtml` |
| `ImageStorage` / `LocalImageStorage` / `S3ImageStorage` | `IImageStorage` / `LocalImageStorage` / `AzureBlobImageStorage` |
| `application.yaml` (dev/prod) | `appsettings.json` (Development/Production) |
| `./mvnw ... → gallery.jar` (내장 Tomcat) | `dotnet publish → gallery.dll` (내장 Kestrel) |
| `java -jar` (systemd 서비스) | `dotnet` / **IIS + ANCM** |

---

## 라이선스

상위 저장소의 라이선스·이용 조건을 따른다.
