-- Teaching resources: quizzes, notes, documents and videos that lecturers
-- drop for students, organized by subject/category.

CREATE TABLE resource_categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    description     VARCHAR(500)
) ENGINE=InnoDB;

CREATE TABLE teaching_resources (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    type                ENUM('QUIZ','NOTES','DOCUMENT','VIDEO') NOT NULL,
    category_id         BIGINT NOT NULL,
    uploaded_by         BIGINT NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    original_filename   VARCHAR(255) NOT NULL,
    content_type        VARCHAR(100),
    file_size_bytes     BIGINT NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resources_category FOREIGN KEY (category_id) REFERENCES resource_categories(id),
    CONSTRAINT fk_resources_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_resources_category ON teaching_resources(category_id);
CREATE INDEX idx_resources_uploader ON teaching_resources(uploaded_by);
CREATE INDEX idx_resources_type ON teaching_resources(type);
