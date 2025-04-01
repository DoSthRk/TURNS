package com.qj.back.service;

import com.qj.back.entity.AccountType;
import com.qj.back.entity.Consultants;

import java.util.List;
import java.util.Map;

public interface AccountTypeService {
    
    // 获取所有启用的账号类型
    List<AccountType> getAllEnabledAccountTypes();
    
    // 按分组获取账号类型
    List<AccountType> getAccountTypesByGroup(String groupName);
    
    // 获取账号类型详情，包括关联的顾问列表
    AccountType getAccountTypeWithConsultants(Integer accountTypeId);
    
    // 创建或更新账号类型
    boolean saveOrUpdateAccountType(AccountType accountType);
    
    // 启用/禁用账号类型
    boolean toggleAccountTypeStatus(Integer accountTypeId, Integer status);
    
    // 为账号类型关联顾问
    boolean associateConsultantsToAccountType(Integer accountTypeId, List<Integer> consultantIds);
    
    // 获取指定账号类型的顾问列表（已排序）
    List<Consultants> getConsultantsByAccountType(Integer accountTypeId);
    
    // 获取所有账号类型分组
    List<String> getAllAccountTypeGroups();
    
    // 获取按分组组织的账号类型Map
    Map<String, List<AccountType>> getAccountTypesByGroups();
    
    // 添加新分组
    boolean addGroup(String groupName);
    
    // 重命名分组
    boolean renameGroup(String oldName, String newName);
    
    // 删除分组
    boolean deleteGroup(String groupName);
    
    // 检查分组下是否有账号类型
    boolean groupHasAccountTypes(String groupName);
    
    /**
     * 根据分组名称获取该分组下所有账号类型（包括隐藏的）
     */
    List<AccountType> getAccountTypesByGroupName(String groupName);

    /**
     * 删除账号类型
     */
    boolean deleteAccountType(Integer accountTypeId);
}
