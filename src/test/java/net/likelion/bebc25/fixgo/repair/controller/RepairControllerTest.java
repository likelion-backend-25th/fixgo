package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairController.class)
class RepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairService repairService;

    private CustomUserDetails createUserDetails() {
        Member member = new Member("TEST001", "test@example.com", "password", "테스트 직원", Role.ROLE_TECHNICIAN, "profile.png");

        return new CustomUserDetails(member);
    }

    // 수리 등록 테스트
    @Test
    void createRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        when(repairService.createRepair(any(RepairRequest.class), eq("test@example.com"))).thenReturn(Map.of("message", "수리 정보가 등록되었습니다."));

        mockMvc.perform(post("/api/v1/as/repairs").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                                {
                                                                                                                                                    "asRequestId": 10,
                                                                                                                                                    "technicianId": 20,
                                                                                                                                                    "diagnosis": "메인보드 점검",
                                                                                                                                                    "warrantyResult": "FREE",
                                                                                                                                                    "status": "REQUESTED"
                                                                                                                                                }
                                                                                                                                                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("수리 정보가 등록되었습니다."));

        verify(repairService, times(1)).createRepair(any(RepairRequest.class), eq("test@example.com"));
    }

    // 전체 수리 목록 조회 테스트
    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void getAllRepairsTest() throws Exception {

        when(repairService.getAllRepairs()).thenReturn(List.of(new RepairResponse()));

        mockMvc.perform(get("/api/v1/as/repairs")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        verify(repairService, times(1)).getAllRepairs();
    }

    // 수리 상세 조회 테스트
    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void getRepairTest() throws Exception {

        when(repairService.getRepair(1L)).thenReturn(new RepairResponse());

        mockMvc.perform(get("/api/v1/as/repairs/1")).andExpect(status().isOk());

        verify(repairService, times(1)).getRepair(1L);
    }

    // A/S 접수번호로 수리 상세 조회 테스트
    @Test
    @WithMockUser
    void getRepairByReceptionNumberTest() throws Exception {

        String receptionNumber = "AS-2026-1112";

        when(repairService.getRepairByReceptionNumber(receptionNumber)).thenReturn(new RepairResponse());

        mockMvc.perform(get("/api/v1/as/repairs/reception/{receptionNumber}", receptionNumber)).andExpect(status().isOk());

        verify(repairService, times(1)).getRepairByReceptionNumber(receptionNumber);
    }

    // 수리 정보 수정 테스트
    @Test
    void updateRepairTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairResponse response = new RepairResponse();
        response.setDiagnosis("CPU 점검");

        when(repairService.updateRepair(eq(1L), any(RepairRequest.class), eq("test@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/v1/as/repairs/1").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                                 {
                                                                                                                                                     "diagnosis": "CPU 점검",
                                                                                                                                                     "warrantyResult": "FREE",
                                                                                                                                                     "status": "REQUESTED"
                                                                                                                                                 }
                                                                                                                                                 """)).andExpect(status().isOk()).andExpect(jsonPath("$.diagnosis").value("CPU 점검"));

        verify(repairService, times(1)).updateRepair(eq(1L), any(RepairRequest.class), eq("test@example.com"));
    }

    // 수리 상태 변경 테스트
    @Test
    void changeRepairStatusTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        doNothing().when(repairService).changeRepairStatus(1L, RepairStatus.REQUESTED, "test@example.com");

        mockMvc.perform(patch("/api/v1/as/repairs/1/status").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                                          {
                                                                                                                                                              "status": "REQUESTED"
                                                                                                                                                          }
                                                                                                                                                          """)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("수리 상태가 변경되었습니다."));

        verify(repairService, times(1)).changeRepairStatus(1L, RepairStatus.REQUESTED, "test@example.com");
    }

    // 수리 삭제 테스트
    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void deleteRepairTest() throws Exception {

        doNothing().when(repairService).deleteRepair(1L);

        mockMvc.perform(delete("/api/v1/as/repairs/1").with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("수리 정보가 삭제되었습니다."));

        verify(repairService, times(1)).deleteRepair(1L);
    }
}
