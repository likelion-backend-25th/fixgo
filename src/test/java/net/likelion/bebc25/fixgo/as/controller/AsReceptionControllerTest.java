package net.likelion.bebc25.fixgo.as.controller;

import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AsReceptionController.class)
class AsReceptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsReceptionService asReceptionService;

    @Test
    void createReceptionReturnsCreatedReception() throws Exception {
        when(asReceptionService.createReception(any(), any())).thenReturn(new AsReceptionDto.CreateReceptionResponse(
                1L, "AS-260404-1", 2L, 3L, 4L, 5L, AsReceptionStatus.RECEIVED, LocalDateTime.of(2026, 11, 4, 10, 0)
        ));

        mockMvc.perform(post("/api/v1/as-receptions")
                        .with(csrf())
                        .with(user(receptionist()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 2,
                                  "productId": 3,
                                  "technicianId": 5,
                                  "symptom": "충전불량",
                                  "symptomDetail": "충전이 안 됩니다"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receptionNumber").value("AS-260404-1"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        verify(asReceptionService).createReception(any(CustomUserDetails.class), any(AsReceptionDto.CreateReceptionRequest.class));
    }

    @Test
    void createReceptionDoesNotSaveWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/v1/as-receptions")
                        .with(csrf())
                        .with(user(receptionist()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        verify(asReceptionService, never()).createReception(any(), any());
    }

    @Test
    @WithMockUser
    void getReceptionsPassesSearchConditions() throws Exception {
        AsReceptionDto.ListResponse item = new AsReceptionDto.ListResponse(
                1L,
                "AS-ALPHA-001",
                LocalDateTime.of(2026, 11, 4, 10, 0),
                "한소희",
                "010-1111-2222",
                "Gram 15",
                AsReceptionStatus.RECEIVED,
                RepairStatus.DIAGNOSING,
                "김기사"
        );
        when(asReceptionService.getReceptions(any(), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/as-receptions")
                        .param("keyword", "lcd")
                        .param("status", "RECEIVED")
                        .param("technicianId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].receptionNumber").value("AS-ALPHA-001"))
                .andExpect(jsonPath("$.content[0].receptionStatus").value("RECEIVED"))
                .andExpect(jsonPath("$.content[0].repairStatus").value("DIAGNOSING"))
                .andExpect(jsonPath("$.content[0].technicianName").value("김기사"));

        verify(asReceptionService).getReceptions(
                eq(new AsReceptionDto.ReceptionSearchRequest("lcd", AsReceptionStatus.RECEIVED, 5L)),
                any()
        );
    }

    @Test
    @WithMockUser
    void getReceptionReturnsDetail() throws Exception {
        Customer customer = new Customer("한소희", "010-1111-2222", "hansohee@test.com", "12345", "서울", "강남구", 1L);
        Product product = new Product(1L, "SN-1004", LocalDateTime.of(2026, 1, 1, 0, 0), 1L);
        when(asReceptionService.getReception(1L)).thenReturn(new AsReceptionDto.DetailResponse(
                1L,
                "AS-ALPHA-001",
                LocalDateTime.of(2026, 11, 4, 10, 0),
                customer,
                product,
                new AsReceptionDto.MemberInfo(4L, "최접수"),
                new AsReceptionDto.MemberInfo(5L, "김기사"),
                AsReceptionStatus.RECEIVED,
                "충전불량",
                "충전이 안 됩니다",
                "메모",
                ReturnMethod.DELIVERY,
                null
        ));

        mockMvc.perform(get("/api/v1/as-receptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receptionNumber").value("AS-ALPHA-001"))
                .andExpect(jsonPath("$.symptom").value("충전불량"))
                .andExpect(jsonPath("$.technician.name").value("김기사"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    private CustomUserDetails receptionist() {
        return new CustomUserDetails(
                new Member("receptionist@test.com", "password", "최접수", "010-0000-0001", "서울시 강남구", Role.ROLE_RECEPTIONIST, "profile.png")
        );
    }
}
