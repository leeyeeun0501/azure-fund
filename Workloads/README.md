# Cloud Workloads

`Cloud/` 트랙의 실습에서 **클라우드 인프라 위에 배포하는 애플리케이션**을 모아 둔 디렉터리다.
시리즈 문서와 소규모 예제는 각 시리즈 디렉터리에 두고, 이 디렉터리에는 **앱 소스**만 둔다.

> **이 디렉터리가 세 트랙의 정본이다.** `Container/Workloads/` 와 `System/Workloads/` 는 여기서 동기화한다.
> 앱 소스를 수정할 때는 이곳에서 수정하고 나머지 두 곳에 반영한다.

---

## 개요

- **역할**: 인프라를 구성한 뒤 그 위에서 **실제로 동작시켜 확인하는 대상**이다. 서버, 로드밸런서, 오브젝트 스토리지, 관리형 데이터베이스를 연결한 결과가 화면으로 드러난다
- **성격**: 애플리케이션을 만드는 법이 아니라 **클라우드 서비스를 애플리케이션에 연결하는 법**을 다룬다

---

## 포함된 Workload

| 이름 | 경로 | 런타임 | 설명 |
|------|------|--------|------|
| **gallery-spring-boot** | [`gallery-spring-boot/`](gallery-spring-boot/) | Java 21 | 이미지 갤러리 웹 앱. Spring Boot 3.5.x, Thymeleaf + MyBatis |
| **gallery-asp-dotnet** | [`gallery-asp-dotnet/`](gallery-asp-dotnet/) | .NET 8 | gallery-spring-boot의 .NET 포팅. ASP.NET Core MVC + Razor |
| **identicon** | [`identicon/`](identicon/) | Java 21 | 식별자로 아바타 이미지를 생성하는 보조 서비스. Spring Boot 3.3.x, Redis 사용 |

### gallery-spring-boot가 지원하는 백엔드

앱 하나가 여러 클라우드의 서비스를 선택해 사용할 수 있다. `app.storage.type` 으로 전환한다.

| 스토리지 | `app.storage.type` | 비고 |
|---------|-------------------|------|
| 로컬 디스크 | `local` | 정적 리소스로 노출한다 |
| Amazon S3 | `s3` | |
| Azure Blob Storage | `azure-blob` | `DefaultAzureCredential` 사용 |
| NCP Object Storage | `ncp-object` | Server Role 기반 키리스 자격증명. 사설·공인 엔드포인트를 분리한다 |

메타데이터 저장소는 H2 또는 MariaDB를 선택한다.

**같은 앱으로 여러 클라우드를 대조할 수 있다.** 코드가 아니라 설정만 바뀐다는 점이 드러난다.

---

## Cloud Series와의 관계

| 시리즈 | 관계 |
|--------|------|
| **AWS Fundamentals** | **primary.** 시리즈의 대표 실습 앱이다. 서버·로드밸런서·S3·RDS 구성과 정렬한다 |
| **Azure Fundamentals** | **primary.** Azure Blob Storage 연동을 포함한다 |
| **NCP Fundamentals** | **primary.** NCP Object Storage 연동과 Server Role 자격증명을 포함한다 |
| **AWS Basics** | 필요 시 참조한다 |
| **AWS IaC Core** | 인프라를 코드로 프로비저닝한 뒤 배포 대상으로 둔다 |
| **Azure IaC with Terraform** | 동일 |
| **GCP IaC with Terraform** | 동일 |
| **Terraform Core** | Provider 예제에서 배포 대상으로 참조한다 |
| **GCP Fundamentals** | 연결 예정 |
| **Cloud Infrastructure Architecture & Design** | 설계 사례의 대상으로 참조한다 |

상세 매핑은 [`.claude/plan/matrix.md`](.claude/plan/matrix.md) 를 따른다.

---

## 다른 트랙과의 관계

같은 앱을 세 트랙이 **다른 층위에서** 사용한다.

| 트랙 | 무엇을 배포하는가 |
|------|-----------------|
| **`System/`** | 운영체제 위에 서비스로 직접 배포한다 |
| **`Container/`** | 이미지로 만들어 컨테이너와 클러스터에 배포한다 |
| **`Cloud/`** | 클라우드 인프라 위에 배포하고 관리형 서비스를 연결한다 |

앱이 고정돼 있으므로 **층위에 따라 무엇이 달라지는지**가 학습 대상이 된다.

---

## 배포 관련 참고

- **`HostIdentity`** 가 요청을 처리한 인스턴스를 식별해 화면 푸터에 표시한다
  - 클라우드 메타데이터에 의존하지 않고 hostname을 사용한다. 안정적이지 않은 환경에서는 주 NIC의 사설 IPv4로 대체한다
  - **로드밸런서 뒤에 인스턴스를 여러 대 두었을 때 요청이 분산되는지 화면에서 확인할 수 있다**
- NCP Object Storage는 서버가 객체를 올리는 사설 도메인과 브라우저가 이미지를 받아 가는 공인 도메인이 다르다. `endpoint` 와 `public-endpoint` 로 나누어 지정한다
- 빌드 산출물(`target/`, `bin/`, `obj/`, `publish/`)은 저장소에 포함하지 않는다

---

## 라이선스

상위 저장소의 라이선스와 이용 조건을 따른다.
