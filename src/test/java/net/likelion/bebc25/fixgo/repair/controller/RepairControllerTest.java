package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.handler.CustomAccessDeniedHandler;
import net.likelion.bebc25.fixgo.auth.handler.CustomAuthenticationEntryPoint;
import net.likelion.bebc25.fixgo.auth.jwt.JwtProvider;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.service.CustomUserDetailsService;
import net.likelion.bebc25.fixgo.global.security.SecurityConfig;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(RepairController.class)
@Import(SecurityConfig.class)
class RepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairService repairService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    // 테스트용 기사 계정 생성
    private CustomUserDetails createUserDetails() {

        Member member = new Member(
                "test@example.com",
                "password",
                "테스트 직원",
                "010-1234-5678",
                "서울시 강남구",
                Role.ROLE_TECHNICIAN,
                "profile.png"
        );

        return new CustomUserDetails(member);
    }

    // 테스트용 관리자 계정 생성
    private CustomUserDetails createAdminUserDetails() {

        Member member = new Member(
                "admin@example.com",
                "password",
                "테스트 관리자",
                "010-1234-5679",
                "서울시 강남구",
                Role.ROLE_ADMIN,
                "profile.png"
        );

        return new CustomUserDetails(member);
    }

    // Record 생성 방식의 테스트용 응답 DTO
    private RepairResponse createRepairResponse() {

        return new RepairResponse(
                1L,
                10L,
                20L,
                "메인보드 점검",
                "부품 교체",
                RepairDecision.REPAIRABLE,
                "FREE",
                "보증 기간 내",
                10000,
                5000,
                15000,
                "REQUESTED",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L,
                1L
        );
    }

    // 수리 등록 테스트
    @Test
    void createRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        when(repairService.createRepair(
                any(RepairRequest.class),
                eq("test@example.com")
        )).thenReturn(
                new RepairMessageResponse("수리 정보가 등록되었습니다.")
        );

        mockMvc.perform(post("/api/v1/as/repairs")
                                .with(csrf())
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "asReceptionId": 10,
                                                     "technicianId": 20,
                                                     "diagnosis": "메인보드 점검",
                                                     "warrantyResult": "FREE",
                                                     "status": "REQUESTED",
                                                     "laborRate": 0,
                                                     "inspectionCost": 0,
                                                     "finalAmount": 0
                                                 }
                                                 """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                                   .value("수리 정보가 등록되었습니다."));

        verify(repairService, times(1))
                .createRepair(any(RepairRequest.class), eq("test@example.com"));
    }

    // 전체 수리 목록 조회 테스트
    @Test
    @WithMockUser
    void getAllRepairsTest() throws Exception {

        when(repairService.getAllRepairs())
                .thenReturn(List.of(createRepairResponse()));

        mockMvc.perform(get("/api/v1/as/repairs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(repairService, times(1)).getAllRepairs();
    }

    // 수리 상세 조회 테스트
    @Test
    @WithMockUser
    void getRepairTest() throws Exception {

        when(repairService.getRepair(1L))
                .thenReturn(createRepairResponse());

        mockMvc.perform(get("/api/v1/as/repairs/1"))
                .andExpect(status().isOk());

        verify(repairService, times(1)).getRepair(1L);
    }

    // A/S 접수번호로 수리 상세 조회 테스트
    @Test
    @WithMockUser
    void getRepairByReceptionNumberTest() throws Exception {

        String receptionNumber = "AS-2026-1112";

        when(repairService.getRepairByReceptionNumber(receptionNumber))
                .thenReturn(createRepairResponse());

        mockMvc.perform(get(
                        "/api/v1/as/repairs/reception/{receptionNumber}",
                        receptionNumber
                ))
                .andExpect(status().isOk());

        verify(repairService, times(1))
                .getRepairByReceptionNumber(receptionNumber);
    }

    // 본인 담당 수리 목록 조회 테스트
    @Test
    void getMyRepairsTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        when(repairService.getMyRepairs(userDetails.getId()))
                .thenReturn(List.of(createRepairResponse()));

        mockMvc.perform(get("/api/v1/as/repairs/my")
                                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(repairService, times(1))
                .getMyRepairs(userDetails.getId());
    }

    // 본인 담당 수리 상세 조회 테스트
    @Test
    void getMyRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairResponse response = new RepairResponse(
                1L,
                10L,
                userDetails.getId(),
                "메인보드 점검",
                "부품 교체",
                RepairDecision.REPAIRABLE,
                "FREE",
                "보증 기간 내",
                10000,
                5000,
                15000,
                "REQUESTED",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L,
                1L
        );

        when(repairService.getMyRepair(1L, userDetails.getId()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/as/repairs/my/1")
                                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(repairService, times(1))
                .getMyRepair(1L, userDetails.getId());
    }

    // 수리 정보 수정 테스트
    @Test
    void updateRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairResponse response = new RepairResponse(
                1L,
                10L,
                userDetails.getId(),
                "CPU 점검",
                "부품 교체",
                RepairDecision.REPAIRABLE,
                "FREE",
                "보증 기간 내",
                10000,
                5000,
                15000,
                "REQUESTED",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L,
                1L
        );

        when(repairService.updateRepair(
                eq(1L),
                any(RepairRequest.class),
                eq("test@example.com")
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/as/repairs/1")
                                .with(csrf())
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "asReceptionId": 10,
                                                     "technicianId": 20,
                                                     "diagnosis": "CPU 점검",
                                                     "warrantyResult": "FREE",
                                                     "status": "REQUESTED",
                                                     "laborRate": 0,
                                                     "inspectionCost": 0,
                                                     "finalAmount": 0
                                                 }
                                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("CPU 점검"));

        verify(repairService, times(1))
                .updateRepair(
                        eq(1L),
                        any(RepairRequest.class),
                        eq("test@example.com")
                );
    }

    // 수리 상태 변경 테스트
    @Test
    @DisplayName("관리자는 일반 수리 상태를 변경할 수 있다.")
    void changeRepairStatusTest() throws Exception {

        CustomUserDetails adminDetails = createAdminUserDetails();

        mockMvc.perform(patch("/api/v1/as/repairs/1/status")
                                .with(csrf())
                                .with(user(adminDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "status": "REQUESTED"
                                                 }
                                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                                   .value("수리 상태가 변경되었습니다."));

        verify(repairService, times(1))
                .changeRepairStatus(
                        1L,
                        RepairStatus.REQUESTED,
                        "admin@example.com"
                );
    }

    // 수리 시작 테스트
    @Test
    @DisplayName("담당 기사는 수리 시작 API를 호출할 수 있다.")
    void startRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        mockMvc.perform(patch("/api/v1/as/repairs/1/start")
                                .with(csrf())
                                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                                   .value("수리가 시작되었습니다."));

        verify(repairService, times(1))
                .startRepair(
                        1L,
                        userDetails.getId(),
                        "test@example.com"
                );
    }

    // 관리자 수리 시작 접근 차단 테스트
    @Test
    @DisplayName("관리자는 기사 전용 수리 시작 API를 호출할 수 없다.")
    void startRepairForbiddenTest() throws Exception {

        CustomUserDetails adminDetails = createAdminUserDetails();

        mockMvc.perform(patch("/api/v1/as/repairs/1/start")
                                .with(csrf())
                                .with(user(adminDetails)))
                .andExpect(status().isForbidden());

        verify(repairService, never())
                .startRepair(anyLong(), anyLong(), anyString());
    }

    // 수리 삭제 테스트
    @Test
    @WithMockUser
    void deleteRepairTest() throws Exception {

        mockMvc.perform(delete("/api/v1/as/repairs/1")
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                                   .value("수리 정보가 삭제되었습니다."));

        verify(repairService, times(1)).deleteRepair(1L);
    }
}