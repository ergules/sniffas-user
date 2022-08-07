INSERT INTO users
    ( firstname, lastname, email, deleted, version, firebase_uid, mobile_phone, profile_photo, `role`, store_link, store_name, username, iban)
VALUES ('User', 'One', 'u1@mail.com', 0, 0, 'uid1', NULL, NULL, 'USER', NULL, NULL, 'uu1', NULL),
       ('User', 'Two', 'u2@mail.com', 0, 0, 'uid2', NULL, NULL, 'USER', NULL, NULL, 'uu2', NULL),
       ('User', 'Three', 'u3@mail.com', 0, 0, 'uid3', NULL, NULL, 'USER', NULL, NULL, 'uu3', NULL),
       ('User', 'Four', 'u4@mail.com', 1, 0, 'uid4', NULL, NULL, 'USER', NULL, NULL, 'uu4', NULL),
       ('User', 'Five', 'u5@mail.com', 1, 0, 'uid5', NULL, NULL, 'USER', NULL, NULL, 'uu5', NULL),
       ('Company', 'One', 'c1@mail.com', 0, 0, 'uid6', NULL, NULL, 'COMPANY', NULL, 'The Company', 'c1', 'DE9012341234123423'),
       ('Seller', 'One', 's1@mail.com', 0, 0, 'uid7', NULL, NULL, 'SELLER', NULL, 'OneStore', 'ss1', 'DE9012341234123412'),
       ('Seller', 'Two', 's2@mail.com', 0, 0, 'uid8', NULL, NULL, 'SELLER', '', 'TwinStore', 'ss2', 'TR9012341234123412'),
       ('Seller', 'Three', 's3@mail.com', 1, 0, 'uid9', NULL, NULL, 'SELLER', '', 'ThirdStore', 'ss3', 'GB9012341234123412'),
       ('Adam', 'Mann', 'admin@sniffas.com', 0, 0, 'uid', NULL, NULL, 'ADMIN', NULL, NULL, 'admin', NULL);


INSERT INTO addresses
(user_id, title, zip_code, deleted, version, address_string)
VALUES (1, 'us1 adr', 'UZ01', 0, 0, 'first address'),
       (2, 'us2 adr', 'UZ02', 0, 0, 'second address'),
       (3, 'us3 adr', 'UZ03', 0, 0, 'third address'),
       (6, 'c1 usr adr', 'UZ06', 0, 0, 'com user address'),
       (null, 'com adr', 'C00', 0, 0, 'com address'),
       (8, 's1 usr adr', 'UZ07', 0, 0, 'seller1 address'),
       (9, 's1 usr adr', 'UZ08', 0, 0, 'seller2 address');

INSERT INTO companies
(user_id, company_name, company_executive_first_name, deleted, version, address_id)
VALUES (6, 'The Company', 'Adam', 0, 0, 5);