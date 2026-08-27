package com.efe.traderecon.cucumber;

import com.efe.traderecon.TradeReconApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = TradeReconApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CucumberSpringConfiguration {
}
