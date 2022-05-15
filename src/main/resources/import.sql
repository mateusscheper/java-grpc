INSERT INTO usuario (id_usuario, cpf, email, nome, senha) VALUES (5000, '36123347004', 'admin@admin.com', 'Admin', '$2a$10$BcAP/rQyVZ3cr1UvCWI1suqJiCGN0wP39eQUCoYc0J4HMXOh6Nmfm');
INSERT INTO role (id_role, nome) VALUES (5000, 'ADMIN'), (5001, 'USUARIO');
INSERT INTO usuario_roles (usuario_id_usuario, roles_id_role) VALUES (5000, 5000);
