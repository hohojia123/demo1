package com.example.demo.model.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeDoc {

    private Long docId;
    private Long blogId;
    private String title;
    private String status;
    private Integer chunkCount;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;


}
