# Azure Fundamentals

Azure Portal에서 핵심 자원을 직접 만들고 연결하며 Cloud Infrastructure의 구조를 익히는 시리즈다.

콘솔 클릭은 온프레미스 작업을 웹에서 하는 것과 다르지 않다. 그 편의 때문에 자원 전체의 연결 구조가 노출되지 않는다. 이 시리즈는 클릭 절차를 나열하지 않고 클릭 뒤의 연결과 구조를 서술한다.

## What you will learn

- Azure Portal 기반 자원 생성과 구성
- Virtual Machine, VNet, Entra ID, Storage, Database, Monitor, Container Apps의 구조와 실습
- 자원 간 연결 관계 — Networking, Security, Compute, Storage
- 네트워킹과 고가용성 같은 CS 기초 개념의 Azure 구현 방식
- 애플리케이션을 배포할 수 있는 형태의 Azure 환경 구성

CLI와 IaC는 다루지 않는다. 코드화는 Azure IaC with Terraform이 담당한다.

## Table of Contents

9개 Chapter, 44개 Section, Section Lab 25개, Gallery 실습 9개로 구성된다.

### 01 Cloud와 Azure 시작하기

| No | Section | Lab |
|----|---------|-----|
| 01 | Cloud Computing 개요 | — |
| 02 | Azure Global Infrastructure | — |
| 03 | Azure 계정과 Portal | lab01 |

### 02 Azure IAM & Microsoft Entra ID

| No | Section | Lab |
|----|---------|-----|
| 01 | Microsoft Entra ID 개요 | — |
| 02 | 구독과 리소스 그룹 | lab02 |
| 03 | 리소스 네이밍 규칙 | — |
| 04 | 사용자, 그룹, 역할 관리 | lab03 |
| 05 | RBAC | lab04 |

### 03 Compute - Azure Virtual Machine

| No | Section | Lab |
|----|---------|-----|
| 01 | Azure VM 개요 | — |
| 02 | VM 생성 및 기본 구성 | lab05 |
| 03 | VM 디스크와 스토리지 연결 | lab06 |
| 04 | [실습] Gallery - Azure VM 기본 배포 | Gallery |

### 04 VNet & Networking

| No | Section | Lab |
|----|---------|-----|
| 01 | VNet·Subnet와 인터넷 연결 구조 | lab07 |
| 02 | Private Subnet 격리 | — |
| 03 | NSG - 트래픽 제어 | lab08 |
| 04 | Azure Bastion | lab09 |
| 05 | NAT Gateway | lab10 |
| 06 | 네트워크 흐름 정리 | — |
| 07 | [실습] Gallery - Custom VNet 이전 | Gallery |

### 05 Traffic Management & High Availability

| No | Section | Lab |
|----|---------|-----|
| 01 | Azure Load Balancer 개요 | — |
| 02 | Azure Load Balancer 구성 | lab11 |
| 03 | 프로비저닝 자동화 - bake vs provision | lab12 |
| 04 | [실습] Gallery - Load Balancer 이전 | Gallery |
| 05 | Azure Application Gateway | lab13 |
| 06 | [실습] Gallery - Application Gateway 전환 | Gallery |
| 07 | 고가용성 구성 | lab14 |
| 08 | VM Scale Set | lab15 |
| 09 | [실습] Gallery - VM Scale Set 적용 | Gallery |

### 06 Azure Storage

| No | Section | Lab |
|----|---------|-----|
| 01 | Azure Storage 개요 | — |
| 02 | Storage Account와 Blob Storage | lab16, lab17 |
| 03 | Storage 접근 제어 | lab18 |
| 04 | Azure Files | lab19 |
| 05 | [실습] Gallery - Blob Storage 연동 | Gallery |

### 07 Azure Database

| No | Section | Lab |
|----|---------|-----|
| 01 | Azure Database 서비스 개요 | — |
| 02 | Azure Database for MySQL | lab20 |
| 03 | VM에서 Database 연결 | lab21 |
| 04 | [실습] Gallery - MySQL 연동 | Gallery |
| 05 | 백업과 복원 | lab22 |

### 08 관측과 운영 - Azure Monitor

| No | Section | Lab |
|----|---------|-----|
| 01 | Azure Monitor 개요와 메트릭·로그 | lab23 |
| 02 | [실습] Gallery - 알림과 스택 관측 | Gallery |

### 09 Container - ACR & Azure Container Apps

| No | Section | Lab |
|----|---------|-----|
| 01 | 컨테이너와 Azure 컨테이너 서비스 개요 | — |
| 02 | ACR - 이미지 빌드와 푸시 | lab24 |
| 03 | Azure Container Apps 배포 | lab25 |
| 04 | [실습] Gallery - Container Apps 배포 | Gallery |

## Learning Path

```
Cloud 이해 → 인증·권한 → 서버 생성 → 네트워크 설계 → 트래픽·고가용성
  (Ch01)      (Ch02)      (Ch03)       (Ch04)          (Ch05)

→ 스토리지 → 데이터베이스 → 관측·운영 → 컨테이너 전환
   (Ch06)      (Ch07)         (Ch08)      (Ch09)
```

## Series Project: Gallery

각 Chapter의 Section Lab과 별도로 Gallery 애플리케이션을 점진적으로 완성한다. Gallery는 시리즈 전체에서 배포 대상으로 쓰는 이미지 갤러리 웹 앱이다. 소스는 `Cloud/Workloads/gallery-spring-boot`에 있다.

Section Lab은 자원을 만들고 실습이 끝나면 반납한다. Gallery는 하나의 리소스 그룹에 누적되며 챕터를 넘겨 유지된다.

| Chapter | Gallery 실습 | 구성 | 다루는 것 |
|---------|-------------|------|----------|
| Ch03 | Azure VM 기본 배포 | VM | Public IP와 SSH로 직접 배포. 로컬 디스크와 H2를 쓰는 최소 구성 |
| Ch04 | Custom VNet 이전 | VM + Custom VNet | Private Subnet 배치. 외부 접속 경로가 없는 상태 확인 |
| Ch05 | Load Balancer 이전 | LB + VM 2대 | 골든 이미지로 2대를 만들고 Load Balancer 뒤에 배치 |
| Ch05 | Application Gateway 전환 | Application Gateway | L4에서 L7로 전환. HTTP 라우팅 구성 |
| Ch05 | VM Scale Set 적용 | VMSS | cloud-init 기반 프로비저닝으로 자동 스케일링 구성 |
| Ch06 | Blob Storage 연동 | VMSS + Blob | 업로드 파일을 Blob Storage로 전환. Managed Identity로 인증 |
| Ch07 | MySQL 연동 | VMSS + Blob + MySQL | H2를 Azure Database for MySQL로 전환 |
| Ch08 | 알림과 스택 관측 | Azure Monitor | 배포 스택 통합 관측과 Metric Alert 구성 |
| Ch09 | Container Apps 배포 | ACR + Container Apps | 같은 앱을 컨테이너로 전환해 배포 |

### Target Architecture

```
[Application Gateway] → [VMSS (Private Subnet)] → [MySQL Flexible Server (VNet 통합)]
                              ↓
                        [Blob Storage]
                              ↓
                        [Azure Monitor]
```

## Prerequisites

- Backend 또는 Frontend 개발 경험 4년 이상 권장
- Azure 계정. 무료 평가판 또는 종량제 구독
- Docker 기본 지식. Ch09에서 사용한다

## Series Context

Cloud Series는 4개 레이어로 구성된다. 이 시리즈는 Layer 0에 속한다.

```
Layer 0  Fundamentals    AWS, Azure ← 현재 시리즈, GCP, NCP
Layer 1  Core            Terraform Core, AWS IaC Core
Layer 2  IaC             Azure IaC with Terraform, GCP IaC with Terraform
Layer 3  Architecture    Cloud Infrastructure Architecture & Design
```

레이어 구조와 시리즈 간 경계는 Cloud Series README에 있다.

다음 시리즈는 Azure IaC with Terraform이다. Portal에서 익힌 인프라를 Terraform 코드로 다룬다.
