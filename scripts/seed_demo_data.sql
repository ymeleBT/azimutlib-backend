-- One-off demo data seed for local/dev use.
-- Adds ~200 users (STUDENT/LECTURER/LIBRARIAN/ADMIN), ~400 books with copies,
-- and a mix of active, overdue and returned loans.
-- All seeded users share the password: Password123!
-- Run with:
--   mysql -h localhost -P 3306 -u <user> -p<pass> library_db < scripts/seed_demo_data.sql
-- Safe to run only once against an (otherwise) empty catalog/user set — it does not
-- check for pre-existing seeded rows, so re-running it will add a second batch.

SET @demo_password_hash = '$2a$10$lKseBOW3vOPtOdnw6UOqhuZQxXXrZ9vXYaQ/Y3.nG3zjNGUJztW.e'; -- Password123!

-- ---------------------------------------------------------------------------
-- Reference name/word pools (temporary tables, dropped automatically at end of session)
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_first_names;
CREATE TEMPORARY TABLE tmp_first_names (n INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));
INSERT INTO tmp_first_names (name) VALUES
('Jean'),('Marie'),('Paul'),('Aminatou'),('Bello'),('Ngozi'),('Chantal'),('Serge'),
('Brice'),('Solange'),('Achille'),('Beatrice'),('Clovis'),('Delphine'),('Emmanuel'),
('Florence'),('Gervais'),('Huguette'),('Innocent'),('Josephine'),('Kevin'),('Larissa'),
('Michel'),('Nadege'),('Olivier'),('Patricia'),('Raissa'),('Samuel'),('Tatiana'),
('Ulrich'),('Vanessa'),('William'),('Yvonne'),('Zacharie'),('Ahmadou'),('Fatima'),
('Ibrahim'),('Aissatou'),('Moussa'),('Cecile');

DROP TEMPORARY TABLE IF EXISTS tmp_last_names;
CREATE TEMPORARY TABLE tmp_last_names (n INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));
INSERT INTO tmp_last_names (name) VALUES
('Mballa'),('Nkeng'),('Fotso'),('Talla'),('Ngoua'),('Etoundi'),('Onana'),('Abanda'),
('Ateba'),('Bassong'),('Ekani'),('Fouda'),('Guemgne'),('Hamadou'),('Kamdem'),
('Lontsi'),('Mvondo'),('Ngo'),('Owona'),('Petcheu'),('Simo'),('Tchoumi'),('Um'),
('Voundi'),('Wafo'),('Yombi'),('Zang'),('Nguema'),('Moukoko'),('Essomba');

DROP TEMPORARY TABLE IF EXISTS tmp_subjects;
CREATE TEMPORARY TABLE tmp_subjects (n INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(80));
INSERT INTO tmp_subjects (name) VALUES
('Algorithms'),('Databases'),('Computer Networks'),('Operating Systems'),
('Organic Chemistry'),('Cameroonian History'),('French Grammar'),('English Literature'),
('Constitutional Law'),('Criminal Law'),('Human Anatomy'),('Pharmacology'),
('Macroeconomics'),('Microeconomics'),('Business Management'),('Accounting'),
('Structural Engineering'),('Electrical Circuits'),('Renewable Energy'),
('African Philosophy'),('Political Science'),('Sociology'),('Agricultural Economics'),
('Soil Science'),('Statistics'),('Linear Algebra'),('Thermodynamics'),
('Quantum Physics'),('World Geography'),('International Relations'),
('Software Engineering'),('Artificial Intelligence'),('Public Health'),
('Nursing Practice'),('Civil Procedure'),('Corporate Finance'),('Marketing'),
('Educational Psychology'),('Linguistics'),('Environmental Science');

DROP TEMPORARY TABLE IF EXISTS tmp_templates;
CREATE TEMPORARY TABLE tmp_templates (n INT AUTO_INCREMENT PRIMARY KEY, tpl VARCHAR(40));
INSERT INTO tmp_templates (tpl) VALUES
('Introduction to'),('Principles of'),('Advanced'),('Fundamentals of'),
('Understanding'),('A Guide to'),('Handbook of'),('Topics in'),('Essentials of'),
('Concepts in');

DROP TEMPORARY TABLE IF EXISTS tmp_publishers;
CREATE TEMPORARY TABLE tmp_publishers (n INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100));
INSERT INTO tmp_publishers (name) VALUES
('Presses Universitaires de Yaounde'),('Editions CLE'),('Harmattan Cameroun'),
('Oxford University Press'),('Pearson Education'),('Springer'),('Dokolo Press'),
('Cameroon University Press');

-- ---------------------------------------------------------------------------
-- Categories
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO categories (name, description) VALUES
('Computer Science', 'Software, hardware and computing theory'),
('Mathematics', 'Pure and applied mathematics'),
('Physics', 'Classical and modern physics'),
('Law', 'Legal studies and jurisprudence'),
('Medicine and Health Sciences', 'Medical and health-related studies'),
('Economics and Management', 'Economics, business and management'),
('Literature and Languages', 'Fiction, poetry and linguistics'),
('History and Geography', 'Historical and geographical studies'),
('Civil Engineering', 'Structural and construction engineering'),
('Electrical Engineering', 'Electrical and electronic systems'),
('Philosophy', 'Philosophical studies'),
('Agricultural Sciences', 'Agronomy and agricultural economics');

-- ---------------------------------------------------------------------------
-- Users: 150 STUDENT, 35 LECTURER, 12 LIBRARIAN, 3 ADMIN = 200
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_user_seq;
CREATE TEMPORARY TABLE tmp_user_seq (n INT PRIMARY KEY, role VARCHAR(20));
INSERT INTO tmp_user_seq (n, role)
SELECT n,
    CASE
        WHEN n <= 150 THEN 'STUDENT'
        WHEN n <= 185 THEN 'LECTURER'
        WHEN n <= 197 THEN 'LIBRARIAN'
        ELSE 'ADMIN'
    END
FROM (
    WITH RECURSIVE seq AS (
        SELECT 1 AS n
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < 200
    )
    SELECT n FROM seq
) s;

INSERT INTO users (matricule, full_name, email, phone, password_hash, role, department, status, created_at, updated_at)
SELECT
    CASE role
        WHEN 'STUDENT'   THEN CONCAT('STU', LPAD(n, 5, '0'))
        WHEN 'LECTURER'  THEN CONCAT('LEC', LPAD(n - 150, 4, '0'))
        WHEN 'LIBRARIAN' THEN CONCAT('LIB', LPAD(n - 185, 4, '0'))
        ELSE CONCAT('ADM', LPAD(n - 197 + 1, 4, '0'))
    END AS matricule,
    CONCAT(first_name, ' ', last_name) AS full_name,
    LOWER(CONCAT(first_name, '.', last_name, n, '@library.local')) AS email,
    CONCAT('+237 6', LPAD(FLOOR(RAND() * 100000000), 8, '0')) AS phone,
    @demo_password_hash AS password_hash,
    role,
    department,
    CASE
        WHEN RAND() < 0.90 THEN 'ACTIVE'
        WHEN RAND() < 0.5 THEN 'SUSPENDED'
        ELSE 'INACTIVE'
    END AS status,
    NOW(), NOW()
FROM (
    SELECT
        s.n, s.role,
        (SELECT name FROM tmp_first_names ORDER BY RAND() LIMIT 1) AS first_name,
        (SELECT name FROM tmp_last_names ORDER BY RAND() LIMIT 1) AS last_name,
        (SELECT name FROM (
            SELECT 'Computer Science' AS name UNION ALL
            SELECT 'Law' UNION ALL
            SELECT 'Medicine' UNION ALL
            SELECT 'Economics and Management' UNION ALL
            SELECT 'Literature and Languages' UNION ALL
            SELECT 'Civil Engineering' UNION ALL
            SELECT 'Electrical Engineering' UNION ALL
            SELECT 'History and Geography'
        ) d ORDER BY RAND() LIMIT 1) AS department
    FROM tmp_user_seq s
) picked;

-- ---------------------------------------------------------------------------
-- Books: 400 new titles spread across the new categories
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_book_seq;
CREATE TEMPORARY TABLE tmp_book_seq (n INT PRIMARY KEY);
INSERT INTO tmp_book_seq (n)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 400
)
SELECT n FROM seq;

INSERT INTO books (isbn, title, authors, publisher, publication_year, category_id, language, description, created_at, updated_at)
SELECT
    CONCAT('978', LPAD(6000000000 + n, 10, '0')) AS isbn,
    CONCAT(tpl, ' ', subj) AS title,
    CONCAT(first_name, ' ', last_name) AS authors,
    publisher,
    1980 + FLOOR(RAND() * 46) AS publication_year,
    category_id,
    CASE WHEN RAND() < 0.7 THEN 'EN' ELSE 'FR' END AS language,
    NULL AS description,
    NOW(), NOW()
FROM (
    SELECT
        s.n,
        (SELECT tpl FROM tmp_templates ORDER BY RAND() LIMIT 1) AS tpl,
        (SELECT name FROM tmp_subjects ORDER BY RAND() LIMIT 1) AS subj,
        (SELECT name FROM tmp_first_names ORDER BY RAND() LIMIT 1) AS first_name,
        (SELECT name FROM tmp_last_names ORDER BY RAND() LIMIT 1) AS last_name,
        (SELECT name FROM tmp_publishers ORDER BY RAND() LIMIT 1) AS publisher,
        (SELECT id FROM categories ORDER BY RAND() LIMIT 1) AS category_id
    FROM tmp_book_seq s
) picked;

-- ---------------------------------------------------------------------------
-- Book copies: 1-3 copies per new book
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_new_books;
CREATE TEMPORARY TABLE tmp_new_books (book_id BIGINT, copy_count INT);
INSERT INTO tmp_new_books (book_id, copy_count)
SELECT id,
    CASE
        WHEN RAND() < 0.4 THEN 1
        WHEN RAND() < 0.8 THEN 2
        ELSE 3
    END
FROM books
WHERE isbn LIKE '978600%';

DROP TEMPORARY TABLE IF EXISTS tmp_copy_idx;
CREATE TEMPORARY TABLE tmp_copy_idx (n INT PRIMARY KEY);
INSERT INTO tmp_copy_idx (n)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 3
)
SELECT n FROM seq;

INSERT INTO book_copies (book_id, inventory_code, status, acquired_at, created_at, updated_at)
SELECT
    b.book_id,
    CONCAT(b.book_id, '-', c.n),
    'AVAILABLE',
    DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 2000) DAY),
    NOW(), NOW()
FROM tmp_new_books b
JOIN tmp_copy_idx c ON c.n <= b.copy_count;

-- ---------------------------------------------------------------------------
-- Loans: 150 loans (60 returned, 50 active, 40 overdue) over distinct copies
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_loan_copies;
CREATE TEMPORARY TABLE tmp_loan_copies (rn INT PRIMARY KEY, book_copy_id BIGINT);
INSERT INTO tmp_loan_copies (rn, book_copy_id)
SELECT ROW_NUMBER() OVER (ORDER BY id), id
FROM (SELECT id FROM book_copies ORDER BY RAND() LIMIT 150) picked;

DROP TEMPORARY TABLE IF EXISTS tmp_loan_borrowers;
CREATE TEMPORARY TABLE tmp_loan_borrowers (rn INT PRIMARY KEY, user_id BIGINT, role VARCHAR(20));
INSERT INTO tmp_loan_borrowers (rn, user_id, role)
SELECT ROW_NUMBER() OVER (ORDER BY id), id, role
FROM (
    SELECT id, role FROM users WHERE role IN ('STUDENT', 'LECTURER') ORDER BY RAND() LIMIT 150
) picked;

-- Guard: only proceed with as many loans as we actually have distinct copies/borrowers for
SET @loan_count = LEAST(
    (SELECT COUNT(*) FROM tmp_loan_copies),
    (SELECT COUNT(*) FROM tmp_loan_borrowers)
);

DROP TEMPORARY TABLE IF EXISTS tmp_loans_plan;
CREATE TEMPORARY TABLE tmp_loans_plan AS
SELECT
    lc.rn,
    lc.book_copy_id,
    lb.user_id AS borrower_id,
    lb.role AS borrower_role,
    (SELECT id FROM users WHERE role IN ('LIBRARIAN', 'ADMIN') ORDER BY RAND() LIMIT 1) AS issuer_id,
    CASE WHEN lb.role = 'LECTURER' THEN 30 ELSE 14 END AS loan_duration,
    CASE
        WHEN lc.rn <= 60 THEN 'RETURNED'
        WHEN lc.rn <= 110 THEN 'ACTIVE'
        ELSE 'OVERDUE'
    END AS status
FROM tmp_loan_copies lc
JOIN tmp_loan_borrowers lb ON lb.rn = lc.rn
WHERE lc.rn <= @loan_count;

DROP TEMPORARY TABLE IF EXISTS tmp_loans_dates;
CREATE TEMPORARY TABLE tmp_loans_dates AS
SELECT
    book_copy_id, borrower_id, issuer_id, loan_duration, status,
    CASE
        WHEN status = 'RETURNED' THEN DATE_SUB(CURDATE(), INTERVAL (30 + FLOOR(RAND() * 300)) DAY)
        WHEN status = 'ACTIVE' THEN DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 10) DAY)
        ELSE DATE_SUB(CURDATE(), INTERVAL (loan_duration + 5 + FLOOR(RAND() * 30)) DAY)
    END AS borrow_date
FROM tmp_loans_plan;

INSERT INTO loans (book_copy_id, user_id, issued_by, borrow_date, due_date, return_date, status, renewal_count, created_at, updated_at)
SELECT
    book_copy_id,
    borrower_id,
    issuer_id,
    borrow_date,
    DATE_ADD(borrow_date, INTERVAL loan_duration DAY),
    CASE
        WHEN status = 'RETURNED' THEN DATE_ADD(borrow_date, INTERVAL (loan_duration + FLOOR(RAND() * 10) - 5) DAY)
        ELSE NULL
    END AS return_date,
    status,
    FLOOR(RAND() * 2) AS renewal_count,
    NOW(), NOW()
FROM tmp_loans_dates;

-- Mark copies currently on loan (ACTIVE/OVERDUE) as BORROWED
UPDATE book_copies bc
JOIN tmp_loans_plan p ON p.book_copy_id = bc.id AND p.status IN ('ACTIVE', 'OVERDUE')
SET bc.status = 'BORROWED';

-- ---------------------------------------------------------------------------
-- Cleanup temp tables
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_first_names, tmp_last_names, tmp_subjects, tmp_templates,
    tmp_publishers, tmp_user_seq, tmp_book_seq, tmp_new_books, tmp_copy_idx,
    tmp_loan_copies, tmp_loan_borrowers, tmp_loans_plan, tmp_loans_dates;
