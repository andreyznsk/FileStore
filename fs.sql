-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: fileSotrage
-- ------------------------------------------------------
-- Server version	8.0.23

DROP SCHEMA IF EXISTS `filestorage`;
CREATE SCHEMA `filestorage`;
USE `filestorage`;

DROP TABLE IF EXISTS `users_fs`;

CREATE TABLE `users_fs` (
  `idUSERS` int NOT NULL AUTO_INCREMENT,
  `login_fs` varchar(45) NOT NULL,
  `Password_fs` varchar(45) NOT NULL,
  `nickname_fs` varchar(45) NOT NULL,
  `DefDir` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idUSERS`),
  UNIQUE KEY `login_UNIQUE` (`login_fs`),
  UNIQUE KEY `Password_UNIQUE` (`Password_fs`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
LOCK TABLES `users_fs` WRITE;
INSERT INTO `users_fs` VALUES (1,'1','356a192b7913b04c54574d18c28d46e6395428ab','1','1'),(3,'Andrey','9dc3bfc2ddc8e4f4ff5afbbf6f8b138671b13e9d','Andrey','Andrey'),(4,'Dima','04d843672160f04b9ca13ad37a382cc7681d1f16','Dima','Dima'),(5,'4','1b6453892473a467d07372d45eb05abc2031647a','4','4'),(8,'5','ac3478d69a3c81fa62e60f5c3696165a4e5e6ac4','5','5');
UNLOCK TABLES;
DROP USER IF EXISTS 'filestorage'@'localhost';
CREATE USER 'filestorage'@'localhost' IDENTIFIED BY 'passwd_2021_!@#';
GRANT ALL PRIVILEGES ON filestorage.users_fs TO 'filestorage'@'localhost';
FLUSH PRIVILEGES;
