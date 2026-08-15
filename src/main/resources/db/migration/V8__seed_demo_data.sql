-- Demo/test data for the Railway deployment smoke test.
-- Intended for a fresh database only (explicit PKs assume empty tables).
--
-- The ADMIN001/ChangeMe123! account below duplicates what
-- cm.univ.library.config.AdminSeeder already creates on first boot when no
-- ADMIN exists -- it's inserted here too so its id is available for the FK
-- references below (migrations run before CommandLineRunners, so the
-- seeder's row wouldn't exist yet at this point). AdminSeeder's own guard
-- (countByRole(ADMIN) > 0) will simply no-op once it sees this row.

INSERT INTO users (id, matricule, full_name, email, phone, password_hash, role, department, status) VALUES
    (1, 'ADMIN001', 'System Administrator', 'admin@library.local', '+237600000001', '$2a$10$SW3WWJLQUGapQKM4qXAxzO5UQD5rXMivJWw9TNRcGOmSH0OyEzBQa', 'ADMIN', 'Administration', 'ACTIVE'),
    (2, 'LIB001',   'Aminatou Bello',        'aminatou.bello@library.local', '+237600000002', '$2a$10$SW3WWJLQUGapQKM4qXAxzO5UQD5rXMivJWw9TNRcGOmSH0OyEzBQa', 'LIBRARIAN', 'Library', 'ACTIVE'),
    (3, 'LEC001',   'Dr. Paul Ngassa',       'paul.ngassa@library.local',    '+237600000003', '$2a$10$SW3WWJLQUGapQKM4qXAxzO5UQD5rXMivJWw9TNRcGOmSH0OyEzBQa', 'LECTURER',  'Computer Science', 'ACTIVE'),
    (4, 'STU001',   'Chantal Mballa',        'chantal.mballa@student.local', '+237600000004', '$2a$10$SW3WWJLQUGapQKM4qXAxzO5UQD5rXMivJWw9TNRcGOmSH0OyEzBQa', 'STUDENT',   'Computer Science', 'ACTIVE'),
    (5, 'STU002',   'Herve Fotso',           'herve.fotso@student.local',    '+237600000005', '$2a$10$SW3WWJLQUGapQKM4qXAxzO5UQD5rXMivJWw9TNRcGOmSH0OyEzBQa', 'STUDENT',   'Mathematics', 'ACTIVE');
-- All seed accounts share the ADMIN001 password (ChangeMe123!) for convenience during testing.

INSERT INTO categories (id, name, description) VALUES
    (1, 'Computer Science', 'Programming, algorithms and software engineering'),
    (2, 'Mathematics', 'Pure and applied mathematics'),
    (3, 'Literature', 'Fiction and African literature');

INSERT INTO books (id, isbn, title, authors, publisher, publication_year, category_id, language, description) VALUES
    (1, '9780262046305', 'Introduction to Algorithms', 'Cormen, Leiserson, Rivest, Stein', 'MIT Press', 2022, 1, 'EN', 'Comprehensive introduction to algorithms and data structures.'),
    (2, '9780471000075', 'Calculus I', 'James Stewart', 'Cengage Learning', 2020, 2, 'EN', 'Single-variable calculus for undergraduates.'),
    (3, '9780435905255', 'Things Fall Apart', 'Chinua Achebe', 'Heinemann', 1958, 3, 'EN', 'Classic novel of pre-colonial African life.'),
    (4, '9780131873254', 'Data Structures in Java', 'Robert Lafore', 'Sams Publishing', 2018, 1, 'EN', 'Practical data structures with Java examples.');

INSERT INTO book_copies (id, book_id, inventory_code, status, acquired_at) VALUES
    (1, 1, '1-1', 'BORROWED',  '2024-09-01'),
    (2, 1, '1-2', 'AVAILABLE', '2024-09-01'),
    (3, 2, '2-1', 'AVAILABLE', '2024-09-01'),
    (4, 3, '3-1', 'BORROWED',  '2023-10-15'),
    (5, 3, '3-2', 'AVAILABLE', '2023-10-15'),
    (6, 4, '4-1', 'AVAILABLE', '2024-01-20');

INSERT INTO loans (id, book_copy_id, user_id, issued_by, borrow_date, due_date, return_date, status, renewal_count) VALUES
    (1, 1, 4, 2, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), NULL, 'ACTIVE',  0),
    (2, 4, 5, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 'OVERDUE', 0);

INSERT INTO fines (id, loan_id, type, user_id, amount_xaf, reason, paid) VALUES
    (1, 2, 'LATE', 5, 600.00, 'Late return - 6 days overdue', FALSE);

INSERT INTO reservations (id, book_id, user_id, status, expires_at) VALUES
    (1, 1, 5, 'PENDING', DATE_ADD(NOW(), INTERVAL 3 DAY));

INSERT INTO notifications (id, user_id, type, channel, message, status, sent_at) VALUES
    (1, 5, 'OVERDUE',  'IN_APP', 'Your loan for "Things Fall Apart" is overdue. Please return it as soon as possible.', 'SENT', NOW()),
    (2, 4, 'DUE_SOON', 'EMAIL',  'Your loan for "Introduction to Algorithms" is due in 4 days.', 'PENDING', NULL);

INSERT INTO resource_categories (id, name, description) VALUES
    (1, 'Computer Science', 'CS course material, past papers and theses'),
    (2, 'Mathematics', 'Mathematics course material, past papers and theses');

-- Note: these rows reference files that don't exist on disk (no binary was
-- actually uploaded) -- listing/detail views will work, download will 404
-- until a real file is uploaded through the app to replace them.
INSERT INTO teaching_resources (id, title, description, author, academic_year, type, category_id, uploaded_by, file_path, original_filename, content_type, file_size_bytes) VALUES
    (1, 'Data Structures Practice Quiz', 'Self-assessment quiz on trees and graphs.', NULL, NULL, 'QUIZ', 1, 3, 'seed/placeholder-quiz.pdf', 'ds-quiz.pdf', 'application/pdf', 102400),
    (2, 'Analyse Numerique - Memoire de Licence', 'Undergraduate thesis on numerical analysis methods.', 'Jean Paul Ateba', '2023/2024', 'THESIS', 2, 2, 'seed/placeholder-thesis.pdf', 'memoire-ateba.pdf', 'application/pdf', 2458000),
    (3, 'Algorithmique - Epreuve Session Normale', 'Past exam paper, normal session.', NULL, '2024/2025', 'EXAM_PAPER', 1, 2, 'seed/placeholder-exam.pdf', 'epreuve-algo-2024.pdf', 'application/pdf', 512000);
