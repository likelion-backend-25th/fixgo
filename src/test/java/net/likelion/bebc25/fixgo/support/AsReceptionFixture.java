package net.likelion.bebc25.fixgo.support;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 결제 등 A/S 접수에 의존하는 테스트에서 쓰는 AsReception 픽스처.
 * <p>
 * JPA 테스트: {@code @Import(AsReceptionFixture.class)} 후 주입받아 {@link #save()}를 호출한다.
 * 리포지토리로 저장하므로 테스트 트랜잭션이 없는 {@code NOT_SUPPORTED} 테스트에서도 동작한다.
 * <p>
 * 엔티티 단위 테스트: 저장이 필요 없으면 {@link #transientReception()}을 쓴다.
 */
@TestComponent
public class AsReceptionFixture {

    private static final AtomicLong SEQ = new AtomicLong();

    @Autowired
    private AsReceptionRepository asReceptionRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductModelRepository productModelRepository;
    @Autowired
    private ProductRepository productRepository;

    /**
     * DB에 저장하지 않은 AsReception. 엔티티 단위 테스트에서 mock 대신 사용한다.
     */
    public static AsReception transientReception() {
        return new AsReception("TEST-" + SEQ.incrementAndGet(), null, null, null, null,
                "증상", "상세 증상", null, ReturnMethod.DELIVERY, null);
    }

    /**
     * 연관 엔티티(직원, 고객, 제품)까지 저장한 AsReception을 만든다. 호출할 때마다 새 접수가 생긴다.
     */
    public AsReception save() {
        long n = SEQ.incrementAndGet();

        Member staff = memberRepository.save(new Member(
                "fixture-staff-" + n + "@test.com", "password", "테스트직원", "010-0000-0000",
                "서울시", Role.ROLE_RECEPTIONIST, "default.png"));
        Long createdBy = staff.getId();

        ProductModel model = productModelRepository.save(
                new ProductModel("테스트모델", "MODEL-" + n, Category.LAPTOP, 12, createdBy));
        Customer customer = customerRepository.save(new Customer(
                "테스트고객", "010-1111-2222", "fixture-customer-" + n + "@test.com",
                "12345", "서울", "강남구", createdBy));
        Product product = productRepository.save(
                new Product(model.getId(), "SN-FIXTURE-" + n, LocalDateTime.now()));

        return asReceptionRepository.save(new AsReception(
                "R-FIXTURE-" + n, customer, product, staff, staff,
                "증상", "상세 증상", null, ReturnMethod.DELIVERY, null));
    }

    /**
     * 트랜잭션 롤백이 없는 테스트의 정리용. 결제 데이터를 먼저 지운 뒤 호출해야 한다(FK 순서).
     */
    public void deleteAll() {
        asReceptionRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        productModelRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }
}
