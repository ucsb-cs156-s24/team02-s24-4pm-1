package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemsController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemsControllerTests extends ControllerTestCase {
        
        @MockBean
        UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdiningcommonsmenuitems/all

        public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems/all"))
                            .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitems() throws Exception {

                //arrange

                UCSBDiningCommonsMenuItems item1 = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Carrillo")
                                .name("Ham & Pineapple")
                                .station("Pizza")
                                .build();
                
                UCSBDiningCommonsMenuItems item2 = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Carrillo")
                                .name("Wheat Penne Chard & Sun Dried")
                                .station("Pasta")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItems> expectedItems = new ArrayList<>();
                expectedItems.addAll(Arrays.asList(item1, item2));

                when(ucsbDiningCommonsMenuItemsRepository.findAll()).thenReturn(expectedItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems/all"))
                                .andExpect(status().isOk()).andReturn();
                
                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommonsmenuitems/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitems/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitems/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_menuitem() throws Exception {
                
                // arrange
                UCSBDiningCommonsMenuItems item1 = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Ortega")
                                .name("Sandwich")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.save(eq(item1))).thenReturn(item1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenuitems/post?diningCommonsCode=Ortega&name=Sandwich&station=Entrees")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(item1);
                String expectedJson = mapper.writeValueAsString(item1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsbdiningcommonsmenuitems?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                UCSBDiningCommonsMenuItems item = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Ortega")
                                .name("Classic Burger")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.of(item));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(item);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange
                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitems?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItems with id 7 not found", json.get("message"));
        }

        // Tests for DELETE /api/ucsbdiningcommons?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_menuitem() throws Exception {
                
                // arrange
                UCSBDiningCommonsMenuItems item = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Ortega")
                                .name("Steak Burrito")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.of(item));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitems?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(15L));
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_item_and_gets_right_error_message()
                        throws Exception {
                // arrange
                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitems?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(15L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 15 not found", json.get("message"));
        }

         // Tests for PUT /api/ucsbdiningcommons?...

         @WithMockUser(roles = { "ADMIN", "USER" })
         @Test
         public void admin_can_edit_an_existing_item() throws Exception {
                 // arrange
 
                 UCSBDiningCommonsMenuItems itemOrig = UCSBDiningCommonsMenuItems.builder()
                                 .diningCommonsCode("Portola")
                                 .name("Spicy Tuna Roll")
                                 .station("International")
                                 .build();
 
                 UCSBDiningCommonsMenuItems itemEdited = UCSBDiningCommonsMenuItems.builder()
                                 .diningCommonsCode("Ortega")
                                 .name("Chicken Caesar Salad")
                                 .station("Entrees")
                                 .build();
 
                 String requestBody = mapper.writeValueAsString(itemEdited);
 
                 when(ucsbDiningCommonsMenuItemsRepository.findById(eq(6L))).thenReturn(Optional.of(itemOrig));
 
                 // act
                 MvcResult response = mockMvc.perform(
                                 put("/api/ucsbdiningcommonsmenuitems?id=6")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .characterEncoding("utf-8")
                                                 .content(requestBody)
                                                 .with(csrf()))
                                 .andExpect(status().isOk()).andReturn();
 
                 // assert
                 verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(6L));
                 verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(itemEdited); // should be saved with updated info
                 String responseString = response.getResponse().getContentAsString();
                 assertEquals(requestBody, responseString);
         }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_item_that_does_not_exist() throws Exception {
                
                // arrange
                UCSBDiningCommonsMenuItems editedItem = UCSBDiningCommonsMenuItems.builder()
                                .diningCommonsCode("Ortega")
                                .name("Chicken Caesar Salad")
                                .station("Entrees")
                                .build();

                String requestBody = mapper.writeValueAsString(editedItem);

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitems?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(67L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 67 not found", json.get("message"));

        }
}