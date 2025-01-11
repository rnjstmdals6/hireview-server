-- ===================================
-- Users 테이블 생성 (소프트 딜리트 포함)
-- ===================================
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL,
                       name VARCHAR(100),
                       picture VARCHAR(255),
                       job VARCHAR(100),
                       token INT DEFAULT 0,
                       last_attendance_date DATE,
                       deleted BOOLEAN DEFAULT FALSE,
                       deleted_at TIMESTAMP NULL
);

-- ===================================
-- Categories 테이블 생성
-- ===================================
CREATE TABLE categories (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL
);

-- ===================================
-- Posts 테이블 생성 (소프트 딜리트 포함)
-- ===================================
CREATE TABLE posts (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(255) NOT NULL,
                       description TEXT NOT NULL,
                       category VARCHAR(100),
                       views BIGINT DEFAULT 0,
                       user_id BIGINT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       deleted BOOLEAN DEFAULT FALSE,
                       deleted_at TIMESTAMP NULL,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ===================================
-- Jobs 테이블 생성
-- ===================================
CREATE TABLE jobs (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      category_id BIGINT,
                      name VARCHAR(100) NOT NULL,
                      FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ===================================
-- Questions 테이블 생성
-- ===================================
CREATE TABLE questions (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           content TEXT NOT NULL,
                           priority INT,
                           difficulty INT,
                           job_id BIGINT,
                           tags VARCHAR(255),
                           FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL
);

-- ===================================
-- Feedbacks 테이블 생성
-- ===================================
CREATE TABLE feedbacks (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           score DOUBLE NOT NULL,
                           feedback TEXT NOT NULL,
                           answer TEXT NOT NULL,
                           question_id BIGINT,
                           user_id BIGINT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE SET NULL,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ===================================
-- Comments 테이블 생성 (소프트 딜리트 포함)
-- ===================================
CREATE TABLE comments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          description TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          user_id BIGINT,
                          post_id BIGINT,
                          deleted BOOLEAN DEFAULT FALSE,
                          deleted_at TIMESTAMP NULL,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                          FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL
);

-- ===================================
-- Likes 테이블 생성
-- ===================================
CREATE TABLE likes (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       post_id BIGINT,
                       user_id BIGINT,
                       liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ===================================
-- FollowUpQuestions 테이블 생성
-- ===================================
CREATE TABLE follow_up_questions (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     question_id BIGINT NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     content TEXT NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_suggestion (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     evaluation TEXT NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     content TEXT NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);