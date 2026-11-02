package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.repository.AsAttachmentRepository;
import net.likelion.bebc25.fixgo.as.repository.AsRequestRepository;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsRequestService {

    private final AsRequestRepository asRequestRepository;
    private final AsAttachmentRepository asAttachmentRepository;
    private final AsStatusHistoryRepository asStatusHistoryRepository;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;



}
