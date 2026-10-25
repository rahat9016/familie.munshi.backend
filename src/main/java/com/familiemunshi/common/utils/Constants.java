package com.familiemunshi.common.utils;

public class Constants {
    public static final String APP_NAME = "Familie Munshi API";

    // API VERSIONING
    public static final String API = "/api";
    public static final String API_VERSION = "/v1";
    public static final String PUBLIC = "/public";
    public static final String API_BASE = API + API_VERSION;

    // AUTHENTICATION
    public static final String AUTH = "/auth";
    public static final String LOGIN = "/login";
    public static final String SIGNUP = "/signup";
    public static final String REFRESH_TOKEN = "/refresh-token";

    // USERS
    public static final String USERS = "/users";

    // SYSTEM ROLES
    public static final String SYSTEM = "/system";
    // ROLES
    public static final String ROLES = "/roles";

    public static final String ID = "/{id}";

    // PERMISSIONS
    public static final String PERMISSIONS = "/permissions";

    // BRANCHES
    public static final String BRANCHES = "/branches";


    // Swagger & Actuator
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
}
