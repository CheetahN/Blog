package main.api.response;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class InitResponse {
    @Value("${blog.title}")
    private String title;
    @Value("${blog.subtitle}")
    private String subtitle;
    @Value("${blog.phone}")
    private String phone;
    @Value("${blog.email}")
    private String email;
    @Value("${blog.copyright}")
    private String copyright;
    @Value("${blog.copyrightFrom}")
    private String copyrightFrom;


}



//	          "title": "DevPub",
//            "subtitle": "Рассказы разработчиков",
//            "phone": "+7 903 666-44-55",
//            "email": "mail@mail.ru",
//            "copyright": "Дмитрий Сергеев",
//            "copyrightFrom": "2005"