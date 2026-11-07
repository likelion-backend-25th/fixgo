package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepairWorkController.class)
class RepairWorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairWorkService repairWorkService;

    private CustomUserDetails createUserDetails() {
        Member member = new Member(
                "test@example.com",
                "password",
                "테스트 직원",
                "010-1234-5678",
                "서울시 강남구",
                Role.ROLE_ADMIN,
                "profile.png"
        );

        return new CustomUserDetails(member);
    }

    // 테스트용 응답 DTO 생성
    private RepairWorkResponse createRepairWorkResponse() {
        return new RepairWorkResponse(
                1L,
                1L,
                "기본 점검",
                "메인보드 점검",
                null,
                null,
                1L,
                1L
        );
    }

    // 수리 작업 내역 등록 테스트
    @Test
    void createRepairWorkTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairWorkResponse response = createRepairWorkResponse();

        when(repairWorkService.createRepairWork(
                any(RepairWorkRequest.class),
                eq("test@example.com")
        )).thenReturn(response);

        mockMvc.perform(post("/api/v1/as/repair-works")
                                .with(csrf())
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "repairId": 1,
                                                     "repairPresetName": "기본 점검",
                                                     "workName": "메인보드 점검"
                                                 }
                                                 """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repairId").value(1))
                .andExpect(jsonPath("$.repairPresetName").value("기본 점검"))
                .andExpect(jsonPath("$.workName").value("메인보드 점검"));

        verify(repairWorkService, times(1))
                .createRepairWork(
                        any(RepairWorkRequest.class),
                        eq("test@example.com")
                );
    }

    // 전체 수리 작업 내역 조회 테스트
    @Test
    @WithMockUser
    void getAllRepairWorksTest() throws Exception {

        when(repairWorkService.getAllRepairWorks())
                .thenReturn(List.of(createRepairWorkResponse()));

        mockMvc.perform(get("/api/v1/as/repair-works"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(repairWorkService, times(1)).getAllRepairWorks();
    }

    // 수리 작업 내역 단건 조회 테스트
    @Test
    @WithMockUser
    void getRepairWorkTest() throws Exception {

        RepairWorkResponse response = createRepairWorkResponse();

        when(repairWorkService.getRepairWork(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/as/repair-works/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workName").value("메인보드 점검"));

        verify(repairWorkService, times(1)).getRepairWork(1L);
    }

    // 특정 수리 건의 작업 내역 조회 테스트
    @Test
    @WithMockUser
    void getRepairWorksByRepairIdTest() throws Exception {

        when(repairWorkService.getRepairWorksByRepairId(1L))
                .thenReturn(List.of(createRepairWorkResponse()));

        mockMvc.perform(get("/api/v1/as/repair-works/repair/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(repairWorkService, times(1))
                .getRepairWorksByRepairId(1L);
    }

    // 작업명 검색 테스트
    @Test
    @WithMockUser
    void searchRepairWorksTest() throws Exception {

        when(repairWorkService.searchRepairWorks("점검"))
                .thenReturn(List.of(createRepairWorkResponse()));

        mockMvc.perform(get("/api/v1/as/repair-works/search")
                                .param("workName", "점검"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(repairWorkService, times(1))
                .searchRepairWorks("점검");
    }

    // 수리 작업 내역 수정 테스트
    @Test
    void updateRepairWorkTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairWorkResponse response = new RepairWorkResponse(
                1L,
                1L,
                "부품 교체",
                "메인보드 교체",
                null,
                null,
                1L,
                1L
        );

        when(repairWorkService.updateRepairWork(
                eq(1L),
                any(RepairWorkUpdate.class),
                eq("test@example.com")
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/as/repair-works/1")
                                .with(csrf())
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                     "repairPresetName": "부품 교체",
                                                     "workName": "메인보드 교체"
                                                 }
                                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairPresetName").value("부품 교체"))
                .andExpect(jsonPath("$.workName").value("메인보드 교체"));

        verify(repairWorkService, times(1))
                .updateRepairWork(
                        eq(1L),
                        any(RepairWorkUpdate.class),
                        eq("test@example.com")
                );
    }

    // 수리 작업 내역 삭제 테스트
    @Test
    @WithMockUser
    void deleteRepairWorkTest() throws Exception {

        mockMvc.perform(delete("/api/v1/as/repair-works/1")
                                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(repairWorkService, times(1))
                .deleteRepairWork(1L);
    }
}