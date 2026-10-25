package com.mindae.departmentsvc;

import com.mindae.departmentsvc.entity.Department;
import com.mindae.departmentsvc.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:department-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class DepartmentPersistenceAndControllerTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistDepartment() {
        Department saved = departmentRepository.saveAndFlush(
                new Department(200L, "Engineering"));

        Department loaded = departmentRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(200L, loaded.getId());
        assertEquals("Engineering", loaded.getName());
    }

    @Test
    void shouldNotOverwriteExistingDepartmentDuringSeedInitialization() {
        departmentRepository.saveAndFlush(
                new Department(100L, "Existing department"));

        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            new ResourceDatabasePopulator(
                    new ClassPathResource("data.sql")).populate(connection);
            return null;
        });

        assertEquals("Existing department", jdbcTemplate.queryForObject(
                "SELECT name FROM department WHERE id = 100", String.class));
    }

    @Test
    void shouldKeepExistingDepartmentResponse() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Finance"));
    }

    @Test
    void shouldReturnPersistedDepartmentsFromCollectionEndpoint() throws Exception {
        departmentRepository.saveAndFlush(new Department(200L, "Engineering"));

        mockMvc.perform(get("/api/v1/departments/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].name").value("Finance"))
                .andExpect(jsonPath("$[1].id").value(200))
                .andExpect(jsonPath("$[1].name").value("Engineering"));

        assertTrue(departmentRepository.existsById(100L));
    }
}
