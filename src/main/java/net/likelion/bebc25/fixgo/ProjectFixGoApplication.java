package net.likelion.bebc25.fixgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectFixGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectFixGoApplication.class, args);
    }

}
