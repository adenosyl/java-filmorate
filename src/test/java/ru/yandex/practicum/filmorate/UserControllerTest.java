package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUserAndAddRemoveFriendOneWay() throws Exception {
        String user1Json = """
    {
                  "email": "user1@example.com",
                  "login": "user1",
                  "name": "User One",
                  "birthday": "1990-01-01"
                }
    """;

        String user2Json = """ 
    {
                  "email": "user2@example.com",
                  "login": "user2",
                  "name": "User Two",
                  "birthday": "1991-02-02"
                }
    """;

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int userId1 = objectMapper.readTree(response1).get("id").asInt();
        int userId2 = objectMapper.readTree(response2).get("id").asInt();

        mockMvc.perform(put("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId2));

        mockMvc.perform(get("/users/" + userId2 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}