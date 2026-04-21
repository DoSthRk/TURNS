package com.qj.back.service.Impl;


import com.qj.back.entity.SyncDoc;
import com.qj.back.mapper.SyncDocMapper;
import com.qj.back.service.SyncDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SyncDocServiceImpl implements SyncDocService {
    @Autowired
    private SyncDocMapper syncDocMapper;

    @Override
    public SyncDoc getLatestContent() {
        SyncDoc doc = syncDocMapper.getLatestContent();
        if (doc == null) {
            // 如果没有内容，返回空文档
            doc = new SyncDoc();
            doc.setContent("");
            doc.setUpdateTime(LocalDateTime.now());
        }
        return doc;
    }

    @Override
    public boolean updateContent(SyncDoc doc) {
        try {
            // 设置更新时间
            doc.setUpdateTime(LocalDateTime.now());

            // 如果是第一次创建
            if (syncDocMapper.getLatestContent() == null) {
                return syncDocMapper.insert(doc) > 0;
            }

            // 更新现有内容
            return syncDocMapper.update(doc) > 0;
        } catch (Exception e) {
            log.error("更新文档内容失败:", e);
            return false;
        }
    }
}
