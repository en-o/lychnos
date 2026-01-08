package cn.tannn.lychnos.common.query;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;


/**
 * 公共字段的 Spec
 * <pre>
     Specification<Menu> spec = SpecQuery.<Menu>of()
     .eqName(name)
     .eqParentCode(parentCode)
     .neCode(selfCode)
     .eqPlatformType(platform)
     .build()
 * </pre>
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2025/7/4 09:51
 */
public class SpecQuery<T> {

    private Specification<T> specification = (root, query, cb) -> query.getRestriction();


    public static <T> SpecQuery<T> of() {
        return new SpecQuery<>();
    }
    public Specification<T> build() {
        return this.specification;
    }


    /**
     * or 方法
     <pre>
     .or(
         SpecQuery.<Menu>of().eqName("菜单1"),
         SpecQuery.<Menu>of().eqName("菜单2")
         )
     </pre>
     * @param queries SpecQuery
     * @return  SpecQuery
     */
    @SafeVarargs
    public final SpecQuery<T> or(SpecQuery<T>... queries) {
        if (!ObjectUtils.isEmpty(queries)) {
            for (SpecQuery<T> query : queries) {
                specification = specification.or(query.build());
            }
        }
        return this;
    }



    /**
     * 条件  status ==
     * @param status 状态[0、不可用，1、可用]
     */
    public SpecQuery<T> eqStatus(Integer status) {
        if (!ObjectUtils.isEmpty(status)) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }
        return this;
    }


    /**
     * 条件  name  like
     */
    public  SpecQuery<T>  likeName(String name) {
        if (!ObjectUtils.isEmpty(name)) {
            specification = specification.and((root, query, builder) ->
                    builder.like(root.get("name"), "%" + name + "%"));
        }
        return this;
    }

    /**
     * 条件  name  like
     */
    public SpecQuery<T> eqName(String name) {
        if (!ObjectUtils.isEmpty(name)) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("name"), name));
        }
        return this;
    }

    /**
     * 条件  parentCode  =
     */
    public SpecQuery<T> eqParentCode(String parentCode) {
        if (!ObjectUtils.isEmpty(parentCode)) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("parentCode"), parentCode));
        }
        return this;
    }

    /**
     * 条件  code  !=
     */
    public SpecQuery<T> neCode(String code) {
        if (!ObjectUtils.isEmpty(code)) {
            specification = specification.and((root, query, builder) ->
                    builder.notEqual(root.get("code"), code));
        }
        return this;
    }


}
