package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.hibernate.graph.internal.parse.HEGLTokenTypes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {
    
    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;   

     // Tests for GET /api/HelpRequest/all
        
     @Test
     public void logged_out_users_cannot_get_all() throws Exception {
             mockMvc.perform(get("/api/helprequest/all"))
                             .andExpect(status().is(403)); // logged out users can't get all
     }

     @WithMockUser(roles = { "USER" })
     @Test
     public void logged_in_users_can_get_all() throws Exception {
             mockMvc.perform(get("/api/helprequest/all"))
                             .andExpect(status().is(200)); // logged
     }

     @WithMockUser(roles = { "USER" })
     @Test
     public void logged_in_user_can_get_all_helprequests() throws Exception {

             // arrange
             LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

             HelpRequest helpRequest1 = HelpRequest.builder()
                             .requesterEmail("aidenpham@ucsb.edu")
                             .teamId("s24-4pm-1")
                             .tableOrBreakoutRoom("1")
                             .requestTime(ldt1)
                             .explanation("swagger")
                             .solved(false)
                             .build();

             LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

             HelpRequest helpRequest2 = HelpRequest.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .teamId("s24-4pm-5")
                            .tableOrBreakoutRoom("13")
                            .requestTime(ldt2)
                            .explanation("merge conflict")
                            .solved(true)
                            .build();

             ArrayList<HelpRequest> expectedDates = new ArrayList<>();
             expectedDates.addAll(Arrays.asList(helpRequest1, helpRequest2));

             when(helpRequestRepository.findAll()).thenReturn(expectedDates);

             // act
             MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                             .andExpect(status().isOk()).andReturn();

             // assert

             verify(helpRequestRepository, times(1)).findAll();
             String expectedJson = mapper.writeValueAsString(expectedDates);
             String responseString = response.getResponse().getContentAsString();
             assertEquals(expectedJson, responseString);
     }

     // Tests for POST /api/helprequest/post...

     @Test
     public void logged_out_users_cannot_post() throws Exception {
             mockMvc.perform(post("/api/helprequest/post"))
                             .andExpect(status().is(403));
     }

     @WithMockUser(roles = { "USER" })
     @Test
     public void logged_in_regular_users_cannot_post() throws Exception {
             mockMvc.perform(post("/api/helprequest/post"))
                             .andExpect(status().is(403)); // only admins can post
     }

     @WithMockUser(roles = { "ADMIN", "USER" })
     @Test
     public void an_admin_user_can_post_a_new_helprequest() throws Exception {
             // arrange

             LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

             HelpRequest helpRequest1 = HelpRequest.builder()
                                    .requesterEmail("aidenpham@ucsb.edu")
                                    .teamId("4pm-1")
                                    .tableOrBreakoutRoom("13")
                                    .requestTime(ldt1)
                                    .explanation("swagger")
                                    .solved(false)
                                    .build();

             when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

             // act
             MvcResult response = mockMvc.perform(
                             post("/api/helprequest/post?requesterEmail=aidenpham@ucsb.edu&teamId=4pm-1&tableOrBreakoutRoom=13&requestTime=2022-01-03T00:00:00&explanation=swagger&solved=false")
                                             .with(csrf()))
                             .andExpect(status().isOk()).andReturn();

             // assert
             verify(helpRequestRepository, times(1)).save(helpRequest1);
             String expectedJson = mapper.writeValueAsString(helpRequest1);
             String responseString = response.getResponse().getContentAsString();
             assertEquals(expectedJson, responseString);
     }

     // Tests for GET /api/helprequest?id=...

     @Test
     public void logged_out_users_cannot_get_by_id() throws Exception {
             mockMvc.perform(get("/api/helprequest?id=639361"))
                             .andExpect(status().is(403)); // logged out users can't get by id
     }

     @WithMockUser(roles = { "USER" })
     @Test
     public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

             // arrange
             LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

             HelpRequest helpRequest = HelpRequest.builder()
                                    .requesterEmail("aidenpham@ucsb.edu")
                                    .teamId("s24-4pm-1")
                                    .tableOrBreakoutRoom("1")
                                    .requestTime(ldt)
                                    .explanation("swagger")
                                    .solved(false)
                                    .build();

             when(helpRequestRepository.findById(eq(639361L))).thenReturn(Optional.of(helpRequest));

             // act
             MvcResult response = mockMvc.perform(get("/api/helprequest?id=639361"))
                             .andExpect(status().isOk()).andReturn();

             // assert

             verify(helpRequestRepository, times(1)).findById(eq(639361L));
             String expectedJson = mapper.writeValueAsString(helpRequest);
             String responseString = response.getResponse().getContentAsString();
             assertEquals(expectedJson, responseString);
     }

     @WithMockUser(roles = { "USER" })
     @Test
     public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

             // arrange

             when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

             // act
             MvcResult response = mockMvc.perform(get("/api/helprequest?id=639361"))
                             .andExpect(status().isNotFound()).andReturn();

             // assert

             verify(helpRequestRepository, times(1)).findById(eq(639361L));
             Map<String, Object> json = responseToJson(response);
             assertEquals("EntityNotFoundException", json.get("type"));
             assertEquals("HelpRequest with id 639361 not found", json.get("message"));
     }

        // Tests for PUT /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                HelpRequest helpRequestOrig = HelpRequest.builder()
                                .requesterEmail("aidenpham@ucsb.edu")
                                .teamId("s24-4pm-1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .build();

                HelpRequest helpRequestEdited = HelpRequest.builder()
                                .requesterEmail("cgauchooo@ucsb.edu")
                                .teamId("s24-5pm-3")
                                .tableOrBreakoutRoom("11")
                                .requestTime(ldt2)
                                .explanation("Need more help with Swagger-ui")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(helpRequestEdited);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(helpRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                verify(helpRequestRepository, times(1)).save(helpRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_helprequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest editedHelpRequest = HelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s24-4pm-1")
                                .tableOrBreakoutRoom("1")
                                .requestTime(ldt1)
                                .explanation("Need help with Swagger-ui")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedHelpRequest);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 67 not found", json.get("message"));

        }
}
