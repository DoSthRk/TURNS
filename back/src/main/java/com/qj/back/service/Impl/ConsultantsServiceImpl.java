package com.qj.back.service.Impl;
import com.qj.back.entity.Consultants;
import com.qj.back.mapper.ConsultantsMapper;
import com.qj.back.service.ConsultantsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class ConsultantsServiceImpl implements ConsultantsService {
    // 实现所有接口方法
    // 把原来 ConsultantsService 类中的具体实现代码复制到这里
    @Autowired
    private ConsultantsMapper consultantsMapper;

    @Override
    public void updateStatus(Integer id, Integer status) {
        consultantsMapper.updateStatusById(id, status);
    }

    @Override
    public void updateShiftOrder(Integer id, Integer orderStatus) {
        consultantsMapper.updateShiftOrderById(id, orderStatus);
    }

    @Override
    public List<Consultants> getConsultantsOrderedByNormal() {
        return consultantsMapper.selectConsultantsOrderedByNormal();
    }

    @Override
    public List<Consultants> getConsultantsOrderedBySem() {
        return consultantsMapper.selectConsultantsOrderedBySem();
    }

    @Override
    public List<Consultants> getConsultantsOrderedBySingle1() {
        return consultantsMapper.selectConsultantsOrderedBySingle1();
    }

    @Override
    public List<Consultants> getAllConsultants() {
        return consultantsMapper.selectList(null);
    }

    @Override
    public Consultants getConsultantById(Integer id) {
        return consultantsMapper.selectById(id);
    }

}
