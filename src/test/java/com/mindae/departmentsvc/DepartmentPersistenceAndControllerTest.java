package com.mindae.departmentsvc;

import com.mindae.departmentsvc.entity.Department;
import com.mindae.departmentsvc.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:department-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("local")
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
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:department-seed-test;DB_CLOSE_DELAY=-1",
                "sa",
                "");
        dataSource.setDriverClassName("org.h2.Driver");
        JdbcTemplate seedJdbcTemplate = new JdbcTemplate(dataSource);
        seedJdbcTemplate.execute(
                "CREATE TABLE department (id BIGINT PRIMARY KEY, name VARCHAR(255))");
        seedJdbcTemplate.update(
                "INSERT INTO department (id, name) VALUES (100, 'Existing department')");

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("data-local.sql"));
        populator.execute(dataSource);
        populator.execute(dataSource);

        assertEquals("Existing department", seedJdbcTemplate.queryForObject(
                "SELECT name FROM department WHERE id = 100", String.class));
        assertEquals(4, seedJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM department", Integer.class));
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
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].name").value("Finance"))
                .andExpect(jsonPath("$[1].name").value("Engineering"))
                .andExpect(jsonPath("$[2].name").value("Human Resources"))
                .andExpect(jsonPath("$[3].id").value(200))
                .andExpect(jsonPath("$[3].name").value("Engineering"));

        assertTrue(departmentRepository.existsById(100L));
    }

    @Test
    void shouldCreateDepartmentWithGeneratedIdAndTrimmedName() throws Exception {
        mockMvc.perform(post("/api/v1/departments")
                        .contentType("application/json")
                        .content("{\"name\":\"  Engineering  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(103))
                .andExpect(jsonPath("$.name").value("Engineering"));

        assertEquals("Engineering", departmentRepository.findById(103L)
                .orElseThrow().getName());
    }

    @Test
    void shouldGenerateIdAboveExistingIdsWhenAllocatorIsStale() throws Exception {
        departmentRepository.saveAndFlush(new Department(700L, "Existing"));
        jdbcTemplate.update(
                "UPDATE department_id_allocator SET next_id = 101 WHERE id = 1");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType("application/json")
                        .content("{\"name\":\"Engineering\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(701));

        assertTrue(departmentRepository.existsById(700L));
        assertTrue(departmentRepository.existsById(701L));
    }

    @Test
    void shouldUpdateDepartmentAndTrimName() throws Exception {
        departmentRepository.saveAndFlush(new Department(200L, "Engineering"));

        mockMvc.perform(put("/api/v1/departments/200")
                        .contentType("application/json")
                        .content("{\"name\":\"  Research  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.name").value("Research"));

        assertEquals("Research", departmentRepository.findById(200L)
                .orElseThrow().getName());
    }

    @Test
    void shouldDeleteDepartment() throws Exception {
        departmentRepository.saveAndFlush(new Department(200L, "Engineering"));

        mockMvc.perform(delete("/api/v1/departments/200"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertFalse(departmentRepository.existsById(200L));
    }

    @Test
    void shouldRejectBlankAndShortDepartmentNames() throws Exception {
        for (String name : new String[]{"   ", "ab", " a "}) {
            mockMvc.perform(post("/api/v1/departments")
                            .contentType("application/json")
                            .content("{\"name\":\"" + name + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void shouldReturnNotFoundForMissingUpdateAndDeleteTargets() throws Exception {
        mockMvc.perform(put("/api/v1/departments/999")
                        .contentType("application/json")
                        .content("{\"name\":\"Engineering\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/departments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldProtectReservedDepartmentFromUpdateAndDeletion() throws Exception {
        mockMvc.perform(put("/api/v1/departments/100")
                        .contentType("application/json")
                        .content("{\"name\":\"Operations\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/departments/100"))
                .andExpect(status().isConflict());

        assertEquals("Finance", departmentRepository.findById(100L)
                .orElseThrow().getName());
    }
}
