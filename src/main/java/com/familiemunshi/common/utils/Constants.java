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
    public static final String LOGOUT = "/logout";

    // USERS
    public static final String USERS = "/users";

    // SYSTEM ROLES
    public static final String SYSTEM = "/system";
    // ROLES
    public static final String ROLES = "/roles";

    public static final String ID = "/{id}";
    public static final String BRANCH_ID = "/{branchId}";
    public static final String USER_ID = "/{userId}";
    public static final String ROLE_ID = "/{roleId}";

    // PERMISSIONS
    public static final String PERMISSIONS = "/permissions";

    // BRANCHES
    public static final String BRANCHES = "/branches";

    public static final String PRODUCT_CLASSIFICATIONS = "/product-classifications";
    public static final String PRODUCT_CLASSIFICATION_TREE = "/tree";
    public static final String PRODUCT_CLASSIFICATION_GROUP_ID = "/{groupId}";
    public static final String PRODUCT_CLASSIFICATION_ROOTS = "/roots";
    public static final String PRODUCT_CLASSIFICATION_CHILDREN = "/children";
    public static final String PRODUCT_CLASSIFICATION_BY_LEVEL = "/by-level";
    public static final String PRODUCT_CLASSIFICATION_LEVEL = "/{level}";


    // Swagger & Actuator
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
}
