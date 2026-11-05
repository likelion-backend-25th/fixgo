package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AsReceptionService {

    private final AsReceptionRepository asReceptionRepository;
    private final AsStatusHistoryService asStatusHistoryService;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 접수 등록
    @Transactional
    public AsReceptionDto.DetailResponse createReception(CustomUserDetails userDetails, AsReceptionDto.CreateReceptionRequest request) {
        if (userDetails.getMember().getRole().equals(Role.ROLE_TECHNICIAN)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        Customer customer = customerRepository.findById(request.customerId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 고객입니다."));

        Product product = productRepository.findById(request.productId()).orElseThrow(() -> new NoSuchElementException("지원하지 않는 제품입니다."));

        Member receptionist = memberRepository.findById(userDetails.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        Member technician = memberRepository.findById(request.technicianId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        // A/S 접수 등록
        AsReception asReception = asReceptionRepository.save(request.toEntity(customer, product, receptionist, technician));

        // 최초 접수 상태 이력 저장
        asStatusHistoryService.record(asReception, null, AsReceptionStatus.RECEIVED.name(), "A/S 최초 접수", receptionist);

        return AsReceptionDto.DetailResponse.from(asReception);
    }

    // A/S 접수 상태 변경
    @Transactional
    public void changeReceptionStatus(Long receptionId, AsReceptionStatus newStatus, String reason, CustomUserDetails userDetails) {
        AsReception asReception = asReceptionRepository.findById(receptionId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 A/S 접수입니다."));

        AsReceptionStatus currentStatus = asReception.getStatus();

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("이미 해당 상태로 처리되어 있습니다.");
        }

        Member changedBy = memberRepository.findById(userDetails.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 직원입니다."));

        asReception.changeStatus(newStatus);

        asStatusHistoryService.record(asReception, currentStatus.name(), newStatus.name(), reason, changedBy);
    }
}
