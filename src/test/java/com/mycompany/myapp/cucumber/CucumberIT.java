package com.mycompany.myapp.cucumber;

import static io.cucumber.junit.platform.engine.Constants.*;

import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/mycompany/myapp/cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.mycompany.myapp.cucumber")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/com/mycompany/myapp/cucumber")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@album-sorting")
class CucumberIT {}
