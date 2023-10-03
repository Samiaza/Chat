INSERT INTO  users (login, password) VALUES ('test_user', '$2a$10$tiqsohj6ivUheTtA5iyypO9OhepHmf4w7HLd4d6td3AneES6nfgB2');

INSERT INTO  chatrooms (name, creator) VALUES ('test_room', 1);

INSERT INTO  messages (author, room, text) VALUES (1, 1, 'test_message');