-- 用户分析日志表
CREATE TABLE IF NOT EXISTS tb_user_analysis_log (
    id BIGINT PRIMARY KEY COMMENT 'uuid',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    call_ip VARCHAR(50) COMMENT '调用IP',

    -- 模型相关（使用别人数据时为空）
    model_id BIGINT COMMENT '模型ID',
    model_name VARCHAR(200) COMMENT '模型名称',
    model_vendor VARCHAR(100) COMMENT '模型厂商',
    model_type VARCHAR(20) COMMENT '模型类型：TEXT/IMAGE',
    model_source INT COMMENT '模型来源：0-官方/1-私人/2-公开',

    -- 业务相关
    usage_type VARCHAR(20) NOT NULL COMMENT '用途：BOOK_PARSE/BOOK_IMAGE',
    book_title VARCHAR(500) COMMENT '书籍标题',
    book_analyse_id BIGINT COMMENT '书籍分析ID',

    -- 执行结果
    success BOOLEAN DEFAULT TRUE COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',

    -- 是否直接使用已有数据
    use_existing_data BOOLEAN DEFAULT FALSE COMMENT '是否直接使用已有数据',

    -- 时间字段
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user VARCHAR(100) COMMENT '创建人',
    update_user VARCHAR(100) COMMENT '更新人',

    INDEX idx_user_id (user_id),
    INDEX idx_usage_type (usage_type),
    INDEX idx_create_time (create_time),
    INDEX idx_book_analyse_id (book_analyse_id)
) COMMENT='用户分析日志';
