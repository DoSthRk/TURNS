package com.qj.back.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Component
@Slf4j
public class JwtUtil {
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 生成token
    public static String generateToken(String account) {
        return Jwts.builder()
                .setSubject(account)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // 验证token
    public static boolean validateToken(String token) {
        try {
            log.info("验证token: {}", token);  // 添加日志
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("token验证失败", e);  // 添加日志
            return false;
        }
    }

    // 从token中获取账号
    public static String getAccountFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
} 