package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.AccountType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountTypeMapper extends BaseMapper<AccountType> {
    
    @Select("SELECT * FROM account_type WHERE status = 1 ORDER BY group_name, display_order")
    List<AccountType> selectAllEnabled();
    
    @Select("SELECT * FROM account_type WHERE group_name = #{groupName} AND status = 1 ORDER BY display_order")
    List<AccountType> selectByGroup(String groupName);
}
