package net.likelion.bebc25.fixgo.customer.excel;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.customer.dto.CustomerDto;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerExcelService {

    private static final String[] HEADERS = {
            "이름", "연락처", "이메일", "우편번호", "기본주소", "상세주소"
    };
    private static final int MAX_ROWS = 1000;
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[016789]-?\\d{3,4}-?\\d{4}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private final CustomerRepository customerRepository;

    public byte[] generateTemplate() {
        // .xlsx 파일 객체 생성
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("고객등록엑셀템플릿");
            // excel 1행에 데이터 헤더 출력
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            // 입력 데이터 write
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("양식 파일 생성에 실패했습니다.", e);
        }
    }

    // data 등록 전 양식/중복 연락처 검수
    private String validateBasics(String name, String phone, String email, Set<String> seenPhones) {
        if (name == null || name.isBlank()
                || phone == null || phone.isBlank()
                || email == null || email.isBlank()) {
            return "이름, 연락처, 이메일은 필수입니다.";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "연락처 형식이 올바르지 않습니다.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "이메일 형식이 올바르지 않습니다.";
        }
        if (!seenPhones.add(phone)) {
            return "파일 내 중복된 연락처입니다.";
        }
        return null;
    }

    // 셀의 내용을 return
    private String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private boolean isBlankRow(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            if (!getCellString(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    // 업로드한 excel 파일 검증 후 결과 반환
    public CustomerDto.BulkValidateResponse parseAndValidate(MultipartFile file) {
        // excel 파일 유효성 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다.");
        }
        String fileName = file.getOriginalFilename();
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("파일이 올바른 엑셀 형식이 아닙니다.");
        }

        try (InputStream in = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            // 파일 헤더 검사
            if (sheet.getRow(0) == null) {
                throw new IllegalArgumentException("헤더가 없습니다.");
            }
            for (int i = 0; i < HEADERS.length; i++) {
                String value = getCellString(sheet.getRow(0), i);
                if (!HEADERS[i].equals(value)) {
                    throw new IllegalArgumentException("올바른 헤더 형식이 아닙니다.");
                }
            }

            // 1행부터 data 검사
            List<CustomerDto.BulkRowResult> rows = new ArrayList<>();
            Set<String> seenPhones = new HashSet<>();
            int dataRowCount = 0;

            // null이나 빈 행은 pass
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row)) {
                    continue;
                }

                dataRowCount++;
                if (dataRowCount > MAX_ROWS) {
                    throw new IllegalArgumentException("한 번에 등록할 수 있는 최대 행 수는 " + MAX_ROWS + "건입니다.");
                }

                // 고객 정보
                int rowNumber = i + 1;
                String name = getCellString(row, 0);
                String phone = getCellString(row, 1);
                String email = getCellString(row, 2);
                String zipcode = getCellString(row, 3).isBlank() ? null : getCellString(row, 3);
                String basicAddress = getCellString(row, 4).isBlank() ? null : getCellString(row, 4);
                String detailAddress = getCellString(row, 5).isBlank() ? null : getCellString(row, 5);

                String reason = validateBasics(name, phone, email, seenPhones);
                boolean registrable = (reason == null);

                rows.add(new CustomerDto.BulkRowResult(
                        rowNumber, name, phone, email,
                        zipcode, basicAddress, detailAddress,
                        registrable, reason
                ));
            }
            // 등록 예정 정보 중 연락처만 추출
            Set<String> candidatePhones = rows.stream()
                    .filter(CustomerDto.BulkRowResult::registrable)
                    .map(CustomerDto.BulkRowResult::phone)
                    .collect(Collectors.toSet());

            // 연락처 중복 체크
            Set<String> existingPhones = candidatePhones.isEmpty()
                    ? Set.of()
                    : customerRepository.findByPhoneIn(candidatePhones).stream()
                    .map(Customer::getPhone)
                    .collect(Collectors.toSet());

            // 고객 정보 등록
            List<CustomerDto.BulkRowResult> finalRows = new ArrayList<>();
            int validCount = 0;
            for (CustomerDto.BulkRowResult row : rows) {
                if (row.registrable() && existingPhones.contains(row.phone())) {
                    finalRows.add(new CustomerDto.BulkRowResult(
                            row.rowNumber(), row.name(), row.phone(), row.email(),
                            row.zipcode(), row.basicAddress(), row.detailAddress(),
                            false, "이미 등록된 연락처입니다."
                    ));
                } else {
                    finalRows.add(row);
                    if (row.registrable()) {
                        validCount++;
                    }
                }
            }

            int totalCount = finalRows.size();
            int invalidCount = totalCount - validCount;

            return new CustomerDto.BulkValidateResponse(
                    fileName, totalCount, validCount, invalidCount, finalRows
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("파일이 없거나 올바른 엑셀 형식이 아닙니다.", e);
        }

    }
}
