package com.qj.back.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qj.back.entity.ShiftModuleConfig;
import com.qj.back.mapper.ShiftModuleConfigMapper;
import com.qj.back.service.ShiftModuleConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShiftModuleConfigServiceImpl implements ShiftModuleConfigService {

    private static final String MODULE_NORMAL = "normal";
    private static final String MODULE_SEM = "sem";
    private static final String MODULE_SINGLE1 = "single1";

    private static final Map<String, String[]> MODULE_FIELD_MAPPING = new HashMap<>();
    private static final Map<Integer, List<DefaultModule>> DEFAULT_CONFIGS = new HashMap<>();

    static {
        MODULE_FIELD_MAPPING.put(MODULE_NORMAL, new String[]{"countNormal", "orderStatusNormal", "waitingNormal"});
        MODULE_FIELD_MAPPING.put(MODULE_SEM, new String[]{"countSem", "orderStatusSem", "waitingSem"});
        MODULE_FIELD_MAPPING.put(MODULE_SINGLE1, new String[]{"countSingle1", "orderStatusSingle1", "waitingSingle1"});
        MODULE_FIELD_MAPPING.put("extra1", new String[]{"countExtra1", "orderStatusExtra1", "waitingExtra1"});
        MODULE_FIELD_MAPPING.put("extra2", new String[]{"countExtra2", "orderStatusExtra2", "waitingExtra2"});
        MODULE_FIELD_MAPPING.put("extra3", new String[]{"countExtra3", "orderStatusExtra3", "waitingExtra3"});

        DEFAULT_CONFIGS.put(1, Arrays.asList(
                new DefaultModule(MODULE_NORMAL, "官号(拉群)", 1, 1),
                new DefaultModule(MODULE_SINGLE1, "主动添加", 1, 2),
                new DefaultModule(MODULE_SEM, "SEM", 1, 3)
        ));
        DEFAULT_CONFIGS.put(2, Arrays.asList(
                new DefaultModule(MODULE_NORMAL, "官号", 1, 1),
                new DefaultModule(MODULE_SEM, "SEM", 1, 2)
        ));
        DEFAULT_CONFIGS.put(3, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "推月申诉", 1, 1)
        ));
        DEFAULT_CONFIGS.put(4, Arrays.asList(
                new DefaultModule(MODULE_NORMAL, "国际课程", 1, 1),
                new DefaultModule(MODULE_SINGLE1, "国际课程主动留资", 1, 2),
                new DefaultModule(MODULE_SEM, "托福", 1, 3)
        ));
        DEFAULT_CONFIGS.put(5, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "推月辅导", 1, 1)
        ));
        DEFAULT_CONFIGS.put(6, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "汇诺辅导", 1, 1)
        ));
        DEFAULT_CONFIGS.put(7, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "汇诺申诉", 1, 1)
        ));
        DEFAULT_CONFIGS.put(8, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "智云辅导", 1, 1)
        ));
        DEFAULT_CONFIGS.put(9, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "留学堂辅导", 1, 1)
        ));
        DEFAULT_CONFIGS.put(10, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "集好家辅导", 1, 1)
        ));
        DEFAULT_CONFIGS.put(11, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "集好家申诉", 1, 1)
        ));
        DEFAULT_CONFIGS.put(12, Collections.singletonList(
                new DefaultModule(MODULE_SINGLE1, "留学堂申诉", 1, 1)
        ));
    }

    @Autowired
    private ShiftModuleConfigMapper shiftModuleConfigMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        ensureTableExists();
        dropUniqueConstraintIfExists();
        migrateConsultantExtraColumns();
        seedDefaultsIfEmpty();
    }

    @Override
    public Map<Integer, List<ShiftModuleConfig>> getAllByType() {
        ensureDefaultsForAllTypes();
        List<ShiftModuleConfig> all = shiftModuleConfigMapper.selectAllOrdered();
        Map<Integer, List<ShiftModuleConfig>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        ShiftModuleConfig::getConsultantType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Integer consultantType : DEFAULT_CONFIGS.keySet()) {
            grouped.putIfAbsent(consultantType, new ArrayList<>());
        }
        return grouped;
    }

    @Override
    public List<ShiftModuleConfig> getByConsultantType(Integer consultantType) {
        if (consultantType == null) {
            return Collections.emptyList();
        }
        ensureDefaultsForType(consultantType);
        return shiftModuleConfigMapper.selectByConsultantType(consultantType);
    }

    @Override
    @Transactional
    public boolean saveByConsultantType(Integer consultantType, List<ShiftModuleConfig> configs) {
        if (consultantType == null) {
            return false;
        }

        QueryWrapper<ShiftModuleConfig> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("consultant_type", consultantType);
        shiftModuleConfigMapper.delete(deleteWrapper);

        if (configs == null || configs.isEmpty()) {
            return true;
        }

        List<ShiftModuleConfig> sanitizedConfigs = new ArrayList<>();
        int fallbackOrder = 1;
        for (ShiftModuleConfig config : configs) {
            ShiftModuleConfig sanitized = sanitizeConfig(consultantType, config, fallbackOrder++);
            if (sanitized != null) {
                sanitizedConfigs.add(sanitized);
            }
        }

        sanitizedConfigs.sort(Comparator.comparingInt(ShiftModuleConfig::getDisplayOrder));
        for (ShiftModuleConfig config : sanitizedConfigs) {
            shiftModuleConfigMapper.insert(config);
        }
        return true;
    }

    private ShiftModuleConfig sanitizeConfig(Integer consultantType, ShiftModuleConfig raw, int fallbackOrder) {
        if (raw == null || raw.getModuleKey() == null) {
            return null;
        }

        String moduleKey = raw.getModuleKey().trim().toLowerCase(Locale.ROOT);
        if (!MODULE_FIELD_MAPPING.containsKey(moduleKey)) {
            return null;
        }

        ShiftModuleConfig config = new ShiftModuleConfig();
        config.setConsultantType(consultantType);
        config.setModuleKey(moduleKey);

        String[] fields = MODULE_FIELD_MAPPING.get(moduleKey);
        config.setCountFieldName(fields[0]);
        config.setOrderStatusFieldName(fields[1]);
        config.setWaitingFieldName(fields[2]);

        String moduleLabel = raw.getModuleLabel();
        if (moduleLabel == null || moduleLabel.trim().isEmpty()) {
            moduleLabel = getDefaultLabel(consultantType, moduleKey);
        }
        config.setModuleLabel(moduleLabel.trim());

        config.setEnabled(raw.getEnabled() != null && raw.getEnabled() == 1 ? 1 : 0);
        config.setDisplayOrder(raw.getDisplayOrder() != null ? raw.getDisplayOrder() : fallbackOrder);
        return config;
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS shift_module_config ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "consultant_type INT NOT NULL,"
                + "module_key VARCHAR(32) NOT NULL,"
                + "module_label VARCHAR(64) NOT NULL,"
                + "count_field_name VARCHAR(64) NOT NULL,"
                + "order_status_field_name VARCHAR(64) NOT NULL,"
                + "waiting_field_name VARCHAR(64) NOT NULL,"
                + "enabled TINYINT NOT NULL DEFAULT 1,"
                + "display_order INT NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        jdbcTemplate.execute(sql);
    }

    private void dropUniqueConstraintIfExists() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.statistics"
                    + " WHERE table_schema = DATABASE()"
                    + " AND table_name = 'shift_module_config'"
                    + " AND index_name = 'uk_shift_module_type_key'",
                    Integer.class);
            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE shift_module_config DROP INDEX uk_shift_module_type_key");
                log.info("Dropped unique constraint uk_shift_module_type_key from shift_module_config");
            }
        } catch (Exception e) {
            log.warn("Could not check/drop unique constraint: {}", e.getMessage());
        }
    }

    private void migrateConsultantExtraColumns() {
        String[][] columns = {
            {"count_extra1",        "INT NOT NULL DEFAULT 0"},
            {"order_status_extra1", "INT NOT NULL DEFAULT 1"},
            {"waiting_extra1",      "INT NOT NULL DEFAULT 0"},
            {"count_extra2",        "INT NOT NULL DEFAULT 0"},
            {"order_status_extra2", "INT NOT NULL DEFAULT 1"},
            {"waiting_extra2",      "INT NOT NULL DEFAULT 0"},
            {"count_extra3",        "INT NOT NULL DEFAULT 0"},
            {"order_status_extra3", "INT NOT NULL DEFAULT 1"},
            {"waiting_extra3",      "INT NOT NULL DEFAULT 0"},
        };
        for (String[] col : columns) {
            try {
                Integer exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns"
                        + " WHERE table_schema = DATABASE()"
                        + " AND table_name = 'consultants'"
                        + " AND column_name = ?",
                        Integer.class, col[0]);
                if (exists == null || exists == 0) {
                    jdbcTemplate.execute("ALTER TABLE consultants ADD COLUMN " + col[0] + " " + col[1]);
                    log.info("Added column {} to consultants", col[0]);
                }
            } catch (Exception e) {
                log.warn("Could not add column {} to consultants: {}", col[0], e.getMessage());
            }
        }
    }

    private void seedDefaultsIfEmpty() {
        Integer total = shiftModuleConfigMapper.selectCount(new QueryWrapper<>());
        if (total != null && total > 0) {
            return;
        }

        for (Map.Entry<Integer, List<DefaultModule>> entry : DEFAULT_CONFIGS.entrySet()) {
            Integer consultantType = entry.getKey();
            for (DefaultModule module : entry.getValue()) {
                shiftModuleConfigMapper.insert(buildConfigFromDefault(consultantType, module));
            }
        }
        log.info("Initialized default shift module configs");
    }

    private void ensureDefaultsForAllTypes() {
        for (Integer consultantType : DEFAULT_CONFIGS.keySet()) {
            ensureDefaultsForType(consultantType);
        }
    }

    private void ensureDefaultsForType(Integer consultantType) {
        QueryWrapper<ShiftModuleConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("consultant_type", consultantType);
        Integer count = shiftModuleConfigMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            return;
        }

        List<DefaultModule> defaults = DEFAULT_CONFIGS.getOrDefault(consultantType, Collections.emptyList());
        for (DefaultModule module : defaults) {
            shiftModuleConfigMapper.insert(buildConfigFromDefault(consultantType, module));
        }
    }

    private ShiftModuleConfig buildConfigFromDefault(Integer consultantType, DefaultModule module) {
        ShiftModuleConfig config = new ShiftModuleConfig();
        config.setConsultantType(consultantType);
        config.setModuleKey(module.moduleKey);
        config.setModuleLabel(module.moduleLabel);
        config.setEnabled(module.enabled);
        config.setDisplayOrder(module.displayOrder);

        String[] fields = MODULE_FIELD_MAPPING.get(module.moduleKey);
        config.setCountFieldName(fields[0]);
        config.setOrderStatusFieldName(fields[1]);
        config.setWaitingFieldName(fields[2]);
        return config;
    }

    private String getDefaultLabel(Integer consultantType, String moduleKey) {
        List<DefaultModule> defaults = DEFAULT_CONFIGS.get(consultantType);
        if (defaults != null) {
            for (DefaultModule module : defaults) {
                if (module.moduleKey.equals(moduleKey)) {
                    return module.moduleLabel;
                }
            }
        }

        if (MODULE_NORMAL.equals(moduleKey)) {
            return "官号";
        }
        if (MODULE_SEM.equals(moduleKey)) {
            return "SEM";
        }
        return "轮班模块";
    }

    private static class DefaultModule {
        private final String moduleKey;
        private final String moduleLabel;
        private final int enabled;
        private final int displayOrder;

        private DefaultModule(String moduleKey, String moduleLabel, int enabled, int displayOrder) {
            this.moduleKey = moduleKey;
            this.moduleLabel = moduleLabel;
            this.enabled = enabled;
            this.displayOrder = displayOrder;
        }
    }
}

