package net.likelion.bebc25.fixgo.repair.controller;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.SymptomPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairPresetService;
import net.likelion.bebc25.fixgo.repair.service.SymptomPresetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SymptomPresetController.class)
class SymptomPresetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SymptomPresetService symptomPresetService;

    @MockitoBean
    private RepairPresetService repairPresetService;

    @Test
    @DisplayName("고장 증상 프리셋 전체 조회 성공")
    void getSymptomPresets() throws Exception {
        // given
        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        when(symptomPresetService.getSymptomPresets())
                .thenReturn(List.of(
                        new SymptomPresetResponse(
                                1L,
                                "스마트폰",
                                "화면 파손"
                        ),
                        new SymptomPresetResponse(
                                2L,
                                "노트북",
                                "전원 불량"
                        )
                ));

        // when & then
        mockMvc.perform(
                        get("/api/v1/as/symptom-presets")
                                .with(authentication(authentication))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productCategory").value("스마트폰"))
                .andExpect(jsonPath("$[0].name").value("화면 파손"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("전원 불량"));

        verify(symptomPresetService).getSymptomPresets();
    }

    @Test
    @DisplayName("고장 증상 프리셋 검색 성공")
    void searchSymptomPresets() throws Exception {
        // given
        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        when(symptomPresetService.searchSymptomPresets("화면"))
                .thenReturn(List.of(
                        new SymptomPresetResponse(
                                1L,
                                "스마트폰",
                                "화면 파손"
                        )
                ));

        // when & then
        mockMvc.perform(
                        get("/api/v1/as/symptom-presets/search")
                                .with(authentication(authentication))
                                .param("name", "화면")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productCategory").value("스마트폰"))
                .andExpect(jsonPath("$[0].name").value("화면 파손"));

        verify(symptomPresetService).searchSymptomPresets("화면");
    }

    @Test
    @DisplayName("증상에 연결된 수리 프리셋 조회 성공")
    void getRepairPresetsBySymptom() throws Exception {
        // given
        CustomUserDetails userDetails = createTechnicianUserDetails();

        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        LocalDateTime now = LocalDateTime.of(2026, 11, 10, 17, 0);

        when(repairPresetService.getPresetsBySymptom(1L))
                .thenReturn(List.of(
                        new RepairPresetResponse(
                                10L,
                                "액정 교체",
                                "스마트폰 액정 교체 작업",
                                now,
                                now,
                                1L,
                                1L,
                                50000L,
                                30L
                        )
                ));

        // when & then
        mockMvc.perform(
                        get("/api/v1/as/symptom-presets/{symptomId}/repair-presets", 1L)
                                .with(authentication(authentication))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("액정 교체"))
                .andExpect(jsonPath("$[0].basePrice").value(50000))
                .andExpect(jsonPath("$[0].estimatedMinutes").value(30));

        verify(repairPresetService).getPresetsBySymptom(1L);
    }

    private CustomUserDetails createTechnicianUserDetails() {
        Member member = mock(Member.class);

        when(member.getId()).thenReturn(1L);
        when(member.getEmail()).thenReturn("technician@test.com");
        when(member.getRole()).thenReturn(Role.ROLE_TECHNICIAN);

        return new CustomUserDetails(member);
    }
}
