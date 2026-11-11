package com.monentreprise.rh.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Intercepteur qui expose l'URI de la requête courante dans le modèle Thymeleaf.
 * Cela évite d'utiliser #httpServletRequest (non disponible en tests @WebMvcTest).
 *
 * Usage dans les templates : ${requestUri}
 */
@Component
public class RequestUriInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        // On n'ajoute la variable que pour les vues (pas pour les redirections)
        if (modelAndView != null && !modelAndView.isReference()
                && !modelAndView.getViewName().startsWith("redirect:")) {
            modelAndView.addObject("requestUri", request.getRequestURI());
        }
    }
}
