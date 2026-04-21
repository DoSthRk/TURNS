package com.qj.back.mapper;

import com.qj.back.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface OperationLogMapper {
    @Insert("INSERT INTO operation_log(account, operation, module, method, params, ip, create_time) " +
            "VALUES(#{account}, #{operation}, #{module}, #{method}, #{params}, #{ip}, #{createTime})")
    int insert(OperationLog log);

    @Select({
            "<script>",
            "SELECT * FROM operation_log",
            "WHERE 1 = 1",
            "<if test='account != null and account != \"\"'>",
            "  AND account LIKE CONCAT('%', #{account}, '%')",
            "</if>",
            "<if test='module != null and module != \"\"'>",
            "  AND module = #{module}",
            "</if>",
            "<if test='startTime != null'>",
            "  AND create_time <![CDATA[>=]]> #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "  AND create_time <![CDATA[<=]]> #{endTime}",
            "</if>",
            "ORDER BY create_time DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<OperationLog> selectPage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("account") String account,
                                  @Param("module") String module,
                                  @Param("startTime") Date startTime,
                                  @Param("endTime") Date endTime);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM operation_log",
            "WHERE 1 = 1",
            "<if test='account != null and account != \"\"'>",
            "  AND account LIKE CONCAT('%', #{account}, '%')",
            "</if>",
            "<if test='module != null and module != \"\"'>",
            "  AND module = #{module}",
            "</if>",
            "<if test='startTime != null'>",
            "  AND create_time <![CDATA[>=]]> #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "  AND create_time <![CDATA[<=]]> #{endTime}",
            "</if>",
            "</script>"
    })
    long count(@Param("account") String account,
               @Param("module") String module,
               @Param("startTime") Date startTime,
               @Param("endTime") Date endTime);
}
