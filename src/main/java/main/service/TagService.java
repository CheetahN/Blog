package main.service;

import main.api.response.TagListResponse;
import org.springframework.stereotype.Service;

@Service
public interface TagService {
    public TagListResponse getTag(String query);
    public void addTag(String tagName, Integer postId);
    public void removeTagsFromPost(Integer postId);
}
