package com.lodwar.ticket_tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lodwar.ticket_tracker.repository.TicketHistoryRepository;
import com.lodwar.ticket_tracker.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack API tests over a real Postgres (Testcontainers) via MockMvc.
 * Covers the behaviours that carry the most risk: create, stats, the
 * state-machine 409, and filtered listing.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TicketApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TicketRepository tickets;
    @Autowired
    private TicketHistoryRepository history;

    @BeforeEach
    void clean() {
        // Each test starts from a known-empty state (shared container across suite).
        history.deleteAll();
        tickets.deleteAll();
    }

    @Test
    void createsTicketAsOpenAndWritesCreatedHistory() throws Exception {
        String body = """
                {"title":"Login returns 500","description":"after submit","priority":"high"}
                """;

        String response = mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("open")))
                .andExpect(jsonPath("$.priority", is("high")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // The create wrote exactly one 'created' history row for this ticket.
        mockMvc.perform(get("/api/v1/tickets/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].action", is("created")));
    }

    @Test
    void rejectsBlankTitleWith400AndFieldErrors() throws Exception {
        String body = """
                {"title":"  ","priority":"low"}
                """;

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("ValidationError")))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void statsReflectCreatedAndTransitionedTickets() throws Exception {
        String openId = create("Open one", "low");
        String toProgress = create("Progress one", "medium");
        patchStatus(toProgress, "in_progress");

        mockMvc.perform(get("/api/v1/tickets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.open", is(1)))
                .andExpect(jsonPath("$.in_progress", is(1)))
                .andExpect(jsonPath("$.closed", is(0)))
                .andExpect(jsonPath("$.low", is(1)))
                .andExpect(jsonPath("$.medium", is(1)));
    }

    @Test
    void rejectsIllegalStatusTransitionWith409() throws Exception {
        String id = create("Skip test", "medium");

        // open -> closed skips in_progress: illegal.
        mockMvc.perform(patch("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"closed\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("InvalidTransition")));
    }

    @Test
    void filtersListByStatus() throws Exception {
        create("Open A", "low");
        create("Open B", "high");
        String inProg = create("Progress", "medium");
        patchStatus(inProg, "in_progress");

        // Only the two open tickets come back when filtering status=open.
        mockMvc.perform(get("/api/v1/tickets").param("status", "open").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.content[0].status", is("open")));

        // Sanity: unfiltered sees all three.
        mockMvc.perform(get("/api/v1/tickets").param("size", "50"))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)));
    }

    // ---- helpers ----

    private String create(String title, String priority) throws Exception {
        String body = objectMapper.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("title", title);
                    put("priority", priority);
                }});
        String response = mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asText();
    }

    private void patchStatus(String id, String status) throws Exception {
        mockMvc.perform(patch("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }
}
