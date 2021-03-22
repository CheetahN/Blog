package main.service.impl;

import main.service.FileService;
import main.service.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String uploadFile(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();
        if (image == null) {
            errors.put("image", "файл пуст");
        } else {
            if (!(image.getOriginalFilename().endsWith(".jpg") || (image.getOriginalFilename().endsWith(".png")))) {
                errors.put("image", "неверный формат файла");
            } else {
                String randomPath = "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/";
                File uploadDir = new File(uploadPath + randomPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String fullPath = uploadPath + randomPath + image.getOriginalFilename();
                try {
                    image.transferTo(new File(fullPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "/upload" + randomPath + image.getOriginalFilename();
            }
        }
        throw new BadRequestException(errors);
    }
}

