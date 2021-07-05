package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PMBUserDetailsService pmbUserDetailsServiceMock;

    @MockBean
    private PasswordEncoder passwordEncoderMock;

    @Test
    @DisplayName("WHEN asking for the home page" +
            " THEN return status is ok and the expected view is the homepage")
    void showHomePageTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNameConstants.HOME));
    }


    @Test
    @DisplayName("WHEN asking for the under construction page" +
            " THEN return status is ok and the expected view " +
            "is the under construction page")
    void showUnderConstructionPageTest() throws Exception {
        mockMvc.perform(get("/underConstruction"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNameConstants.UNDER_CONSTRUCTION));
    }

}
