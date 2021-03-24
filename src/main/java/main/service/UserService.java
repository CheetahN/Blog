package main.service;

import main.api.request.ProfileRequest;
import main.api.response.ResultResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public ResultResponse changeMyProfile(ProfileRequest profileRequest);
}
