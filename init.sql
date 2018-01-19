CREATE DATABASE active;

USE active;

CREATE TABLE items (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  comment TEXT,
  changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID)
);

INSERT INTO items (id, name, comment) VALUES
  (1, 'sample', 'sample\'s comment'),
  (2, 'example', 'example\'s comment'),
  (3, 'something', null),
  (4, 'else', null),
  (5, 'foobar', 'foobar\'s comment');