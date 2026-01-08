package cn.tannn.lychnos.common.pojo;

import cn.tannn.jdevelops.jpa.generator.UuidCustomGenerator;
import cn.tannn.jdevelops.jpa.modle.json.JpaAuditTimeFormatFields;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;


/**
 * 公共的实体类- 处理时间的 建议用这个
 * @author tn
 * @date 2021-01-21 14:20
 */
@MappedSuperclass
@DynamicInsert
@DynamicUpdate
@SelectBeforeUpdate
@Access(AccessType.FIELD)
@Getter
@Setter
public class JpaCommonBean<B> extends JpaAuditTimeFormatFields<B> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuidCustomGenerator")
    @GenericGenerator(name = "uuidCustomGenerator", type = UuidCustomGenerator.class)
    @Column(columnDefinition="bigint")
    @Comment("uuid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Override
    public String toString() {
        return "CommonBean{" +
               "id=" + id +
               '}';
    }
}

