package net.likelion.bebc25.fixgo.admin.controller;

import net.likelion.bebc25.fixgo.admin.dto.LaborRateDto;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.service.LaborRateService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.handler.CustomAccessDeniedHandler;
import net.likelion.bebc25.fixgo.auth.handler.CustomAuthenticationEntryPoint;
import net.likelion.bebc25.fixgo.auth.jwt.JwtProvider;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.service.CustomUserDetailsService;
import net.likelion.bebc25.fixgo.global.security.SecurityConfig;
import net.likelion.bebc25.fixgo.product.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaborRateController.class)
@Import(SecurityConfig.class)
class LaborRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaborRateService laborRateService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("인증된 사용자는 공임비·점검비 전체 목록을 조회할 수 있다.")
    void getAllLaborRates() throws Exception {
        when(laborRateService.getAllLaborRates()).thenReturn(List.of(
                response(1L, Category.LAPTOP, 50000, RateType.LABOR_RATE),
                response(2L, Category.PHONE, 15000, RateType.INSPECTION_COST)
        ));

        mockMvc.perform(
                        get("/api/v1/labor-rates")
                                .with(user(createTechnicianUserDetails()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cost").value(50000))
                .andExpect(jsonPath("$[1].rateType").value("INSPECTION_COST"));
    }

    @Test
    @DisplayName("관리자는 공임비·점검비를 수정할 수 있다.")
    void updateLaborRateByAdmin() throws Exception {
        when(laborRateService.updateLaborRate(any()))
                .thenReturn(response(1L, Category.LAPTOP, 55000, RateType.LABOR_RATE));

        mockMvc.perform(
                        patch("/api/v1/labor-rates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": 1,
                                          "cost": 55000
                                        }
                                        """)
                                .with(user(createAdminUserDetails()))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cost").value(55000));

        verify(laborRateService).updateLaborRate(any());
    }

    @Test
    @DisplayName("관리자가 아니면 공임비·점검비를 수정할 수 없다.")
    void updateLaborRateForbidden() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/labor-rates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": 1,
                                          "cost": 55000
                                        }
                                        """)
                                .with(user(createTechnicianUserDetails()))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(laborRateService);
    }

    private CustomUserDetails createTechnicianUserDetails() {
        Member member = new Member(
                "tech@example.com",
                "password",
                "테스트 기사",
                "010-1111-2222",
                "서울시",
                Role.ROLE_TECHNICIAN);
        return new CustomUserDetails(member);
    }

    private CustomUserDetails createAdminUserDetails() {
        Member member = new Member(
                "admin@example.com",
                "password",
                "테스트 관리자",
                "010-3333-4444",
                "서울시",
                Role.ROLE_ADMIN);
        return new CustomUserDetails(member);
    }

    private LaborRateDto.Response response(
            Long id,
            Category category,
            int cost,
            RateType rateType
    ) {
        return new LaborRateDto.Response(
                id,
                category,
                cost,
                rateType,
                LocalDate.of(2025, 1, 1),
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 9, 0),
                1L,
                1L
        );
    }
}
