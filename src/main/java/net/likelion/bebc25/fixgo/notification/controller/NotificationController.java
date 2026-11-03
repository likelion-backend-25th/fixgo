package net.likelion.bebc25.fixgo.notification.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 이메일 발송
    @PostMapping("/emails")
    public EmailDto.Response sendEmail(@RequestBody EmailDto.CreateRequest request) {
        return notificationService.sendEmail(request);
    }
}
