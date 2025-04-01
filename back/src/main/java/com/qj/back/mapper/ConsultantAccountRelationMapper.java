package com.qj.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qj.back.entity.ConsultantAccountRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConsultantAccountRelationMapper extends BaseMapper<ConsultantAccountRelation> {
    
    @Select("SELECT * FROM consultant_account_relation WHERE account_type_id = #{accountTypeId} ORDER BY count, order_status")
    List<ConsultantAccountRelation> selectByAccountTypeOrdered(Integer accountTypeId);
    
    @Select("SELECT * FROM consultant_account_relation WHERE consultant_id = #{consultantId}")
    List<ConsultantAccountRelation> selectByConsultantId(Integer consultantId);
    
    @Select("SELECT * FROM consultant_account_relation WHERE consultant_id = #{consultantId} AND account_type_id = #{accountTypeId}")
    ConsultantAccountRelation selectByConsultantAndAccountType(Integer consultantId, Integer accountTypeId);
}
