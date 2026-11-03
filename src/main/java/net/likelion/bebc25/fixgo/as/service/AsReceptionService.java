package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.repository.AsAttachmentRepository;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsReceptionService {

    private final AsReceptionRepository asRequestRepository;
    private final AsAttachmentRepository asAttachmentRepository;
    private final AsStatusHistoryRepository asStatusHistoryRepository;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

//    // AS 접수
//    public AsReceptionDto createAsReception(Long receptionistId, AsReceptionDto.CreateRequest request) {}

}

//    @Service
//    @RequiredArgsConstructor
//    @Transactional(readOnly = true)
//    public class PostService {
//
//        private final PostRepository postRepository;
//        private final CommentRepository commentRepository;
//        private final MemberRepository memberRepository;
//
//        @Transactional
//        public PostDto.Response createPost(Long memberId, PostDto.CreateRequest request){
//            Member member = memberRepository.findById(memberId)
//                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + memberId));
//            Post post = new Post(member, request.content());
//            Post savedPost = postRepository.save(post);
//            return PostDto.Response.from(savedPost);
//        }
//
//        public PostDto.DetailResponse getPost(Long id){
//            Post post = postRepository.findById(id)
//                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 입니다. id=" + id));
//            return PostDto.DetailResponse.from(post);
//        }
//
//        @Transactional
//        public CommentDto.Response addComment(Long postId, Long memberId, CommentDto.CreateRequest request){
//            Post post = postRepository.findById(postId)
//                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 입니다. id=" + postId));
//            Member member = memberRepository.findById(memberId)
//                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + memberId));
//
//            Comment comment = new Comment(request.content(), member);
//            // 연관관계 편의 메서드를 먼저 호출하여 comment.post 참조를 설정하고 컬렉션에 추가
//            post.addComment(comment);
//            // 즉시 Comment ID를 채번하기 위해 직접 저장
//            Comment savedComment = commentRepository.save(comment);
//
//            return CommentDto.Response.from(savedComment);
//        }
//
//}
