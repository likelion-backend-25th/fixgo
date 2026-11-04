package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

        when(repairCostService.calculateRepairCost(repairId, "tester")).thenReturn(finalAmount);

        mockMvc.perform(post("/api/as/v1/repairs/{repairId}/cost", repairId).with(csrf())).andExpect(status().isOk()).andExpect(content().string("60000"));

        verify(repairCostService).calculateRepairCost(repairId, "tester");
    }

    @Test
    @WithMockUser(username = "tester")
    @DisplayName("수리비 계산 실패")
    void calculateRepairCostFail() throws Exception {
        Long repairId = 999L;

        when(repairCostService.calculateRepairCost(repairId, "tester")).thenThrow(new IllegalArgumentException("수리 정보를 찾을 수 없습니다."));

        mockMvc.perform(post("/api/as/v1/repairs/{repairId}/cost", repairId).with(csrf())).andExpect(status().is5xxServerError());

        verify(repairCostService).calculateRepairCost(repairId, "tester");
    }
}