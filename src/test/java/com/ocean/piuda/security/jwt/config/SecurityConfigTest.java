package com.ocean.piuda.security.jwt.config;

import com.ocean.piuda.admin.bio.controller.AdminSpeciesController;
import com.ocean.piuda.bio.service.SpeciesService;
import com.ocean.piuda.record.reference.controller.RecordReferenceController;
import com.ocean.piuda.security.jwt.controller.AuthController;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.jwt.handler.CustomAccessDeniedHandler;
import com.ocean.piuda.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.ocean.piuda.security.jwt.service.AuthService;
import com.ocean.piuda.security.jwt.service.CustomUserDetailsService;
import com.ocean.piuda.security.jwt.util.JwtTokenProvider;
import com.ocean.piuda.security.jwt.util.TokenCookieFactory;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import com.ocean.piuda.site.service.SiteNameOptionService;
import com.ocean.piuda.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        RecordReferenceController.class,
        AdminSpeciesController.class,
        AuthController.class
})
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private SpeciesService speciesService;

    @MockitoBean
    private SiteNameOptionService siteNameOptionService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenCookieFactory tokenCookieFactory;

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void userCanAccessRecordSpeciesApi() throws Exception {
        when(speciesService.getAllSpecies()).thenReturn(List.of());

        mockMvc.perform(get("/api/record/species"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void userCanAccessRecordSiteOptionsApi() throws Exception {
        when(siteNameOptionService.getActiveOptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/record/site-options"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void userCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/species"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanAccessAdminApi() throws Exception {
        when(speciesService.getAllSpecies()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/species"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_NOT_REGISTERED")
    void notRegisteredCannotAccessRecordApi() throws Exception {
        mockMvc.perform(get("/api/record/species"))
                .andExpect(status().isForbidden());
    }

    @Test
    void notRegisteredCanAccessCompleteSignupApi() throws Exception {
        User user = User.builder()
                .username("not-registered@test.com")
                .role(Role.NOT_REGISTERED)
                .build();

        PrincipalDetails principalDetails = new PrincipalDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails,
                        null,
                        List.of(new SimpleGrantedAuthority(Role.NOT_REGISTERED.getKey()))
                );

        mockMvc.perform(post("/api/auth/complete-sign-up/user")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "일반 사용자",
                                  "email": "user@test.com",
                                  "phone": "010-0000-0000"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousCannotAccessRecordApi() throws Exception {
        mockMvc.perform(get("/api/record/species"))
                .andExpect(status().isUnauthorized());
    }
}