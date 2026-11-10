package net.likelion.bebc25.fixgo.customer.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.customer.dto.CustomerDto;
import net.likelion.bebc25.fixgo.customer.excel.CustomerExcelService;
import net.likelion.bebc25.fixgo.customer.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerExcelService customerExcelService;

    // id로 CustomerDto.Response 반환
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    // 연락처 중복 체크
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam String phone
    ) {
        boolean duplicated = customerService.checkDuplicate(
                new CustomerDto.DuplicateCheckRequest(phone)
        );
        return ResponseEntity.ok(Map.of("duplicated", duplicated));
    }

    // 고객 등록
    @PostMapping
    public ResponseEntity<CustomerDto.Response> create(
            @RequestBody CustomerDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long createdBy = user.getMember().getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.create(request, createdBy));
    }

    // 고객 검색시 해당되는 결과 반환
    @GetMapping
    public ResponseEntity<List<CustomerDto.Response>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email
    ) {
        return ResponseEntity.ok(
                customerService.search(new CustomerDto.SearchRequest(name, phone, email))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDto.Response>> searchPage(
            @ModelAttribute CustomerDto.SearchRequest request,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(customerService.searchPage(request, pageable));
    }


    // 고객 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> update(
            @PathVariable Long id,
            @RequestBody CustomerDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedBy = user.getMember().getId();
        return ResponseEntity.ok(customerService.update(id, request, updatedBy));
    }

    // excel 파일 양식 다운로드
    @GetMapping("/bulk/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] body = customerExcelService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''customer_template.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    // 고객 정보 등록 전 검증
    @PostMapping(value = "/bulk/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerDto.BulkValidateResponse> validate(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(customerExcelService.parseAndValidate(file));
    }

    // 고객 정보 일괄 등록
    @PostMapping("/bulk")
    public ResponseEntity<CustomerDto.BulkCreateResponse> bulkCreate(
            @RequestBody CustomerDto.BulkCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long createdBy = user.getMember().getId();
        return ResponseEntity.ok(customerService.bulkCreate(request, createdBy));
    }

}
