package exercise.controller;

import org.junit.jupiter.api.Test;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import org.instancio.Instancio;
import org.instancio.Select;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import exercise.repository.TaskRepository;
import exercise.model.Task;
import org.springframework.util.Assert;

// BEGIN
@SpringBootTest
@AutoConfigureMockMvc
// END
class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    public void testWelcomePage() throws Exception {
        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Welcome to Spring!");
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }


    private Task generateTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getTitle), () -> faker.lorem().word())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().paragraph())
                .create();
    }

    // BEGIN

    @Test
    public void testShow() throws Exception {
        Task task = generateTask();
        taskRepository.save(task);
        var result = mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andReturn();

        assertThatJson(result.getResponse().getContentAsString()).and(
                id -> id.node("id").isEqualTo(1),
                title -> title.node("title").isEqualTo(task.getTitle()),
                description -> description.node("description").isEqualTo(task.getDescription()));
    }

    @Test
    public void testCreate() throws Exception {
        var data = new HashMap<>();
        data.put("title", faker.lorem().sentence());
        data.put("description", faker.lorem().paragraph());

        String sentJson = om.writeValueAsString(data);

        var request = post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sentJson);

        var result = mockMvc.perform(request).andExpect(status().isCreated()).andReturn();

        var recievedTask = om.readValue(result.getResponse().getContentAsString(), Task.class);

        Task taskFromDb = taskRepository.findById(recievedTask.getId()).get();

        assertThat(taskFromDb.getId()).isEqualTo(recievedTask.getId());
        assertThat(taskFromDb.getTitle()).isEqualTo(recievedTask.getTitle());
        assertThat(taskFromDb.getDescription()).isEqualTo(recievedTask.getDescription());
        assertThat(taskFromDb.getCreatedAt()).isEqualTo(recievedTask.getCreatedAt());
        assertThat(taskFromDb.getUpdatedAt()).isEqualTo(recievedTask.getUpdatedAt());
        assertThatJson(sentJson).and(
                title -> title.node("title").isEqualTo(taskFromDb.getTitle()),
                descr -> descr.node("description").isEqualTo(taskFromDb.getDescription())
        );

    }

    @Test
    public void testUpdate() throws Exception {
        var task = generateTask();
        taskRepository.save(task);

        var data = new HashMap<>();
        data.put("title", faker.lorem().sentence());
        data.put("description", faker.lorem().paragraph());

        var request = put("/tasks/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        task = taskRepository.findById(task.getId()).get();
        assertThat(task.getTitle()).isEqualTo(data.get("title"));
        assertThat(task.getDescription()).isEqualTo(data.get("description"));
    }

    @Test
    public void testDelete() throws Exception {
        Task task = generateTask();
        taskRepository.save(task);

        var request = delete("/tasks/" + task.getId());
        mockMvc.perform(request).andExpect(status().isOk());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
    // END
}
