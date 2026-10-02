# gallery-asp-dotnet — Mac + VSCode 개발 셋업

Mac(Apple Silicon/Intel)에서 VSCode로 이 프로젝트를 빌드·실행하는 절차다. Java 배경 기준으로, 대응되는 개념을 함께 표기한다. **빌드·로컬 실행은 Mac에서, 최종 실행/IIS 배포는 Windows Server**(→ `README.md`, 06.01)에서 한다.

> 큰 그림: `.NET SDK`(=JDK+Maven 역할) 설치 → VSCode 확장 → `dotnet restore/build/run`.

---

## 1. .NET 8 SDK 설치

**방법 A — 공식 설치 프로그램(권장)**
[dotnet.microsoft.com/download/dotnet/8.0](https://dotnet.microsoft.com/download/dotnet/8.0) → **SDK 8.0.x** → macOS **Arm64**(Apple Silicon) 또는 **x64**(Intel) `.pkg` 설치.

**방법 B — Homebrew**
```bash
brew install --cask dotnet-sdk
```

**확인**
```bash
dotnet --version          # 8.0.x
dotnet --list-sdks        # 8.0.x 포함 확인
```
`dotnet`이 안 잡히면 새 터미널을 열거나 `~/.zshrc`에 PATH(`/usr/local/share/dotnet` 또는 `/opt/homebrew/...`)를 추가한다.

> 대응: `.NET SDK` = **JDK + Maven**을 합친 것. `dotnet` CLI 하나로 복원·빌드·실행·게시를 다 한다.

---

## 2. VSCode 확장

VSCode에서 확장 하나만 설치하면 된다.

- **C# Dev Kit** (`ms-dotnettools.csdevkit`) — C# 언어 지원 + 솔루션 탐색기 + 디버깅 + 테스트가 묶여 있다. 설치 시 의존 확장(C#, IntelliCode)도 함께 들어온다.

> 대응: **IntelliJ IDEA + Spring 플러그인** 정도의 통합 경험을 VSCode에서 제공한다.

---

## 3. 프로젝트 열기 · 복원 · 빌드

```bash
cd .../System/Workloads/gallery-asp-dotnet
code .            # VSCode로 폴더 열기
```

터미널(VSCode 내장 터미널도 가능)에서:
```bash
dotnet restore    # NuGet 패키지 복원 (Maven: dependency resolve)
dotnet build      # 컴파일 (Maven: compile)
```
- 첫 `restore`/`build`는 EF Core·Pomelo·Azure.Blobs 패키지를 NuGet에서 내려받아 시간이 걸린다
- `Build succeeded` 가 뜨면 성공. 경고/오류가 나면 그 메시지를 그대로 캡처해 질문하면 된다

> 대응: `dotnet restore` = Maven 의존성 다운로드, `dotnet build` = `mvn compile`.

---

## 4. 로컬 실행과 확인 (Mac에서도 됨)

.NET은 크로스 플랫폼이라 Mac에서도 그대로 돈다(기본: SQLite + 로컬 스토리지 + 포트 8080).

```bash
dotnet run              # 실행 (기본: Development → SQLite)
# ... Now listening on: http://0.0.0.0:8080
# 중지: 이 터미널에서 Ctrl + C  (kill 필요 없음)
```

> **환경과 DB**: `Properties/launchSettings.json`이 `dotnet run`을 Development로 띄우고, 기본 DB는 SQLite(`gallery.db` 자동 생성)라 별도 DB 서버가 필요 없다. MariaDB로 붙이려면 실행 시 명시한다:
> ```bash
> dotnet run --Database:Provider=mariadb \
>   --ConnectionStrings:Default="Server=localhost;Port=3306;Database=gallery;User=gallery;Password=실제비번;"
> ```
> (MariaDB에 `gallery` 데이터베이스와 `gallery` 계정이 준비돼 있어야 한다)

다른 터미널에서:
```bash
curl http://localhost:8080/health          # Healthy
curl -s http://localhost:8080/ | head       # 갤러리 HTML
```

브라우저에서 `http://localhost:8080` → **이미지 올리기**로 업로드. 테스트 이미지는 `wwwroot/assets/photos/im1.jpg` 등을 쓰면 된다.

확인 포인트:
- 실행 시 `gallery.db`(SQLite)와 `uploads/` 폴더가 자동 생성되는가
- 업로드 → 목록에 나타나는가 → 삭제 버튼으로 사라지는가
- `wwwroot/assets/` 정적 자원(CSS/JS)이 로드되어 화면이 원본과 같은가

> 대응: `dotnet run` = `./mvnw spring-boot:run`.

### 핫 리로드 (선택)
```bash
dotnet watch run    # 코드 저장 시 자동 재빌드·재시작 (Spring DevTools 감각)
```

---

## 5. 디버깅 (F5)

1. `Program.cs`나 `Controllers/ItemController.cs`에 중단점(줄 번호 왼쪽 클릭)
2. VSCode에서 **F5** (C# Dev Kit이 실행 구성을 자동 생성). "C#" 디버거 선택
3. 브라우저로 요청을 보내면 중단점에서 멈추고 변수·호출 스택을 본다

> 대응: IntelliJ의 Debug 실행. `launch.json`/`tasks.json`은 C# Dev Kit이 알아서 만든다.

---

## 6. 자주 쓰는 dotnet 명령 (Maven 대응)

| 목적 | dotnet | Maven/Spring |
|------|--------|--------------|
| 의존성 복원 | `dotnet restore` | 의존성 다운로드 |
| 컴파일 | `dotnet build` | `mvn compile` |
| 실행 | `dotnet run` | `./mvnw spring-boot:run` |
| 핫 리로드 | `dotnet watch run` | DevTools |
| 테스트 | `dotnet test` | `mvn test` |
| 게시(배포 산출물) | `dotnet publish -c Release -o publish` | `mvn package` → jar |
| 환경 지정 | `ASPNETCORE_ENVIRONMENT=Production dotnet run` | `--spring.profiles.active=prod` |

---

## 7. 트러블슈팅

| 증상 | 확인 |
|------|------|
| `dotnet: command not found` | 새 터미널/PATH. `dotnet --list-sdks` |
| SDK 버전 불일치 | 8.0.x 설치 여부. `global.json`은 이 프로젝트에 없음 |
| `restore` 실패(NuGet) | 네트워크/프록시. `dotnet nuget locals all --clear` 후 재시도 |
| 포트 8080 사용 중 | `ASPNETCORE_URLS=http://localhost:5080 dotnet run` 로 변경 |
| SQLite 관련 오류 | Apple Silicon에서도 EF Core SQLite 정상. `bin`/`obj` 지우고 재빌드(`dotnet clean`) |
| MariaDB 연결 실패 | `Database:Provider=mariadb`일 때만. MariaDB 미기동이면 기본(SQLite)로 실행 |
| `Access denied for user 'gallery'@...`(MySql)인데 SQLite로 원함 | `dotnet run`이 Production으로 떠서 MariaDB를 시도한 것. `Properties/launchSettings.json`(Development) 있는지 확인, 셸에 `ASPNETCORE_ENVIRONMENT`가 export돼 있으면 `unset`. 기본은 SQLite |

---

## 8. 실행/배포는 Windows Server에서

Mac에서 **빌드·검증**한 뒤, 최종 실행과 IIS 배포는 Windows Server VM에서 한다.
- Mac에서: `dotnet publish -c Release -o publish` → `publish/` 폴더를 Windows로 전송
- Windows에서: **README.md의 "Windows Server IIS 배포"** 또는 강의 **06.01** 참고 (Hosting Bundle → 앱풀 No Managed Code → 8080 → NTFS 권한)

> Mac에서 만든 게시 산출물이 Windows에서 그대로 돈다. .NET은 크로스 플랫폼이다.

---

빌드하다 막히거나 궁금한 점이 생기면, 오류 메시지/명령을 그대로 공유해 주면 함께 짚는다.
