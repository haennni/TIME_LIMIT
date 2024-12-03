## database 생성
CREATE DATABASE moviedb DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;


## dabatabase 선택
use moviedb;


## database 초기화
CREATE TABLE movie (
	idx INT(11) NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(200) NOT NULL,
    image VARCHAR(80) NOT NULL,
    PRIMARY KEY(idx) 
);


insert into movie(title, content, image) values
("영화1", "재미있는 영화다1", "/img/movie1.jpg"),
("영화2", "재미있는 영화다2", "/img/movie2.jpg"),
("영화3", "재미있는 영화다3", "/img/movie3.jpg"),
("영화4", "재미있는 영화다4", "/img/movie4.jpg");