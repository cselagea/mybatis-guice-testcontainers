CREATE TABLE book
(
    id INT IDENTITY PRIMARY KEY,
    title VARCHAR(100),
    pages INT
);

INSERT INTO book (title, pages)
VALUES ('A Short History of Nearly Everything', 544), ('1984', 328);