package net.likelion.bebc25.fixgo.customer.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.customer.dto.CustomerDto;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // id로 customer entity 조회(내부용)
    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 고객입니다."));
    }

    // 고객 상세조회(Controller, Dto 반환용)
    public CustomerDto.Response getById(Long id) {
        return CustomerDto.Response.from(findCustomer(id));
    }

    // 연락처 중복 확인
    public boolean checkDuplicate(CustomerDto.DuplicateCheckRequest request) {
        return customerRepository.existsByPhone(request.phone());
    }

    // 고객 등록
    @Transactional
    public CustomerDto.Response create(CustomerDto.CreateRequest request, Long createdBy) {
        if (customerRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("이미 등록된 연락처입니다.");
        }
        Customer customer = customerRepository.save(request.toEntity(createdBy));
        return CustomerDto.Response.from(customer);
    }

    public List<CustomerDto.Response> search(CustomerDto.SearchRequest request) {
        // 고객명 검색
        if (request.name() != null && !request.name().isBlank()) {
            return customerRepository.findByNameContaining(request.name())
                    .stream()
                    .map(CustomerDto.Response::from)
                    .toList();
        }
        // 전화번호 검색
        else if (request.phone() != null && !request.phone().isBlank()) {
            return customerRepository.findByPhoneContaining(request.phone())
                    .stream()
                    .map(CustomerDto.Response::from)
                    .toList();
        }
        // 이메일 검색
        else if (request.email() != null && !request.email().isBlank()) {
            return customerRepository.findByEmailContaining(request.email())
                    .stream()
                    .map(CustomerDto.Response::from)
                    .toList();
        } else {
            return List.of();
        }
    }

    public Page<CustomerDto.Response> searchPage(CustomerDto.SearchRequest request, Pageable pageable) {
        if (request.name() != null && !request.name().isBlank()) {
            return customerRepository.findByNameContaining(request.name(), pageable).map(CustomerDto.Response::from);
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            return customerRepository.findByPhoneContaining(request.phone(), pageable).map(CustomerDto.Response::from);
        }
        if (request.email() != null && !request.email().isBlank()) {
            return customerRepository.findByEmailContaining(request.email(), pageable).map(CustomerDto.Response::from);
        }
        return Page.empty(pageable);
    }


    // 고객 정보 수정
    @Transactional
    public CustomerDto.Response update(Long id, CustomerDto.UpdateRequest request, Long updatedBy) {
        Customer customer = findCustomer(id);
        customer.update(
                request.name(), request.phone(), request.email(),
                request.zipcode(), request.basicAddress(), request.detailAddress(),
                updatedBy
        );
        return CustomerDto.Response.from(customer);
    }

    // excel로 고객 정보 일괄 등록 요청
    @Transactional
    public CustomerDto.BulkCreateResponse bulkCreate(CustomerDto.BulkCreateRequest request, Long createdBy) {
        // null값 처리
        if (request == null || request.customers() == null || request.customers().isEmpty()) {
            throw new IllegalArgumentException("등록할 고객 정보가 없습니다.");
        }

        // 등록 연락처 후보 추출
        List<CustomerDto.BulkCreateItem> items = request.customers();
        Set<String> seenPhones = new HashSet<>();
        Set<String> requestPhones = items.stream()
                .map(CustomerDto.BulkCreateItem::phone)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toSet());

        // db에서 연락처 중복 체크
        Set<String> existingPhones = requestPhones.isEmpty()
                ? Set.of()
                : customerRepository.findByPhoneIn(requestPhones).stream()
                .map(Customer::getPhone)
                .collect(Collectors.toSet());

        List<Customer> toSave = new ArrayList<>();
        List<CustomerDto.BulkFailure> failures = new ArrayList<>();

        // 고객 정보 등록 loop
        for (CustomerDto.BulkCreateItem item : items) {
            String reason = null;
            // elements null값 처리 및 등록 불가 사유 정의
            if (item.name() == null || item.name().isBlank()
                    || item.phone() == null || item.phone().isBlank()
                    || item.email() == null || item.email().isBlank()) {
                reason = "이름, 연락처, 이메일은 필수입니다.";
            } else if (!seenPhones.add(item.phone())) {
                reason = "요청 내 중복된 연락처입니다.";
            } else if (existingPhones.contains(item.phone())) {
                reason = "이미 등록된 연락처입니다.";
            }

            if (reason != null) {
                failures.add(new CustomerDto.BulkFailure(item.rowNumber(), reason));
                continue;
            }

            toSave.add(new CustomerDto.CreateRequest(
                    item.name(), item.phone(), item.email(),
                    item.zipcode(), item.basicAddress(), item.detailAddress()
            ).toEntity(createdBy));
        }

        if (!toSave.isEmpty()) {
            customerRepository.saveAll(toSave);
        }

        return new CustomerDto.BulkCreateResponse(
                toSave.size(),
                failures.size(),
                failures
        );
    }
}
