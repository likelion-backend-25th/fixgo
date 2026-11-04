-- 관리자 계정 초기 데이터
-- email: admin001@fixgo.com / password: admin1234 (BCrypt)
INSERT INTO member (email,
                    password_hash,
                    name,
                    phone,
                    role,
                    address,
                    profile_img_url,
                    status,
                    deleted_at,
                    created_at,
                    updated_at,
                    created_by,
                    updated_by)
VALUES ('admin001@fixgo.com',
        '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
        '관리자',
        '010-XXXX-XXXX',
        'ROLE_ADMIN',
        '서울특별시 OO구 OO동 ~',
        '',
        'ACTIVE',
        NULL,
        NOW(),
        NOW(),
        0,
        0);


# 이메일 관련 더미데이터
START TRANSACTION;

-- fixgo-notification-demo-20261104-001 | PENDING | 2026-11-04 08:00
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-001',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-11-04 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-001');

-- fixgo-notification-demo-20261104-002 | FAILED | 2026-10-18 15:11
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-002',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-18 15:11:19'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-002');

-- fixgo-notification-demo-20261104-003 | SENDING | 2026-10-01 09:22
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-003',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-10-01 09:22:38'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-003');

-- fixgo-notification-demo-20261104-004 | SENT | 2026-09-14 16:33
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-004',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-14 16:34:54',
       '2026-09-14 16:33:57'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-004');

-- fixgo-notification-demo-20261104-005 | SENT | 2026-08-28 10:44
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-005',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-28 10:45:26',
       '2026-08-28 10:44:16'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-005');

-- fixgo-notification-demo-20261104-006 | SENT | 2026-10-23 17:55
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-006',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-23 17:56:58',
       '2026-10-23 17:55:35'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-006');

-- fixgo-notification-demo-20261104-007 | FAILED | 2026-10-06 11:06
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-007',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-06 11:06:54'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-007');

-- fixgo-notification-demo-20261104-008 | SENDING | 2026-09-19 18:17
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-008',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-09-19 18:17:13'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-008');

-- fixgo-notification-demo-20261104-009 | PENDING | 2026-09-02 12:28
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-009',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-09-02 12:28:32'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-009');

-- fixgo-notification-demo-20261104-010 | SENT | 2026-10-28 19:39
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-010',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-28 19:40:07',
       '2026-10-28 19:39:51'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-010');

-- fixgo-notification-demo-20261104-011 | FAILED | 2026-10-11 13:50
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-011',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-11 13:50:10'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-011');

-- fixgo-notification-demo-20261104-012 | SENT | 2026-09-24 20:01
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-012',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-24 20:02:11',
       '2026-09-24 20:01:29'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-012');

-- fixgo-notification-demo-20261104-013 | FAILED | 2026-09-07 14:12
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-013',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-07 14:12:48'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-013');

-- fixgo-notification-demo-20261104-014 | SENT | 2026-11-02 08:23
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-014',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-02 08:24:15',
       '2026-11-02 08:23:07'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-014');

-- fixgo-notification-demo-20261104-015 | SENT | 2026-10-16 15:34
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-015',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-16 15:35:47',
       '2026-10-16 15:34:26'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-015');

-- fixgo-notification-demo-20261104-016 | SENT | 2026-09-29 09:45
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-016',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-29 09:47:19',
       '2026-09-29 09:45:45'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-016');

-- fixgo-notification-demo-20261104-017 | PENDING | 2026-09-12 16:56
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-017',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-09-12 16:56:04'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-017');

-- fixgo-notification-demo-20261104-018 | SENT | 2026-08-26 10:07
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-018',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-26 10:09:23',
       '2026-08-26 10:07:23'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-018');

-- fixgo-notification-demo-20261104-019 | SENT | 2026-10-21 17:18
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-019',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-21 17:18:56',
       '2026-10-21 17:18:42'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-019');

-- fixgo-notification-demo-20261104-020 | FAILED | 2026-10-04 11:29
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-020',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-04 11:29:01'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-020');

-- fixgo-notification-demo-20261104-021 | SENT | 2026-09-17 18:40
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-021',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-17 18:41:00',
       '2026-09-17 18:40:20'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-021');

-- fixgo-notification-demo-20261104-022 | SENT | 2026-08-31 12:51
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-022',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-31 12:52:32',
       '2026-08-31 12:51:39'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-022');

-- fixgo-notification-demo-20261104-023 | FAILED | 2026-10-26 19:02
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-023',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-26 19:02:58'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-023');

-- fixgo-notification-demo-20261104-024 | SENT | 2026-10-09 13:13
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-024',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-09 13:14:36',
       '2026-10-09 13:13:17'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-024');

-- fixgo-notification-demo-20261104-025 | FAILED | 2026-09-22 20:24
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-025',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-22 20:24:36'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-025');

-- fixgo-notification-demo-20261104-026 | SENT | 2026-09-05 14:35
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-026',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-05 14:37:40',
       '2026-09-05 14:35:55'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-026');

-- fixgo-notification-demo-20261104-027 | SENT | 2026-10-31 08:46
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-027',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-31 08:48:12',
       '2026-10-31 08:46:14'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-027');

-- fixgo-notification-demo-20261104-028 | FAILED | 2026-10-14 15:57
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-028',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-14 15:57:33'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-028');

-- fixgo-notification-demo-20261104-029 | SENT | 2026-09-27 09:08
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-029',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-27 09:09:17',
       '2026-09-27 09:08:52'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-029');

-- fixgo-notification-demo-20261104-030 | FAILED | 2026-09-10 16:19
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-030',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-10 16:19:11'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-030');

-- fixgo-notification-demo-20261104-031 | SENT | 2026-08-24 10:30
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-031',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-24 10:31:21',
       '2026-08-24 10:30:30'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-031');

-- fixgo-notification-demo-20261104-032 | SENDING | 2026-10-19 17:41
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-032',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-10-19 17:41:49'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-032');

-- fixgo-notification-demo-20261104-033 | SENT | 2026-10-02 11:52
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-033',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-02 11:53:25',
       '2026-10-02 11:52:08'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-033');

-- fixgo-notification-demo-20261104-034 | SENT | 2026-09-15 18:03
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-034',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-15 18:04:57',
       '2026-09-15 18:03:27'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-034');

-- fixgo-notification-demo-20261104-035 | SENT | 2026-08-29 12:14
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-035',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-29 12:16:29',
       '2026-08-29 12:14:46'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-035');

-- fixgo-notification-demo-20261104-036 | SENT | 2026-10-24 19:25
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-036',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-24 19:27:01',
       '2026-10-24 19:25:05'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-036');

-- fixgo-notification-demo-20261104-037 | SENT | 2026-10-07 13:36
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-037',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-07 13:36:34',
       '2026-10-07 13:36:24'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-037');

-- fixgo-notification-demo-20261104-038 | SENT | 2026-09-20 20:47
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-038',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-20 20:48:06',
       '2026-09-20 20:47:43'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-038');

-- fixgo-notification-demo-20261104-039 | SENT | 2026-09-03 14:58
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-039',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-03 14:58:38',
       '2026-09-03 14:58:02'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-039');

-- fixgo-notification-demo-20261104-040 | SENT | 2026-10-29 08:09
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-040',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-29 08:10:10',
       '2026-10-29 08:09:21'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-040');

-- fixgo-notification-demo-20261104-041 | FAILED | 2026-10-12 15:20
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-041',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-12 15:20:40'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-041');

-- fixgo-notification-demo-20261104-042 | FAILED | 2026-09-25 09:31
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-042',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-25 09:31:59'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-042');

-- fixgo-notification-demo-20261104-043 | SENT | 2026-09-08 16:42
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-043',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-08 16:43:46',
       '2026-09-08 16:42:18'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-043');

-- fixgo-notification-demo-20261104-044 | SENT | 2026-11-03 10:53
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-044',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-03 10:55:18',
       '2026-11-03 10:53:37'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-044');

-- fixgo-notification-demo-20261104-045 | FAILED | 2026-10-17 17:04
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-045',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-10-17 17:04:56'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-045');

-- fixgo-notification-demo-20261104-046 | FAILED | 2026-09-30 11:15
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-046',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-30 11:15:15'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-046');

-- fixgo-notification-demo-20261104-047 | FAILED | 2026-09-13 18:26
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-047',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-09-13 18:26:34'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-047');

-- fixgo-notification-demo-20261104-048 | SENT | 2026-08-27 12:37
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-048',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-27 12:38:27',
       '2026-08-27 12:37:53'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-048');

-- fixgo-notification-demo-20261104-049 | FAILED | 2026-10-22 19:48
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-049',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-22 19:48:12'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-049');

-- fixgo-notification-demo-20261104-050 | SENT | 2026-10-05 13:59
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-050',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-05 14:00:31',
       '2026-10-05 13:59:31'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-050');

-- fixgo-notification-demo-20261104-051 | SENT | 2026-09-18 20:10
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-051',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-18 20:12:03',
       '2026-09-18 20:10:50'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-051');

-- fixgo-notification-demo-20261104-052 | SENT | 2026-09-01 14:21
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-052',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-01 14:22:35',
       '2026-09-01 14:21:09'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-052');

-- fixgo-notification-demo-20261104-053 | SENT | 2026-10-27 08:32
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-053',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-27 08:34:07',
       '2026-10-27 08:32:28'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-053');

-- fixgo-notification-demo-20261104-054 | FAILED | 2026-10-10 15:43
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-054',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-10-10 15:43:47'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-054');

-- fixgo-notification-demo-20261104-055 | SENT | 2026-09-23 09:54
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-055',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-23 09:54:12',
       '2026-09-23 09:54:06'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-055');

-- fixgo-notification-demo-20261104-056 | SENT | 2026-09-06 16:05
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-056',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-06 16:05:44',
       '2026-09-06 16:05:25'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-056');

-- fixgo-notification-demo-20261104-057 | SENT | 2026-11-01 10:16
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-057',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-01 10:17:16',
       '2026-11-01 10:16:44'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-057');

-- fixgo-notification-demo-20261104-058 | SENT | 2026-10-15 17:27
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-058',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-15 17:27:48',
       '2026-10-15 17:27:03'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-058');

-- fixgo-notification-demo-20261104-059 | FAILED | 2026-09-28 11:38
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-059',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-09-28 11:38:22'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-059');

-- fixgo-notification-demo-20261104-060 | FAILED | 2026-09-11 18:49
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at)
SELECT 'fixgo-notification-demo-20261104-060',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-11 18:49:41'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-060');

COMMIT;


-- 이메일 프리셋
INSERT INTO email_preset (name,
                          message_title,
                          message,
                          created_at,
                          updated_at)
VALUES ('접수 완료 안내',
        '[FixGo] A/S 접수 완료 안내',
        '안녕하세요. FixGo입니다.\n고객님의 A/S 접수가 정상적으로 완료되었습니다.\n진행 상황은 고객 안내 링크에서 확인하실 수 있습니다.',
        NOW(), NOW()),
       ('견적서 발급 완료 안내',
        '[FixGo] A/S 견적서 발급 안내',
        '안녕하세요. FixGo입니다.\n고객님의 A/S 견적서가 발급되었습니다.\n고객 안내 링크에서 견적 내용을 확인해 주세요.',
        NOW(), NOW()),
       ('수리 진행 안내',
        '[FixGo] A/S 수리 진행 안내',
        '안녕하세요. FixGo입니다.\n고객님의 제품 수리가 진행 중입니다.\n수리 진행 상황은 고객 안내 링크에서 확인하실 수 있습니다.',
        NOW(), NOW()),
       ('수리 완료 안내',
        '[FixGo] A/S 수리 완료 안내',
        '안녕하세요. FixGo입니다.\n고객님의 제품 수리가 완료되었습니다.\n수령 방법에 따라 제품을 전달해 드리겠습니다.',
        NOW(), NOW()),
       ('수리 완료 및 결제 안내',
        '[FixGo] 수리 완료 및 결제 안내',
        '안녕하세요. FixGo입니다.\n고객님의 제품 수리가 완료되었습니다.\n수리 비용 및 결제 정보를 고객 안내 링크에서 확인해 주세요.',
        NOW(), NOW()),
       ('결제 요청 안내',
        '[FixGo] A/S 결제 요청 안내',
        '안녕하세요. FixGo입니다.\nA/S 수리 비용 결제를 요청드립니다.\n고객 안내 링크에서 결제 내용을 확인해 주세요.',
        NOW(), NOW()),
       ('결제 완료 안내',
        '[FixGo] A/S 결제 완료 안내',
        '안녕하세요. FixGo입니다.\nA/S 수리 비용 결제가 정상적으로 완료되었습니다.\nFixGo를 이용해 주셔서 감사합니다.',
        NOW(), NOW()),
       ('배송 시작 안내',
        '[FixGo] A/S 제품 배송 시작 안내',
        '안녕하세요. FixGo입니다.\n고객님의 수리 완료 제품이 택배로 발송되었습니다.\n배송 조회 정보는 고객 안내 링크에서 확인하실 수 있습니다.',
        NOW(), NOW()),
       ('방문 수령 안내',
        '[FixGo] A/S 제품 방문 수령 안내',
        '안녕하세요. FixGo입니다.\n고객님의 제품 수리가 완료되어 방문 수령이 가능합니다.\n방문 전 수령 가능 여부를 확인해 주세요.',
        NOW(), NOW()),
       ('A/S 처리 완료 안내',
        '[FixGo] A/S 처리 완료 안내',
        '안녕하세요. FixGo입니다.\n고객님의 A/S 처리가 최종 완료되었습니다.\nFixGo 서비스를 이용해 주셔서 감사합니다.',
        NOW(), NOW());

