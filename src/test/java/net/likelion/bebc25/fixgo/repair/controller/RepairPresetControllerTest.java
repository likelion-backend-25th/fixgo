package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetRequest;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetUpdate;
import net.likelion.bebc25.fixgo.repair.service.RepairPresetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepairPresetController.class)
class RepairPresetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairPresetService repairPresetService;

    private CustomUserDetails createUserDetails() {
        Member member = new Member("test@example.com", "password", "테스트 직원", "010-1234-5678", "서울시 강남구", Role.ROLE_TECHNICIAN, "profile.png");

        return new CustomUserDetails(member);
    }

    private RepairPresetResponse createResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new RepairPresetResponse(1L, "액정 수리", "스마트폰 액정 교체", now, now, "admin", "admin", 50000L);
    }

    // 수리 프리셋 등록 테스트
    @Test
    void createPresetTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        when(repairPresetService.createPreset(any(RepairPresetRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/api/as/repair-presets").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                                    {
                                                                                                                                                        "name": "액정 수리",
                                                                                                                                                        "description": "스마트폰 액정 교체",
                                                                                                                                                        "createdBy": "admin"
                                                                                                                                                    }
                                                                                                                                                    """)).andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("액정 수리")).andExpect(jsonPath("$.basePrice").value(50000));

        verify(repairPresetService, times(1)).createPreset(any(RepairPresetRequest.class));
    }

    // 전체 수리 프리셋 조회 테스트
    @Test
    @WithMockUser
    void getAllPresetsTest() throws Exception {

        when(repairPresetService.getAllPresets()).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/as/repair-presets")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$[0].name").value("액정 수리")).andExpect(jsonPath("$[0].basePrice").value(50000));

        verify(repairPresetService, times(1)).getAllPresets();
    }

    // 특정 수리 프리셋 조회 테스트
    @Test
    @WithMockUser
    void getPresetTest() throws Exception {

        when(repairPresetService.getPreset(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/as/repair-presets/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("액정 수리"));

        verify(repairPresetService, times(1)).getPreset(1L);
    }

    // 수리 프리셋 수정 테스트
    @Test
    void updatePresetTest() throws Exception {

        CustomUserDetails userDetails = createUserDetails();

        RepairPresetResponse response = new RepairPresetResponse(1L, "배터리 수리", "스마트폰 배터리 교체", LocalDateTime.now(), LocalDateTime.now(), "admin", "admin", 30000L);

        when(repairPresetService.updatePreset(eq(1L), any(RepairPresetUpdate.class))).thenReturn(response);

        mockMvc.perform(put("/api/as/repair-presets/1").with(csrf()).with(user(userDetails)).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                                                     {
                                                                                                                                                         "name": "배터리 수리",
                                                                                                                                                         "description": "스마트폰 배터리 교체",
                                                                                                                                                         "updatedBy": "admin"
                                                                                                                                                     }
                                                                                                                                                     """)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("배터리 수리")).andExpect(jsonPath("$.basePrice").value(30000));

        verify(repairPresetService, times(1)).updatePreset(eq(1L), any(RepairPresetUpdate.class));
    }

    // 수리 프리셋 삭제 테스트
    @Test
    @WithMockUser
    void deletePresetTest() throws Exception {

        doNothing().when(repairPresetService).deletePreset(1L);

        mockMvc.perform(delete("/api/as/repair-presets/1").with(csrf())).andExpect(status().isNoContent());

        verify(repairPresetService, times(1)).deletePreset(1L);
    }

    @Test
    @DisplayName("프리셋 이름 검색 API 성공")
    @WithMockUser(roles = "USER")
    void searchPresetsTest() throws Exception {

        when(repairPresetService.searchPresets("배터리")).thenReturn(List.of());

        mockMvc.perform(get("/api/as/repair-presets/search").param("name", "배터리")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());

        verify(repairPresetService).searchPresets("배터리");
    }
}
