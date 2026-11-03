-- 관리자 계정 초기 데이터
-- email: admin001@fixgo.com / password: admin1234 (BCrypt)
INSERT IGNORE INTO member (code,
                           email,
                           password_hash,
                           name,
                           phone,
                           role,
                           profile_img_url,
                           status,
                           deleted_at,
                           created_at,
                           updated_at,
                           created_by,
                           updated_by)
VALUES ('admin001',
        'admin001@fixgo.com',
        '$2a$10$vFJ1GvhwL8vQFOSGsKTjMeH7cQUItVadnGDGRHF1Q8xGefzmSlzMS',
        '관리자',
        '010-XXXX-XXXX',
        'ROLE_ADMIN',
        '',
        'ACTIVE',
        NULL,
        NOW(),
        NOW(),
        'system',
        'system');


# 이메일 관련 더미데이터
INSERT IGNORE INTO notification_log (event_key,
                                     notification_type,
                                     recipient_email,
                                     message_title,
                                     message,
                                     status,
                                     retry_count,
                                     error_message,
                                     sent_at,
                                     created_at,
                                     as_reception_id,
                                     created_by)
VALUES
-- 오늘: 발송 성공
('test-email-001', 'RECEIVED', 'kim@gmail.com',
 '[FixGo] A/S 접수 완료 안내', 'A/S 접수가 정상적으로 완료되었습니다.',
 'SENT', 0, NULL, NOW(), NOW(), NULL, NULL),

('test-email-002', 'ESTIMATE', 'lee@naver.com',
 '[FixGo] 견적 안내', 'A/S 수리 견적이 등록되었습니다.',
 'SENT', 0, NULL, NOW(), NOW(), NULL, NULL),

('test-email-003', 'REPAIR', 'park@gmail.com',
 '[FixGo] 수리 완료 안내', '고객님의 제품 수리가 완료되었습니다.',
 'SENT', 0, NULL, NOW(), NOW(), NULL, NULL),

('test-email-004', 'PAYMENT', 'choi@daum.net',
 '[FixGo] 결제 안내', 'A/S 수리 비용 결제를 안내드립니다.',
 'SENT', 0, NULL, NOW(), NOW(), NULL, NULL),

('test-email-005', 'SHIPPING', 'jung@gmail.com',
 '[FixGo] 배송 시작 안내', '고객님의 제품이 택배로 발송되었습니다.',
 'SENT', 0, NULL, NOW(), NOW(), NULL, NULL),

-- 오늘: 발송 실패
('test-email-006', 'RECEIVED', 'wrong@gmail.com',
 '[FixGo] A/S 접수 완료 안내', 'A/S 접수가 정상적으로 완료되었습니다.',
 'FAILED', 0, 'MailSendException', NULL, NOW(), NULL, NULL),

('test-email-007', 'ESTIMATE', 'error@naver.com',
 '[FixGo] 견적 안내', 'A/S 수리 견적이 등록되었습니다.',
 'FAILED', 1, 'MailAuthenticationException', NULL, NOW(), NULL, NULL),

('test-email-008', 'REPAIR', 'fail@gmail.com',
 '[FixGo] 수리 완료 안내', '고객님의 제품 수리가 완료되었습니다.',
 'FAILED', 2, 'MailSendException', NULL, NOW(), NULL, NULL),

-- 오늘: 발송 대기 및 처리 중
('test-email-009', 'PAYMENT', 'pending@gmail.com',
 '[FixGo] 결제 안내', 'A/S 결제를 요청드립니다.',
 'PENDING', 0, NULL, NULL, NOW(), NULL, NULL),

('test-email-010', 'SHIPPING', 'sending@naver.com',
 '[FixGo] 배송 시작 안내', '고객님의 제품 배송이 시작되었습니다.',
 'SENDING', 0, NULL, NULL, NOW(), NULL, NULL),

-- 어제: 발송 성공
('test-email-011', 'RECEIVED', 'hong@gmail.com',
 '[FixGo] A/S 접수 완료 안내', 'A/S 접수가 완료되었습니다.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),

('test-email-012', 'ESTIMATE', 'yoon@naver.com',
 '[FixGo] 견적 안내', '수리 견적을 확인해 주세요.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),

('test-email-013', 'REPAIR', 'han@gmail.com',
 '[FixGo] 수리 완료 안내', '제품 수리가 완료되었습니다.',
 'SENT', 1, NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),

-- 어제: 발송 실패
('test-email-014', 'PAYMENT', 'failed@daum.net',
 '[FixGo] 결제 안내', '결제 요청 이메일입니다.',
 'FAILED', 1, 'MailSendException', NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),

('test-email-015', 'SHIPPING', 'delivery@gmail.com',
 '[FixGo] 배송 시작 안내', '배송이 시작되었습니다.',
 'FAILED', 0, 'MailSendException', NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),

-- 3일 전
('test-email-016', 'RECEIVED', 'customer1@gmail.com',
 '[FixGo] 접수 안내', 'A/S 접수가 완료되었습니다.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL),

('test-email-017', 'REPAIR', 'customer2@naver.com',
 '[FixGo] 수리 안내', '수리가 완료되었습니다.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL),

-- 지난달
('test-email-018', 'PAYMENT', 'customer3@gmail.com',
 '[FixGo] 결제 안내', '결제를 완료해 주세요.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 35 DAY),
 DATE_SUB(NOW(), INTERVAL 35 DAY), NULL, NULL),

('test-email-019', 'SHIPPING', 'customer4@naver.com',
 '[FixGo] 배송 안내', '제품이 발송되었습니다.',
 'SENT', 0, NULL,
 DATE_SUB(NOW(), INTERVAL 35 DAY),
 DATE_SUB(NOW(), INTERVAL 35 DAY), NULL, NULL),

('test-email-020', 'RECEIVED', 'lynaeryn@gmail.com',
 '[FixGo] A/S 접수 안내', '고객님의 A/S 접수가 접수되었습니다.',
 'FAILED', 0, 'MailSendException', NULL,
 DATE_SUB(NOW(), INTERVAL 35 DAY), NULL, NULL);