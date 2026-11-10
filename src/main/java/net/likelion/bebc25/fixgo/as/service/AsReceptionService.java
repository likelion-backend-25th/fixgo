package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsTechnicianAssignmentHistory;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.repository.AsTechnicianAssignmentHistoryRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.portal.dto.CustomerPortalLinkResponse;
import net.likelion.bebc25.fixgo.portal.service.CustomerPortalLinkService;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AsReceptionService {

    private final AsReceptionRepository asReceptionRepository;
    private final AsStatusHistoryService asStatusHistoryService;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CustomerPortalLinkService customerPortalLinkService;
    private final AsTechnicianAssignmentHistoryRepository assignmentHistoryRepository;
    private final ProductModelRepository productModelRepository;
    private final RepairRepository repairRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 접수 등록
    @Transactional
    public AsReceptionDto.CreateReceptionResponse createReception(
            CustomUserDetails userDetails,
            AsReceptionDto.CreateReceptionRequest request
    ) {
        if (userDetails.getMember().getRole().equals(Role.ROLE_TECHNICIAN)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 고객입니다."));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NoSuchElementException("지원하지 않는 제품입니다."));

        Member receptionist = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        Member technician = memberRepository.findById(request.technicianId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        // 기사 역할 확인
        if (technician.getRole() != Role.ROLE_TECHNICIAN) {
            throw new IllegalArgumentException("수리 기사만 담당자로 배정할 수 있습니다.");
        }

        // 활성 상태 확인
        if (technician.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 기사만 배정할 수 있습니다.");
        }

        // A/S 접수 등록
        AsReception asReception = asReceptionRepository.save(
                request.toEntity(customer,
                                 product,
                                 receptionist,
                                 technician)
        );

        // 고객 포털 링크 발급
        String customerLink = customerPortalLinkService.createLink(asReception.getId());

        // 최초 접수 상태 이력 저장
        asStatusHistoryService.record(asReception, null, AsReceptionStatus.RECEIVED.name(), "A/S 최초 접수", receptionist);

        // 접수 완료 이메일 이벤트
        eventPublisher.publishEvent(
                new ReceptionCreatedEvent(
                        asReception.getId(),
                        asReception.getReceptionNumber(),
                        customer.getName(),
                        customer.getEmail(),
                        customerLink
                )
        );
        return AsReceptionDto.CreateReceptionResponse.from(asReception);
    }

    // A/S 접수 상태 변경
    @Transactional
    public void changeReceptionStatus(
            Long receptionId,
            AsReceptionStatus newStatus,
            String reason,
            CustomUserDetails userDetails
    ) {
        AsReception asReception = asReceptionRepository.findByIdWithLock(receptionId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 A/S 접수입니다."));

        AsReceptionStatus currentStatus = asReception.getStatus();

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("이미 해당 상태로 처리되어 있습니다.");
        }

        Member changedBy = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        asReception.changeStatus(newStatus);

        asStatusHistoryService.record(asReception, currentStatus.name(), newStatus.name(), reason, changedBy);
    }

    // 접수번호, 고객명, 증상 검색과 상태/담당 기사 필터
    public Page<AsReceptionDto.ListResponse> getReceptions(
            AsReceptionDto.ReceptionSearchRequest request,
            Pageable pageable
    ) {
        String searchKeyword = request.keyword() == null || request.keyword().isBlank()
                ? null
                : request.keyword().trim();
        return asReceptionRepository.search(searchKeyword, request.status(), request.technicianId(), pageable);
    }

    // 고객에게 발급한 안내 링크 조회
    public CustomerPortalLinkResponse getPortalLink(Long id) {
        if (!asReceptionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 접수입니다.");
        }
        return customerPortalLinkService.getLink(id);
    }

    // 접수 상세 조회
    public AsReceptionDto.DetailResponse getReception(Long id) {
        AsReception reception = asReceptionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 접수입니다."));
        return AsReceptionDto.DetailResponse.from(reception, getModelName(reception));
    }

    // 접수증 조회
    public AsReceptionDto.ReceptionReceiptResponse getReceptionReceipt(Long id) {
        AsReception reception = asReceptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 접수입니다."
                ));

        return AsReceptionDto.ReceptionReceiptResponse.from(reception, getModelName(reception));
    }

    // 고객 포탈 접수 영역 조회
    public AsReceptionDto.CustomerReceptionView getCustomerView(Long id) {
        AsReception reception = asReceptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 접수입니다."
                ));

        return AsReceptionDto.CustomerReceptionView.of(
                reception,
                getModelName(reception),
                asStatusHistoryService.getCustomerHistory(id)
        );
    }

    private String getModelName(AsReception reception) {
        return productModelRepository
                .findById(reception.getProduct().getProductModelId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 제품 모델입니다."
                ))
                .getModelName();
    }

    @Transactional
    public AsReceptionDto.DetailResponse updateReception(
            Long id,
            CustomUserDetails userDetails,
            AsReceptionDto.UpdateReceptionRequest request
    ) {
        Role role = userDetails.getMember().getRole();
        if (role != Role.ROLE_ADMIN && role != Role.ROLE_RECEPTIONIST) {
            throw new AccessDeniedException("접수 정보를 수정할 권한이 없습니다.");
        }

        AsReception reception = asReceptionRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 A/S 접수입니다."));
        if (role == Role.ROLE_RECEPTIONIST
                && !reception.getReceptionist().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("해당 건을 등록한 접수 담당자만 수정할 수 있습니다.");
        }

        switch (reception.getStatus()) {
            case RECEIVED, INSPECTION, WARRANTY_REVIEW,
                 AWAITING_QUOTE_APPROVAL, AWAITING_REPAIR -> reception.updateDetails(
                    request.symptom(),
                    request.symptomDetail(),
                    request.memo(),
                    request.returnMethod()
            );
            default -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "수리 시작 전 접수만 수정할 수 있습니다."
            );
        }

        return AsReceptionDto.DetailResponse.from(reception, getModelName(reception));
    }

    // 담당 기사 재배정
    @Transactional
    public AsReceptionDto.DetailResponse updateTechnician(
            Long receptionId,
            Long technicianId,
            String reason,
            Member changedBy
    ) {
        List<AsReceptionStatus> statuses = new ArrayList<>();
        statuses.add(AsReceptionStatus.AWAITING_PAYMENT);
        statuses.add(AsReceptionStatus.AWAITING_SHIPMENT);
        statuses.add(AsReceptionStatus.AWAITING_PICKUP);
        statuses.add(AsReceptionStatus.COMPLETED);
        AsReception reception = asReceptionRepository.findByIdAndStatusNotIn(receptionId, statuses)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 접수입니다."
                ));

        Member technician = memberRepository.findById(technicianId).
                orElseThrow(() -> new NoSuchElementException(
                        "존재하지 않는 직원입니다."
                ));

        if (technician.getRole() != Role.ROLE_TECHNICIAN) {
            throw new IllegalArgumentException("수리 기사만 담당자로 배정할 수 있습니다.");
        }

        if (technician.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 기사만 배정할 수 있습니다.");
        }

        Member previousTechnician = reception.getTechnician();
        reception.updateTechnician(technician);
        repairRepository.findByAsReception_Id(receptionId)
                .ifPresent(repair -> repair.updateTechnician(technician));

        assignmentHistoryRepository.save(new AsTechnicianAssignmentHistory(
                reception,
                previousTechnician,
                technician,
                reason,
                changedBy
        ));

        return AsReceptionDto.DetailResponse.from(reception, getModelName(reception));
    }

    @Transactional(readOnly = true)
    public List<AsReceptionDto.TechnicianAssignmentHistoryResponse>
    getTechnicianAssignmentHistory(Long receptionId) {
        return assignmentHistoryRepository
                .findByAsReception_IdOrderByCreatedAtDescIdDesc(receptionId)
                .stream()
                .map(AsReceptionDto.TechnicianAssignmentHistoryResponse::from)
                .toList();
    }

    // 로그인한 기사에게 현재 배정된 접수의 상세 정보를 조회한다.
    public AsReceptionDto.MyReceptionDetailResponse getMyReception(Long id, Long technicianId) {
        AsReception reception = asReceptionRepository
                .findByIdAndTechnician_Id(id, technicianId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "조회할 수 있는 접수가 없습니다."
                ));

        var detail = AsReceptionDto.DetailResponse.from(reception, getModelName(reception));
        var repair = repairRepository.findByAsReception_Id(id)
                .map(RepairResponse::from)
                .orElse(null);
        return new AsReceptionDto.MyReceptionDetailResponse(detail, repair);
    }

    // 담당 기사의 점검 기록을 생성하고, 재호출 시 기존 기록을 반환한다.
    @Transactional
    public RepairResponse startMyInspection(Long id, Long technicianId) {
        AsReception reception = asReceptionRepository.findByIdWithLock(id)
                .filter(it -> it.getTechnician().getId().equals(technicianId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "조회할 수 있는 접수가 없습니다."
                ));

        // 첫 호출에서 접수 상태가 INSPECTION 으로 바뀌므로 기존 기록을 먼저 찾는다.
        Optional<Repair> existingRepair = repairRepository.findByAsReception_Id(id);
        if (existingRepair.isPresent()) {
            return RepairResponse.from(existingRepair.get());
        }

        AsReceptionStatus previousStatus = reception.getStatus();
        if (previousStatus != AsReceptionStatus.RECEIVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "점검을 시작할 수 있는 접수 상태가 아닙니다."
            );
        }

        Member technician = reception.getTechnician();
        Repair repair = repairRepository.save(new Repair(
                reception,
                technician,
                WarrantyResult.UNDECIDED,
                RepairStatus.DIAGNOSING
        ));

        reception.changeStatus(AsReceptionStatus.INSPECTION);
        asStatusHistoryService.record(
                reception,
                previousStatus.name(),
                AsReceptionStatus.INSPECTION.name(),
                "담당 기사 점검 시작",
                technician
        );

        return RepairResponse.from(repair);
    }

    // A/S 접수 완료 이메일 발송 이벤트
    public record ReceptionCreatedEvent(
            Long asReceptionId,
            String receptionNumber,
            String customerName,
            String recipientEmail,
            String customerLink
    ) {}

}
