package com.example.demo.mapper;

import com.example.demo.model.pojo.KnowledgeChunk;
import com.example.demo.model.pojo.KnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RagMapper {

    //----Doc----
    void insertDoc(KnowledgeDoc doc);

    KnowledgeDoc findDocByBlogId(Long blogId);

    List<KnowledgeDoc> findAllodcs();

    void updateDocStatus(KnowledgeDoc doc);

    void deleteDoc(Long docId);

    //----Chunk----

    void insertChunk(KnowledgeChunk chunk);

    void insertChunkBatch(@Param("chunks") List<KnowledgeChunk> chunks);

    List<KnowledgeChunk> findAllChunks();

    List<KnowledgeChunk> searchByFulltext(@Param("text")String text,@Param("limit")int limit);

    List<KnowledgeChunk> findRecentChunks(@Param("limit")int limit);

    List<KnowledgeChunk> findChunksByDocId(Long docId);

    void deleteChunksByDocId(Long docId);

    void deleteAllDocs();

    void deleteAllChunks();



}
