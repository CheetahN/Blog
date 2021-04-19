package main.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {

    public String uploadImage(MultipartFile image);
    public String uploadAvatar(MultipartFile image);
    public void removeImage(String imagePath);
}
