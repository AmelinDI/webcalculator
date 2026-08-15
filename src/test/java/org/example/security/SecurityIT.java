package org.example.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIT {

    private static final String PAYLOAD = "{\"a\":1,\"b\":2,\"op\":\"+\"}";

    @Autowired
    private MockMvc mvc;

    private MockHttpSession loggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login.html").session(session));
        mvc.perform(post("/login").session(session)
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        return session;
    }

    @Test
    void root_withoutSession_redirectsToLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }

    @Test
    void loginPage_isOpen_andSetsCsrfCookie() throws Exception {
        mvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void login_withWrongPassword_fails() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login.html").session(session));
        mvc.perform(post("/login").session(session)
                        .param("username", "user")
                        .param("password", "wrong")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?error"));
    }

    @Test
    void api_withoutAuthentication_redirectsToLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login.html").session(session));
        mvc.perform(post("/api/calc").session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }

    @Test
    void api_withLoggedInSession_returnsResult() throws Exception {
        MockHttpSession session = loggedIn();
        mvc.perform(post("/api/calc").session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3.0));
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        MockHttpSession session = loggedIn();
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?logout"));
        assertThat(session.isInvalid()).isTrue();
    }
}
