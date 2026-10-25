package com.mindae.departmentsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:department-context-test;DB_CLOSE_DELAY=-1"
})
class DepartmentSvcApplicationTests {

    @Test
    void contextLoads() {
    }

}
