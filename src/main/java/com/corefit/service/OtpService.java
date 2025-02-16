package com.corefit.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final StringRedisTemplate redisTemplate;
    protected static final int EXPIRATION_TIME = 20;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateOtp(String email) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        redisTemplate.opsForValue().set(email, otp, EXPIRATION_TIME, TimeUnit.MINUTES);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(email);
        return storedOtp != null && storedOtp.equals(otp);
    }
}
