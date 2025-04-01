package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qj.back.entity.AccountType;
import com.qj.back.entity.ConsultantAccountRelation;
import com.qj.back.entity.Consultants;
import com.qj.back.mapper.AccountTypeMapper;
import com.qj.back.mapper.ConsultantAccountRelationMapper;
import com.qj.back.mapper.ConsultantsMapper;
import com.qj.back.service.AccountTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountTypeServiceImpl implements AccountTypeService {

    @Autowired
    private AccountTypeMapper accountTypeMapper;
    
    @Autowired
    private ConsultantAccountRelationMapper relationMapper;
    
    @Autowired
    private ConsultantsMapper consultantsMapper;

    @Override
    public List<AccountType> getAllEnabledAccountTypes() {
        return accountTypeMapper.selectAllEnabled();
    }

    @Override
    public List<AccountType> getAccountTypesByGroup(String groupName) {
        return accountTypeMapper.selectByGroup(groupName);
    }

    @Override
    public AccountType getAccountTypeWithConsultants(Integer accountTypeId) {
        AccountType accountType = accountTypeMapper.selectById(accountTypeId);
        if (accountType != null) {
            // 获取关联的顾问ID列表
            List<ConsultantAccountRelation> relations = relationMapper.selectByAccountTypeOrdered(accountTypeId);
            List<Integer> consultantIds = relations.stream()
                    .map(ConsultantAccountRelation::getConsultantId)
                    .collect(Collectors.toList());
            accountType.setConsultantIds(consultantIds);
        }
        return accountType;
    }

    @Override
    @Transactional
    public boolean saveOrUpdateAccountType(AccountType accountType) {
        boolean result;
        if (accountType.getId() == null) {
            // 新增
            result = accountTypeMapper.insert(accountType) > 0;
        } else {
            // 更新
            result = accountTypeMapper.updateById(accountType) > 0;
        }
        return result;
    }

    @Override
    public boolean toggleAccountTypeStatus(Integer accountTypeId, Integer status) {
        AccountType accountType = accountTypeMapper.selectById(accountTypeId);
        if (accountType != null) {
            accountType.setStatus(status);
            return accountTypeMapper.updateById(accountType) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean associateConsultantsToAccountType(Integer accountTypeId, List<Integer> consultantIds) {
        // 先删除旧的关联关系
        QueryWrapper<ConsultantAccountRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("account_type_id", accountTypeId);
        relationMapper.delete(wrapper);
        
        // 如果consultantIds为空，则直接返回成功
        if (consultantIds == null || consultantIds.isEmpty()) {
            return true;
        }
        
        // 重新建立关联关系
        for (Integer consultantId : consultantIds) {
            ConsultantAccountRelation relation = new ConsultantAccountRelation();
            relation.setConsultantId(consultantId);
            relation.setAccountTypeId(accountTypeId);
            relation.setCount(0);          // 初始客资数为0
            relation.setOrderStatus(0);    // 初始排序状态为0
            relation.setWaiting(0);        // 初始等待数为0
            relationMapper.insert(relation);
        }
        
        return true;
    }

    @Override
    public List<Consultants> getConsultantsByAccountType(Integer accountTypeId) {
        List<ConsultantAccountRelation> relations = relationMapper.selectByAccountTypeOrdered(accountTypeId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取顾问ID列表
        List<Integer> consultantIds = relations.stream()
                .map(ConsultantAccountRelation::getConsultantId)
                .collect(Collectors.toList());
        
        // 查询顾问信息
        List<Consultants> consultants = new ArrayList<>();
        for (Integer id : consultantIds) {
            Consultants consultant = consultantsMapper.selectById(id);
            if (consultant != null) {
                consultants.add(consultant);
            }
        }
        
        return consultants;
    }

    @Override
    public List<String> getAllAccountTypeGroups() {
        // 获取所有账号类型，包括禁用状态的分组标记
        QueryWrapper<AccountType> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT group_name");  // 只选择分组名
        List<AccountType> accountTypes = accountTypeMapper.selectList(wrapper);
        
        // 提取所有不重复的分组名称
        return accountTypes.stream()
                .map(AccountType::getGroupName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<AccountType>> getAccountTypesByGroups() {
        // 获取所有启用的账号类型
        List<AccountType> accountTypes = accountTypeMapper.selectAllEnabled();
        
        // 获取所有分组（包括没有启用账号类型的分组）
        List<String> allGroups = getAllAccountTypeGroups();
        
        // 按分组进行分类
        Map<String, List<AccountType>> groupedTypes = accountTypes.stream()
                .collect(Collectors.groupingBy(AccountType::getGroupName));
        
        // 确保每个分组都有一个列表（即使是空的）
        for (String group : allGroups) {
            if (!groupedTypes.containsKey(group)) {
                groupedTypes.put(group, new ArrayList<>());
            }
        }
        
        return groupedTypes;
    }

    @Override
    public boolean addGroup(String groupName) {
        // 检查分组是否已存在
        List<String> existingGroups = getAllAccountTypeGroups();
        if (existingGroups.contains(groupName)) {
            return false;
        }
        
        // 创建一个空的账号类型作为分组标记（实际不会使用）
        AccountType marker = new AccountType();
        marker.setTypeName("_GROUP_MARKER_");
        marker.setGroupName(groupName);
        marker.setStatus(1); // 启用状态，以确保能在前端显示
        marker.setDescription("分组标记，请勿直接使用");
        marker.setCountFieldName("");
        marker.setOrderStatusFieldName("");
        marker.setWaitingFieldName("");
        marker.setDisplayOrder(0);
        return accountTypeMapper.insert(marker) > 0;
    }

    @Override
    @Transactional
    public boolean renameGroup(String oldName, String newName) {
        // 检查旧分组是否存在
        List<String> existingGroups = getAllAccountTypeGroups();
        if (!existingGroups.contains(oldName) || existingGroups.contains(newName)) {
            return false;
        }
        
        // 获取该分组下的所有账号类型
        List<AccountType> accountTypes = accountTypeMapper.selectByGroup(oldName);
        
        // 更新所有账号类型的分组名称
        for (AccountType accountType : accountTypes) {
            accountType.setGroupName(newName);
            accountTypeMapper.updateById(accountType);
        }
        
        return true;
    }

    @Override
    public boolean deleteGroup(String groupName) {
        // 检查分组是否存在
        List<String> existingGroups = getAllAccountTypeGroups();
        if (!existingGroups.contains(groupName)) {
            return false;
        }
        
        // 获取所有标记为该分组的账号类型
        QueryWrapper<AccountType> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName)
              .eq("type_name", "_GROUP_MARKER_");
        return accountTypeMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean groupHasAccountTypes(String groupName) {
        // 查询该分组下是否有实际的账号类型
        QueryWrapper<AccountType> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName)
              .ne("type_name", "_GROUP_MARKER_");
        return accountTypeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<AccountType> getAccountTypesByGroupName(String groupName) {
        // 创建查询条件
        QueryWrapper<AccountType> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName);
        
        // 不包含特殊的分组标记
        wrapper.ne("type_name", "_GROUP_MARKER_");
        
        // 按显示顺序排序
        wrapper.orderByAsc("display_order");
        
        return accountTypeMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean deleteAccountType(Integer accountTypeId) {
        // 首先检查是否存在
        AccountType accountType = accountTypeMapper.selectById(accountTypeId);
        if (accountType == null) {
            return false;
        }
        
        // 删除与此账号类型关联的顾问关系
        QueryWrapper<ConsultantAccountRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("account_type_id", accountTypeId);
        relationMapper.delete(wrapper);
        
        // 删除账号类型
        return accountTypeMapper.deleteById(accountTypeId) > 0;
    }
}
