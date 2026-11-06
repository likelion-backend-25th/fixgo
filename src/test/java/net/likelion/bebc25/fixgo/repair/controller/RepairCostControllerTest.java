package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairCostController.class)
class RepairCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairCostService repairCostService;

    @Test
    @WithMockUser(username = "tester")
    @DisplayName("수리비 계산 성공")
    void calculateRepairCostSuccess() throws Exception {
        Long repairId = 1L;
        int finalAmount = 60000;

        when(repairCostService.calculateRepairCost(repairId))
                .thenReturn(finalAmount);

        mockMvc.perform(
                        post("/api/v1/as/repairs/{repairId}/cost", repairId)
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("60000"));

        verify(repairCostService).calculateRepairCost(repairId);
    }

    @Test
    @WithMockUser(username = "tester")
    @DisplayName("수리비 계산 실패")
    void calculateRepairCostFail() throws Exception {
        Long repairId = 999L;

        when(repairCostService.calculateRepairCost(repairId))
                .thenThrow(new IllegalArgumentException("수리 정보를 찾을 수 없습니다."));

        mockMvc.perform(
                        post("/api/v1/as/repairs/{repairId}/cost", repairId)
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());

        verify(repairCostService).calculateRepairCost(repairId);
    }

    @Test
    @DisplayName("수리비 확정 성공")
    void confirmRepairCostSuccess() throws Exception {
        Long repairId = 1L;

        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        mockMvc.perform(
                        post("/api/v1/as/repairs/{repairId}/cost/confirm", repairId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                                  {
                                                      "message": "수리비가 확정되었습니다."
                                                  }
                                                  """));

        verify(repairCostService).confirmRepairCost(repairId, userDetails);
    }

    @Test
    @DisplayName("수리비 확정 실패")
    void confirmRepairCostFail() throws Exception {
        Long repairId = 999L;

        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        doThrow(new IllegalArgumentException("수리 정보를 찾을 수 없습니다."))
                .when(repairCostService)
                .confirmRepairCost(repairId, userDetails);

        mockMvc.perform(
                        post("/api/v1/as/repairs/{repairId}/cost/confirm", repairId)
                                .with(authentication(authentication))
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());

        verify(repairCostService).confirmRepairCost(repairId, userDetails);
    }

    private CustomUserDetails createTechnicianUserDetails() {
        Member member = mock(Member.class);

        when(member.getId()).thenReturn(1L);
        when(member.getEmail()).thenReturn("technician@test.com");
        when(member.getRole()).thenReturn(Role.ROLE_TECHNICIAN);

        return new CustomUserDetails(member);
    }
}