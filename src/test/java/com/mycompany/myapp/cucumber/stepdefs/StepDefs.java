package com.mycompany.myapp.cucumber.stepdefs;

import com.mycompany.myapp.security.AuthoritiesConstants;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;

public abstract class StepDefs {

    protected ResultActions actions;

    protected void setupDefaultAuthentication() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));

        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
            "user",
            "",
            true,
            true,
            true,
            true,
            grantedAuthorities
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            principal.getPassword(),
            principal.getAuthorities()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
