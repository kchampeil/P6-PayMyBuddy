package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import com.paymybuddy.webapp.service.contract.IUserService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
    private IUserService userServiceMock;

    @MockBean
    private PasswordEncoder passwordEncoderMock;

    private static UserDTO userInDb;

    @BeforeAll
    private static void setUp() {
        userInDb = new UserDTO();
        userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
        userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
    }

    @WithMockUser
    @Test
    @DisplayName("WHEN asking for the home page wile logged in " +
            "THEN return status is ok and the expected view is the user homepage")
    void showHomePageTest_LoggedIn() throws Exception {
        //GIVEN
        when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

        //THEN
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNameConstants.USER_HOME));
    }


    @Test
    @DisplayName("WHEN asking for the home page wile not logged in " +
            "THEN return status is ok and the expected view is the PMB homepage")
    void showHomePageTest_NotLoggedIn() throws Exception {
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
