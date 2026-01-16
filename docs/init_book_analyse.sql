-- ============================================
-- 测试数据说明
-- ============================================
-- poster_url 格式说明：
--   1. 本地存储: 相对路径格式 "yyyyMMdd/书名.png"，通过 /api/image?path=xxx 访问
--   2. 公共HTTP: 完整URL "https://..." 或 "http://..."，前端直接访问
--   3. 认证HTTP: "auth-http://credential-key@host/path"，后端代理访问（需配置credentials）
--   4. FTP存储: "ftp://credential-key@host:port/path"，后端代理访问（需配置credentials）
-- ============================================

-- 示例1: 本地存储（AI生成的图片）
INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (1, '2024-01-15 10:00:00', 'administrator', NULL, NULL, '科幻', '[\"黑暗森林法则\", \"三体文明\", \"降维打击\"]', '20240115/三体.png', '这是一部硬核科幻巨作，探讨了宇宙文明的终极法则和人类命运。刘慈欣以宏大的视角和深邃的思考，构建了一个震撼人心的宇宙图景。', '[\"宇宙文明\", \"科技哲学\", \"人性探索\"]', '三体', '深邃宏大');

-- 示例2: 公共HTTP图片（直接访问）
INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (2, '2024-01-16 10:00:00', 'administrator', NULL, NULL, '现实主义', '[\"福贵的一生\", \"历史洪流\", \"生存韧性\"]', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800', '余华用朴实的语言讲述了一个人一生的苦难与坚韧。这是一部关于生存、关于活着本身意义的深刻作品。', '[\"生命意义\", \"苦难\", \"时代变迁\"]', '活着', '沉重压抑');

-- 示例3: 本地存储
INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (3, '2024-01-17 10:00:00', 'administrator', NULL, NULL, '治愈系', '[\"神奇信箱\", \"跨时空对话\", \"人生困惑\"]', '20240117/解忧杂货店.png', '东野圭吾的温情之作，通过一个神奇的杂货店，连接起不同时空中迷茫的心灵。每个故事都传递着温暖与希望。', '[\"时空穿越\", \"人生抉择\", \"救赎\"]', '解忧杂货店', '温暖治愈');

-- 示例4: 公共HTTP图片
INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (4, '2024-01-18 10:00:00', 'administrator', NULL, NULL, '历史科普', '[\"智人崛起\", \"农业革命\", \"科学革命\"]', 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800', '尤瓦尔·赫拉利从宏观视角审视人类历史，用通俗的语言解读复杂的演化过程，引发对未来的深刻思考。', '[\"人类演化\", \"认知革命\", \"未来展望\"]', '人类简史', '理性思辨');

-- 示例5: 公共HTTP图片（外部CDN）
INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (5, '2024-01-20 10:00:00', 'administrator', NULL, NULL, '推理爱情', '[\"大正浪漫\", \"叙述性诡计\", \"戏中戏\"]', 'https://youke3.picui.cn/s1/2026/01/09/69611c6608907.png', '日本抒情推理大师连城三纪彦的代表作之一。故事以大正时代为背景，将男女之间细腻凄婉的情感与精妙的诡计完美融合。文字极具画面感与古典美，在如梦似幻的氛围中，揭开最后令人战栗又悲凉的真相。', '[\"爱恨纠葛\", \"人性幽微\", \"悲剧美学\"]', '宵待草夜情', '唯美哀婉');

-- ============================================
-- 以下是需要配置凭证的示例（注释状态）
-- ============================================

-- 示例6: 认证HTTP（需要在application.yaml配置oss-key-1凭证）
-- INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
-- VALUES (6, '2024-01-21 10:00:00', 'administrator', NULL, NULL, '奇幻', '[\"魔法世界\", \"友谊\", \"成长\"]', 'auth-http://oss-key-1@my-bucket.oss-cn-hangzhou.aliyuncs.com/books/哈利波特.png', '一部经典的奇幻成长小说，讲述了一个平凡男孩在魔法世界的冒险。', '[\"勇气\", \"友谊\", \"善恶\"]', '哈利·波特与魔法石', '奇幻冒险');

-- 示例7: FTP存储（需要在application.yaml配置ftp-key-1凭证）
-- INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
-- VALUES (7, '2024-01-22 10:00:00', 'administrator', NULL, NULL, '武侠', '[\"江湖恩怨\", \"侠义精神\", \"爱恨情仇\"]', 'ftp://ftp-key-1@192.168.1.100:21/books/天龙八部.png', '金庸武侠小说的巅峰之作，气势磅礴，人物众多，情节跌宕起伏。', '[\"侠义\", \"情仇\", \"宿命\"]', '天龙八部', '荡气回肠');

-- ============================================
-- 凭证配置示例（需在 application.yaml 中添加）
-- ============================================
-- app:
--   image:
--     credentials:
--       oss-key-1:
--         type: oss
--         endpoint: https://oss-cn-hangzhou.aliyuncs.com
--         access-key-id: ${OSS_ACCESS_KEY_ID}
--         access-key-secret: ${OSS_ACCESS_KEY_SECRET}
--         bucket: my-bucket
--       ftp-key-1:
--         type: ftp
--         host: 192.168.1.100
--         port: 21
--         username: ${FTP_USERNAME}
--         password: ${FTP_PASSWORD}
