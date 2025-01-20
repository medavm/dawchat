
-- INSERT INTO dbo.users (username, secret, createdat)
-- VALUES ('startuser', 'startuserhash', 1732130582);

INSERT INTO dbo.users (username, secret, createdat)
VALUES ('startuser', '$2a$10$77sgpZ8eMHilbCCi2oNnOOxXH/Fhnmja3aZAlRGq.0KvyWtg1Z.6a', 1732130582);

INSERT INTO dbo.userinvites (token, createdby, timestamp)
VALUES ('startuserinvitetoken', 1, 1732130682);