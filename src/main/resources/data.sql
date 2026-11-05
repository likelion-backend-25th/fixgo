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
SELECT 'admin001@fixgo.com',
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
        0
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'admin001@fixgo.com');


-- 이메일 관련 더미데이터
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


-- A/S 접수 더미데이터 10건: 상태별 1건, 접수번호 AS-yyMMdd-UUID
-- 연관 데이터는 고정 ID 대신 이메일/모델명/시리얼번호로 연결한다.
-- 수동 실행 시 이 주석 아래 전체를 순서대로 실행한다.
-- as_reception INSERT만 실행하면 선행 고객이 없어 customer_id가 NULL이 된다.

INSERT INTO member (email, password_hash, name, phone, role, address, profile_img_url, status, created_at, updated_at,
                    created_by, updated_by)
SELECT 'reception.demo@fixgo.example',
       '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
       '접수담당 더미',
       '010-0000-0000',
       'ROLE_RECEPTIONIST',
       '서울시 테스트센터',
       '',
       'ACTIVE',
       NOW(),
       NOW(),
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'reception.demo@fixgo.example');

INSERT INTO member (email, password_hash, name, phone, role, address, profile_img_url, status, created_at, updated_at,
                    created_by, updated_by)
SELECT 'technician.demo@fixgo.example',
       '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
       '수리기사 더미',
       '010-0000-0000',
       'ROLE_TECHNICIAN',
       '서울시 테스트센터',
       '',
       'ACTIVE',
       NOW(),
       NOW(),
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'technician.demo@fixgo.example');

INSERT INTO product_model (name, model_name, category, warranty_months, is_active, created_at, updated_at, created_by,
                           updated_by)
SELECT '더미 노트북',
       'FIXGO-DEMO-LAPTOP',
       'LAPTOP',
       24,
       TRUE,
       NOW(),
       NOW(),
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP');

-- 01. 김하늘 / RECEIVED
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '김하늘',
       '010-9000-0001',
       'customer01@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 1',
       '1층',
       '2026-11-05 09:00:00',
       '2026-11-05 09:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer01@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer01@fixgo.example'),
       'FIXGO-DEMO-SN-01',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-05 09:00:00',
       '2026-11-05 09:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-01');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261105-8f3a2c10-7b6e-4d90-a123-000000000001',
       (SELECT MIN(id) FROM customer WHERE email = 'customer01@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-01'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '전원 불량',
       '전원 불량 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 01',
       'DELIVERY',
       'RECEIVED',
       '2026-11-05 09:00:00',
       NULL,
       NULL,
       '2026-11-05 09:00:00',
       '2026-11-05 09:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261105-8f3a2c10-7b6e-4d90-a123-000000000001');

-- 02. 이서준 / INSPECTION
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '이서준',
       '010-9000-0002',
       'customer02@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 2',
       '2층',
       '2026-11-05 09:00:00',
       '2026-11-05 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer02@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer02@fixgo.example'),
       'FIXGO-DEMO-SN-02',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-05 09:00:00',
       '2026-11-05 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-02');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261105-8f3a2c10-7b6e-4d90-a123-000000000002',
       (SELECT MIN(id) FROM customer WHERE email = 'customer02@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-02'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '충전 불량',
       '충전 불량 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 02',
       'PICKUP',
       'INSPECTION',
       '2026-11-05 15:00:00',
       NULL,
       NULL,
       '2026-11-05 09:00:00',
       '2026-11-05 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261105-8f3a2c10-7b6e-4d90-a123-000000000002');

-- 03. 박지민 / WARRANTY_REVIEW
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '박지민',
       '010-9000-0003',
       'customer03@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 3',
       '3층',
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer03@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer03@fixgo.example'),
       'FIXGO-DEMO-SN-03',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-03');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261104-8f3a2c10-7b6e-4d90-a123-000000000003',
       (SELECT MIN(id) FROM customer WHERE email = 'customer03@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-03'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '화면 깜빡임',
       '화면 깜빡임 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 03',
       'DELIVERY',
       'WARRANTY_REVIEW',
       '2026-11-04 15:00:00',
       NULL,
       NULL,
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261104-8f3a2c10-7b6e-4d90-a123-000000000003');

-- 04. 최유진 / AWAITING_QUOTE_APPROVAL
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '최유진',
       '010-9000-0004',
       'customer04@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 4',
       '4층',
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer04@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer04@fixgo.example'),
       'FIXGO-DEMO-SN-04',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-04');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261104-8f3a2c10-7b6e-4d90-a123-000000000004',
       (SELECT MIN(id) FROM customer WHERE email = 'customer04@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-04'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '키보드 불량',
       '키보드 불량 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 04',
       'PICKUP',
       'AWAITING_QUOTE_APPROVAL',
       '2026-11-04 15:00:00',
       NULL,
       NULL,
       '2026-11-04 09:00:00',
       '2026-11-04 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261104-8f3a2c10-7b6e-4d90-a123-000000000004');

-- 05. 정민수 / AWAITING_REPAIR
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '정민수',
       '010-9000-0005',
       'customer05@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 5',
       '5층',
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer05@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer05@fixgo.example'),
       'FIXGO-DEMO-SN-05',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-05');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261103-8f3a2c10-7b6e-4d90-a123-000000000005',
       (SELECT MIN(id) FROM customer WHERE email = 'customer05@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-05'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '배터리 소모',
       '배터리 소모 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 05',
       'DELIVERY',
       'AWAITING_REPAIR',
       '2026-11-03 15:00:00',
       NULL,
       NULL,
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261103-8f3a2c10-7b6e-4d90-a123-000000000005');

-- 06. 강수빈 / REPAIR_IN_PROGRESS
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '강수빈',
       '010-9000-0006',
       'customer06@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 6',
       '6층',
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer06@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer06@fixgo.example'),
       'FIXGO-DEMO-SN-06',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-06');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261103-8f3a2c10-7b6e-4d90-a123-000000000006',
       (SELECT MIN(id) FROM customer WHERE email = 'customer06@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-06'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '과열',
       '과열 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 06',
       'PICKUP',
       'REPAIR_IN_PROGRESS',
       '2026-11-03 15:00:00',
       NULL,
       NULL,
       '2026-11-03 09:00:00',
       '2026-11-03 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261103-8f3a2c10-7b6e-4d90-a123-000000000006');

-- 07. 조현우 / AWAITING_PAYMENT
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '조현우',
       '010-9000-0007',
       'customer07@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 7',
       '7층',
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer07@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer07@fixgo.example'),
       'FIXGO-DEMO-SN-07',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-07');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000007',
       (SELECT MIN(id) FROM customer WHERE email = 'customer07@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-07'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '소음 발생',
       '소음 발생 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 07',
       'DELIVERY',
       'AWAITING_PAYMENT',
       '2026-11-02 15:00:00',
       NULL,
       NULL,
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000007');

-- 08. 윤서연 / AWAITING_SHIPMENT
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '윤서연',
       '010-9000-0008',
       'customer08@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 8',
       '8층',
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer08@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer08@fixgo.example'),
       'FIXGO-DEMO-SN-08',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-08');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000008',
       (SELECT MIN(id) FROM customer WHERE email = 'customer08@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-08'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '와이파이 불량',
       '와이파이 불량 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 08',
       'DELIVERY',
       'AWAITING_SHIPMENT',
       '2026-11-02 15:00:00',
       NULL,
       NULL,
       '2026-11-02 09:00:00',
       '2026-11-02 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000008');

-- 09. 장도윤 / AWAITING_PICKUP
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '장도윤',
       '010-9000-0009',
       'customer09@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 9',
       '9층',
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer09@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer09@fixgo.example'),
       'FIXGO-DEMO-SN-09',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-09');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000009',
       (SELECT MIN(id) FROM customer WHERE email = 'customer09@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-09'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '터치패드 불량',
       '터치패드 불량 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 09',
       'PICKUP',
       'AWAITING_PICKUP',
       '2026-11-01 15:00:00',
       NULL,
       NULL,
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000009');

-- 10. 임지우 / COMPLETED
INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT '임지우',
       '010-9000-0010',
       'customer10@fixgo.example',
       '06200',
       '서울시 강남구 테스트로 10',
       '10층',
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'customer10@fixgo.example');

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'),
       (SELECT MIN(id) FROM customer WHERE email = 'customer10@fixgo.example'),
       'FIXGO-DEMO-SN-10',
       '2026-01-01 00:00:00',
       '2026-03-01 00:00:00',
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE serial_number = 'FIXGO-DEMO-SN-10');

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010',
       (SELECT MIN(id) FROM customer WHERE email = 'customer10@fixgo.example'),
       (SELECT id FROM product WHERE serial_number = 'FIXGO-DEMO-SN-10'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'technician.demo@fixgo.example'),
       '화면 파손',
       '화면 파손 증상으로 점검을 요청합니다.',
       'A/S 목록 확인용 더미데이터 10',
       'PICKUP',
       'COMPLETED',
       '2026-11-01 15:00:00',
       '2026-11-01 15:00:00',
       NULL,
       '2026-11-01 09:00:00',
       '2026-11-01 15:00:00',
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
WHERE NOT EXISTS (SELECT 1 FROM as_reception WHERE reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010');
