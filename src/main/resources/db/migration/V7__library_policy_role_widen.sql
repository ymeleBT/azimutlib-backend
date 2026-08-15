ALTER TABLE library_policy MODIFY COLUMN role ENUM('STUDENT','LECTURER','LIBRARIAN','ADMIN') NOT NULL;

INSERT INTO library_policy (role, loan_duration_days, max_concurrent_loans, max_renewals, fine_per_day_xaf) VALUES
    ('LIBRARIAN', 30, 15, 3, 0.00),
    ('ADMIN',     30, 15, 3, 0.00);
