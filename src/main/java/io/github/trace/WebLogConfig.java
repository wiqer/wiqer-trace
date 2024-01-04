package io.github.trace;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * ：WebConfig
 *
 * @author ：李岚峰、lilanfeng、
 * @device name ：user
 * @date ：Created in 26 / 2023/12/26  18:40
 * @description：
 * @modified By：
 */
@Configuration
public class WebLogConfig implements WebMvcConfigurer {

    @Resource
    private WebLogInterceptor myInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myInterceptor);
    }
}
