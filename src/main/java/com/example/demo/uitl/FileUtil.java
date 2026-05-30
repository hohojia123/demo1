package com.example.demo.uitl;

import com.example.demo.configuration.ImgUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class FileUtil {

    @Autowired
    private ImgUploadConfig imgUploadConfig;

    public String upload(MultipartFile file) throws Exception {
        return AliossUtil.upload(file.getBytes(),file.getOriginalFilename());
    }




}
