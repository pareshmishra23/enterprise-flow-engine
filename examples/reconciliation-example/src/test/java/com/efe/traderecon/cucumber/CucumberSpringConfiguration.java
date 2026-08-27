package com.efe.traderecon.cucumber;

import com.efe.traderecon.ReconciliationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = ReconciliationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CucumberSpringConfiguration {
}
