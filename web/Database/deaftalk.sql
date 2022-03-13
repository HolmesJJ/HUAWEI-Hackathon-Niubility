-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jan 12, 2022 at 11:39 AM
-- Server version: 10.4.21-MariaDB
-- PHP Version: 7.4.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `deaftalk`
--

-- --------------------------------------------------------

--
-- Table structure for table `Award`
--

CREATE TABLE `Award` (
  `Id` int(11) NOT NULL,
  `Award` varchar(100) NOT NULL,
  `Point` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Award`
--

INSERT INTO `Award` (`Id`, `Award`, `Point`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 'Warrior', 0, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(2, 'Elite', 100, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(3, 'Master', 200, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(4, 'Grandmaster', 300, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(5, 'Epic', 400, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(6, 'Legend', 500, '2022-01-12 02:39:00', '2022-01-12 02:39:00'),
(7, 'Mythic', 600, '2022-01-12 02:39:00', '2022-01-12 02:39:00');

-- --------------------------------------------------------

--
-- Table structure for table `CubeHub`
--

CREATE TABLE `CubeHub` (
  `Id` int(11) NOT NULL,
  `Grade` int(3) NOT NULL,
  `PatientId` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `CubeHub`
--

INSERT INTO `CubeHub` (`Id`, `Grade`, `PatientId`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 15, 1, '2022-01-12 02:44:46', '2022-01-12 02:44:46'),
(2, 17, 1, '2022-01-12 02:44:46', '2022-01-12 02:44:46'),
(3, 12, 1, '2022-01-12 02:44:46', '2022-01-12 02:44:46'),
(4, 19, 1, '2022-01-12 02:44:46', '2022-01-12 02:44:46'),
(5, 16, 1, '2022-01-12 02:44:46', '2022-01-12 02:44:46');

-- --------------------------------------------------------

--
-- Table structure for table `JumpForFun`
--

CREATE TABLE `JumpForFun` (
  `Id` int(11) NOT NULL,
  `Grade` int(3) NOT NULL,
  `PatientId` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `JumpForFun`
--

INSERT INTO `JumpForFun` (`Id`, `Grade`, `PatientId`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 22, 1, '2022-01-12 02:44:16', '2022-01-12 02:44:16'),
(2, 24, 1, '2022-01-12 02:44:16', '2022-01-12 02:44:16'),
(3, 17, 1, '2022-01-12 02:44:16', '2022-01-12 02:44:16'),
(4, 26, 1, '2022-01-12 02:44:16', '2022-01-12 02:44:16'),
(5, 19, 1, '2022-01-12 02:44:16', '2022-01-12 02:44:16');

-- --------------------------------------------------------

--
-- Table structure for table `Patient`
--

CREATE TABLE `Patient` (
  `Id` int(11) NOT NULL,
  `Username` varchar(100) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `Phone` varchar(20) NOT NULL,
  `Email` varchar(50) NOT NULL,
  `Profile` text NOT NULL,
  `Gender` enum('Male','Female') NOT NULL,
  `Age` varchar(3) NOT NULL,
  `Hearing` varchar(3) NOT NULL,
  `Point` int(11) NOT NULL DEFAULT 0,
  `AwardId` int(11) NOT NULL,
  `DeviceId` text DEFAULT NULL,
  `LoggedInAt` timestamp NULL DEFAULT NULL,
  `LoginTimes` int(11) NOT NULL DEFAULT 0,
  `AccountStatus` enum('Valid','Invalid') NOT NULL DEFAULT 'Valid',
  `ValidationCode` char(4) DEFAULT NULL,
  `ExpiredTime` timestamp NULL DEFAULT NULL,
  `TherapistId` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Patient`
--

INSERT INTO `Patient` (`Id`, `Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `Point`, `AwardId`, `DeviceId`, `LoggedInAt`, `LoginTimes`, `AccountStatus`, `ValidationCode`, `ExpiredTime`, `TherapistId`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 'holmesjj', '123456', 'Hu Jiajun', '97654321', 'holmesjj@gmail.com', 'profile-content', 'Male', '7', '55', 0, 1, NULL, NULL, 0, 'Valid', 'ABCD', '2022-01-12 06:51:48', 1, '2022-01-12 02:40:31', '2022-01-12 02:40:31'),
(2, 'bluebell', '123456', 'Zhang Zhiyao', '91234567', 'bluebell@gmail.com', 'profile-content', 'Male', '8', '51', 0, 1, NULL, NULL, 0, 'Valid', NULL, NULL, 1, '2022-01-12 02:40:31', '2022-01-12 02:40:31'),
(3, 'ryan', '123456', 'Ryan Tan', '91122334', 'ryan@gmail.com', 'profile-content', 'Male', '7', '47', 0, 1, NULL, NULL, 0, 'Valid', NULL, NULL, 1, '2022-01-12 02:40:31', '2022-01-12 02:40:31'),
(4, 'sarah', '123456', 'Sarah Cheng', '95566778', 'sarah@gmail.com', 'profile-content', 'Female', '6', '41', 0, 1, NULL, NULL, 0, 'Valid', NULL, NULL, 1, '2022-01-12 02:40:31', '2022-01-12 02:40:31'),
(5, 'alicia', '123456', 'Alicia Loke', '93345677', 'alicia@gmail.com', 'profile-content', 'Female', '6', '43', 0, 1, NULL, NULL, 0, 'Valid', NULL, NULL, 1, '2022-01-12 02:40:31', '2022-01-12 02:40:31');

-- --------------------------------------------------------

--
-- Table structure for table `Therapist`
--

CREATE TABLE `Therapist` (
  `Id` int(11) NOT NULL,
  `Username` varchar(100) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `Phone` varchar(20) NOT NULL,
  `Email` varchar(50) NOT NULL,
  `Profile` text NOT NULL,
  `LoggedInAt` timestamp NULL DEFAULT NULL,
  `LoginTimes` int(11) NOT NULL DEFAULT 0,
  `AccountStatus` enum('Valid','Invalid') NOT NULL DEFAULT 'Valid',
  `ValidationCode` char(4) DEFAULT NULL,
  `ExpiredTime` timestamp NULL DEFAULT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `Therapist`
--

INSERT INTO `Therapist` (`Id`, `Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `LoggedInAt`, `LoginTimes`, `AccountStatus`, `ValidationCode`, `ExpiredTime`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 'olivia', '123456', 'Olivia Wee', '61234578', 'olivia@gmail.com', 'profile-content', NULL, 0, 'Valid', NULL, NULL, '2022-01-12 02:39:37', '2022-01-12 02:39:37'),
(2, 'charlotte', '123456', 'Charlotte Elizabeth', '68754321', 'charlotte@gmail.com', 'profile-content', NULL, 0, 'Valid', NULL, NULL, '2022-01-12 02:39:37', '2022-01-12 02:39:37');

-- --------------------------------------------------------

--
-- Table structure for table `TrainingContent`
--

CREATE TABLE `TrainingContent` (
  `Id` int(11) NOT NULL,
  `Content` text NOT NULL,
  `Detail` text NOT NULL,
  `TrainingTypeId` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `TrainingContent`
--

INSERT INTO `TrainingContent` (`Id`, `Content`, `Detail`, `TrainingTypeId`, `CreatedAt`, `UpdatedAt`) VALUES
(1, '/i:/', '1) Open your mouth as if you are smiling, show your teeth, lips outstretched to the sides and flatten out.\n2) Lift the front of the tongue as far as possible toward the hard palate. The tongue touches the back of the lower teeth slightly.\n3) The lips tensed, the tongue muscles tensed, and as the vocal cords vibrate, and the /i:/ sound is produced.', 1, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(2, '/æ/', '1) The distance between the lips should be as far as possible, the mouth should be wide open, and the upper and lower teeth should be about the width of two fingers.\n2) The front of the tongue should be raised during pronunciation with the tip of the tongue slightly touching the back of the lower tooth.\n3) Gradually lower your tongue and jaw as you pronounce them, exaggerating as much as possible, and as vibrate the vocal cords and emit a /æ/ sound.', 1, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(3, '/eɪ/', '1) To learn the pronunciation of the sound, please learn the two basic sounds: /e/ and /i/ first.\n2) Spread your mouth in a semicircle and begin to pronounce /e/ sound.\n3) Then slowly lift the jaw toward /i/ tone and close the lips slowly.', 1, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(4, 'Bee', '1) Bee\n2) Bee\n3) Bee', 2, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(5, 'Baby', '1) Bady\n2) Bady\n3) Bady', 2, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(6, 'Tea', '1) Tea\n2) Tea\n3) Tea', 2, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(7, 'How are you', '1) How are you\n2) How are you\n3) How are you', 3, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(8, 'Good morning', '1) Good morning\n2) Good morning\n3) Good morning', 3, '2022-01-12 02:41:02', '2022-01-12 02:41:02'),
(9, 'Good night', '1) Good night\n2) Good night\n3) Good night', 3, '2022-01-12 02:41:02', '2022-01-12 02:41:02');

-- --------------------------------------------------------

--
-- Table structure for table `TrainingProgress`
--

CREATE TABLE `TrainingProgress` (
  `Id` int(11) NOT NULL,
  `TrainingContentId` int(11) NOT NULL,
  `PatientId` int(11) NOT NULL,
  `Grade` int(3) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `TrainingProgress`
--

INSERT INTO `TrainingProgress` (`Id`, `TrainingContentId`, `PatientId`, `Grade`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 3, 1, 86, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(2, 1, 1, 78, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(3, 3, 1, 96, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(4, 3, 1, 67, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(5, 1, 1, 97, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(6, 1, 1, 76, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(7, 5, 1, 49, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(8, 3, 1, 63, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(9, 3, 1, 99, '2022-01-12 02:44:05', '2022-01-12 02:44:05'),
(10, 5, 1, 79, '2022-01-12 02:44:05', '2022-01-12 02:44:05');

-- --------------------------------------------------------

--
-- Table structure for table `TrainingSchedule`
--

CREATE TABLE `TrainingSchedule` (
  `Id` int(11) NOT NULL,
  `StartDate` date NOT NULL,
  `EndDate` date NOT NULL,
  `TrainingContentId` int(11) NOT NULL,
  `PatientId` int(11) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `TrainingSchedule`
--

INSERT INTO `TrainingSchedule` (`Id`, `StartDate`, `EndDate`, `TrainingContentId`, `PatientId`, `CreatedAt`, `UpdatedAt`) VALUES
(1, '2022-01-01', '2022-06-01', 1, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(2, '2022-01-01', '2022-06-01', 2, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(3, '2022-01-01', '2022-06-01', 3, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(4, '2022-01-01', '2022-06-01', 4, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(5, '2022-01-01', '2022-06-01', 5, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(6, '2022-01-01', '2022-06-01', 6, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(7, '2022-01-01', '2022-06-01', 7, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(8, '2022-01-01', '2022-06-01', 8, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33'),
(9, '2022-01-01', '2022-06-01', 9, 1, '2022-01-12 02:43:33', '2022-01-12 02:43:33');

-- --------------------------------------------------------

--
-- Table structure for table `TrainingType`
--

CREATE TABLE `TrainingType` (
  `Id` int(11) NOT NULL,
  `Type` varchar(20) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `UpdatedAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `TrainingType`
--

INSERT INTO `TrainingType` (`Id`, `Type`, `CreatedAt`, `UpdatedAt`) VALUES
(1, 'IPA', '2022-01-12 02:40:52', '2022-01-12 02:40:52'),
(2, 'Word', '2022-01-12 02:40:52', '2022-01-12 02:40:52'),
(3, 'Sentence', '2022-01-12 02:40:52', '2022-01-12 02:40:52');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Award`
--
ALTER TABLE `Award`
  ADD PRIMARY KEY (`Id`);

--
-- Indexes for table `CubeHub`
--
ALTER TABLE `CubeHub`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `cubehub_ibfk_1` (`PatientId`);

--
-- Indexes for table `JumpForFun`
--
ALTER TABLE `JumpForFun`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `jumpforfun_ibfk_1` (`PatientId`);

--
-- Indexes for table `Patient`
--
ALTER TABLE `Patient`
  ADD PRIMARY KEY (`Id`),
  ADD UNIQUE KEY `Username` (`Username`),
  ADD UNIQUE KEY `Phone` (`Phone`),
  ADD UNIQUE KEY `Email` (`Email`),
  ADD KEY `AwardId` (`AwardId`),
  ADD KEY `TherapistId` (`TherapistId`);

--
-- Indexes for table `Therapist`
--
ALTER TABLE `Therapist`
  ADD PRIMARY KEY (`Id`),
  ADD UNIQUE KEY `Email` (`Email`),
  ADD UNIQUE KEY `Phone` (`Phone`),
  ADD UNIQUE KEY `Username` (`Username`);

--
-- Indexes for table `TrainingContent`
--
ALTER TABLE `TrainingContent`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `TrainingTypeId` (`TrainingTypeId`);

--
-- Indexes for table `TrainingProgress`
--
ALTER TABLE `TrainingProgress`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `TrainingContentId` (`TrainingContentId`),
  ADD KEY `PatientId` (`PatientId`);

--
-- Indexes for table `TrainingSchedule`
--
ALTER TABLE `TrainingSchedule`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `trainingschedule_ibfk_1` (`TrainingContentId`),
  ADD KEY `trainingschedule_ibfk_2` (`PatientId`);

--
-- Indexes for table `TrainingType`
--
ALTER TABLE `TrainingType`
  ADD PRIMARY KEY (`Id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Award`
--
ALTER TABLE `Award`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `CubeHub`
--
ALTER TABLE `CubeHub`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `JumpForFun`
--
ALTER TABLE `JumpForFun`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `Patient`
--
ALTER TABLE `Patient`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `Therapist`
--
ALTER TABLE `Therapist`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `TrainingContent`
--
ALTER TABLE `TrainingContent`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `TrainingProgress`
--
ALTER TABLE `TrainingProgress`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `TrainingSchedule`
--
ALTER TABLE `TrainingSchedule`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `TrainingType`
--
ALTER TABLE `TrainingType`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `CubeHub`
--
ALTER TABLE `CubeHub`
  ADD CONSTRAINT `cubehub_ibfk_1` FOREIGN KEY (`PatientId`) REFERENCES `patient` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `JumpForFun`
--
ALTER TABLE `JumpForFun`
  ADD CONSTRAINT `jumpforfun_ibfk_1` FOREIGN KEY (`PatientId`) REFERENCES `patient` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `Patient`
--
ALTER TABLE `Patient`
  ADD CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`AwardId`) REFERENCES `award` (`Id`),
  ADD CONSTRAINT `patient_ibfk_2` FOREIGN KEY (`TherapistId`) REFERENCES `therapist` (`Id`);

--
-- Constraints for table `TrainingContent`
--
ALTER TABLE `TrainingContent`
  ADD CONSTRAINT `trainingcontent_ibfk_1` FOREIGN KEY (`TrainingTypeId`) REFERENCES `trainingtype` (`Id`);

--
-- Constraints for table `TrainingProgress`
--
ALTER TABLE `TrainingProgress`
  ADD CONSTRAINT `trainingprogress_ibfk_1` FOREIGN KEY (`TrainingContentId`) REFERENCES `trainingcontent` (`Id`),
  ADD CONSTRAINT `trainingprogress_ibfk_2` FOREIGN KEY (`PatientId`) REFERENCES `patient` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `TrainingSchedule`
--
ALTER TABLE `TrainingSchedule`
  ADD CONSTRAINT `trainingschedule_ibfk_1` FOREIGN KEY (`TrainingContentId`) REFERENCES `trainingcontent` (`Id`),
  ADD CONSTRAINT `trainingschedule_ibfk_2` FOREIGN KEY (`PatientId`) REFERENCES `patient` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
