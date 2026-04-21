package com.qj.back.service;

import com.qj.back.entity.SyncDoc;

public interface SyncDocService {
    /**
     * 获取最新的文档内容
     */
    SyncDoc getLatestContent();

    /**
     * 更新文档内容
     * @param doc 文档内容
     * @return 是否更新成功
     */
    boolean updateContent(SyncDoc doc);
}
