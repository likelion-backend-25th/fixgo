package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponseDto;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdateDto;
import net.likelion.bebc25.fixgo.repair.service.RepairWorkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairWorkController.class)
class RepairWorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairWorkService repairWorkService;

    // 수리 작업 내역 등록 테스트
    @Test
    @WithMockUser
    void createRepairWorkTest() throws Exception {

        RepairWorkResponseDto response = new RepairWorkResponseDto();
        response.setId(1L);
        response.setRepairId(1L);
        response.setRepairPresetName("기본 점검");
        response.setWorkName("메인보드 점검");

        when(repairWorkService.createRepairWork(any(RepairWorkRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/repair-works").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                       {
                                                                                                                           "repairId": 1,
                                                                                                                           "repairPresetName": "기본 점검",
                                                                                                                           "workName": "메인보드 점검"
                                                                                                                       }
                                                                                                                       """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.repairId").value(1)).andExpect(jsonPath("$.repairPresetName").value("기본 점검")).andExpect(jsonPath("$.workName").value("메인보드 점검"));

        verify(repairWorkService, times(1)).createRepairWork(any(RepairWorkRequestDto.class));
    }

    // 전체 수리 작업 내역 조회 테스트
    @Test
    @WithMockUser
    void getAllRepairWorksTest() throws Exception {

        when(repairWorkService.getAllRepairWorks()).thenReturn(List.of(new RepairWorkResponseDto()));

        mockMvc.perform(get("/api/repair-works")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        verify(repairWorkService, times(1)).getAllRepairWorks();
    }

    // 수리 작업 내역 단건 조회 테스트
    @Test
    @WithMockUser
    void getRepairWorkTest() throws Exception {

        RepairWorkResponseDto response = new RepairWorkResponseDto();
        response.setId(1L);
        response.setWorkName("메인보드 점검");

        when(repairWorkService.getRepairWork(1L)).thenReturn(response);

        mockMvc.perform(get("/api/repair-works/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.workName").value("메인보드 점검"));

        verify(repairWorkService, times(1)).getRepairWork(1L);
    }

    // 특정 수리 건의 작업 내역 조회 테스트
    @Test
    @WithMockUser
    void getRepairWorksByRepairIdTest() throws Exception {

        when(repairWorkService.getRepairWorksByRepairId(1L)).thenReturn(List.of(new RepairWorkResponseDto()));

        mockMvc.perform(get("/api/repair-works/repair/1")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        verify(repairWorkService, times(1)).getRepairWorksByRepairId(1L);
    }

    // 작업명 검색 테스트
    @Test
    @WithMockUser
    void searchRepairWorksTest() throws Exception {

        when(repairWorkService.searchRepairWorks("점검")).thenReturn(List.of(new RepairWorkResponseDto()));

        mockMvc.perform(get("/api/repair-works/search").param("workName", "점검")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        verify(repairWorkService, times(1)).searchRepairWorks("점검");
    }

    // 수리 작업 내역 수정 테스트
    @Test
    @WithMockUser
    void updateRepairWorkTest() throws Exception {

        RepairWorkResponseDto response = new RepairWorkResponseDto();
        response.setRepairPresetName("부품 교체");
        response.setWorkName("메인보드 교체");

        when(repairWorkService.updateRepairWork(eq(1L), any(RepairWorkUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/repair-works/1").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                        {
                                                                                                                            "repairPresetName": "부품 교체",
                                                                                                                            "workName": "메인보드 교체"
                                                                                                                        }
                                                                                                                        """)).andExpect(status().isOk()).andExpect(jsonPath("$.repairPresetName").value("부품 교체")).andExpect(jsonPath("$.workName").value("메인보드 교체"));

        verify(repairWorkService, times(1)).updateRepairWork(eq(1L), any(RepairWorkUpdateDto.class));
    }

    // 수리 작업 내역 삭제 테스트
    @Test
    @WithMockUser
    void deleteRepairWorkTest() throws Exception {

        doNothing().when(repairWorkService).deleteRepairWork(1L);

        mockMvc.perform(delete("/api/repair-works/1").with(csrf())).andExpect(status().isNoContent());

        verify(repairWorkService, times(1)).deleteRepairWork(1L);
    }
}