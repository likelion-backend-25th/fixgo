package net.likelion.bebc25.fixgo.as.controller;

import net.likelion.bebc25.fixgo.admin.controller.AsReceptionController;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AsReceptionController.class)
class AsReceptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsStatusHistoryService asStatusHistoryService;

    @MockitoBean
    private AsReceptionService asReceptionService;

    private CustomUserDetails createUserDetails() {
        Member member = new Member("test@example.com", "password", "테스트 직원", "010-1234-5678", "서울시 강남구", Role.ROLE_ADMIN, "profile.png");

        return new CustomUserDetails(member);
    }

    // A/S 접수 등록 테스트
    @Test
    void createReceptionTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        AsReceptionDto.DetailResponse response = mock(AsReceptionDto.DetailResponse.class);

        when(asReceptionService.createReception(any(CustomUserDetails.class), any(AsReceptionDto.CreateReceptionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/as").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                        {
                                                                                                                                            "customerId": 1,
                                                                                                                                            "productId": 1,
                                                                                                                                            "technicianId": 2,
                                                                                                                                            "symptom": "전원 불량",
                                                                                                                                            "symptomDetail": "전원이 켜지지 않습니다.",
                                                                                                                                            "memo": "빠른 점검 요청",
                                                                                                                                            "returnMethod": "DELIVERY"
                                                                                                                                        }
                                                                                                                                        """)).andExpect(status().isOk());

        verify(asReceptionService, times(1)).createReception(any(CustomUserDetails.class), any(AsReceptionDto.CreateReceptionRequest.class));
    }

    // A/S 접수 상태 변경 테스트
    // A/S 접수 상태 변경 테스트
    @Test
    void changeReceptionStatusTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        mockMvc.perform(patch("/api/v1/as/1/status").with(csrf()).with(user(userDetails)).param("newStatus", "INSPECTION").param("reason", "제품 점검 시작")).andExpect(status().isOk());

        verify(asReceptionService, times(1)).changeReceptionStatus(eq(1L), eq(AsReceptionStatus.INSPECTION), eq("제품 점검 시작"), any(CustomUserDetails.class));
    }

    // A/S 접수 상태 변경 - 사유 없이 요청
    @Test
    void changeReceptionStatusWithoutReasonTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        mockMvc.perform(patch("/api/v1/as/1/status").with(csrf()).with(user(userDetails)).param("newStatus", "INSPECTION")).andExpect(status().isOk());

        verify(asReceptionService, times(1)).changeReceptionStatus(eq(1L), eq(AsReceptionStatus.INSPECTION), isNull(), any(CustomUserDetails.class));
    }

    // 잘못된 상태값 요청 테스트
    @Test
    void changeReceptionStatusInvalidStatusTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        mockMvc.perform(patch("/api/v1/as/1/status").with(csrf()).with(user(userDetails)).param("newStatus", "INVALID_STATUS")).andExpect(status().isBadRequest());

        verify(asReceptionService, never()).changeReceptionStatus(anyLong(), any(), any(), any());
    }

    // A/S 상태 변경 이력 조회 테스트
    @Test
    void getHistoryTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        AsStatusHistoryDto response = new AsStatusHistoryDto(1L, "RECEIVED", "INSPECTION", "제품 점검 시작", "테스트 직원", LocalDateTime.of(2026, 11, 5, 10, 30));

        when(asStatusHistoryService.getHistory(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/as/1/history").with(csrf()).with(user(userDetails))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[0].fromStatus").value("RECEIVED")).andExpect(jsonPath("$[0].toStatus").value("INSPECTION")).andExpect(jsonPath("$[0].reason").value("제품 점검 시작")).andExpect(jsonPath("$[0].changedBy").value("테스트 직원"));

        verify(asStatusHistoryService, times(1)).getHistory(1L);
    }
}
