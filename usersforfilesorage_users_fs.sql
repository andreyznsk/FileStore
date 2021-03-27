-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: usersforfilesorage
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users_fs`
--

DROP TABLE IF EXISTS `users_fs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_fs` (
  `idUSERS` int NOT NULL AUTO_INCREMENT,
  `login_fs` varchar(45) NOT NULL,
  `Password_fs` varchar(45) NOT NULL,
  `nickname_fs` varchar(45) NOT NULL,
  `DefDir` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idUSERS`),
  UNIQUE KEY `login_UNIQUE` (`login_fs`),
  UNIQUE KEY `Password_UNIQUE` (`Password_fs`) /*!80000 INVISIBLE */
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_fs`
--

LOCK TABLES `users_fs` WRITE;
/*!40000 ALTER TABLE `users_fs` DISABLE KEYS */;
INSERT INTO `users_fs` VALUES (1,'1','356a192b7913b04c54574d18c28d46e6395428ab','1','1'),(3,'Andrey','9dc3bfc2ddc8e4f4ff5afbbf6f8b138671b13e9d','Andrey','Andrey'),(4,'Dima','04d843672160f04b9ca13ad37a382cc7681d1f16','Dima','Dima'),(5,'4','1b6453892473a467d07372d45eb05abc2031647a','4','4'),(8,'5','ac3478d69a3c81fa62e60f5c3696165a4e5e6ac4','5','5');
/*!40000 ALTER TABLE `users_fs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-03-22 13:47:26
