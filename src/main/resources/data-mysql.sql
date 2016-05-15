
INSERT INTO users (username, password, enabled)
VALUES ('mkyong', '123456', TRUE);
INSERT INTO users (username, password, enabled)
VALUES ('alex', '123456', TRUE);

INSERT INTO user_roles (username, role)
VALUES ('mkyong', 'ROLE_USER');
INSERT INTO user_roles (username, role)
VALUES ('mkyong', 'ROLE_ADMIN');
INSERT INTO user_roles (username, role)
VALUES ('alex', 'ROLE_USER');