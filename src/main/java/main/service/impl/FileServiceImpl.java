package main.service.impl;

import main.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class FileServiceImpl implements FileService {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public Object uploadFile(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();
        if (image != null) {
            if (!(image.getOriginalFilename().endsWith(".jpg") || (image.getOriginalFilename().endsWith(".png")))) {
                errors.put("image", "неверный формат");
                return errors;
            }
            String randomPath = "/" + getRandomString(2) +  "/" + getRandomString(2) + "/" + getRandomString(2) + "/";
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
        } else {
            errors.put("image", "файл пуст");
            return errors;
        }
    }

    private String getRandomString(int length) {
        int leftLimit = 48; // '0'
        int rightLimit = 122; // 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

