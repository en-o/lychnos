-- ============================================
-- 测试数据
-- ============================================

INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (1, '2024-01-15 10:00:00', 'administrator', NULL, NULL, '科幻', '[\"黑暗森林法则\", \"三体文明\", \"降维打击\"]', 'h:0:https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800', '这是一部硬核科幻巨作，探讨了宇宙文明的终极法则和人类命运。刘慈欣以宏大的视角和深邃的思考，构建了一个震撼人心的宇宙图景。', '[\"宇宙文明\", \"科技哲学\", \"人性探索\"]', '三体', '深邃宏大');

INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (2, '2024-01-16 10:00:00', 'administrator', NULL, NULL, '现实主义', '[\"福贵的一生\", \"历史洪流\", \"生存韧性\"]', 'h:0:https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800', '余华用朴实的语言讲述了一个人一生的苦难与坚韧。这是一部关于生存、关于活着本身意义的深刻作品。', '[\"生命意义\", \"苦难\", \"时代变迁\"]', '活着', '沉重压抑');

INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (3, '2024-01-17 10:00:00', 'administrator', NULL, NULL, '治愈系', '[\"神奇信箱\", \"跨时空对话\", \"人生困惑\"]', 'h:0:https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', '东野圭吾的温情之作，通过一个神奇的杂货店，连接起不同时空中迷茫的心灵。每个故事都传递着温暖与希望。', '[\"时空穿越\", \"人生抉择\", \"救赎\"]', '解忧杂货店', '温暖治愈');

INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (4, '2024-01-18 10:00:00', 'administrator', NULL, NULL, '历史科普', '[\"智人崛起\", \"农业革命\", \"科学革命\"]', 'h:0:https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800', '尤瓦尔·赫拉利从宏观视角审视人类历史，用通俗的语言解读复杂的演化过程，引发对未来的深刻思考。', '[\"人类演化\", \"认知革命\", \"未来展望\"]', '人类简史', '理性思辨');

INSERT INTO `tb_book_analyse` (`id`, `create_time`, `create_user_name`, `update_time`, `update_user_name`, `genre`, `key_elements`, `poster_url`, `recommendation`, `themes`, `title`, `tone`)
VALUES (5, '2024-01-20 10:00:00', 'administrator', NULL, NULL, '推理爱情', '[\"大正浪漫\", \"叙述性诡计\", \"戏中戏\"]', 'h:0:https://youke3.picui.cn/s1/2026/01/09/69611c6608907.png', '日本抒情推理大师连城三纪彦的代表作之一。故事以大正时代为背景，将男女之间细腻凄婉的情感与精妙的诡计完美融合。文字极具画面感与古典美，在如梦似幻的氛围中，揭开最后令人战栗又悲凉的真相。', '[\"爱恨纠葛\", \"人性幽微\", \"悲剧美学\"]', '宵待草夜情', '唯美哀婉');

-- ============================================
-- poster_url 格式说明
-- ============================================
-- 统一格式: 协议:鉴权:路径
--
-- 协议代码:
--   h    - HTTP/HTTPS
--   ali  - 阿里云 OSS
--   qiniu - 七牛云
--   s3   - AWS S3
--   f    - FTP
--   l    - 本地存储
--
-- 鉴权标识:
--   0 - 无需鉴权（公开访问）
--   1 - 需要鉴权（需配置凭证）
--
-- 示例:
--   h:0:https://images.unsplash.com/photo-xxx.jpg           # 无鉴权HTTP
--   h:1:https://private-cdn.com/books/xxx.png               # 有鉴权HTTP
--   ali:1:/my-bucket/books/三体.png                          # 阿里云OSS（需鉴权）
--   qiniu:0:https://cdn.qiniu.com/books/xxx.png             # 七牛云公开URL
--   s3:1:/my-bucket/books/xxx.png                           # AWS S3（需鉴权）
--   f:0:ftp://192.168.1.100/public/books/xxx.png            # FTP无鉴权
--   f:1:ftp://192.168.1.100/private/books/xxx.png           # FTP需鉴权
--   l:0:/public/20240115/三体.png                            # 本地公开目录
--   l:1:/20240115/三体.png                                   # 本地私有目录（AI生成）
