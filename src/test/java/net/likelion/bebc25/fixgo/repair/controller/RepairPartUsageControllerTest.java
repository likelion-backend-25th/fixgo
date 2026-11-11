package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairPartUsageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepairPartUsageController.class)
class RepairPartUsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairPartUsageService repairPartUsageService;

    @Test
    @WithMockUser(username = "tester")
    @DisplayName("실제 수리 부품 사용 등록 성공")
    void createUsageSuccess() throws Exception {

        // given
        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 2);

        RepairPartUsageResponse response =
                new RepairPartUsageResponse(
                        1L,
                        1L,
                        3L,
                        "노트북 메인보드",
                        2,
                        150000,
                        300000,
                        LocalDateTime.of(2026, 11, 7, 20, 0)
                );

        when(repairPartUsageService.createUsage(
                any(CustomUserDetails.class),
                any(RepairPartUsageRequest.class)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/as/repair-part-usages")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "repairWorkId": 1,
                                                     "partId": 3,
                                                     "quantity": 2
                                                 }
                                                 """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repairWorkId").value(1))
                .andExpect(jsonPath("$.partId").value(3))
                .andExpect(jsonPath("$.partName").value("노트북 메인보드"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(150000))
                .andExpect(jsonPath("$.totalPrice").value(300000));

        verify(repairPartUsageService).createUsage(
                any(CustomUserDetails.class),
                any(RepairPartUsageRequest.class)
        );
    }

    @Test
    @DisplayName("실제 수리 부품 사용 등록 실패")
    void createUsageFail() throws Exception {

        // given
        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        doThrow(new IllegalArgumentException("부품 재고가 부족합니다."))
                .when(repairPartUsageService)
                .createUsage(
                        any(CustomUserDetails.class),
                        any(RepairPartUsageRequest.class)
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/as/repair-part-usages")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "repairWorkId": 1,
                                                     "partId": 3,
                                                     "quantity": 20
                                                 }
                                                 """)
                )
                .andExpect(status().isBadRequest());

        verify(repairPartUsageService).createUsage(
                any(CustomUserDetails.class),
                any(RepairPartUsageRequest.class)
        );
    }

    private CustomUserDetails createTechnicianUserDetails() {

        Member member = mock(Member.class);

        when(member.getId()).thenReturn(1L);
        when(member.getEmail()).thenReturn("technician@test.com");
        when(member.getRole()).thenReturn(Role.ROLE_TECHNICIAN);

        return new CustomUserDetails(member);
    }
}