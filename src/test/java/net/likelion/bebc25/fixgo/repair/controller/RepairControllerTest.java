package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponseDto;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairController.class)
class RepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairService repairService;

    // 수리 등록 테스트
    @Test
    @WithMockUser
    void createRepairTest() throws Exception {

        when(repairService.createRepair(any(RepairRequestDto.class))).thenReturn(Map.of("message", "수리 정보가 등록되었습니다."));

        mockMvc.perform(post("/api/repairs").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                  {
                                                                                                                      "asRequestId": 10,
                                                                                                                      "technicianId": 20,
                                                                                                                      "diagnosis": "메인보드 점검",
                                                                                                                      "warrantyResult": "FREE",
                                                                                                                      "status": "REQUESTED"
                                                                                                                  }
                                                                                                                  """)).andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("수리 정보가 등록되었습니다."));

        verify(repairService, times(1)).createRepair(any(RepairRequestDto.class));
    }

    // 전체 수리 목록 조회 테스트
    @Test
    @WithMockUser
    void getAllRepairsTest() throws Exception {

        when(repairService.getAllRepairs()).thenReturn(List.of(new RepairResponseDto()));

        mockMvc.perform(get("/api/repairs")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        verify(repairService, times(1)).getAllRepairs();
    }

    // 수리 상세 조회 테스트
    @Test
    @WithMockUser
    void getRepairTest() throws Exception {

        when(repairService.getRepair(1L)).thenReturn(new RepairResponseDto());

        mockMvc.perform(get("/api/repairs/1")).andExpect(status().isOk());

        verify(repairService, times(1)).getRepair(1L);
    }

    // 수리 정보 수정 테스트
    @Test
    @WithMockUser
    void updateRepairTest() throws Exception {

        RepairResponseDto response = new RepairResponseDto();
        response.setDiagnosis("CPU 점검");

        when(repairService.updateRepair(eq(1L), any(RepairRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/repairs/1").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                   {
                                                                                                                       "diagnosis": "CPU 점검",
                                                                                                                       "warrantyResult": "FREE",
                                                                                                                       "status": "REQUESTED"
                                                                                                                   }
                                                                                                                   """)).andExpect(status().isOk()).andExpect(jsonPath("$.diagnosis").value("CPU 점검"));

        verify(repairService, times(1)).updateRepair(eq(1L), any(RepairRequestDto.class));
    }

    // 수리 삭제 테스트
    @Test
    @WithMockUser
    void deleteRepairTest() throws Exception {

        doNothing().when(repairService).deleteRepair(1L);

        mockMvc.perform(delete("/api/repairs/1").with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("수리 정보가 삭제되었습니다."));

        verify(repairService, times(1)).deleteRepair(1L);
    }
}
