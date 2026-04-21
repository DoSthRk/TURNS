package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qj.back.entity.ShiftTypeConfig;
import com.qj.back.mapper.ShiftTypeConfigMapper;
import com.qj.back.service.ShiftTypeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class ShiftTypeConfigServiceImpl implements ShiftTypeConfigService {

    private static final LinkedHashMap<Integer, String> DEFAULT_TYPE_LABELS = new LinkedHashMap<>();

    static {
        DEFAULT_TYPE_LABELS.put(1, "辅导");
        DEFAULT_TYPE_LABELS.put(2, "申诉");
        DEFAULT_TYPE_LABELS.put(4, "国际课程");
        DEFAULT_TYPE_LABELS.put(3, "推月申诉");
        DEFAULT_TYPE_LABELS.put(5, "推月辅导");
        DEFAULT_TYPE_LABELS.put(6, "汇诺辅导");
        DEFAULT_TYPE_LABELS.put(7, "汇诺申诉");
        DEFAULT_TYPE_LABELS.put(8, "智云辅导");
        DEFAULT_TYPE_LABELS.put(9, "留学堂辅导");
        DEFAULT_TYPE_LABELS.put(12, "留学堂申诉");
        DEFAULT_TYPE_LABELS.put(10, "集好家辅导");
        DEFAULT_TYPE_LABELS.put(11, "集好家申诉");
    }

    @Autowired
    private ShiftTypeConfigMapper shiftTypeConfigMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        ensureTableExists();
        ensureDefaultRows();
    }

    @Override
    public List<ShiftTypeConfig> getAllTypeConfigs() {
        ensureDefaultRows();
        return shiftTypeConfigMapper.selectAllOrdered();
    }

    @Override
    public List<ShiftTypeConfig> getEnabledTypeConfigs() {
        ensureDefaultRows();
        return shiftTypeConfigMapper.selectEnabledOrdered();
    }

    @Override
    @Transactional
    public boolean saveTypeConfigs(List<ShiftTypeConfig> configs) {
        if (configs == null) {
            return false;
        }

        List<ShiftTypeConfig> sanitized = new ArrayList<>();
        Set<Integer> typeSet = new HashSet<>();
        int fallbackOrder = 1;

        for (ShiftTypeConfig item : configs) {
            if (item == null || item.getConsultantType() == null || item.getConsultantType() <= 0) {
                continue;
            }
            Integer consultantType = item.getConsultantType();
            if (typeSet.contains(consultantType)) {
                continue;
            }
            typeSet.add(consultantType);

            ShiftTypeConfig config = new ShiftTypeConfig();
            config.setConsultantType(consultantType);
            String label = item.getTypeLabel();
            if (label == null || label.trim().isEmpty()) {
                label = DEFAULT_TYPE_LABELS.getOrDefault(consultantType, "类型" + consultantType);
            }
            config.setTypeLabel(label.trim());
            config.setEnabled(item.getEnabled() != null && item.getEnabled() == 1 ? 1 : 0);
            config.setDisplayOrder(item.getDisplayOrder() != null ? item.getDisplayOrder() : fallbackOrder++);
            sanitized.add(config);
        }

        sanitized.sort(Comparator.comparingInt(ShiftTypeConfig::getDisplayOrder));
        shiftTypeConfigMapper.delete(new QueryWrapper<>());
        for (ShiftTypeConfig config : sanitized) {
            shiftTypeConfigMapper.insert(config);
        }
        return true;
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS shift_type_config ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "consultant_type INT NOT NULL,"
                + "type_label VARCHAR(64) NOT NULL,"
                + "enabled TINYINT NOT NULL DEFAULT 1,"
                + "display_order INT NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_shift_type_consultant_type (consultant_type)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        jdbcTemplate.execute(sql);
    }

    private void ensureDefaultRows() {
        Integer total = shiftTypeConfigMapper.selectCount(new QueryWrapper<>());
        if (total != null && total > 0) {
            return;
        }

        int order = 1;
        for (Map.Entry<Integer, String> entry : DEFAULT_TYPE_LABELS.entrySet()) {
            ShiftTypeConfig config = new ShiftTypeConfig();
            config.setConsultantType(entry.getKey());
            config.setTypeLabel(entry.getValue());
            config.setEnabled(1);
            config.setDisplayOrder(order++);
            shiftTypeConfigMapper.insert(config);
        }
        log.info("Initialized default shift type configs");
    }
}

