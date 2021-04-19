package main.service.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import main.service.FileService;
import main.service.exceptions.BadRequestException;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class FileServiceAWS implements FileService {

    @Value("${aws.upload.path}")
    private String uploadPath;
    @Value("${avatar.size}")
    private int avatarSize;
    @Value("${aws.bucket.name}")
    private String bucketName;
    @Value("${aws.root.url}")
    private String awsRoot;
    private final Regions region = Regions.EU_WEST_3;
    private final AmazonS3 s3Client;

    public FileServiceAWS() {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();;
    }

    @Override
    public String uploadImage(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();
        if (image == null) {
            errors.put("image", "файл пуст");
        } else {
            if (!(image.getOriginalFilename().endsWith(".jpg") || (image.getOriginalFilename().endsWith(".png")))) {
                errors.put("image", "неверный формат файла");
            } else {
                try {
                    File file = File.createTempFile("blogImage", ".tmp");
                    image.transferTo(file);
                    return uploadFile(file, image.getOriginalFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                    errors.put("image", "невозможно создать временный файл");
                }
            }
        }
        throw new BadRequestException(errors);
    }

    @Override
    public String uploadAvatar(MultipartFile image) {
        File file = null;
        try {
            file = File.createTempFile("blogAvatar", ".tmp");
            image.transferTo(file);
            cropAndResizeAvatar(file);
            return uploadFile(file, image.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uploadFile(file, image.getOriginalFilename());
    }

    @Override
    public void removeImage(String imagePath) {
        s3Client.deleteObject(bucketName, imagePath);
    }

    private String uploadFile(File file, String originalFileName) {

        String randomPath = "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/" + UtilService.getRandomString(2) + "/";
        String objKey = uploadPath + randomPath + originalFileName;
        s3Client.putObject(bucketName, objKey, file);
        return awsRoot + objKey;
    }

    private void cropAndResizeAvatar(File avatar) throws IOException {
        BufferedImage image = ImageIO.read(avatar);
        int min = Math.min(image.getHeight(), image.getWidth());
        image = Scalr.crop(image, min, min);
        if (min > avatarSize)
            image = Scalr.resize(image, avatarSize, avatarSize);
        ImageIO.write(image, avatar.getName().substring(avatar.getName().length() - 3), avatar);
    }
}
