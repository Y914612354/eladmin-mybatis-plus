/*
 *  Copyright 2019-2020 Fang Jin Biao
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.admin.system.security.controller;

import cn.hutool.core.util.IdUtil;
import com.admin.annotation.AnonymousAccess;
import com.admin.annotation.Log;
import com.admin.annotation.rest.AnonymousDeleteMapping;
import com.admin.annotation.rest.AnonymousGetMapping;
import com.admin.annotation.rest.AnonymousPostMapping;
import com.admin.config.LoginProperties;
import com.admin.config.RsaProperties;
import com.admin.config.SecurityProperties;
import com.admin.exception.BadRequestException;
import com.admin.modules.security.security.TokenProvider;
import com.admin.modules.security.service.dto.AuthUserDto;
import com.admin.system.dto.AdminJwtUserDto;
import com.admin.system.security.service.AdminOnlineUserService;
import com.admin.system.service.ICaptchaService;
import com.admin.utils.RedisUtils;
import com.admin.utils.RsaUtils;
import com.admin.utils.SecurityUtils;
import com.admin.utils.StringUtils;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author adyfang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Api(tags = "系统：系统授权接口")
public class AuthController {

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final AdminOnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RsaProperties rsaProperties;
    private final LoginProperties loginProperties;
    private final ICaptchaService captchaService;

    @SuppressWarnings({"serial"})
    @Log("用户登录")
    @ApiOperation("登录授权")
    @AnonymousAccess
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request)
            throws Exception {
        // 密码解密
        String password = RsaUtils.decryptByPrivateKey(rsaProperties.getPrivateKey(), authUser.getPassword());
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authUser.getUsername(), password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌
        String token = tokenProvider.createToken(authentication);
        final AdminJwtUserDto jwtUser = (AdminJwtUserDto) authentication.getPrincipal();
        // 保存在线信息
        onlineUserService.save(jwtUser, token, request);
        // 返回 token 与 用户信息
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {
            {
                put("token", properties.getTokenStartWith() + token);
                put("user", jwtUser);
            }
        };
        if (loginProperties.isSingleLogin()) {
            // 踢掉之前已经登录的token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    @ApiOperation("获取用户信息")
    @GetMapping(value = "/info")
    public ResponseEntity<Object> getUserInfo() {
        return ResponseEntity.ok(SecurityUtils.getCurrentUser());
    }

    @SuppressWarnings("serial")
    @AnonymousAccess
    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 获取运算的结果
        Captcha captcha = captchaService.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        // 保存
        redisUtils.set(uuid, captcha.text(), loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {
            {
                put("img", captcha.toBase64());
                put("uuid", uuid);
            }
        };
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("退出登录")
    @AnonymousAccess
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
