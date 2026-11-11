package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairHistoryController.class)
class RepairHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairHistoryService repairHistoryService;

    @Test
    @DisplayName("A/S ID로 수리 이력을 조회한다")
    @WithMockUser
    void getRepairHistory() throws Exception {

        // given
        Long asReceptionId = 1L;

        RepairHistoryResponse response = new RepairHistoryResponse(
                10L,
                asReceptionId,
                "모터 고장",
                "모터 교체 완료",
                null,
                null,
                50000,
                "COMPLETED",
                null,
                null,
                List.of()
        );

        given(repairHistoryService.getRepairHistory(asReceptionId))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/as/repair-histories/{asReceptionId}",
                            asReceptionId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairId").value(10))
                .andExpect(jsonPath("$.asReceptionId").value(1))
                .andExpect(jsonPath("$.diagnosis").value("모터 고장"))
                .andExpect(jsonPath("$.workNote").value("모터 교체 완료"))
                .andExpect(jsonPath("$.finalAmount").value(50000))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.repairWorks").isArray());

        verify(repairHistoryService).getRepairHistory(asReceptionId);
    }
}
