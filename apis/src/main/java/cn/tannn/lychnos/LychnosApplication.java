package cn.tannn.lychnos;

import cn.tannn.jdevelops.autoschema.scan.EnableAutoSchema;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAutoSchema
@EnableAsync
public class LychnosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LychnosApplication.class, args);
    }

}
