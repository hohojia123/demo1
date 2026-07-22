package com.example.demo.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {
          String conversationId;
          Integer userId;
          String title;
          Integer status;
          LocalDateTime createdAt;
          LocalDateTime updatedAt;

}

