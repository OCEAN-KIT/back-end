package com.ocean.piuda.common.security;

import com.ocean.piuda.security.jwt.enums.Role;

public interface CurrentUserProvider {
    Long getCurrentUserId();
    Role getCurrentUserRole();
}

