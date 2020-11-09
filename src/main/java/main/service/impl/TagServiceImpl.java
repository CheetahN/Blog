package main.service.impl;

import main.api.response.TagDTO;
import main.api.response.TagResponse;
import main.repository.TagRepository;
import main.repository.TagToPostRepository;
import main.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class TagServiceImpl implements TagService {
    private TagToPostRepository tagToPostRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, TagToPostRepository tagToPostRepository) {
        this.tagToPostRepository = tagToPostRepository;
    }

    @Override
    public TagResponse getTag(String query) {
        List<TagDTO> response = new ArrayList<>();
        List<Object[]> pairs = tagToPostRepository.countAggregatedTags();
        long max = 0;
        for (Object[]  pair: pairs ) {
            if ((Long) pair[1] > max)
                max = (Long) pair[1];
        }

        long finalMax = max;

        String finalQuery;
        if (query == null)
            finalQuery = "";
        else finalQuery = query;

        pairs.forEach(pair -> {
            if (((String) pair[0]).startsWith(finalQuery))
                response.add(new TagDTO((String) pair[0], ((Long) pair[1]).floatValue() / finalMax));
        });

        return new TagResponse(response);
    }
}
