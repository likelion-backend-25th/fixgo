# 1. AS업무 통합관리시스템 - FixGo 코드 컨벤션 정의서

## 목차
- [1. AS업무 통합관리시스템 - FixGo 코드 컨벤션 정의서](#1-as업무-통합관리시스템---fixgo-코드-컨벤션-정의서)
- [1.1 개요 및 기본 원칙](#11-개요-및-기본-원칙)
- [1.2 Java 및 Spring Boot 표준 스타일](#12-java-및-spring-boot-표준-스타일)
- [1.3 식별자 명명 규칙](#13-식별자-명명-규칙)
- [1.4 백엔드 레이어드 아키텍처 및 구현 표준](#14-백엔드-레이어드-아키텍처-및-구현-표준)
- [1.5 깃(Git) 협업 및 코드 리뷰 컨벤션](#15-깃git-협업-및-코드-리뷰-컨벤션)

---

## 1.1 개요 및 기본 원칙

### 1.1.1 작성 목적
본 문서는 AS업무 통합관리시스템 - FixGo 프로젝트에 참여하는 팀원 간의 일관된 코딩 스타일, 디렉토리 구조, 네이밍 규칙, 아키텍처 원칙을 정의하여 코드의 가독성을 극대화하고 결함을 사전에 방지하는 것을 목적으로 함.

### 1.1.2 기본 준수 원칙
- 단일성 원칙: 프로젝트 내 모든 코드는 마치 한 명의 개발자가 작성한 것과 같은 통일된 서식과 규칙을 유지함.
- 가독성 우선: 지나치게 함축적이거나 모호한 표현을 지양하고 의도가 명확히 드러나는 어휘와 구조를 채택함.
- 안정성 및 무결성: DTO와 Entity의 엄격한 분리, 불변성을 보장하는 생성자 주입, 선언적 트랜잭션 경계 설정을 통해 런타임 잠재 결함을 원천 차단함.
- 표준 기반: Google Java Style Guide 및 Naver Hackday Java Convention을 기본 토대로 하며 FixGo 도메인 환경에 맞게 구체화함.

---

## 1.2 Java 및 Spring Boot 표준 스타일

### 1.2.1 기본 서식 규칙
- 들여쓰기(Indentation): 탭(Tab) 문자를 일체 사용하지 않으며, 공백 문자(Space) 4칸을 고정 적용함.
- 줄 너비(Line Length): 코드 한 줄의 최대 길이는 120자로 제한하며, 초과 시 점(.), 쉼표(,), 연산자 뒤에서 자연스럽게 줄바꿈을 수행함.
- 중괄호 배치(Braces Placement): K&R(Kernighan & Ritchie) 스타일을 적용하여 선언문과 같은 줄 끝에서 중괄호를 열고, 닫는 중괄호는 새 줄에 단독 배치함.
- 단문 제어문 중괄호 강제: if, for, while 등의 제어문 실행 블록이 단 한 줄이더라도 중괄호를 절대 생략하지 않음.

```java
// 올바른 제어문 작성 예시
if (isExpired) {
    throw new CourseEnrollmentExpiredException("수강 기간이 만료되었습니다.");
}

// 부적절한 작성 예시 (중괄호 누락)
if (isExpired) throw new CourseEnrollmentExpiredException("수강 기간이 만료되었습니다.");
```

### 1.2.2 임포트(Import) 규칙
- 와일드카드 import 금지: `import java.util.*;`와 같은 와일드카드 사용을 원칙적으로 금지하며, 사용하는 개별 클래스만을 명시적으로 import함.
- 동일 패키지 내에서 5개 이상의 클래스 사용 시 와일드카드 사용을 허용
- 미사용 import 제거: 사용하지 않는 import 문은 커밋 전 반드시 정리함 (IntelliJ Optimize Imports 활용).
- import 정렬 순서:
  1) `jakarta.*`
  2) FixGo 도메인 패키지 (`net.likelion.bebc25.lms.*`)
  3) 서드파티 라이브러리 (`org.*`, `com.*` 등)
  4) Spring 프레임워크 (`org.springframework.*`)
  5) `java.*` 및 `javax.*`
  6) 정적 임포트(`import static ...`)는 최하단에 그룹화하여 배치

### 1.2.3 주석 및 어노테이션 규칙
- 코드 자체로 설명: 불필요한 사족 주석을 배제하고 메서드명과 변수명만으로 의도를 이해할 수 있도록 작성함.
- 클래스 어노테이션 배치:
  - 클래스 선언 바로 위에 배치하며, 가독성을 위해 한 줄에 하나의 어노테이션 선언을 원칙으로 함.
  - 배치 순서: `@Slf4j` -> 컴포넌트 어노테이션(`@RestController`, `@Service`, `@Repository`) -> 트랜잭션/보안 어노테이션(`@Transactional`, `@Secured`) -> 롬복(`@RequiredArgsConstructor`).

---

## 1.3 식별자 명명 규칙

### 1.3.1 패키지 명명 규칙
- 소문자 알파벳 단어를 결합하여 계층 구조로 구성하며 언더스코어나 대문자를 사용하지 않음.
- 도메인 주도 설계(DDD) 관점의 도메인 중심 패키지 구조를 적용함.
  - 패키지 루트: `net.likelion.bebc25.fixgo`
  - 공통 모듈: `net.likelion.bebc25.fixgo.global` (config, error, security, util)
  - 회원/인증 도메인: `net.likelion.bebc25.fixgo.domain.member` (controller, service, repository, entity, dto)
  - (추후 추가)

### 1.3.2 클래스 및 인터페이스 명명 규칙
- 파스칼 케이스(PascalCase)를 적용하며 명사 또는 명사구로 작성함.
- 아키텍처 역할에 따른 명확한 접미사(Suffix)를 반드시 부여함:
- (아래는 예시, 추후 구조에 맞춰서 수정)
  - 컨트롤러: `MemberController`
  - 서비스: 
  - 리포지토리: 
  - 엔티티: 
  - 요청 DTO: `[기능/목적]Request`
  - 응답 DTO: `[기능/목적]Response`
  - 예외 클래스: `[도메인/사유]Exception`
  - 설정 클래스: `[역할]Config` (예: `SecurityConfig`, `BatchConfig`, `SwaggerConfig`)

### 1.3.3 메서드 및 변수 명명 규칙
- 카멜 케이스(camelCase)를 적용하며, 메서드는 반드시 동사로 시작함.
- 조회 메서드 관례:
  - 단건 필수 조회(미존재 시 NotFound 예외 발생): `getById`, `getByEmail`
  - 선택적 조회(Optional 또는 List 반환): `findByEmail`, `findAllByStatus`, `searchCourses`
  - 존재 여부 판별(boolean 반환): `existsByEmail`, `isEnrolled`
- 상태 변경 메서드 관례:
  - 생성 및 등록: `create`, `register`, `save`
  - 수정 및 상태 전환: `updateStatus`, `completeLesson`, `cancelOrder`
  - 삭제: `delete`, `remove`
- 논리 판별 변수: `is~`, `has~`, `can~` 접두사를 사용하여 true/false 의도를 명확히 표현함 (예: `isCompleted`, `hasCertificate`).

### 1.3.4 상수 명명 규칙
- `static final` 상수는 대문자 스네이크 케이스(UPPER_SNAKE_CASE)로 작성함.
  - 예시: `MAX_ENROLLMENT_DAYS = 365;`
  - 예시: `DEFAULT_PAGE_SIZE = 20;`
  - 예시: `COURSE_COMPLETION_RATE_THRESHOLD = 80;`

### 1.3.5 데이터베이스 테이블 및 컬럼 명명 규칙
- 소문자 스네이크 케이스(snake_case)를 적용함.
- 테이블명: 복수형 명사 또는 도메인 명사 사용 (예: `members`, `courses`, `lessons`, `enrollments`).
- 기본키(PK): `[테이블단수명]_id` 형태로 일관되게 부여 (예: `course_id`, `member_id`).
- 외래키(FK): 연관 테이블의 PK 명칭과 완전히 일치시킴 (예: `lessons.course_id`).
- 날짜 컬럼: 접미사 `_at` 적용 (예: `created_at`, `updated_at`, `completed_at`).
- 여부 컬럼: 접두사 `is_` 적용 (예: `is_deleted`, `is_free`).

---

## 1.4 백엔드 레이어드 아키텍처 및 구현 표준

### 1.4.1 계층 간 책임 분리 원칙
- Controller 계층:
  - 클라이언트 요청 파라미터 바인딩 및 `@Valid` 기반 유효성 검증 수행.
  - Service 계층 호출 후 표준 응답 규격(`CommonResponse<T>`)으로 감싸서 반환.
  - 비즈니스 로직 및 직접적인 데이터 조작 일체 금지.
- Service 계층:
  - 비즈니스 흐름 제어, 도메인 상태 변경, 트랜잭션 경계 설정.
  - DTO와 Entity 간의 변환 및 여러 Repository 간의 유기적 조합 수행.
- Repository 계층:
  - Spring Data JPA 및 QueryDSL을 활용한 데이터베이스 영속화와 쿼리 실행 전담.
  - 비즈니스 로직 작성 금지.

### 1.4.2 DTO와 Entity의 엄격한 분리
- 엔티티 객체를 Controller의 파라미터로 직접 바인딩하거나 반환 객체로 외부에 노출하는 행위 엄격히 금지.
- 사유:
  - 지연 로딩(Lazy Loading) 객체 직렬화 시 `LazyInitializationException` 발생 차단.
  - 양방향 연관관계 엔티티의 JSON 직렬화 순환 참조 오류 원천 차단.
  - 클라이언트 요구사항 변경 시 핵심 도메인 엔티티가 오염되는 현상 방지.
  - 패스워드 등 민감 정보의 외부 유출 방지.

### 1.4.3 의존성 주입 표준 (생성자 주입 강제)
- 필드 주입(`@Autowired`)을 전면 금지하며, 불변성을 보장하는 `final` 필드 기반 생성자 주입을 의무화함.
- Lombok의 `@RequiredArgsConstructor`를 사용하여 간결하고 안전하게 구성함.

```java
// 올바른 생성자 주입 표준 예시
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createCourse(CourseCreateRequest request, Long instructorId) {
        Member instructor = memberRepository.getById(instructorId);
        Course course = request.toEntity(instructor);
        return courseRepository.save(course).getId();
    }
}
```

### 1.4.4 선언적 트랜잭션(@Transactional) 관리 표준
- 클래스 레벨 기본값: 조회 최적화를 위해 서비스 클래스 상단에 `@Transactional(readOnly = true)`를 선언함.
  - 영속성 컨텍스트 스냅샷 생성 생략으로 인한 힙 메모리 절약.
  - 데이터베이스 읽기 전용 힌트 적용을 통한 성능 최적화.
- 상태 변경 메서드: 등록, 수정, 삭제가 발생하는 메서드에 한하여 `@Transactional` 어노테이션을 별도로 명시하여 쓰기 트랜잭션 보장.

### 1.4.5 전역 예외 처리 표준
- 비즈니스 예외 발생 시 `BusinessException`을 상속한 커스텀 예외를 발생시키며, 적절한 `ErrorCode`를 지정함.
- Controller 계층에서 개별 `try-catch` 블록 사용을 지양하고, `@RestControllerAdvice` 전역 핸들러에서 통일된 `ErrorResponse` 규격으로 변환하여 반환함.

```json
{
  "timestamp": "2026-10-19T11:15:00",
  "status": 404,
  "code": "COURSE_NOT_FOUND",
  "message": "요청하신 강좌 정보를 찾을 수 없습니다."
}
```

---

## 1.5 깃(Git) 협업 및 코드 리뷰 컨벤션

### 1.5.1 커밋 메시지 규격 (Conventional Commits)
커밋 메시지는 다음의 3단 구조를 기본으로 준수함:

```text
<type>: <subject>

<body> (선택사항)

<footer> (선택사항)
```

- 타입(Type) 분류:
  - `feat`: 새로운 기능 구현 (예: `feat: 강좌 상세 커리큘럼 조회 API 구현`)
  - `fix`: 버그 및 오류 수정 (예: `fix: 차시 진도율 80% 달성 판정 오류 수정`)
  - `refactor`: 프로덕션 코드 구조 개선 (동작 변경 없음, 예: `refactor: 강좌 목록 쿼리 Fetch Join 적용`)
  - `docs`: 문서 생성 및 수정 (예: `docs: REST API 명세서 결제 콜백 규격 추가`)
  - `style`: 코드 서식, 세미콜론 정돈 등 기능에 영향 없는 변경 (예: `style: 들여쓰기 공백 4칸 정돈`)
  - `test`: 단위 및 통합 테스트 코드 추가 및 수정 (예: `test: 결제 사전 검증 단위 테스트 작성`)
  - `chore`: 빌드 설정, 의존성 라이브러리 추가, 설정 변경 (예: `chore: QueryDSL 의존성 버전 업데이트`)
- 작성 지침:
  - 제목은 50자 이내로 명확하게 작성하며 끝에 마침표를 찍지 않음.
  - 명령조 또는 단정한 명사구 어휘로 마감함.

### 1.5.2 브랜치 전략 및 명명 규칙
- `main`: 운영 프로덕션 배포 브랜치 (직접 푸시 금지, 관리자 승인 머지).
- `develop`: 팀 개발 통합 기본 브랜치 (Default Branch, 직접 푸시 금지, 2인 승인 및 CI 통과 필수).
- `feature/[이슈번호]-[기능명]`: 새로운 기능 개발 브랜치 (예: `feature/12-course-list`).
- `fix/[이슈번호]-[버그명]`: 버그 수정 브랜치 (예: `fix/25-progress-calc`).

### 1.5.3 PR(Pull Request) 및 코드 리뷰 체크리스트
모든 PR은 아래 체크리스트를 통과해야 머지가 승인됨:
- [ ] 브랜치 방향이 `feature/*` -> `develop` 인지 확인함.
- [ ] 로컬에서 `./gradlew test`를 수행하여 모든 테스트가 통과함을 확인함.
- [ ] 백엔드 코드 컨벤션(들여쓰기 4칸, DTO/Entity 분리, 생성자 주입)을 준수함.
- [ ] 불필요한 콘솔 출력(`System.out.println`)이나 미사용 import를 완전히 제거함.
- [ ] 팀원 2인 이상의 `Approve` 리뷰 승인을 획득함.
