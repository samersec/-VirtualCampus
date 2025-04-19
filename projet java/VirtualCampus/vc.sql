-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: virtualcampus
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `batiments`
--

DROP TABLE IF EXISTS `batiments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batiments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) DEFAULT NULL,
  `type` enum('SalleCours','Bibliotheque','Cafeteria','Laboratoire') DEFAULT NULL,
  `capacite` int DEFAULT NULL,
  `conso_wifi` double DEFAULT NULL,
  `conso_elec` double DEFAULT NULL,
  `conso_eau` double DEFAULT NULL,
  `impact_satisfaction` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batiments`
--

LOCK TABLES `batiments` WRITE;
/*!40000 ALTER TABLE `batiments` DISABLE KEYS */;
INSERT INTO `batiments` VALUES (1,'Salle Alpha','SalleCours',30,5,10,3,5),(2,'Salle Bêta','SalleCours',25,4,9,2.5,4),(3,'Salle Gamma','SalleCours',35,6,12,4,6),(4,'Salle Delta','SalleCours',40,6.5,13,4.2,6),(5,'Salle Epsilon','SalleCours',20,3.5,8,2,3),(6,'Bibliothèque Centrale','Bibliotheque',60,8,15,5,9),(7,'Bibliothèque Math','Bibliotheque',30,5,10,3,6),(8,'Bibliothèque Info','Bibliotheque',40,6,12,3.5,7),(9,'Bibliothèque Langues','Bibliotheque',20,3,8,2.5,5),(10,'Bibliothèque Sciences','Bibliotheque',50,7,14,4,8),(11,'Cafétéria Centrale','Cafeteria',100,2,20,25,10),(12,'Café Étudiant','Cafeteria',50,1.5,10,10,6),(13,'Café Express','Cafeteria',30,1,7,10,7),(14,'Snack Polytech','Cafeteria',40,1.2,8,12,7.5),(15,'Café ChillZone','Cafeteria',60,1.8,100,18,6),(16,'Labo Informatique','Laboratoire',25,10,30,5,7),(17,'Labo Physique','Laboratoire',20,7,25,4,6),(18,'Labo Chimie','Laboratoire',15,6,20,6,5),(19,'Labo IA','Laboratoire',10,12,35,10,5),(31,'sa','Bibliotheque',12,10,10,150,7),(33,'wq','Bibliotheque',1,32,10,10,7),(35,'samer','Cafeteria',2,2,400,100,6),(37,'hjda','Bibliotheque',2,1,3,200,7);
/*!40000 ALTER TABLE `batiments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `etudiant`
--

DROP TABLE IF EXISTS `etudiant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `etudiant` (
  `personne_id` varchar(20) NOT NULL,
  `filiere` varchar(50) NOT NULL,
  `niveau` int NOT NULL,
  `heures_cours` int DEFAULT '0',
  `satisfaction` int DEFAULT NULL,
  PRIMARY KEY (`personne_id`),
  CONSTRAINT `etudiant_ibfk_1` FOREIGN KEY (`personne_id`) REFERENCES `personne` (`id`) ON DELETE CASCADE,
  CONSTRAINT `etudiant_chk_1` CHECK ((`satisfaction` between 0 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `etudiant`
--

LOCK TABLES `etudiant` WRITE;
/*!40000 ALTER TABLE `etudiant` DISABLE KEYS */;
INSERT INTO `etudiant` VALUES ('ETD001','Informatique',2,120,35),('ETD003','infooo',1,1,NULL),('ETD014','inf',1,2,NULL);
/*!40000 ALTER TABLE `etudiant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personne`
--

DROP TABLE IF EXISTS `personne`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personne` (
  `id` varchar(20) NOT NULL,
  `type` enum('ETUDIANT','PROFESSEUR') NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `satisfaction` int DEFAULT NULL,
  `date_inscription` date NOT NULL,
  `batiment_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `batiment_id` (`batiment_id`),
  CONSTRAINT `personne_chk_1` CHECK ((`satisfaction` between 0 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personne`
--

LOCK TABLES `personne` WRITE;
/*!40000 ALTER TABLE `personne` DISABLE KEYS */;
INSERT INTO `personne` VALUES ('ETD001','ETUDIANT','Dupont','Jean','jean.dupont@email.com',74,'2022-09-01',7),('ETD003','ETUDIANT','samer','soltani','sss@gmail.com',50,'2025-04-18',1),('ETD004','ETUDIANT','Lefebvre','Emma','emma.lefebvre@email.com',65,'2023-09-01',4),('ETD005','ETUDIANT','Morel','Hugo','hugo.morel@email.com',72,'2022-10-15',5),('ETD006','ETUDIANT','Simon','Léa','lea.simon@email.com',68,'2023-02-20',6),('ETD007','ETUDIANT','Laurent','Mathis','mathis.laurent@email.com',75,'2022-11-11',7),('ETD008','ETUDIANT','Michel','Camille','camille.michel@email.com',70,'2023-03-05',2),('ETD009','ETUDIANT','Garcia','Louis','louis.garcia@email.com',62,'2023-01-18',3),('ETD010','ETUDIANT','David','Chloé','chloe.david@email.com',78,'2022-09-22',1),('ETD011','ETUDIANT','Bertrand','Nathan','nathan.bertrand@email.com',71,'2023-04-10',4),('ETD012','ETUDIANT','Rousseau','Manon','manon.rousseau@email.com',69,'2022-12-03',5),('ETD013','ETUDIANT','Fournier','Tom','tom.fournier@email.com',74,'2023-05-15',6),('ETD014','ETUDIANT','wq','wq','we',25,'2025-04-19',1),('PRO003','PROFESSEUR','Moreau','Isabelle','isabelle.moreau@email.com',74,'2021-09-01',4),('PRO004','PROFESSEUR','Martin','Sophie','sophie.martin@email.com',85,'2020-08-15',2),('PRO005','PROFESSEUR','Bernard','Pierre','pierre.bernard@email.com',90,'2019-09-01',3),('PRO006','PROFESSEUR','Dubois','Marie','marie.dubois@email.com',78,'2021-01-10',1),('PRO007','PROFESSEUR','Thomas','Luc','luc.thomas@email.com',82,'2020-03-22',4),('PRO008','PROFESSEUR','Robert','Julie','julie.robert@email.com',88,'2022-02-14',5),('PRO009','PROFESSEUR','Petit','Antoine','antoine.petit@email.com',76,'2021-11-05',6),('PRO010','PROFESSEUR','Richard','Claire','claire.richard@email.com',92,'2018-09-01',7),('PRO011','PROFESSEUR','Durand','François','francois.durand@email.com',81,'2020-10-18',2),('PRO012','PROFESSEUR','Leroy','Elodie','elodie.leroy@email.com',87,'2021-05-30',3),('PRO013','PROFESSEUR','Roux','Nicolas','nicolas.roux@email.com',79,'2022-01-12',1);
/*!40000 ALTER TABLE `personne` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `professeur`
--

DROP TABLE IF EXISTS `professeur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professeur` (
  `personne_id` varchar(20) NOT NULL,
  `matiere_enseignee` varchar(50) NOT NULL,
  `disponible` tinyint(1) DEFAULT '1',
  `en_greve` tinyint(1) DEFAULT '0',
  `heures_enseignement` int DEFAULT '0',
  PRIMARY KEY (`personne_id`),
  CONSTRAINT `professeur_ibfk_1` FOREIGN KEY (`personne_id`) REFERENCES `personne` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `professeur`
--

LOCK TABLES `professeur` WRITE;
/*!40000 ALTER TABLE `professeur` DISABLE KEYS */;
INSERT INTO `professeur` VALUES ('PRO003','Mécanique ',0,1,180);
/*!40000 ALTER TABLE `professeur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `type` enum('ELECTRICITY','WATER','WIFI','SPACE') NOT NULL,
  `current_consumption` decimal(5,2) NOT NULL,
  `capacity` decimal(10,2) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('NORMAL','CRITICAL','OPTIMIZED') DEFAULT 'NORMAL',
  PRIMARY KEY (`id`),
  CONSTRAINT `resources_chk_1` CHECK ((`current_consumption` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resources`
--

LOCK TABLES `resources` WRITE;
/*!40000 ALTER TABLE `resources` DISABLE KEYS */;
INSERT INTO `resources` VALUES (1,'Main Electricity','ELECTRICITY',66.57,1000.00,'kWh','2025-04-19 11:20:25','NORMAL'),(2,'Water Supply','WATER',99.60,500.00,'m³','2025-04-19 11:18:22','NORMAL'),(3,'Campus WiFi','WIFI',38.44,1000.00,'Mbps','2025-04-19 00:11:25','NORMAL'),(4,'Building Space','SPACE',31.80,1000.00,'m²','2025-04-18 20:53:03','NORMAL');
/*!40000 ALTER TABLE `resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `iduser` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`iduser`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'samer','123'),(2,'sisi','1234');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-19 12:26:36
