package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties uploadProperties;



    public String uploadImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(contentType)){
                throw new LeyouException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            BufferedImage image = null;
            image = ImageIO.read(file.getInputStream());
            if (image==null){
                throw new LeyouException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            String s = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), s, null);
            return uploadProperties.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            log.error("[文件上传] 上传图片失败!",e);
            throw new LeyouException(ExceptionEnum.INVALID_FILE_TYPE);
        }
    }
}
