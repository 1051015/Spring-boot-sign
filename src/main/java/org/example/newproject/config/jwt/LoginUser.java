package org.example.newproject.config.jwt;

import org.example.newproject.user.session.DeviceType;

public record LoginUser(String loginId, String sessionId, DeviceType deviceType) {}
