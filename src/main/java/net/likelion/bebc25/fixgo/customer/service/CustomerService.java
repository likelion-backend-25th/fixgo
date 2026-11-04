package net.likelion.bebc25.fixgo.customer.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.customer.dto.CustomerDto;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
}
