create table users(
    chat_id BIGINT PRIMARY KEY unique,
    email varchar unique not null,
    password varchar not null
);




