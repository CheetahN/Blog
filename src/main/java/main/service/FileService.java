package main.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {

    public String uploadFile(MultipartFile file);
    public String uploadAvatar(MultipartFile image);
    public void removeAvatar();
}
