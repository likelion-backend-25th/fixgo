package net.likelion.bebc25.fixgo.as.entity;

import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class AsStatusHistoryTest {

    @Autowired
    private AsStatusHistoryRepository asStatusHistoryRepository;

    @Autowired
    private AsReceptionRepository asReceptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("접수 상태 변경 이력을 저장하고 다시 읽는다")
    void saveAndFind() {
        Customer customer = customerRepository.save(
                new Customer("한소희", "010-1111-2222", "hansohee@test.com", "12345", "서울", "강남구", 1L)
        );
        Product product = productRepository.save(new Product(1L, "SN-1004", LocalDateTime.now()));
        Member receptionist = memberRepository.save(
                new Member(
                        "receptionist@test.com",
                        "password",
                        "최접수",
                        "010-0000-0001",
                        "서울시 강남구",
                        Role.ROLE_RECEPTIONIST)
        );
        Member technician = memberRepository.save(
                new Member(
                        "tech@test.com",
                        "password",
                        "김기사",
                        "010-0000-0001",
                        "서울시 강남구",
                        Role.ROLE_TECHNICIAN)
        );
        AsReception reception = asReceptionRepository.save(new AsReception(
                "AS-HISTORY-001",
                customer,
                product,
                receptionist,
                technician,
                "충전불량",
                "충전이 안 됩니다",
                "메모",
                ReturnMethod.DELIVERY,
                null
        ));

        AsStatusHistory saved = asStatusHistoryRepository.save(new AsStatusHistory(
                reception, null, AsReceptionStatus.RECEIVED.name(), "접수 등록", receptionist
        ));
        AsStatusHistory found = asStatusHistoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAsReception().getId()).isEqualTo(reception.getId());
        assertThat(found.getFromStatus()).isNull();
        assertThat(found.getToStatus()).isEqualTo("RECEIVED");
        assertThat(found.getReason()).isEqualTo("접수 등록");
        assertThat(found.getChangedBy().getId()).isEqualTo(receptionist.getId());
        assertThat(found.getCreatedAt()).isNotNull();
    }
}
