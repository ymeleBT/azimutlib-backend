INSERT INTO library_policy (role, loan_duration_days, max_concurrent_loans, max_renewals, fine_per_day_xaf) VALUES
    ('STUDENT',  14, 3, 1, 100.00),
    ('LECTURER', 30, 8, 2, 100.00);

-- The default ADMIN account is created at application startup by
-- cm.univ.library.config.AdminSeeder (so its password is hashed with the
-- application's actual PasswordEncoder instead of a hardcoded hash here).
