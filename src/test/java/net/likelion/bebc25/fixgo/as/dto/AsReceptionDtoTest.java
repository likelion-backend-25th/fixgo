package net.likelion.bebc25.fixgo.as.dto;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class AsReceptionDtoTest {

    @Test
    void createRequestBuildsReceptionAndUsesDeliveryWhenReturnMethodIsMissing() {
        Customer customer = customer();
        Product product = product();
        Member receptionist = member(4L, "최접수", Role.ROLE_RECEPTIONIST);
        Member technician = member(5L, "김기사", Role.ROLE_TECHNICIAN);
        AsReceptionDto.CreateReceptionRequest request = new AsReceptionDto.CreateReceptionRequest(
                1L, 2L, 5L, "충전불량", "충전이 안 됩니다", "메모", null, "https://example.com/a.jpg"
        );

        AsReception reception = request.toEntity(customer, product, receptionist, technician);

        assertThat(reception.getReceptionNumber()).startsWith("AS-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-");
        assertThat(reception.getCustomer()).isSameAs(customer);
        assertThat(reception.getProduct()).isSameAs(product);
        assertThat(reception.getReceptionist()).isSameAs(receptionist);
        assertThat(reception.getTechnician()).isSameAs(technician);
        assertThat(reception.getSymptom()).isEqualTo("충전불량");
        assertThat(reception.getSymptomDetail()).isEqualTo("충전이 안 됩니다");
        assertThat(reception.getMemo()).isEqualTo("메모");
        assertThat(reception.getReturnMethod()).isEqualTo(ReturnMethod.DELIVERY);
        assertThat(reception.getFileUrl()).isEqualTo("https://example.com/a.jpg");
        assertThat(reception.getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
    }

    @Test
    void responsesExposeOnlyTheFieldsNeededByEachScreen() {
        Member technician = member(5L, "김기사", Role.ROLE_TECHNICIAN);
        AsReception reception = new AsReception(
                "AS-100",
                customer(),
                product(),
                member(4L, "최접수", Role.ROLE_RECEPTIONIST),
                technician,
                "충전불량",
                "충전이 안 됩니다",
                "메모",
                ReturnMethod.PICKUP,
                null
        );
        ReflectionTestUtils.setField(reception, "id", 10L);
        Repair repair = new Repair(reception, technician, WarrantyResult.FREE, RepairStatus.DIAGNOSING);

        AsReceptionDto.ListResponse list = AsReceptionDto.ListResponse.from(reception, repair, "Gram 15");
        AsReceptionDto.DetailResponse detail = AsReceptionDto.DetailResponse.from(reception);
        AsReceptionDto.CreateReceptionResponse created = AsReceptionDto.CreateReceptionResponse.from(reception);

        assertThat(list.modelName()).isEqualTo("Gram 15");
        assertThat(list.receptionStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(list.repairStatus()).isEqualTo(RepairStatus.DIAGNOSING);
        assertThat(list.technicianName()).isEqualTo("김기사");
        assertThat(detail.returnMethod()).isEqualTo(ReturnMethod.PICKUP);
        assertThat(detail.receptionist().name()).isEqualTo("최접수");
        assertThat(detail.technician().id()).isEqualTo(5L);
        assertThat(created.receptionNumber()).isEqualTo("AS-100");
        assertThat(created.technicianId()).isEqualTo(5L);
        assertThat(AsReceptionDto.MemberInfo.from(null)).isNull();
    }

    @Test
    void createResponseOmitsTechnicianIdWhenTechnicianIsMissing() {
        AsReception reception = new AsReception(
                "AS-100",
                customer(),
                product(),
                member(4L, "최접수", Role.ROLE_RECEPTIONIST),
                null,
                "충전불량",
                "충전이 안 됩니다",
                "메모",
                ReturnMethod.DELIVERY,
                null
        );

        assertThat(AsReceptionDto.CreateReceptionResponse.from(reception).technicianId()).isNull();
    }

    private Member member(Long id, String name, Role role) {
        Member member = new Member(name + "@test.com", "password", name, "010-0000-0001", "서울시 강남구", role, "profile.png");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Customer customer() {
        return new Customer("한소희", "010-1111-2222", "hansohee@test.com", "12345", "서울", "강남구", 1L);
    }

    private Product product() {
        return new Product(1L, "SN-1004", LocalDateTime.now(), 1L);
    }
}
