package com.qj.back.mapper;

import com.qj.back.entity.SyncDoc;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SyncDocMapper {


    @Select("SELECT * FROM sync_doc ORDER BY update_time DESC LIMIT 1")
    SyncDoc getLatestContent();

    @Insert("INSERT INTO sync_doc (content, update_time, update_by, update_by_name) " +
            "VALUES (#{content}, #{updateTime}, #{updateBy}, #{updateByName})")
    int insert(SyncDoc doc);

    @Update("UPDATE sync_doc SET content = #{content}, " +
            "update_time = #{updateTime}, " +
            "update_by = #{updateBy}, " +
            "update_by_name = #{updateByName} " +
            "WHERE id = (SELECT id FROM (SELECT id FROM sync_doc ORDER BY update_time DESC LIMIT 1) tmp)")
    int update(SyncDoc doc);
}
