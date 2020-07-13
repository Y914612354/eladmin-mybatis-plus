/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * 登录验证码配置信息
 *
 * @author liaojinlong
 * @date 2020/6/10 18:53
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "login.login-code", ignoreUnknownFields = false)
public class LoginCode implements Serializable {
    private static final long serialVersionUID = -3559741454934817138L;
    /**
     * 验证码配置
     */
    private LoginCodeEnum codeType = LoginCodeEnum.arithmetic;
    /**
     * 验证码有效期 分钟
     */
    private Long expiration = 2L;
    /**
     * 验证码内容长度
     */
    private Integer length = 111;
    /**
     * 验证码宽度
     */
    private Integer width = 36;
    /**
     * 验证码高度
     */
    private Integer height = 2;
    /**
     * 验证码高度
     */
    private String fontName;
    /**
     * 验证码高度
     */
    private Integer fontSize = 25;


}
