package com.qj.back.service.Impl;

import com.qj.back.entity.Complement;
import com.qj.back.mapper.ComplementMapper;
import com.qj.back.service.ComplementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;




@Service
@Slf4j
public class ComplementServiceImpl implements ComplementService {
    @Autowired
    private ComplementMapper complementMapper;

    @Override
    public List<Complement> findAll() {
        log.info("查询所有补充顾问");
        return complementMapper.findAll();
    }

    @Override
    public boolean add(Complement complement) {
        // 先从 consultants 表获取正确的 type
        Integer type = complementMapper.getConsultantType(complement.getId());
        if (type != null) {
            complement.setType(type);
        }

        // 添加到数据库
        boolean success = complementMapper.insert(complement) > 0;
        if (success) {
            // 直接从插入的对象中获取ID
            Long complementId = complement.getComplementId();
            if (complementId == null) {
                // 如果没有自动设置，则手动查询
                complementId = complementMapper.getLastInsertId();
                complement.setComplementId(complementId);
            }
        }
        return success;
    }

    @Override
    public boolean update(Complement complement) {
        log.info("更新补充顾问: {}", complement);
        try {
            return complementMapper.update(complement) > 0;
        } catch (Exception e) {
            log.error("更新补充顾问失败: ", e);
            return false;
        }
    }

    @Override
    public List<Complement> findByType(Integer type) {
        log.info("按类型查询补充顾问: {}", type);
        return complementMapper.selectByType(type);
    }
    @Override
    public boolean delete(Long complementId) {
        log.info("删除补充顾问: {}", complementId);
        try {
            return complementMapper.deleteById(complementId) > 0;
        } catch (Exception e) {
            log.error("删除补充顾问失败: ", e);
            return false;
        }
    }

    @Override
    public Complement getById(Long complementId) {
        return complementMapper.selectById(complementId);
    }

    @Override
    public boolean deleteByType(Integer type) {
        try {
            // 先获取要删除的数据
            List<Complement> complementsToDelete = findByType(type);
            if (complementsToDelete.isEmpty()) {
                return true; // 如果没有数据要删除，也返回成功
            }

            // 执行批量删除
            int result = complementMapper.deleteByType(type);

            // 如果删除的记录数大于0，表示删除成功
            return result > 0;
        } catch (Exception e) {
            log.error("删除指定类型的补充顾问失败: type={}", type, e);
            return false;
        }
    }
}