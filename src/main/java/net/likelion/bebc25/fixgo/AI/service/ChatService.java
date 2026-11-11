package net.likelion.bebc25.fixgo.AI.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.entity.Chat;
import net.likelion.bebc25.fixgo.AI.repository.ChatRepository;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public List<Chat> readAllChats(CustomUserDetails userDetails) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userDetails.getId() + "");
    }


}
