package main.service;

import main.api.response.StatisticsResponse;
import org.springframework.stereotype.Service;

@Service
public interface StatService {

    public StatisticsResponse getMyStatistics();
    public StatisticsResponse getStatistics();
}
