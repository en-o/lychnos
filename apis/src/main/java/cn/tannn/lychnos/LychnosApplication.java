package cn.tannn.lychnos;

import cn.tannn.jdevelops.autoschema.scan.EnableAutoSchema;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoSchema
public class LychnosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LychnosApplication.class, args);
    }

}
