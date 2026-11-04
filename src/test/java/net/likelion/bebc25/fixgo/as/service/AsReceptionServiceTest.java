package net.likelion.bebc25.fixgo.as.service;

import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsReceptionServiceTest {
    @Mock AsReceptionRepository receptionRepository;
    @Mock AsStatusHistoryRepository historyRepository;
    @Mock CustomerRepository customerRepository;
    @Mock ProductRepository productRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks AsReceptionService service;

    @Test
    void blankKeywordDoesNotRestrictSearchAndFiltersArePreserved() {
        var pageable = PageRequest.of(0, 10);
        when(receptionRepository.search(null, AsReceptionStatus.RECEIVED, 2L, pageable))
                .thenReturn(Page.empty(pageable));

        var result = service.getReceptions(
                new AsReceptionDto.ReceptionSearchRequest("  ", AsReceptionStatus.RECEIVED, 2L),
                pageable
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getSize()).isEqualTo(10);
        verify(receptionRepository).search(null, AsReceptionStatus.RECEIVED, 2L, pageable);
    }

    @Test
    void missingReceptionReturnsNotFound() {
        when(receptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReception(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
