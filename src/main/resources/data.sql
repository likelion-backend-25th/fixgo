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
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-001',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-11-04 08:00:00',
       '2026-11-04 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-001');

-- fixgo-notification-demo-20261104-002 | FAILED | 2026-10-18 15:11
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-002',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-18 15:11:19',
       '2026-10-18 15:11:19'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-002');

-- fixgo-notification-demo-20261104-003 | SENDING | 2026-10-01 09:22
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-003',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-10-01 09:22:38',
       '2026-10-01 09:22:38'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-003');

-- fixgo-notification-demo-20261104-004 | SENT | 2026-09-14 16:33
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-004',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-14 16:34:54',
       '2026-09-14 16:33:57',
       '2026-09-14 16:33:57'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-004');

-- fixgo-notification-demo-20261104-005 | SENT | 2026-08-28 10:44
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-005',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-28 10:45:26',
       '2026-08-28 10:44:16',
       '2026-08-28 10:44:16'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-005');

-- fixgo-notification-demo-20261104-006 | SENT | 2026-10-23 17:55
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-006',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-23 17:56:58',
       '2026-10-23 17:55:35',
       '2026-10-23 17:55:35'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-006');

-- fixgo-notification-demo-20261104-007 | FAILED | 2026-10-06 11:06
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-007',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-06 11:06:54',
       '2026-10-06 11:06:54'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-007');

-- fixgo-notification-demo-20261104-008 | SENDING | 2026-09-19 18:17
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-008',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-09-19 18:17:13',
       '2026-09-19 18:17:13'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-008');

-- fixgo-notification-demo-20261104-009 | PENDING | 2026-09-02 12:28
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-009',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-09-02 12:28:32',
       '2026-09-02 12:28:32'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-009');

-- fixgo-notification-demo-20261104-010 | SENT | 2026-10-28 19:39
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-010',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-28 19:40:07',
       '2026-10-28 19:39:51',
       '2026-10-28 19:39:51'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-010');

-- fixgo-notification-demo-20261104-011 | FAILED | 2026-10-11 13:50
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-011',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-11 13:50:10',
       '2026-10-11 13:50:10'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-011');

-- fixgo-notification-demo-20261104-012 | SENT | 2026-09-24 20:01
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-012',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-24 20:02:11',
       '2026-09-24 20:01:29',
       '2026-09-24 20:01:29'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-012');

-- fixgo-notification-demo-20261104-013 | FAILED | 2026-09-07 14:12
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-013',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-07 14:12:48',
       '2026-09-07 14:12:48'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-013');

-- fixgo-notification-demo-20261104-014 | SENT | 2026-11-02 08:23
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-014',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-02 08:24:15',
       '2026-11-02 08:23:07',
       '2026-11-02 08:23:07'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-014');

-- fixgo-notification-demo-20261104-015 | SENT | 2026-10-16 15:34
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-015',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-16 15:35:47',
       '2026-10-16 15:34:26',
       '2026-10-16 15:34:26'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-015');

-- fixgo-notification-demo-20261104-016 | SENT | 2026-09-29 09:45
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-016',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-29 09:47:19',
       '2026-09-29 09:45:45',
       '2026-09-29 09:45:45'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-016');

-- fixgo-notification-demo-20261104-017 | PENDING | 2026-09-12 16:56
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-017',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'PENDING',
       0,
       NULL,
       NULL,
       '2026-09-12 16:56:04',
       '2026-09-12 16:56:04'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-017');

-- fixgo-notification-demo-20261104-018 | SENT | 2026-08-26 10:07
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-018',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-26 10:09:23',
       '2026-08-26 10:07:23',
       '2026-08-26 10:07:23'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-018');

-- fixgo-notification-demo-20261104-019 | SENT | 2026-10-21 17:18
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-019',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-21 17:18:56',
       '2026-10-21 17:18:42',
       '2026-10-21 17:18:42'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-019');

-- fixgo-notification-demo-20261104-020 | FAILED | 2026-10-04 11:29
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-020',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-04 11:29:01',
       '2026-10-04 11:29:01'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-020');

-- fixgo-notification-demo-20261104-021 | SENT | 2026-09-17 18:40
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-021',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-17 18:41:00',
       '2026-09-17 18:40:20',
       '2026-09-17 18:40:20'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-021');

-- fixgo-notification-demo-20261104-022 | SENT | 2026-08-31 12:51
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-022',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-31 12:52:32',
       '2026-08-31 12:51:39',
       '2026-08-31 12:51:39'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-022');

-- fixgo-notification-demo-20261104-023 | FAILED | 2026-10-26 19:02
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-023',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-26 19:02:58',
       '2026-10-26 19:02:58'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-023');

-- fixgo-notification-demo-20261104-024 | SENT | 2026-10-09 13:13
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-024',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-09 13:14:36',
       '2026-10-09 13:13:17',
       '2026-10-09 13:13:17'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-024');

-- fixgo-notification-demo-20261104-025 | FAILED | 2026-09-22 20:24
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-025',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-22 20:24:36',
       '2026-09-22 20:24:36'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-025');

-- fixgo-notification-demo-20261104-026 | SENT | 2026-09-05 14:35
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-026',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-05 14:37:40',
       '2026-09-05 14:35:55',
       '2026-09-05 14:35:55'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-026');

-- fixgo-notification-demo-20261104-027 | SENT | 2026-10-31 08:46
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-027',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-31 08:48:12',
       '2026-10-31 08:46:14',
       '2026-10-31 08:46:14'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-027');

-- fixgo-notification-demo-20261104-028 | FAILED | 2026-10-14 15:57
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-028',
       'REPAIR',
       'lynaeryn@gmail.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-14 15:57:33',
       '2026-10-14 15:57:33'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-028');

-- fixgo-notification-demo-20261104-029 | SENT | 2026-09-27 09:08
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-029',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-27 09:09:17',
       '2026-09-27 09:08:52',
       '2026-09-27 09:08:52'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-029');

-- fixgo-notification-demo-20261104-030 | FAILED | 2026-09-10 16:19
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-030',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-10 16:19:11',
       '2026-09-10 16:19:11'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-030');

-- fixgo-notification-demo-20261104-031 | SENT | 2026-08-24 10:30
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-031',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-24 10:31:21',
       '2026-08-24 10:30:30',
       '2026-08-24 10:30:30'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-031');

-- fixgo-notification-demo-20261104-032 | SENDING | 2026-10-19 17:41
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-032',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENDING',
       0,
       NULL,
       NULL,
       '2026-10-19 17:41:49',
       '2026-10-19 17:41:49'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-032');

-- fixgo-notification-demo-20261104-033 | SENT | 2026-10-02 11:52
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-033',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-02 11:53:25',
       '2026-10-02 11:52:08',
       '2026-10-02 11:52:08'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-033');

-- fixgo-notification-demo-20261104-034 | SENT | 2026-09-15 18:03
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-034',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-15 18:04:57',
       '2026-09-15 18:03:27',
       '2026-09-15 18:03:27'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-034');

-- fixgo-notification-demo-20261104-035 | SENT | 2026-08-29 12:14
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-035',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-29 12:16:29',
       '2026-08-29 12:14:46',
       '2026-08-29 12:14:46'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-035');

-- fixgo-notification-demo-20261104-036 | SENT | 2026-10-24 19:25
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-036',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-24 19:27:01',
       '2026-10-24 19:25:05',
       '2026-10-24 19:25:05'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-036');

-- fixgo-notification-demo-20261104-037 | SENT | 2026-10-07 13:36
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-037',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-07 13:36:34',
       '2026-10-07 13:36:24',
       '2026-10-07 13:36:24'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-037');

-- fixgo-notification-demo-20261104-038 | SENT | 2026-09-20 20:47
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-038',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-20 20:48:06',
       '2026-09-20 20:47:43',
       '2026-09-20 20:47:43'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-038');

-- fixgo-notification-demo-20261104-039 | SENT | 2026-09-03 14:58
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-039',
       'PAYMENT',
       'seo.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-03 14:58:38',
       '2026-09-03 14:58:02',
       '2026-09-03 14:58:02'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-039');

-- fixgo-notification-demo-20261104-040 | SENT | 2026-10-29 08:09
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-040',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-29 08:10:10',
       '2026-10-29 08:09:21',
       '2026-10-29 08:09:21'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-040');

-- fixgo-notification-demo-20261104-041 | FAILED | 2026-10-12 15:20
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-041',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-10-12 15:20:40',
       '2026-10-12 15:20:40'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-041');

-- fixgo-notification-demo-20261104-042 | FAILED | 2026-09-25 09:31
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-042',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-25 09:31:59',
       '2026-09-25 09:31:59'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-042');

-- fixgo-notification-demo-20261104-043 | SENT | 2026-09-08 16:42
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-043',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-08 16:43:46',
       '2026-09-08 16:42:18',
       '2026-09-08 16:42:18'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-043');

-- fixgo-notification-demo-20261104-044 | SENT | 2026-11-03 10:53
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-044',
       'PAYMENT',
       'park.test@daum.net',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-03 10:55:18',
       '2026-11-03 10:53:37',
       '2026-11-03 10:53:37'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-044');

-- fixgo-notification-demo-20261104-045 | FAILED | 2026-10-17 17:04
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-045',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-10-17 17:04:56',
       '2026-10-17 17:04:56'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-045');

-- fixgo-notification-demo-20261104-046 | FAILED | 2026-09-30 11:15
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-046',
       'RECEIVED',
       'lynaeryn@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-09-30 11:15:15',
       '2026-09-30 11:15:15'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-046');

-- fixgo-notification-demo-20261104-047 | FAILED | 2026-09-13 18:26
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-047',
       'ESTIMATE',
       'lynaeryn@gmail.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-09-13 18:26:34',
       '2026-09-13 18:26:34'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-047');

-- fixgo-notification-demo-20261104-048 | SENT | 2026-08-27 12:37
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-048',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-08-27 12:38:27',
       '2026-08-27 12:37:53',
       '2026-08-27 12:37:53'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-048');

-- fixgo-notification-demo-20261104-049 | FAILED | 2026-10-22 19:48
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-049',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       1,
       'MailSendException',
       NULL,
       '2026-10-22 19:48:12',
       '2026-10-22 19:48:12'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-049');

-- fixgo-notification-demo-20261104-050 | SENT | 2026-10-05 13:59
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-050',
       'SHIPPING',
       'kim.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-05 14:00:31',
       '2026-10-05 13:59:31',
       '2026-10-05 13:59:31'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-050');

-- fixgo-notification-demo-20261104-051 | SENT | 2026-09-18 20:10
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-051',
       'RECEIVED',
       'choi.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-18 20:12:03',
       '2026-09-18 20:10:50',
       '2026-09-18 20:10:50'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-051');

-- fixgo-notification-demo-20261104-052 | SENT | 2026-09-01 14:21
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-052',
       'ESTIMATE',
       'yoon.test@kakao.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       1,
       NULL,
       '2026-09-01 14:22:35',
       '2026-09-01 14:21:09',
       '2026-09-01 14:21:09'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-052');

-- fixgo-notification-demo-20261104-053 | SENT | 2026-10-27 08:32
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-053',
       'REPAIR',
       'song.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-27 08:34:07',
       '2026-10-27 08:32:28',
       '2026-10-27 08:32:28'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-053');

-- fixgo-notification-demo-20261104-054 | FAILED | 2026-10-10 15:43
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-054',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-10-10 15:43:47',
       '2026-10-10 15:43:47'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-054');

-- fixgo-notification-demo-20261104-055 | SENT | 2026-09-23 09:54
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-055',
       'SHIPPING',
       'han.test@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-23 09:54:12',
       '2026-09-23 09:54:06',
       '2026-09-23 09:54:06'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-055');

-- fixgo-notification-demo-20261104-056 | SENT | 2026-09-06 16:05
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-056',
       'RECEIVED',
       'lim.test@gmail.com',
       '[FixGo] A/S 접수 완료 안내',
       '고객님의 A/S 접수가 정상적으로 완료되었습니다. 안내 페이지에서 접수 현황을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-09-06 16:05:44',
       '2026-09-06 16:05:25',
       '2026-09-06 16:05:25'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-056');

-- fixgo-notification-demo-20261104-057 | SENT | 2026-11-01 10:16
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-057',
       'ESTIMATE',
       'lee.test@naver.com',
       '[FixGo] 견적서 발급 안내',
       'A/S 견적서가 발급되었습니다. 안내 페이지에서 견적 내용을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-11-01 10:17:16',
       '2026-11-01 10:16:44',
       '2026-11-01 10:16:44'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-057');

-- fixgo-notification-demo-20261104-058 | SENT | 2026-10-15 17:27
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-058',
       'REPAIR',
       'jung.test@naver.com',
       '[FixGo] 수리 완료 안내',
       '제품 수리가 완료되었습니다. 수령 및 진행 사항을 확인해 주세요.',
       'SENT',
       0,
       NULL,
       '2026-10-15 17:27:48',
       '2026-10-15 17:27:03',
       '2026-10-15 17:27:03'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-058');

-- fixgo-notification-demo-20261104-059 | FAILED | 2026-09-28 11:38
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-059',
       'PAYMENT',
       'lynaeryn@gmail.com',
       '[FixGo] 결제 안내',
       'A/S 비용 결제 안내입니다. 고객 안내 페이지에서 결제 정보를 확인해 주세요.',
       'FAILED',
       2,
       'MailAuthenticationException',
       NULL,
       '2026-09-28 11:38:22',
       '2026-09-28 11:38:22'
WHERE NOT EXISTS (SELECT 1 FROM notification_log WHERE event_key = 'fixgo-notification-demo-20261104-059');

-- fixgo-notification-demo-20261104-060 | FAILED | 2026-09-11 18:49
INSERT INTO notification_log (event_key, notification_type, recipient_email, message_title, message,
                              status, retry_count, error_message, sent_at, created_at, updated_at)
SELECT 'fixgo-notification-demo-20261104-060',
       'SHIPPING',
       'lynaeryn@gmail.com',
       '[FixGo] 배송 시작 안내',
       '수리 완료 제품이 발송되었습니다. 배송 진행 상황을 확인해 주세요.',
       'FAILED',
       0,
       'MailConnectException',
       NULL,
       '2026-09-11 18:49:41',
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

INSERT INTO member (email, password_hash, name, phone, role, address, profile_img_url, status, created_at, updated_at,
                    created_by, updated_by)
SELECT 'max.demo@fixgo.example',
       '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
       '수리기사 맥스',
       '010-0000-0000',
       'ROLE_TECHNICIAN',
       '서울시 테스트센터',
       '',
       'ACTIVE',
       NOW(),
       NOW(),
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'max.demo@fixgo.example');

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


-- 노트북 부품 더미데이터
-- 기존 product_model(FIXGO-DEMO-LAPTOP)만 참조한다.
-- 재고 0은 LOW_QUANTITY, 재고가 있는 사용 부품은 ACTIVE, 단종 부품은 DISCONTINUED.

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '14인치 FHD IPS 노트북 LCD 패널 모듈 (1920x1080, 30핀 eDP, 무광, 슬림 베젤)',
       189000, 50, 8, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '14인치 FHD IPS 노트북 LCD 패널 모듈 (1920x1080, 30핀 eDP, 무광, 슬림 베젤)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '14인치 노트북 LCD 패널 연결용 30핀 eDP 디스플레이 플렉스 케이블',
       18000, 35, 14, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '14인치 노트북 LCD 패널 연결용 30핀 eDP 디스플레이 플렉스 케이블');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 상판 내장 HD 웹캠 모듈 (720p, 듀얼 마이크 일체형, 상판 커버 고정 브라켓 포함)',
       32000, 25, 11, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 상판 내장 HD 웹캠 모듈 (720p, 듀얼 마이크 일체형, 상판 커버 고정 브라켓 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 팜레스트 일체형 한국어 키보드 모듈 (펜타그래프, 화이트 백라이트, 스크류 고정형)',
       76000, 40, 6, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 팜레스트 일체형 한국어 키보드 모듈 (펜타그래프, 화이트 백라이트, 스크류 고정형)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 클릭패드 터치패드 모듈 (좌우 물리 버튼 일체형, 멀티터치, 팜레스트 케이블 포함)',
       42000, 30, 9, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 클릭패드 터치패드 모듈 (좌우 물리 버튼 일체형, 멀티터치, 팜레스트 케이블 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 내장 리튬이온 배터리 팩 (11.55V, 50Wh, 3셀, 4핀 커넥터)',
       98000, 20, 7, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 내장 리튬이온 배터리 팩 (11.55V, 50Wh, 3셀, 4핀 커넥터)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '65W USB-C PD 노트북 전원 어댑터 (20V 3.25A, 1.8m 케이블, 한국형 플러그)',
       39000, 10, 16, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '65W USB-C PD 노트북 전원 어댑터 (20V 3.25A, 1.8m 케이블, 한국형 플러그)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 USB-C 충전 포트 도터보드 (DC-IN, 메인보드 연결 8핀 플렉스 케이블 포함)',
       27000, 35, 5, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 USB-C 충전 포트 도터보드 (DC-IN, 메인보드 연결 8핀 플렉스 케이블 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 메인보드용 CMOS 코인 배터리 (CR2032, 2핀 와이어 하네스)',
       6000, 15, 24, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 메인보드용 CMOS 코인 배터리 (CR2032, 2핀 와이어 하네스)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT 'M.2 2280 NVMe SSD 512GB (PCIe 3.0 x4, 방열 라벨 포함)',
       72000, 20, 10, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = 'M.2 2280 NVMe SSD 512GB (PCIe 3.0 x4, 방열 라벨 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 DDR4 3200MHz SO-DIMM 메모리 8GB (1.2V, 260핀)',
       34000, 15, 18, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 DDR4 3200MHz SO-DIMM 메모리 8GB (1.2V, 260핀)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 CPU 쿨링팬 (4핀 PWM, 좌측 배기, 방진 고무 마운트 포함)',
       29000, 30, 4, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 CPU 쿨링팬 (4핀 PWM, 좌측 배기, 방진 고무 마운트 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 히트파이프 일체형 구리 방열판 모듈 (CPU 접촉면, 서멀패드 미포함)',
       45000, 40, 3, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 히트파이프 일체형 구리 방열판 모듈 (CPU 접촉면, 서멀패드 미포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT 'Intel AX201 Wi-Fi 6 / Bluetooth 5.2 무선랜 카드 (M.2 2230, CNVi)',
       25000, 20, 12, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = 'Intel AX201 Wi-Fi 6 / Bluetooth 5.2 무선랜 카드 (M.2 2230, CNVi)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 좌측 I/O 도터보드 (USB-A 3.0 2포트, HDMI, 3.5mm 콤보 잭)',
       36000, 35, 0, 'LOW_QUANTITY', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 좌측 I/O 도터보드 (USB-A 3.0 2포트, HDMI, 3.5mm 콤보 잭)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 3.5mm 콤보 오디오 잭 기판 (헤드폰/마이크 공용, 4핀 하네스)',
       14000, 25, 0, 'LOW_QUANTITY', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 3.5mm 콤보 오디오 잭 기판 (헤드폰/마이크 공용, 4핀 하네스)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 하단 커버 고정용 M2x3 접시머리 나사 세트 (10개입, 블랙)',
       3000, 10, 40, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 하단 커버 고정용 M2x3 접시머리 나사 세트 (10개입, 블랙)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 LCD 힌지 좌측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)',
       16000, 45, 6, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 LCD 힌지 좌측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 LCD 힌지 우측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)',
       16000, 45, 6, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 LCD 힌지 우측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 스테레오 스피커 모듈 좌우 세트 (2W x 2, 4핀 커넥터, 하판 고정 러버 포함)',
       22000, 30, 8, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 스테레오 스피커 모듈 좌우 세트 (2W x 2, 4핀 커넥터, 하판 고정 러버 포함)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 키보드 백라이트 LED 플렉스 케이블 (팜레스트-메인보드, 12핀)',
       11000, 25, 13, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 키보드 백라이트 LED 플렉스 케이블 (팜레스트-메인보드, 12핀)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 전원 버튼 보드 (지문 인식 센서 미포함, 파워 스위치·LED, 6핀 케이블)',
       19000, 20, 9, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 전원 버튼 보드 (지문 인식 센서 미포함, 파워 스위치·LED, 6핀 케이블)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '노트북 CPU/GPU 접촉면용 서멀 패드 세트 (1.0mm, 10x10mm 4매)',
       5000, 15, 30, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '노트북 CPU/GPU 접촉면용 서멀 패드 세트 (1.0mm, 10x10mm 4매)');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '14인치 HD TN 노트북 LCD 패널 모듈 (1366x768, 40핀 LVDS, 구형 상판 전용)',
       64000, 50, 2, 'DISCONTINUED', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '14인치 HD TN 노트북 LCD 패널 모듈 (1366x768, 40핀 LVDS, 구형 상판 전용)');

-- 위 부품은 모두 기존 노트북 모델 FIXGO-DEMO-LAPTOP과 호환된다.
INSERT INTO part_compatibility (part_id, product_model_id)
SELECT p.id,
       (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP')
FROM part p
WHERE p.name IN (
    '14인치 FHD IPS 노트북 LCD 패널 모듈 (1920x1080, 30핀 eDP, 무광, 슬림 베젤)',
    '14인치 노트북 LCD 패널 연결용 30핀 eDP 디스플레이 플렉스 케이블',
    '노트북 상판 내장 HD 웹캠 모듈 (720p, 듀얼 마이크 일체형, 상판 커버 고정 브라켓 포함)',
    '노트북 팜레스트 일체형 한국어 키보드 모듈 (펜타그래프, 화이트 백라이트, 스크류 고정형)',
    '노트북 클릭패드 터치패드 모듈 (좌우 물리 버튼 일체형, 멀티터치, 팜레스트 케이블 포함)',
    '노트북 내장 리튬이온 배터리 팩 (11.55V, 50Wh, 3셀, 4핀 커넥터)',
    '65W USB-C PD 노트북 전원 어댑터 (20V 3.25A, 1.8m 케이블, 한국형 플러그)',
    '노트북 USB-C 충전 포트 도터보드 (DC-IN, 메인보드 연결 8핀 플렉스 케이블 포함)',
    '노트북 메인보드용 CMOS 코인 배터리 (CR2032, 2핀 와이어 하네스)',
    'M.2 2280 NVMe SSD 512GB (PCIe 3.0 x4, 방열 라벨 포함)',
    '노트북 DDR4 3200MHz SO-DIMM 메모리 8GB (1.2V, 260핀)',
    '노트북 CPU 쿨링팬 (4핀 PWM, 좌측 배기, 방진 고무 마운트 포함)',
    '노트북 히트파이프 일체형 구리 방열판 모듈 (CPU 접촉면, 서멀패드 미포함)',
    'Intel AX201 Wi-Fi 6 / Bluetooth 5.2 무선랜 카드 (M.2 2230, CNVi)',
    '노트북 좌측 I/O 도터보드 (USB-A 3.0 2포트, HDMI, 3.5mm 콤보 잭)',
    '노트북 3.5mm 콤보 오디오 잭 기판 (헤드폰/마이크 공용, 4핀 하네스)',
    '노트북 하단 커버 고정용 M2x3 접시머리 나사 세트 (10개입, 블랙)',
    '노트북 LCD 힌지 좌측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)',
    '노트북 LCD 힌지 우측 브라켓 (알루미늄, 상판-본체 연결, 나사 미포함)',
    '노트북 스테레오 스피커 모듈 좌우 세트 (2W x 2, 4핀 커넥터, 하판 고정 러버 포함)',
    '노트북 키보드 백라이트 LED 플렉스 케이블 (팜레스트-메인보드, 12핀)',
    '노트북 전원 버튼 보드 (지문 인식 센서 미포함, 파워 스위치·LED, 6핀 케이블)',
    '노트북 CPU/GPU 접촉면용 서멀 패드 세트 (1.0mm, 10x10mm 4매)',
    '14인치 HD TN 노트북 LCD 패널 모듈 (1366x768, 40핀 LVDS, 구형 상판 전용)'
)
  AND NOT EXISTS (SELECT 1
                  FROM part_compatibility pc
                  WHERE pc.part_id = p.id
                    AND pc.product_model_id = (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-LAPTOP'));


-- 다량 재고 수정 테스트용 완료 수리 1건
-- 기존 완료 접수(화면 파손, AS-261101-...010)와 그 담당 기사에 연결한다.
-- 사용 부품은 해당 노트북과 호환되고, 재고가 사용 수량 이상인 ACTIVE 부품만 넣는다.
INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       '상판 LCD 패널 균열과 30핀 eDP 케이블 손상으로 화면이 출력되지 않음',
       '14인치 FHD IPS 패널과 eDP 디스플레이 케이블을 교체하고 점등 및 화면 깜빡임을 확인함',
       'REPAIRABLE',
       'PAID',
       '구매일 2026-03-01 기준 보증 기간 내이나, 화면 파손은 고객 과실로 유상 수리',
       50000,
       0,
       257000,
       'COMPLETED',
       NULL,
       '2026-11-01 14:30:00',
       '2026-11-01 10:00:00',
       '2026-11-01 15:00:00',
       ar.technician_id,
       ar.technician_id
FROM as_reception ar
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010'
  AND ar.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

INSERT INTO repair_work (repair_id, repair_preset_name, work_name, created_at, updated_at, created_by, updated_by)
SELECT r.id,
       '화면 파손',
       '14인치 FHD LCD 패널 및 eDP 케이블 교체',
       '2026-11-01 11:00:00',
       '2026-11-01 14:00:00',
       r.technician_id,
       r.technician_id
FROM repair r
         JOIN as_reception ar ON ar.id = r.as_reception_id
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010'
  AND NOT EXISTS (SELECT 1
                  FROM repair_work rw
                  WHERE rw.repair_id = r.id
                    AND rw.work_name = '14인치 FHD LCD 패널 및 eDP 케이블 교체');

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id,
       p.id,
       1,
       p.unit_price,
       '2026-11-01 11:30:00'
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN part p ON p.name = '14인치 FHD IPS 노트북 LCD 패널 모듈 (1920x1080, 30핀 eDP, 무광, 슬림 베젤)'
         JOIN part_compatibility pc ON pc.part_id = p.id AND pc.product_model_id = pr.product_model_id
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010'
  AND rw.work_name = '14인치 FHD LCD 패널 및 eDP 케이블 교체'
  AND r.status = 'COMPLETED'
  AND p.status = 'ACTIVE'
  AND p.stock_quantity >= 1
  AND NOT EXISTS (SELECT 1
                  FROM repair_part_usage u
                  WHERE u.repair_work_id = rw.id
                    AND u.part_id = p.id);

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id,
       p.id,
       1,
       p.unit_price,
       '2026-11-01 11:40:00'
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN part p ON p.name = '14인치 노트북 LCD 패널 연결용 30핀 eDP 디스플레이 플렉스 케이블'
         JOIN part_compatibility pc ON pc.part_id = p.id AND pc.product_model_id = pr.product_model_id
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000010'
  AND rw.work_name = '14인치 FHD LCD 패널 및 eDP 케이블 교체'
  AND r.status = 'COMPLETED'
  AND p.status = 'ACTIVE'
  AND p.stock_quantity >= 1
  AND NOT EXISTS (SELECT 1
                  FROM repair_part_usage u
                  WHERE u.repair_work_id = rw.id
                    AND u.part_id = p.id);

-- 결제 대기, 배송 대기, 수령 대기는 수리가 끝난 접수다.
-- 견적서와 수리내역서를 조회할 수 있도록 확정된 완료 수리를 연결한다.
INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       '냉각팬 베어링 소음으로 고회전 시 진동이 발생함',
       'CPU 쿨링팬을 교체하고 유휴, 부하 상태에서 소음이 사라졌는지 확인함',
       'REPAIRABLE',
       'PAID',
       '보증 기간 내이나 이물 유입에 따른 팬 손상으로 유상 수리',
       50000,
       0,
       79000,
       'COMPLETED',
       NULL,
       '2026-11-02 14:00:00',
       '2026-11-02 11:00:00',
       '2026-11-02 15:00:00',
       ar.technician_id,
       ar.technician_id
FROM as_reception ar
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000007'
  AND ar.status = 'AWAITING_PAYMENT'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

INSERT INTO repair_work (repair_id, repair_preset_name, work_name, created_at, updated_at, created_by, updated_by)
SELECT r.id, '소음 발생', 'CPU 쿨링팬 교체', '2026-11-02 12:00:00', '2026-11-02 14:00:00', r.technician_id, r.technician_id
FROM repair r
         JOIN as_reception ar ON ar.id = r.as_reception_id
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000007'
  AND NOT EXISTS (SELECT 1 FROM repair_work rw WHERE rw.repair_id = r.id AND rw.work_name = 'CPU 쿨링팬 교체');

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id, p.id, 1, p.unit_price, '2026-11-02 12:30:00'
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN part p ON p.name = '노트북 CPU 쿨링팬 (4핀 PWM, 좌측 배기, 방진 고무 마운트 포함)'
         JOIN part_compatibility pc ON pc.part_id = p.id AND pc.product_model_id = pr.product_model_id
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000007'
  AND rw.work_name = 'CPU 쿨링팬 교체'
  AND p.status = 'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM repair_part_usage u WHERE u.repair_work_id = rw.id AND u.part_id = p.id);

INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       '무선랜 카드 인식 불가로 와이파이가 연결되지 않음',
       'Wi-Fi 6 무선랜 카드를 교체하고 2.4GHz, 5GHz 연결을 확인함',
       'REPAIRABLE',
       'PAID',
       '보증 기간 내이나 단자 파손으로 유상 수리',
       50000,
       0,
       75000,
       'COMPLETED',
       NULL,
       '2026-11-02 14:10:00',
       '2026-11-02 11:10:00',
       '2026-11-02 15:00:00',
       ar.technician_id,
       ar.technician_id
FROM as_reception ar
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000008'
  AND ar.status = 'AWAITING_SHIPMENT'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

INSERT INTO repair_work (repair_id, repair_preset_name, work_name, created_at, updated_at, created_by, updated_by)
SELECT r.id, '와이파이 불량', '무선랜 카드 교체', '2026-11-02 12:10:00', '2026-11-02 14:10:00', r.technician_id, r.technician_id
FROM repair r
         JOIN as_reception ar ON ar.id = r.as_reception_id
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000008'
  AND NOT EXISTS (SELECT 1 FROM repair_work rw WHERE rw.repair_id = r.id AND rw.work_name = '무선랜 카드 교체');

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id, p.id, 1, p.unit_price, '2026-11-02 12:40:00'
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN part p ON p.name = 'Intel AX201 Wi-Fi 6 / Bluetooth 5.2 무선랜 카드 (M.2 2230, CNVi)'
         JOIN part_compatibility pc ON pc.part_id = p.id AND pc.product_model_id = pr.product_model_id
WHERE ar.reception_number = 'AS-261102-8f3a2c10-7b6e-4d90-a123-000000000008'
  AND rw.work_name = '무선랜 카드 교체'
  AND p.status = 'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM repair_part_usage u WHERE u.repair_work_id = rw.id AND u.part_id = p.id);

INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       '터치패드 좌클릭이 간헐적으로 인식되지 않음',
       '클릭패드 모듈을 교체하고 클릭, 제스처 동작을 확인함',
       'REPAIRABLE',
       'PAID',
       '보증 기간 내이나 액체 유입 흔적으로 유상 수리',
       50000,
       0,
       92000,
       'COMPLETED',
       NULL,
       '2026-11-01 14:20:00',
       '2026-11-01 11:20:00',
       '2026-11-01 15:00:00',
       ar.technician_id,
       ar.technician_id
FROM as_reception ar
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000009'
  AND ar.status = 'AWAITING_PICKUP'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

INSERT INTO repair_work (repair_id, repair_preset_name, work_name, created_at, updated_at, created_by, updated_by)
SELECT r.id, '터치패드 불량', '클릭패드 모듈 교체', '2026-11-01 12:20:00', '2026-11-01 14:20:00', r.technician_id, r.technician_id
FROM repair r
         JOIN as_reception ar ON ar.id = r.as_reception_id
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000009'
  AND NOT EXISTS (SELECT 1 FROM repair_work rw WHERE rw.repair_id = r.id AND rw.work_name = '클릭패드 모듈 교체');

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id, p.id, 1, p.unit_price, '2026-11-01 12:40:00'
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN part p ON p.name = '노트북 클릭패드 터치패드 모듈 (좌우 물리 버튼 일체형, 멀티터치, 팜레스트 케이블 포함)'
         JOIN part_compatibility pc ON pc.part_id = p.id AND pc.product_model_id = pr.product_model_id
WHERE ar.reception_number = 'AS-261101-8f3a2c10-7b6e-4d90-a123-000000000009'
  AND rw.work_name = '클릭패드 모듈 교체'
  AND p.status = 'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM repair_part_usage u WHERE u.repair_work_id = rw.id AND u.part_id = p.id);

-- 시드 접수마다 고객 안내 링크를 발급한다. 화면에는 /portal/{token} 으로 보여 준다.
INSERT INTO customer_portal_link (as_reception_id, token, expires_at)
SELECT ar.id,
       CONCAT('fixgo-demo-portal-', LPAD(RIGHT(ar.reception_number, 2), 2, '0')),
       '2026-12-09 09:00:00'
FROM as_reception ar
WHERE ar.reception_number LIKE 'AS-2611%-8f3a2c10-7b6e-4d90-a123-0000000000%'
  AND NOT EXISTS (SELECT 1 FROM customer_portal_link link WHERE link.as_reception_id = ar.id);

-- 통계 화면용 더미데이터
-- 기존 수리기사 더미를 포함해 10명, 2025-12부터 2026-11까지 월 5건.
-- 완료 건은 수리 시작/완료 이력, 결제, 배송을 함께 넣어 기간 통계가 비지 않게 한다.

INSERT INTO product_model (name, model_name, category, warranty_months, is_active, created_at, updated_at, created_by,
                           updated_by)
SELECT '더미 스마트폰',
       'FIXGO-DEMO-PHONE',
       'PHONE',
       12,
       TRUE,
       NOW(),
       NOW(),
       0,
       0
WHERE NOT EXISTS (SELECT 1 FROM product_model WHERE model_name = 'FIXGO-DEMO-PHONE');

-- 통계 더미 스마트폰 서류에 표시할 부품. 노트북 부품과 같이 모델 호환을 남긴다.
INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '스마트폰 USB-C 충전 포트 기판', 18000, 30, 40, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '스마트폰 USB-C 충전 포트 기판');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '스마트폰 리튬이온 배터리 팩', 35000, 25, 40, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '스마트폰 리튬이온 배터리 팩');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '스마트폰 OLED 디스플레이 모듈', 120000, 40, 20, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '스마트폰 OLED 디스플레이 모듈');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '스마트폰 전원 버튼 플렉스 케이블', 9000, 20, 40, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '스마트폰 전원 버튼 플렉스 케이블');

INSERT INTO part (name, unit_price, standard_minutes, stock_quantity, status, version, created_at, updated_at,
                  created_by, updated_by)
SELECT '스마트폰 방열 구리 플레이트', 7000, 15, 40, 'ACTIVE', 0, NOW(), NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM part WHERE name = '스마트폰 방열 구리 플레이트');

INSERT INTO part_compatibility (part_id, product_model_id)
SELECT p.id,
       (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-PHONE')
FROM part p
WHERE p.name IN (
    '스마트폰 USB-C 충전 포트 기판',
    '스마트폰 리튬이온 배터리 팩',
    '스마트폰 OLED 디스플레이 모듈',
    '스마트폰 전원 버튼 플렉스 케이블',
    '스마트폰 방열 구리 플레이트'
)
  AND NOT EXISTS (SELECT 1
                  FROM part_compatibility pc
                  WHERE pc.part_id = p.id
                    AND pc.product_model_id = (SELECT MIN(id) FROM product_model WHERE model_name = 'FIXGO-DEMO-PHONE'));

INSERT INTO member (email, password_hash, name, phone, role, address, profile_img_url, status, created_at, updated_at,
                    created_by, updated_by)
WITH RECURSIVE nums AS (
    SELECT 2 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10
)
SELECT CONCAT('stat.tech', LPAD(n, 2, '0'), '@fixgo.example'),
       '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
       ELT(n - 1, '김도윤', '이서연', '박민재', '최하준', '정예린', '한지호', '오수아', '윤태민', '장하늘'),
       CONCAT('010-8100-', LPAD(n, 4, '0')),
       'ROLE_TECHNICIAN',
       '서울시 테스트센터',
       '',
       'ACTIVE',
       NOW(),
       NOW(),
       0,
       0
FROM nums
WHERE NOT EXISTS (SELECT 1
                  FROM member m
                  WHERE m.email = CONCAT('stat.tech', LPAD(nums.n, 2, '0'), '@fixgo.example'));

DROP TEMPORARY TABLE IF EXISTS stat_seed;
CREATE TEMPORARY TABLE stat_seed AS
WITH RECURSIVE nums AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 59
)
SELECT n,
       CASE
           WHEN n DIV 5 = 11 AND n MOD 5 = 4 THEN TIMESTAMP('2026-11-08 10:00:00')
           ELSE DATE_ADD(DATE_ADD(TIMESTAMP('2025-12-03 10:00:00'), INTERVAL (n DIV 5) MONTH), INTERVAL (n MOD 5) DAY)
       END AS created_at,
       MOD((n DIV 5) + (n MOD 5) * 2, 10) AS tech_index,
       CASE
           WHEN MOD((n DIV 5) + (n MOD 5) * 2, 10) = 0 THEN 'technician.demo@fixgo.example'
           ELSE CONCAT('stat.tech', LPAD(MOD((n DIV 5) + (n MOD 5) * 2, 10) + 1, 2, '0'), '@fixgo.example')
       END COLLATE utf8mb4_unicode_ci AS tech_email,
       CASE WHEN n MOD 2 = 0 THEN 'FIXGO-DEMO-LAPTOP' ELSE 'FIXGO-DEMO-PHONE' END COLLATE utf8mb4_unicode_ci AS model_name,
       ELT(n MOD 5 + 1, '전원 불량', '배터리 소모', '화면 파손', '키보드 불량', '과열') AS symptom,
       CASE n MOD 5
           WHEN 0 THEN 'COMPLETED'
           WHEN 1 THEN 'COMPLETED'
           WHEN 2 THEN 'REPAIR_IN_PROGRESS'
           WHEN 3 THEN 'AWAITING_PAYMENT'
           ELSE 'INSPECTION'
       END AS status,
       CASE
           WHEN n MOD 5 IN (0, 1) AND n MOD 4 = 3 THEN 'PICKUP'
           ELSE 'DELIVERY'
       END AS return_method
FROM nums;

ALTER TABLE stat_seed
    ADD COLUMN complete_at DATETIME,
    ADD COLUMN reception_number VARCHAR(40) COLLATE utf8mb4_unicode_ci;

UPDATE stat_seed
SET complete_at = DATE_ADD(created_at, INTERVAL (8 + tech_index * 3) HOUR),
    reception_number = CONCAT('AS-STAT-', DATE_FORMAT(created_at, '%y%m%d'), '-', LPAD(n, 2, '0'));

INSERT INTO customer (name, phone, email, zipcode, basic_address, detail_address, created_at, updated_at, created_by,
                      updated_by)
SELECT CONCAT('통계고객 ', LPAD(n + 1, 2, '0')),
       CONCAT('010-8200-', LPAD(n, 4, '0')),
       CONCAT('stat.customer', LPAD(n, 2, '0'), '@fixgo.example'),
       '06200',
       CONCAT('서울시 강남구 통계로 ', n + 1),
       '1층',
       created_at,
       created_at,
       0,
       0
FROM stat_seed
WHERE NOT EXISTS (SELECT 1
                  FROM customer c
                  WHERE c.email = CONCAT('stat.customer', LPAD(stat_seed.n, 2, '0'), '@fixgo.example'));

INSERT INTO product (product_model_id, customer_id, serial_number, manufactured_at, purchased_at, created_at,
                     updated_at, created_by, updated_by)
SELECT (SELECT MIN(id) FROM product_model WHERE model_name = s.model_name COLLATE utf8mb4_unicode_ci),
       (SELECT MIN(id) FROM customer WHERE email = CONCAT('stat.customer', LPAD(s.n, 2, '0'), '@fixgo.example')),
       CONCAT('STAT-SN-', LPAD(s.n, 2, '0')),
       '2025-06-01 00:00:00',
       '2025-09-01 00:00:00',
       s.created_at,
       s.created_at,
       0,
       0
FROM stat_seed s
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.serial_number = CONCAT('STAT-SN-', LPAD(s.n, 2, '0')));

INSERT INTO as_reception (reception_number, customer_id, product_id, receptionist_id, technician_id,
                          symptom, symptom_detail, memo, return_method, status, status_changed_at, closed_at,
                          file_url, created_at, updated_at, created_by, updated_by)
SELECT s.reception_number,
       (SELECT MIN(id) FROM customer WHERE email = CONCAT('stat.customer', LPAD(s.n, 2, '0'), '@fixgo.example')),
       (SELECT id FROM product WHERE serial_number = CONCAT('STAT-SN-', LPAD(s.n, 2, '0'))),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = s.tech_email COLLATE utf8mb4_unicode_ci),
       s.symptom,
       CONCAT(s.symptom, ' 증상으로 점검을 요청합니다.'),
       '통계 더미데이터',
       s.return_method,
       s.status,
       CASE WHEN s.status = 'COMPLETED' THEN s.complete_at ELSE s.created_at END,
       CASE WHEN s.status = 'COMPLETED' THEN s.complete_at ELSE NULL END,
       NULL,
       s.created_at,
       CASE WHEN s.status = 'COMPLETED' THEN s.complete_at ELSE s.created_at END,
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example'),
       (SELECT id FROM member WHERE email = 'reception.demo@fixgo.example')
FROM stat_seed s
WHERE NOT EXISTS (SELECT 1 FROM as_reception ar
                  WHERE ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci);

INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       CONCAT(s.symptom, ' 원인을 확인하고 부품을 교체함'),
       CONCAT(s.symptom, ' 부품을 교체하고 동작을 확인함'),
       'REPAIRABLE',
       CASE WHEN s.n MOD 2 = 0 THEN 'PAID' ELSE 'FREE' END,
       '통계 더미데이터',
       30000,
       0,
       50000 + s.n * 2500,
       'COMPLETED',
       NULL,
       s.complete_at,
       s.created_at,
       s.complete_at,
       ar.technician_id,
       ar.technician_id
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
WHERE s.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

-- 결제 대기도 수리가 끝난 접수다. 견적서와 수리내역서를 조회할 수 있게 확정된 완료 수리를 연결한다.
INSERT INTO repair (as_reception_id, technician_id, diagnosis, work_note, repair_decision, warranty_result, reason,
                    labor_rate, inspection_cost, final_amount, status, reason_for_rejection, confirmed_at,
                    created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       ar.technician_id,
       CONCAT(ar.symptom, ' 원인을 확인하고 부품을 교체함'),
       CONCAT(ar.symptom, ' 부품을 교체하고 동작을 확인함'),
       'REPAIRABLE',
       CASE WHEN CAST(RIGHT(ar.reception_number, 2) AS UNSIGNED) MOD 2 = 0 THEN 'PAID' ELSE 'FREE' END,
       '통계 더미데이터',
       30000,
       0,
       50000 + CAST(RIGHT(ar.reception_number, 2) AS UNSIGNED) * 2500,
       'COMPLETED',
       NULL,
       DATE_ADD(ar.created_at, INTERVAL 8 HOUR),
       ar.created_at,
       DATE_ADD(ar.created_at, INTERVAL 8 HOUR),
       ar.technician_id,
       ar.technician_id
FROM as_reception ar
WHERE ar.reception_number LIKE 'AS-STAT-%'
  AND ar.status = 'AWAITING_PAYMENT'
  AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id);

-- 견적서와 수리내역서에 작업, 부품, 공임이 나오도록 완료·결제 대기 건에 넣는다.
INSERT INTO repair_work (repair_id, repair_preset_name, work_name, created_at, updated_at, created_by, updated_by)
SELECT r.id,
       ar.symptom,
       CONCAT(ar.symptom, ' 부품 교체'),
       r.created_at,
       r.confirmed_at,
       r.technician_id,
       r.technician_id
FROM repair r
         JOIN as_reception ar ON ar.id = r.as_reception_id
WHERE ar.reception_number LIKE 'AS-STAT-%'
  AND ar.status IN ('COMPLETED', 'AWAITING_PAYMENT')
  AND NOT EXISTS (SELECT 1 FROM repair_work rw WHERE rw.repair_id = r.id);

INSERT INTO repair_part_usage (repair_work_id, part_id, quantity, unit_price, created_at)
SELECT rw.id,
       p.id,
       1,
       r.final_amount - r.labor_rate,
       rw.created_at
FROM repair_work rw
         JOIN repair r ON r.id = rw.repair_id
         JOIN as_reception ar ON ar.id = r.as_reception_id
         JOIN product pr ON pr.id = ar.product_id
         JOIN product_model pm ON pm.id = pr.product_model_id
         JOIN part p ON p.name = CASE
             WHEN pm.category = 'PHONE' AND ar.symptom = '전원 불량' THEN '스마트폰 USB-C 충전 포트 기판'
             WHEN pm.category = 'PHONE' AND ar.symptom = '배터리 소모' THEN '스마트폰 리튬이온 배터리 팩'
             WHEN pm.category = 'PHONE' AND ar.symptom = '화면 파손' THEN '스마트폰 OLED 디스플레이 모듈'
             WHEN pm.category = 'PHONE' AND ar.symptom = '키보드 불량' THEN '스마트폰 전원 버튼 플렉스 케이블'
             WHEN pm.category = 'PHONE' THEN '스마트폰 방열 구리 플레이트'
             WHEN ar.symptom = '전원 불량' THEN '65W USB-C PD 노트북 전원 어댑터 (20V 3.25A, 1.8m 케이블, 한국형 플러그)'
             WHEN ar.symptom = '배터리 소모' THEN '노트북 내장 리튬이온 배터리 팩 (11.55V, 50Wh, 3셀, 4핀 커넥터)'
             WHEN ar.symptom = '화면 파손' THEN '14인치 FHD IPS 노트북 LCD 패널 모듈 (1920x1080, 30핀 eDP, 무광, 슬림 베젤)'
             WHEN ar.symptom = '키보드 불량' THEN '노트북 팜레스트 일체형 한국어 키보드 모듈 (펜타그래프, 화이트 백라이트, 스크류 고정형)'
             ELSE '노트북 CPU 쿨링팬 (4핀 PWM, 좌측 배기, 방진 고무 마운트 포함)'
         END
            AND p.status = 'ACTIVE'
WHERE ar.reception_number LIKE 'AS-STAT-%'
  AND ar.status IN ('COMPLETED', 'AWAITING_PAYMENT')
  AND NOT EXISTS (SELECT 1 FROM repair_part_usage u WHERE u.repair_work_id = rw.id AND u.part_id = p.id);

INSERT INTO as_status_history (as_reception_id, from_status, to_status, reason, changed_by, created_at)
SELECT ar.id,
       'APPROVED',
       'REPAIRING',
       '수리 시작',
       ar.receptionist_id,
       DATE_ADD(s.created_at, INTERVAL 2 HOUR)
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
WHERE s.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1
                  FROM as_status_history h
                  WHERE h.as_reception_id = ar.id
                    AND h.to_status = 'REPAIRING');

INSERT INTO as_status_history (as_reception_id, from_status, to_status, reason, changed_by, created_at)
SELECT ar.id,
       'REPAIRING',
       'COMPLETED',
       '수리 완료',
       ar.technician_id,
       s.complete_at
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
WHERE s.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1
                  FROM as_status_history h
                  WHERE h.as_reception_id = ar.id
                    AND h.from_status = 'REPAIRING'
                    AND h.to_status = 'COMPLETED');

INSERT INTO payment_bill (as_reception_id, amount, status, type, paid_at, created_at, updated_at, created_by, updated_by)
SELECT ar.id,
       50000 + s.n * 2500,
       CASE WHEN s.status = 'COMPLETED' THEN 'PAID' ELSE 'UNPAID' END,
       'REPAIR',
       CASE WHEN s.status = 'COMPLETED' THEN s.complete_at ELSE NULL END,
       s.created_at,
       CASE WHEN s.status = 'COMPLETED' THEN s.complete_at ELSE s.created_at END,
       ar.receptionist_id,
       ar.receptionist_id
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
WHERE s.status IN ('COMPLETED', 'AWAITING_PAYMENT')
  AND NOT EXISTS (SELECT 1 FROM payment_bill pb WHERE pb.as_reception_id = ar.id);

INSERT INTO payment_attempt (payment_bill_id, order_id, payment_key, amount, method, status, approved_at, created_at,
                             updated_at)
SELECT pb.id,
       CONCAT('STAT-ORDER-', s.reception_number),
       CONCAT('STAT-KEY-', s.reception_number),
       pb.amount,
       'CARD',
       'DONE',
       s.complete_at,
       s.created_at,
       s.complete_at
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
         JOIN payment_bill pb ON pb.as_reception_id = ar.id
WHERE s.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM payment_attempt pa
                  WHERE pa.order_id = CONCAT('STAT-ORDER-', s.reception_number) COLLATE utf8mb4_unicode_ci);

INSERT INTO payment_refund (payment_attempt_id, transaction_key, cancel_amount, cancel_reason, status, canceled_at,
                            created_at, updated_at, created_by, updated_by)
SELECT pa.id,
       CONCAT('STAT-REFUND-', s.reception_number),
       10000,
       '통계 더미 부분 환불',
       'DONE',
       DATE_ADD(s.complete_at, INTERVAL 1 DAY),
       DATE_ADD(s.complete_at, INTERVAL 1 DAY),
       DATE_ADD(s.complete_at, INTERVAL 1 DAY),
       ar.receptionist_id,
       ar.receptionist_id
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
         JOIN payment_attempt pa ON pa.order_id = CONCAT('STAT-ORDER-', s.reception_number) COLLATE utf8mb4_unicode_ci
WHERE s.status = 'COMPLETED'
  AND s.n MOD 5 = 0
  AND (s.n DIV 5) MOD 3 = 0
  AND NOT EXISTS (SELECT 1 FROM payment_refund pr
                  WHERE pr.transaction_key = CONCAT('STAT-REFUND-', s.reception_number) COLLATE utf8mb4_unicode_ci);

INSERT INTO shipment (as_reception_id, return_method, recipient_name, recipient_phone, address, address_detail,
                      courier, courier_code, tracking_number, status, shipped_at, completed_at, created_at, updated_at,
                      created_by, updated_by)
SELECT ar.id,
       s.return_method,
       c.name,
       c.phone,
       c.basic_address,
       c.detail_address,
       CASE WHEN s.return_method = 'DELIVERY' THEN 'CJ대한통운' ELSE NULL END,
       CASE WHEN s.return_method = 'DELIVERY' THEN '04' ELSE NULL END,
       CASE WHEN s.return_method = 'DELIVERY' THEN CONCAT('STAT', LPAD(s.n, 6, '0')) ELSE NULL END,
       CASE s.n MOD 4
           WHEN 0 THEN 'DELIVERED'
           WHEN 1 THEN 'SHIPPING'
           WHEN 2 THEN 'READY'
           ELSE 'PICKED_UP'
       END,
       CASE WHEN s.n MOD 4 IN (0, 1) THEN DATE_ADD(s.created_at, INTERVAL 1 DAY) ELSE NULL END,
       CASE WHEN s.n MOD 4 IN (0, 3) THEN s.complete_at ELSE NULL END,
       DATE_ADD(s.created_at, INTERVAL 1 DAY),
       s.complete_at,
       ar.receptionist_id,
       ar.receptionist_id
FROM stat_seed s
         JOIN as_reception ar ON ar.reception_number = s.reception_number COLLATE utf8mb4_unicode_ci
         JOIN customer c ON c.id = ar.customer_id
WHERE s.status = 'COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM shipment sh WHERE sh.as_reception_id = ar.id);

DROP TEMPORARY TABLE IF EXISTS stat_seed;

-- 통계 더미처럼 접수 INSERT 이후에 생긴 건도 고객 안내 링크를 발급한다.
INSERT INTO customer_portal_link (as_reception_id, token, expires_at)
SELECT ar.id,
       CONCAT('fixgo-portal-', ar.id),
       DATE_ADD(NOW(), INTERVAL 30 DAY)
FROM as_reception ar
WHERE NOT EXISTS (SELECT 1 FROM customer_portal_link link WHERE link.as_reception_id = ar.id);
