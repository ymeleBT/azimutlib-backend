-- Core schema for the university library management system
-- Currency amounts are stored in XAF (Central African CFA franc), no decimals needed in practice
-- but kept as DECIMAL for flexibility.

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricule       VARCHAR(50)  NOT NULL UNIQUE,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) UNIQUE,
    phone           VARCHAR(30),
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('STUDENT','LECTURER','LIBRARIAN','ADMIN') NOT NULL,
    department      VARCHAR(100),
    status          ENUM('ACTIVE','SUSPENDED','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500)
) ENGINE=InnoDB;

CREATE TABLE books (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn                VARCHAR(20) UNIQUE,
    title               VARCHAR(255) NOT NULL,
    authors             VARCHAR(255) NOT NULL,
    publisher           VARCHAR(150),
    publication_year    INT,
    category_id         BIGINT,
    language            VARCHAR(10) NOT NULL DEFAULT 'EN',
    description         TEXT,
    cover_url           VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_category ON books(category_id);

CREATE TABLE book_copies (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id         BIGINT NOT NULL,
    inventory_code  VARCHAR(50) NOT NULL UNIQUE,
    status          ENUM('AVAILABLE','BORROWED','RESERVED','LOST','DAMAGED','IN_REPAIR') NOT NULL DEFAULT 'AVAILABLE',
    acquired_at     DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_copies_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_copies_book_status ON book_copies(book_id, status);

CREATE TABLE library_policy (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    role                    ENUM('STUDENT','LECTURER') NOT NULL UNIQUE,
    loan_duration_days      INT NOT NULL,
    max_concurrent_loans    INT NOT NULL,
    max_renewals            INT NOT NULL,
    fine_per_day_xaf        DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE loans (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_copy_id    BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    issued_by       BIGINT,
    borrow_date     DATE NOT NULL,
    due_date        DATE NOT NULL,
    return_date     DATE,
    status          ENUM('ACTIVE','RETURNED','OVERDUE','LOST') NOT NULL DEFAULT 'ACTIVE',
    renewal_count   INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_loans_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies(id),
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_loans_issuer FOREIGN KEY (issued_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_loans_user_status ON loans(user_id, status);
CREATE INDEX idx_loans_copy_status ON loans(book_copy_id, status);
CREATE INDEX idx_loans_due_date ON loans(due_date);

CREATE TABLE reservations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id         BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    status          ENUM('PENDING','FULFILLED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    reserved_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NULL DEFAULT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_reservations_book_status ON reservations(book_id, status);
CREATE INDEX idx_reservations_user ON reservations(user_id);

CREATE TABLE fines (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id         BIGINT NOT NULL,
    amount_xaf      DECIMAL(10,2) NOT NULL,
    reason          VARCHAR(255) NOT NULL,
    paid            BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at         TIMESTAMP NULL DEFAULT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fines_loan FOREIGN KEY (loan_id) REFERENCES loans(id)
) ENGINE=InnoDB;

CREATE INDEX idx_fines_loan ON fines(loan_id);

CREATE TABLE notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            ENUM('DUE_SOON','OVERDUE','RESERVATION_AVAILABLE','FINE_ISSUED') NOT NULL,
    channel         ENUM('EMAIL','SMS','IN_APP') NOT NULL,
    message         TEXT NOT NULL,
    status          ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMP NULL DEFAULT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_user ON notifications(user_id, status);
