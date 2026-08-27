package com.efe.traderecon.cucumber;

import com.efe.traderecon.PlatformDemoApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = PlatformDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CucumberSpringConfiguration {
}
