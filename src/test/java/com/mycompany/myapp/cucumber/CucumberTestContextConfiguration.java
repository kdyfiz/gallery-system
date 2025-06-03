package com.mycompany.myapp.cucumber;

import com.mycompany.myapp.IntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CucumberTestContextConfiguration {}
