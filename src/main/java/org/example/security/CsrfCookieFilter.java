package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Гарантирует, что CSRF-токен существует уже на первом GET-запросе.

 * CsrfFilter токен на «безопасных» методах (GET) не генерирует — только когда
 * его разрешают при POST. Без этого фильтра страница входа не получила бы
 * cookie XSRF-TOKEN, и первый POST /login упирался бы в MissingCsrfTokenException.

 * Ещё одна роль: Security 7 ротирует CSRF-токен при каждом успешном логине
 * (CsrfAuthenticationStrategy очищает старую cookie в ответе POST /login).
 * Без этого фильтра после редиректа на / фронтенду не откуда было бы взять
 * новый токен; фильтр выпускает свежую cookie на каждом следующем GET.

 * Хранилище — CookieCsrfTokenRepository, оно само кладёт токен в cookie
 * (HttpOnly=false), которую фронтенд читает и отправляет в заголовке X-XSRF-TOKEN.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository repository;

    public CsrfCookieFilter(CsrfTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = this.repository.loadToken(request);
        if (token == null) {
            // saveToken у CookieCsrfTokenRepository и пишет cookie — дублировать её не нужно
            this.repository.saveToken(this.repository.generateToken(request), request, response);
        }
        chain.doFilter(request, response);
    }
}
