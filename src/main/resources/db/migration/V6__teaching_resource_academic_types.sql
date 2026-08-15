ALTER TABLE teaching_resources MODIFY COLUMN type ENUM('QUIZ','NOTES','DOCUMENT','VIDEO','THESIS','EXAM_PAPER') NOT NULL;
ALTER TABLE teaching_resources ADD COLUMN author VARCHAR(255) NULL AFTER description;
ALTER TABLE teaching_resources ADD COLUMN academic_year VARCHAR(20) NULL AFTER author;
