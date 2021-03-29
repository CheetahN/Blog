package main.service.impl;

import main.model.User;
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
    @Value("${upload.url.label}")
    private String uploadUrlLabel;
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

    public String uploadFile(MultipartFile image) {
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
        return uploadUrlLabel + randomPath + image.getOriginalFilename();

    }

    public void cropAndResizeAvatar(User user) throws IOException {

        File file = new File(uploadPath + user.getPhoto().substring(uploadUrlLabel.length()));
        BufferedImage image = ImageIO.read(file);
        int min = Math.min(image.getHeight(), image.getWidth());
        image = Scalr.crop(image, min, min);
        if (min > avatarSize)
            image = Scalr.resize(image, avatarSize, avatarSize);
        ImageIO.write(image, user.getPhoto().substring(user.getPhoto().length() - 3), file);
    }

    public void removeImage(String imagePath) throws IOException {
        Files.deleteIfExists(Paths.get(uploadPath + imagePath.substring(uploadUrlLabel.length())));
    }
}

