package com.example.demo.service.impl;


import cn.hutool.core.util.IdUtil;
import com.example.demo.mapper.BolgMapper;
import com.example.demo.mapper.RagMapper;
import com.example.demo.model.pojo.Blog;
import com.example.demo.model.pojo.KnowledgeChunk;
import com.example.demo.model.pojo.KnowledgeDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagService {

    private  final RagMapper ragMapper;
    private  final BolgMapper bolgMapper;
    private  final ZhiPuAiEmbeddingModel zhiPuAiEmbeddingModel;
    public RagService(RagMapper ragMapper, BolgMapper bolgMapper, ZhiPuAiEmbeddingModel zhiPuAiEmbeddingModel) {
        this.ragMapper = ragMapper;
        this.bolgMapper = bolgMapper;
        this.zhiPuAiEmbeddingModel = zhiPuAiEmbeddingModel;
    }


    /**
     * 导入所有博客
     */
    public  String importAllblogs(){
        List<Blog> blogs=bolgMapper.findAllActiveBlogForRag();
        int total=0;
        for(Blog b: blogs){
            if(ragMapper.findDocByBlogId((b.getId().longValue()))!=null)continue;
            List<String>chunks=chunkContent(b.getBody());
             Long docId= IdUtil.getSnowflake().nextId();
            KnowledgeDoc k=new KnowledgeDoc(docId,b.getId().longValue(),b.getTitle(),"completed",chunks.size(), LocalDateTime.now(),LocalDateTime.now());
            ragMapper.insertDoc(k);
             List<KnowledgeChunk>chunkList=new ArrayList<>();
             for(int i=0;i<chunks.size();i++) {
                 float[]vector=zhiPuAiEmbeddingModel.embed(chunks.get(i));
                 String vecstr= Arrays.toString(vector);
                 chunkList.add(new KnowledgeChunk(IdUtil.getSnowflake().nextId(), docId, chunks.get(i), i, vecstr, LocalDateTime.now()));
             }
                 total+=chunks.size();
             ragMapper.insertChunkBatch(chunkList);
        }
        log.info("导入完成，共导入 {} 篇博客，{} 个分块", blogs.size(), total);
        return "导入完成，共 " + total + " 个分块";
    }


    /**
     * 删除某篇博客在知识库中的记录（用于博客删除时同步）
     */
    public void deleteBlogKnowledge(Long blogId) {
        KnowledgeDoc doc = ragMapper.findDocByBlogId(blogId);
        if (doc == null) return;
        ragMapper.deleteChunksByDocId(doc.getDocId());
        ragMapper.deleteDoc(doc.getDocId());
        log.info("已删除博客 {} 的知识库记录", blogId);
    }

    /**
     * 更新某篇博客在知识库中的内容（用于博客编辑时同步）
     */
    public void updateBlogKnowledge(Long blogId, String title, String content) {
        deleteBlogKnowledge(blogId);
        List<String> chunks = chunkContent(content);
        Long docId = IdUtil.getSnowflake().nextId();
        KnowledgeDoc doc = new KnowledgeDoc(docId, blogId, title, "completed", chunks.size(), LocalDateTime.now(), LocalDateTime.now());
        ragMapper.insertDoc(doc);
        List<KnowledgeChunk> chunkList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            float[] vector = zhiPuAiEmbeddingModel.embed(chunks.get(i));
            chunkList.add(new KnowledgeChunk(IdUtil.getSnowflake().nextId(), docId, chunks.get(i), i, Arrays.toString(vector), LocalDateTime.now()));
        }
        ragMapper.insertChunkBatch(chunkList);
        log.info("已更新博客 {} 的知识库，共 {} 个分块", blogId, chunks.size());
    }

    /**
     * 检索知识库
     */
    public List<KnowledgeChunk> search(String query,int topK){
     float[]vector=zhiPuAiEmbeddingModel.embed( query);
     List<KnowledgeChunk>chunks=ragMapper.searchByFulltext(query,50);
     if(chunks.isEmpty())
         chunks=ragMapper.findRecentChunks(50);
     return chunks.parallelStream()
             .filter(c-> c.getEmbedding()!=null)
             .map(c->{
                 double sim=cosineSimilarity(vector,parseEmbedding(c.getEmbedding()));
                 return new Object[]{c,sim};
             })
             .sorted((a,b)->
                 Double.compare((double)b[1],(double)a[1])
             ).limit(topK)
             .map(e->(KnowledgeChunk)e[0])
             .collect(Collectors.toList());
    }

    public double cosineSimilarity(float[] vectorA, List<Float> vectorB){
        double dot=0,normA=0,normB=0;
        for(int i=0;i<vectorA.length;i++){
            dot+=vectorA[i]*vectorB.get(i);
            normA+=vectorA[i]*vectorA[i];
            normB+=vectorB.get(i)*vectorB.get(i);
        }
        return dot/(Math.sqrt(normA)*Math.sqrt(normB)+1e-10);
    }

    public List<Float> parseEmbedding(String embedding){
        String trim=embedding.replace("[","").replace("]","").trim();
        if(trim.isEmpty())return List.of();
        String[] parts=trim.split(",");
        List<Float> result=new ArrayList<>();
        for(String part: parts){
            result.add(Float.parseFloat(part));
        }
        return result;
    }

    public void clearAll(){
        ragMapper.deleteAllChunks();
        ragMapper.deleteAllDocs();
        log.info("知识库已清空");
    }

    public  List<String> chunkContent(String content){
        List<String> chunks=new ArrayList<>();
        String[] paragraphs=content.split("\\n\\s*\\n");
        StringBuilder currentChunk=new StringBuilder();
        for(String para: paragraphs){
            para=para.trim();
            if(para.isEmpty())continue;
            if(currentChunk.length()+para.length()>300&&currentChunk.length()>0){
                chunks.add(currentChunk.toString().trim());
                String overlap=currentChunk.length()>50?currentChunk.substring(currentChunk.length()-50)+"\n":"";
                currentChunk=new StringBuilder(overlap);
            }
            currentChunk.append(para+"\n");
        }
        if(currentChunk.length()>0)
            chunks.add(currentChunk.toString().trim());
        return chunks;

    }





}
