package net.likelion.bebc25.fixgo.as.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.as.repository.AsTechnicianAssignmentHistoryRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.portal.repository.CustomerPortalLinkRepository;
import net.likelion.bebc25.fixgo.portal.service.CustomerPortalLinkService;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, AsReceptionService.class,
        AsStatusHistoryService.class, CustomerPortalLinkService.class})
class AsReceptionServiceTest {

    @Autowired
    private AsReceptionService asReceptionService;
    @Autowired
    private AsReceptionRepository asReceptionRepository;
    @Autowired
    private AsStatusHistoryRepository asStatusHistoryRepository;
    @Autowired
    private AsTechnicianAssignmentHistoryRepository assignmentHistoryRepository;
    @Autowired
    private CustomerPortalLinkRepository customerPortalLinkRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AsReceptionFixture asReceptionFixture;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductModelRepository productModelRepository;
    @Autowired
    private RepairRepository repairRepository;

    private static final String REASSIGNMENT_REASON = "Schedule change";

    private AsReception reception;
    private CustomUserDetails receptionist;
    private Member technician;

    @BeforeEach
    void setUpReception() {
        reception = asReceptionFixture.save();
        receptionist = new CustomUserDetails(reception.getReceptionist());
        setProductOwner(reception.getCustomer().getId());
        technician = memberRepository.save(new Member(
                "technician@test.com", "password", "수리기사", "010-3333-4444",
                "서울시", Role.ROLE_TECHNICIAN, "profile.png"));
    }

    @Test
    @DisplayName("접수 상세조회에 접수 제품의 모델명이 포함된다")
    void getReception_includesProductModelName() {
        Long id = reception.getId();
        String expectedModelName = productModelRepository
                .findById(reception.getProduct().getProductModelId()).orElseThrow().getModelName();
        entityManager.flush();
        entityManager.clear();

        var response = asReceptionService.getReception(id);

        assertThat(response.modelName()).isEqualTo(expectedModelName);
        assertThat(response.product().getId()).isEqualTo(reception.getProduct().getId());
    }

    @Test
    @DisplayName("접수증에 고객, 제품, 접수 및 담당자 정보가 포함된다")
    void getReceptionReceipt_returnsDocumentFields() {
        String expectedModelName = productModelRepository
                .findById(reception.getProduct().getProductModelId()).orElseThrow().getModelName();
        var response = asReceptionService.getReceptionReceipt(reception.getId());
        var customer = reception.getCustomer();

        assertThat(response.receptionNumber()).isEqualTo(reception.getReceptionNumber());
        assertThat(response.createdAt()).isEqualTo(reception.getCreatedAt());
        assertThat(response.customerName()).isEqualTo(customer.getName());
        assertThat(response.customerPhone()).isEqualTo(customer.getPhone());
        assertThat(response.customerEmail()).isEqualTo(customer.getEmail());
        assertThat(response.zipcode()).isEqualTo(customer.getZipcode());
        assertThat(response.basicAddress()).isEqualTo(customer.getBasicAddress());
        assertThat(response.detailAddress()).isEqualTo(customer.getDetailAddress());
        assertThat(response.modelName()).isEqualTo(expectedModelName);
        assertThat(response.serialNumber()).isEqualTo(reception.getProduct().getSerialNumber());
        assertThat(response.symptom()).isEqualTo(reception.getSymptom());
        assertThat(response.returnMethod()).isEqualTo(reception.getReturnMethod());
        assertThat(response.receptionistName()).isEqualTo(reception.getReceptionist().getName());
        assertThat(response.technicianName()).isEqualTo(reception.getTechnician().getName());
    }

    @Test
    @DisplayName("존재하지 않는 접수의 접수증을 조회하면 404를 반환한다")
    void getReceptionReceipt_receptionNotFound() {
        assertThatThrownBy(() -> asReceptionService.getReceptionReceipt(Long.MAX_VALUE))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("접수 담당자가 수정한 필드가 저장되고 생략한 증상 상세는 유지된다")
    void updateReception_ownerPersistsPartialUpdate() {
        Long id = reception.getId();
        String originalDetail = reception.getSymptomDetail();
        var request = new AsReceptionDto.UpdateReceptionRequest("전원 불량", null, "고객 확인 완료",
                ReturnMethod.PICKUP);

        var response = asReceptionService.updateReception(id, receptionist, request);
        entityManager.flush();
        entityManager.clear();

        AsReception saved = asReceptionRepository.findById(id).orElseThrow();
        assertThat(response.symptom()).isEqualTo("전원 불량");
        assertThat(saved.getSymptom()).isEqualTo("전원 불량");
        assertThat(saved.getSymptomDetail()).isEqualTo(originalDetail);
        assertThat(saved.getMemo()).isEqualTo("고객 확인 완료");
        assertThat(saved.getReturnMethod()).isEqualTo(ReturnMethod.PICKUP);
    }

    @Test
    @DisplayName("관리자는 다른 담당자가 등록한 접수의 메모를 지울 수 있다")
    void updateReception_adminCanClearMemo() {
        var admin = new CustomUserDetails(memberRepository.save(new Member(
                "edit-admin@test.com", "password", "관리자", "010-5555-6666",
                "서울시", Role.ROLE_ADMIN, "profile.png")));
        Long id = reception.getId();
        asReceptionService.updateReception(id, receptionist,
                new AsReceptionDto.UpdateReceptionRequest(null, null, "기존 메모", null));

        asReceptionService.updateReception(id, admin,
                new AsReceptionDto.UpdateReceptionRequest(null, null, "", null));
        entityManager.flush();
        entityManager.clear();

        assertThat(asReceptionRepository.findById(id).orElseThrow().getMemo()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ROLE_RECEPTIONIST", "ROLE_TECHNICIAN"})
    @DisplayName("다른 접수 담당자와 수리 기사의 수정은 거절된다")
    void updateReception_deniesUnauthorizedEmployee(Role role) {
        var employee = new CustomUserDetails(memberRepository.save(new Member(
                "edit-employee@test.com", "password", "직원", "010-5555-7777",
                "서울시", role, "profile.png")));
        String originalMemo = reception.getMemo();
        var request = new AsReceptionDto.UpdateReceptionRequest(null, null, "수정 메모", null);

        assertThatThrownBy(() -> asReceptionService.updateReception(reception.getId(), employee, request))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(reception.getMemo()).isEqualTo(originalMemo);
    }

    @Test
    @DisplayName("수리가 시작된 접수는 관리자도 수정할 수 없다")
    void updateReception_rejectsWhenRepairHasStarted() {
        var admin = new CustomUserDetails(memberRepository.save(new Member(
                "edit-admin@test.com", "password", "관리자", "010-5555-6666",
                "서울시", Role.ROLE_ADMIN, "profile.png")));
        reception.changeStatus(AsReceptionStatus.REPAIR_IN_PROGRESS);
        String originalMemo = reception.getMemo();
        var request = new AsReceptionDto.UpdateReceptionRequest(null, null, "수정 메모", null);

        assertThatThrownBy(() -> asReceptionService.updateReception(reception.getId(), admin, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(reception.getMemo()).isEqualTo(originalMemo);
    }

    private void setProductOwner(Long customerId) {
        var product = reception.getProduct();
        product.update(product.getProductModelId(), customerId, product.getSerialNumber(),
                product.getManufacturedAt(), product.getPurchasedAt(), receptionist.getId());
    }

    @Test
    @DisplayName("다른 고객 소유 제품도 접수할 수 있다")
    void create_acceptsAnotherCustomersProduct() {
        Long anotherCustomerId = asReceptionFixture.save().getCustomer().getId();
        assertReceptionCreated(anotherCustomerId);
    }

    @Test
    @DisplayName("소유 고객이 등록되지 않은 제품도 접수할 수 있다")
    void create_acceptsUnownedProduct() {
        setProductOwner(null);
        assertReceptionCreated(reception.getCustomer().getId());
    }

    private void assertReceptionCreated(Long customerId) {
        var request = request(customerId, reception.getProduct().getId(), technician.getId());

        var response = asReceptionService.createReception(receptionist, request);
        entityManager.flush();
        entityManager.clear();

        AsReception saved = asReceptionRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getCustomer().getId()).isEqualTo(customerId);
        assertThat(saved.getProduct().getId()).isEqualTo(request.productId());
    }
    @Test
    @DisplayName("접수를 등록하면 RECEIVED 상태와 연관 정보가 저장되고 접수 ID를 돌려준다")
    void create_success() {
        // given
        var request = request(reception.getCustomer().getId(), reception.getProduct().getId(), technician.getId());

        // when
        var response = asReceptionService.createReception(receptionist, request);
        entityManager.flush();
        entityManager.clear();

        // then
        AsReception saved = asReceptionRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getCustomer().getId()).isEqualTo(request.customerId());
        assertThat(saved.getProduct().getId()).isEqualTo(request.productId());
        assertThat(saved.getReceptionist().getId()).isEqualTo(receptionist.getId());
        assertThat(saved.getTechnician().getId()).isEqualTo(technician.getId());
        assertThat(saved.getSymptom()).isEqualTo("충전불량");
        assertThat(saved.getSymptomDetail()).isEqualTo("충전이 안 됩니다");
        assertThat(saved.getMemo()).isEqualTo("방문 수령 예정");
        assertThat(saved.getReturnMethod()).isEqualTo(ReturnMethod.PICKUP);
        assertThat(saved.getFileUrl()).isEqualTo("https://example.com/receipt.jpg");
        assertThat(saved.getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatusChangedAt()).isNotNull();
        assertThat(response.receptionNumber()).isEqualTo(saved.getReceptionNumber());
        assertThat(saved.getReceptionNumber()).isEqualTo("AS-%s-%05d".formatted(
                saved.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd")),
                saved.getId()));
        assertThatThrownBy(saved::issueReceptionNumber).isInstanceOf(IllegalStateException.class);
        assertThat(response.technicianId()).isEqualTo(technician.getId());
    }

    @Test
    @DisplayName("접수를 등록하면 고객 포털 링크와 최초 접수 이력이 함께 저장된다")
    void create_savesPortalLinkAndHistory() {
        // when
        var response = asReceptionService.createReception(
                receptionist,
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        technician.getId()
                )
        );

        // then
        assertThat(customerPortalLinkRepository.findByAsReceptionId(response.id())).isPresent();
        var histories = asStatusHistoryRepository.findHistoryByReceptionId(response.id());
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getFromStatus()).isNull();
        assertThat(histories.getFirst().getToStatus()).isEqualTo("RECEIVED");
        assertThat(histories.getFirst().getReason()).isEqualTo("A/S 최초 접수");
        assertThat(histories.getFirst().getChangedBy().getId()).isEqualTo(receptionist.getId());
    }

    @Test
    @DisplayName("관리자도 접수를 등록할 수 있다")
    void create_admin() {
        // given
        Member admin = memberRepository.save(new Member("admin@test.com", "password", "관리자",
                                                        "010-5555-6666", "서울시", Role.ROLE_ADMIN, "profile.png"));

        // when
        var response = asReceptionService.createReception(
                new CustomUserDetails(admin),
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        technician.getId()
                )
        );

        // then
        assertThat(asReceptionRepository.findById(response.id()).orElseThrow().getReceptionist().getId())
                .isEqualTo(admin.getId());
    }

    @Test
    @DisplayName("수리 기사는 접수를 등록할 수 없고 접수와 후속 데이터도 저장되지 않는다")
    void create_forbidden() {
        // given
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                new CustomUserDetails(technician),
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        technician.getId()
                )
        ))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("권한이 없습니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
        assertThat(asStatusHistoryRepository.count()).isZero();
        assertThat(customerPortalLinkRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 고객이면 접수되지 않고 기존 접수는 유지된다")
    void create_customerNotFound() {
        // given
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                receptionist,
                request(
                        -1L,
                        reception.getProduct().getId(),
                        technician.getId()
                )
        ))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 고객입니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("존재하지 않는 제품이면 접수되지 않는다")
    void create_productNotFound() {
        // given
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                receptionist,
                request(
                        reception.getCustomer().getId(),
                        -1L,
                        technician.getId()
                )
        ))
                .isInstanceOf(NoSuchElementException.class).hasMessage("지원하지 않는 제품입니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("존재하지 않는 담당 기사이면 접수되지 않는다")
    void create_technicianNotFound() {
        // given
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                receptionist,
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        -1L
                )
        ))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 직원입니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("등록을 요청한 직원이 없으면 접수와 후속 데이터가 저장되지 않는다")
    void create_receptionistNotFound() {
        // given
        var missingUser = missingUser();
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                missingUser,
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        technician.getId()
                )
        ))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 직원입니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
        assertThat(asStatusHistoryRepository.count()).isZero();
        assertThat(customerPortalLinkRepository.count()).isZero();
    }

    @Test
    @DisplayName("수리 기사 역할이 아닌 직원을 배정하면 접수되지 않는다")
    void create_invalidTechnicianRole() {
        // given
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                receptionist,
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        receptionist.getId()
                )
        ))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("수리 기사만 담당자로 배정할 수 있습니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("비활성 기사를 배정하면 접수되지 않는다")
    void create_inactiveTechnician() {
        // given
        technician.changeStatus(Status.INACTIVE);
        long count = asReceptionRepository.count();

        // when & then
        assertThatThrownBy(() -> asReceptionService.createReception(
                receptionist,
                request(
                        reception.getCustomer().getId(),
                        reception.getProduct().getId(),
                        technician.getId()
                )
        ))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("활성 상태의 기사만 배정할 수 있습니다.");
        assertThat(asReceptionRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("접수번호 검색에 상태와 담당 기사 필터를 함께 적용한다")
    void getReceptions_filters() {
        // given
        reception.updateTechnician(technician);
        entityManager.flush();
        var search = new AsReceptionDto.ReceptionSearchRequest(
                "  " + reception.getReceptionNumber() + "  ", AsReceptionStatus.RECEIVED, technician.getId());

        // when
        var result = asReceptionService.getReceptions(search, PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).extracting(AsReceptionDto.ListResponse::id).containsExactly(reception.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("검색어가 공백이면 검색어 제한 없이 접수 목록을 조회한다")
    void getReceptions_blankKeyword() {
        // when
        var result = asReceptionService.getReceptions(
                new AsReceptionDto.ReceptionSearchRequest("  ", null, null), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).extracting(AsReceptionDto.ListResponse::id).containsExactly(reception.getId());
    }

    @Test
    @DisplayName("접수 ID로 고객과 제품이 포함된 상세 정보를 조회한다")
    void getReception_success() {
        // when
        var response = asReceptionService.getReception(reception.getId());

        // then
        assertThat(response.id()).isEqualTo(reception.getId());
        assertThat(response.customer().getId()).isEqualTo(reception.getCustomer().getId());
        assertThat(response.product().getId()).isEqualTo(reception.getProduct().getId());
        assertThat(response.status()).isEqualTo(AsReceptionStatus.RECEIVED);
    }

    @Test
    @DisplayName("존재하지 않는 접수 상세 조회는 404 예외를 반환한다")
    void getReception_notFound() {
        // when & then
        assertThatThrownBy(() -> asReceptionService.getReception(-1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("접수 상태를 변경하면 이전 상태와 수행자 및 사유가 이력에 저장된다")
    void changeReceptionStatus_success() {
        asReceptionService.startMyInspection(reception.getId(), reception.getTechnician().getId());

        // when
        asReceptionService.changeReceptionStatus(
                reception.getId(),
                AsReceptionStatus.WARRANTY_REVIEW,
                "보증 판정",
                receptionist
        );
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(asReceptionRepository.findById(reception.getId()).orElseThrow().getStatus())
                .isEqualTo(AsReceptionStatus.WARRANTY_REVIEW);
        var history = asStatusHistoryRepository.findHistoryByReceptionId(reception.getId());
        assertThat(history).hasSize(2);
        assertThat(history.getFirst().getFromStatus()).isEqualTo("INSPECTION");
        assertThat(history.getFirst().getToStatus()).isEqualTo("WARRANTY_REVIEW");
        assertThat(history.getFirst().getReason()).isEqualTo("보증 판정");
        assertThat(history.getFirst().getChangedBy().getId()).isEqualTo(receptionist.getId());
    }

    @ParameterizedTest
    @EnumSource(value = AsReceptionStatus.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"INSPECTION", "WARRANTY_REVIEW"})
    @DisplayName("점검 중인 접수도 수동 변경으로 다른 업무 단계를 건너뛸 수 없다")
    void changeReceptionStatus_rejectsWorkflowTransitions(AsReceptionStatus target) {
        asReceptionService.startMyInspection(reception.getId(), reception.getTechnician().getId());

        assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                reception.getId(), target, "수동 변경", receptionist))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(reception.getStatus()).isEqualTo(AsReceptionStatus.INSPECTION);
        assertThat(asStatusHistoryRepository.findHistoryByReceptionId(reception.getId())).hasSize(1);
    }

    @Test
    @DisplayName("점검 기록 없이 접수 상태만 점검으로 변경할 수 없다")
    void changeReceptionStatus_requiresInspectionWorkflow() {
        assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                reception.getId(), AsReceptionStatus.INSPECTION, "수동 변경", receptionist))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(reception.getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(repairRepository.findByAsReception_Id(reception.getId())).isEmpty();
        assertThat(asStatusHistoryRepository.findHistoryByReceptionId(reception.getId())).isEmpty();
    }

    @Test
    @DisplayName("등록 담당자가 아닌 접수 담당자와 기사는 접수 상태를 수동 변경할 수 없다")
    void changeReceptionStatus_rejectsUnauthorizedStaff() {
        asReceptionService.startMyInspection(reception.getId(), reception.getTechnician().getId());
        Member otherReceptionist = memberRepository.save(new Member(
                "other-receptionist@test.com", "password", "다른 접수 담당자", "010-1234-5678",
                "서울시", Role.ROLE_RECEPTIONIST, "profile.png"));

        for (Member staff : List.of(otherReceptionist, technician)) {
            assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                    reception.getId(), AsReceptionStatus.WARRANTY_REVIEW, "보증 판정", new CustomUserDetails(staff)))
                    .isInstanceOf(AccessDeniedException.class);
        }
        assertThat(reception.getStatus()).isEqualTo(AsReceptionStatus.INSPECTION);
        assertThat(asStatusHistoryRepository.findHistoryByReceptionId(reception.getId())).hasSize(1);
    }

    @Test
    @DisplayName("같은 상태로 변경하면 예외가 발생하고 상태와 이력은 바뀌지 않는다")
    void changeReceptionStatus_sameStatus() {
        // when & then
        assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                reception.getId(), AsReceptionStatus.RECEIVED, "중복 변경", receptionist))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("이미 해당 상태로 처리되어 있습니다.");
        assertThat(asReceptionRepository.findById(reception.getId()).orElseThrow().getStatus())
                .isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(asStatusHistoryRepository.findHistoryByReceptionId(reception.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 접수는 상태를 변경할 수 없다")
    void changeReceptionStatus_notFound() {
        // when & then
        assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                -1L, AsReceptionStatus.INSPECTION, "점검 시작", receptionist))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 A/S 접수입니다.");
        assertThat(asStatusHistoryRepository.count()).isZero();
    }

    @Test
    @DisplayName("담당 기사를 재배정하면 변경 내용이 DB에 저장된다")
    void updateTechnician_success() {
        Long previousTechnicianId = reception.getTechnician().getId();
        var repair = asReceptionService.startMyInspection(reception.getId(), previousTechnicianId);

        // when
        var response = asReceptionService.updateTechnician(
                reception.getId(), technician.getId(), REASSIGNMENT_REASON, receptionist.getMember());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response.technician().id()).isEqualTo(technician.getId());
        assertThat(asReceptionRepository.findById(reception.getId()).orElseThrow().getTechnician().getId())
                .isEqualTo(technician.getId());

        assertThat(repairRepository.findById(repair.id()).orElseThrow().getTechnician().getId())
                .isEqualTo(technician.getId());
        assertThat(repairRepository.findByAsReception_Technician_Id(previousTechnicianId)).isEmpty();
        assertThat(repairRepository.findByAsReception_Technician_Id(technician.getId()))
                .extracting(it -> it.getId()).containsExactly(repair.id());

        var history = assignmentHistoryRepository.findAll();
        assertThat(history).singleElement().satisfies(entry -> {
            assertThat(((AsReception) ReflectionTestUtils.getField(entry, "asReception")).getId())
                    .isEqualTo(reception.getId());
            assertThat(((Member) ReflectionTestUtils.getField(entry, "previousTechnician")).getId())
                    .isEqualTo(previousTechnicianId);
            assertThat(((Member) ReflectionTestUtils.getField(entry, "newTechnician")).getId())
                    .isEqualTo(technician.getId());
            assertThat(ReflectionTestUtils.getField(entry, "reason")).isEqualTo(REASSIGNMENT_REASON);
            assertThat(ReflectionTestUtils.getField(entry, "createdAt")).isNotNull();
            assertThat(((Member) ReflectionTestUtils.getField(entry, "changedBy")).getId())
                    .isEqualTo(receptionist.getId());
        });
    }

    @Test
    @DisplayName("상태 변경을 요청한 직원이 없으면 기존 상태와 이력은 유지된다")
    void changeReceptionStatus_memberNotFound() {
        // when & then
        assertThatThrownBy(() -> asReceptionService.changeReceptionStatus(
                reception.getId(), AsReceptionStatus.INSPECTION, "점검 시작", missingUser()))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 직원입니다.");
        assertThat(reception.getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(asStatusHistoryRepository.findHistoryByReceptionId(reception.getId())).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = AsReceptionStatus.class, names = {
            "AWAITING_PAYMENT", "AWAITING_SHIPMENT", "AWAITING_PICKUP", "COMPLETED"
    })
    @DisplayName("결제 대기 이후에는 기사를 재배정할 수 없고 기존 담당자는 유지된다")
    void updateTechnician_closedReception(AsReceptionStatus status) {
        // given
        reception.changeStatus(status);
        Long previousTechnicianId = reception.getTechnician().getId();
        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> asReceptionService.updateTechnician(
                reception.getId(), technician.getId(), REASSIGNMENT_REASON, receptionist.getMember()))
                .isInstanceOf(NoSuchElementException.class);
        assertThat(asReceptionRepository.findById(reception.getId()).orElseThrow().getTechnician().getId())
                .isEqualTo(previousTechnicianId);
        assertThat(assignmentHistoryRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 기사는 재배정할 수 없고 기존 담당자는 유지된다")
    void updateTechnician_notFound() {
        // given
        Long previousTechnicianId = reception.getTechnician().getId();

        // when & then
        assertThatThrownBy(() -> asReceptionService.updateTechnician(
                reception.getId(), -1L, REASSIGNMENT_REASON, receptionist.getMember()))
                .isInstanceOf(NoSuchElementException.class).hasMessage("존재하지 않는 직원입니다.");
        assertThat(reception.getTechnician().getId()).isEqualTo(previousTechnicianId);
        assertThat(assignmentHistoryRepository.count()).isZero();
    }

    @Test
    @DisplayName("수리 기사가 아닌 직원으로 재배정하면 기존 담당자는 유지된다")
    void updateTechnician_invalidRole() {
        // given
        Long previousTechnicianId = reception.getTechnician().getId();

        // when & then
        assertThatThrownBy(() -> asReceptionService.updateTechnician(
                reception.getId(), receptionist.getId(), REASSIGNMENT_REASON, receptionist.getMember()))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("수리 기사만 담당자로 배정할 수 있습니다.");
        assertThat(reception.getTechnician().getId()).isEqualTo(previousTechnicianId);
        assertThat(assignmentHistoryRepository.count()).isZero();
    }

    @Test
    @DisplayName("비활성 기사로 재배정하면 기존 담당자는 유지된다")
    void updateTechnician_inactive() {
        // given
        technician.changeStatus(Status.INACTIVE);
        Long previousTechnicianId = reception.getTechnician().getId();

        // when & then
        assertThatThrownBy(() -> asReceptionService.updateTechnician(
                reception.getId(), technician.getId(), REASSIGNMENT_REASON, receptionist.getMember()))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("활성 상태의 기사만 배정할 수 있습니다.");
        assertThat(reception.getTechnician().getId()).isEqualTo(previousTechnicianId);
        assertThat(assignmentHistoryRepository.count()).isZero();
    }

    private CustomUserDetails missingUser() {
        Member member = new Member("missing@test.com", "password", "없는직원", "010-0000-0000",
                                   "서울시", Role.ROLE_RECEPTIONIST, "profile.png");
        ReflectionTestUtils.setField(member, "id", -1L);
        return new CustomUserDetails(member);
    }

    private AsReceptionDto.CreateReceptionRequest request(Long customerId, Long productId, Long technicianId) {
        return new AsReceptionDto.CreateReceptionRequest(
                customerId,
                productId,
                technicianId,
                "충전불량",
                "충전이 안 됩니다",
                "방문 수령 예정",
                ReturnMethod.PICKUP,
                "https://example.com/receipt.jpg"
        );
    }
}
