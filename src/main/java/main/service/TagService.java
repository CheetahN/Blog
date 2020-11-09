package main.service;

import main.api.response.TagResponse;
import org.springframework.stereotype.Service;

@Service
public interface TagService {
    public TagResponse getTag(String query);
}
