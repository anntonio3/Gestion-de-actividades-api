package mx.edu.unpa.actividadesapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ActividadesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActividadesApiApplication.class, args);
    }

}
