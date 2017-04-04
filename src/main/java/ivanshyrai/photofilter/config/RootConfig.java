package ivanshyrai.photofilter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.regex.Pattern;

@Configuration
@ComponentScan(basePackages = {"ivanshyrai.photofilter"},
        excludeFilters = {@Filter(type = FilterType.CUSTOM, value = RootConfig.WebPackege.class)})
public class RootConfig {
    public static class WebPackege extends RegexPatternTypeFilter {
        public WebPackege() {
            super(Pattern.compile("ivanshyrai.photofilter\\.web"));
        }
    }
}
