UPDATE users u
SET role = (
    SELECT 'ROLE_' || r.code
    FROM roles r
    WHERE r.id = u.role_id
)
WHERE u.role IS NULL;
