INSERT INTO department (id, name)
SELECT 100, 'Finance'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE id = 100
)
AND NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Finance'
);

INSERT INTO department (id, name)
SELECT (SELECT COALESCE(MAX(id), 0) + 1 FROM department), 'Finance'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Finance'
);

INSERT INTO department (id, name)
SELECT (SELECT COALESCE(MAX(id), 0) + 1 FROM department), 'Engineering'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Engineering'
);

INSERT INTO department (id, name)
SELECT (SELECT COALESCE(MAX(id), 0) + 1 FROM department), 'Human Resources'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Human Resources'
);

CREATE TABLE IF NOT EXISTS department_id_allocator (
    id INTEGER PRIMARY KEY,
    next_id BIGINT NOT NULL
);

MERGE INTO department_id_allocator (id, next_id)
KEY (id)
SELECT 1, COALESCE(MAX(id), 99) + 1
FROM department;
