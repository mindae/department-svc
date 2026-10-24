INSERT INTO department (id, name)
SELECT 100, 'Finance'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE id = 100
);
