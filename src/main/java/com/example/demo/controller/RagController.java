package com.example.demo.controller;


import com.example.demo.mapper.RagMapper;
import com.example.demo.model.pojo.KnowledgeChunk;
import com.example.demo.service.impl.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

      private  final RagService ragService;
      public  RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/import")
    public String importBlogs(){
          return ragService.importAllblogs();
    }

    @PostMapping("/search")
    public List<KnowledgeChunk> search(@RequestParam String query,
                                       @RequestParam(defaultValue = "5")int topK){
          return ragService.search(query,topK);
    }

    @PostMapping("/clear")
    public String clearAll(){
          ragService.clearAll();
          return "success for clear";
    }


}
