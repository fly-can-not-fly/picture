package com.yupi.sfxpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.yupi.sfxpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class SfxPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfxPictureBackendApplication.class, args);
    }

}
