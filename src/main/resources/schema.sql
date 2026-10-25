CREATE TABLE IF NOT EXISTS department_id_allocator (
    id INTEGER PRIMARY KEY,
    next_id BIGINT NOT NULL
);

INSERT INTO department_id_allocator (id, next_id)
SELECT 1, (SELECT COALESCE(MAX(id), 99) + 1 FROM department)
WHERE NOT EXISTS (
    SELECT 1 FROM department_id_allocator WHERE id = 1
);
