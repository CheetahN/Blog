package main.service.impl;

import main.service.FileService;
import main.service.exceptions.BadRequestException;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    @Value("${upload.path}")
    private String uploadPath;
    @Value("${avatar.size}")
    private int avatarSize;

    @Override
    public String uploadImage(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();
        if (image == null) {
            errors.put("image", "файл пуст");
        } else {
            if (!(image.getOriginalFilename().endsWith(".jpg") || (image.getOriginalFilename().endsWith(".png")))) {
                errors.put("image", "неверный формат файла");
            } else {
                return uploadFile(image);
            }
        }
        throw new BadRequestException(errors);
    }

    private String uploadFile(MultipartFile image) {
        String randomPath = "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/";
        File uploadDir = new File(uploadPath + randomPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        Path outputPath = Paths.get(uploadPath + randomPath + image.getOriginalFilename());
        try {
            image.transferTo(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "/" + uploadPath + randomPath + image.getOriginalFilename();

    }

    private void cropAndResizeAvatar(String imagePath) throws IOException {
        File file = new File(imagePath);
        BufferedImage image = ImageIO.read(file);
        int min = Math.min(image.getHeight(), image.getWidth());
        image = Scalr.crop(image, min, min);
        if (min > avatarSize)
            image = Scalr.resize(image, avatarSize, avatarSize);
        ImageIO.write(image, imagePath.substring(imagePath.length() - 3), file);
    }

    @Override
    public void removeImage(String imagePath)  {
        try {
            Files.deleteIfExists(Paths.get(imagePath.substring(1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String uploadAvatar(MultipartFile image) {
        String path = uploadFile(image);
        try {
            cropAndResizeAvatar(path.substring(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
}

