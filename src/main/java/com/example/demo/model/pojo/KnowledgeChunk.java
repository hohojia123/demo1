package com.example.demo.model.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChunk {

    private Long chunkId;
    private Long docId;
    private String content;
    private Integer chunkIndex;
    private String embedding;
    private LocalDateTime createAt;



}
