package com.qj.back.mapper;

import com.qj.back.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OperationLogMapper {
    @Insert("INSERT INTO operation_log(account, operation, module, method, params, ip, create_time) " +
            "VALUES(#{account}, #{operation}, #{module}, #{method}, #{params}, #{ip}, #{createTime})")
    int insert(OperationLog log);

    @Select("SELECT * FROM operation_log ORDER BY create_time DESC")
    List<OperationLog> selectList();
}
