# Appendix 2: Main SQLs for Application

## SQL Commands

### Award
``` sql
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Warrior', '0');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Elite', '100');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Master', '200');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Grandmaster', '300');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Epic', '400');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Legend', '500');
INSERT INTO `Award`(`Award`, `Point`) VALUES ('Mythic', '600');
```

### Therapist Registration
``` sql
INSERT INTO `Therapist`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`) VALUES ('olivia', '123456', 'Olivia Wee', '61234578', 'olivia@gmail.com', 'profile-content');
INSERT INTO `Therapist`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`) VALUES ('charlotte', '123456', 'Charlotte Elizabeth', '68754321', 'charlotte@gmail.com', 'profile-content');
```

### Patient Registration
``` sql
INSERT INTO `Patient`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `AwardId`, `TherapistId`) VALUES ('holmesjj', '123456', 'Hu Jiajun', '97654321', 'holmesjj@gmail.com', 'profile-content', 'Male', '7', '55', 1, 1);
INSERT INTO `Patient`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `AwardId`, `TherapistId`) VALUES ('bluebell', '123456', 'Zhang Zhiyao', '91234567', 'bluebell@gmail.com', 'profile-content', 'Male', '8', '51', 1, 1);
INSERT INTO `Patient`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `AwardId`, `TherapistId`) VALUES ('ryan', '123456', 'Ryan Tan', '91122334', 'ryan@gmail.com', 'profile-content', 'Male', '7', '47', 1, 1);
INSERT INTO `Patient`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `AwardId`, `TherapistId`) VALUES ('sarah', '123456', 'Sarah Cheng', '95566778', 'sarah@gmail.com', 'profile-content', 'Female', '6', '41', 1, 1);
INSERT INTO `Patient`(`Username`, `Password`, `Name`, `Phone`, `Email`, `Profile`, `Gender`, `Age`, `Hearing`, `AwardId`, `TherapistId`) VALUES ('alicia', '123456', 'Alicia Loke', '93345677', 'alicia@gmail.com', 'profile-content', 'Female', '6', '43', 1, 1);
```

### Training Type
``` sql
INSERT INTO `TrainingType`(`Type`) VALUES ('IPA');
INSERT INTO `TrainingType`(`Type`) VALUES ('Word');
INSERT INTO `TrainingType`(`Type`) VALUES ('Sentence');
```

### Training Content
``` sql
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('/i:/', '1) Open your mouth as if you are smiling, show your teeth, lips outstretched to the sides and flatten out.\n2) Lift the front of the tongue as far as possible toward the hard palate. The tongue touches the back of the lower teeth slightly.\n3) The lips tensed, the tongue muscles tensed, and as the vocal cords vibrate, and the /i:/ sound is produced.', '1');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('/æ/', '1) The distance between the lips should be as far as possible, the mouth should be wide open, and the upper and lower teeth should be about the width of two fingers.\n2) The front of the tongue should be raised during pronunciation with the tip of the tongue slightly touching the back of the lower tooth.\n3) Gradually lower your tongue and jaw as you pronounce them, exaggerating as much as possible, and as vibrate the vocal cords and emit a /æ/ sound.', '1');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('/eɪ/', '1) To learn the pronunciation of the sound, please learn the two basic sounds: /e/ and /i/ first.\n2) Spread your mouth in a semicircle and begin to pronounce /e/ sound.\n3) Then slowly lift the jaw toward /i/ tone and close the lips slowly.', '1');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('Bee', '1) Bee\n2) Bee\n3) Bee', '2');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('Baby', '1) Bady\n2) Bady\n3) Bady', '2');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('Tea', '1) Tea\n2) Tea\n3) Tea', '2');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('How are you', '1) How are you\n2) How are you\n3) How are you', '3');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('Good morning', '1) Good morning\n2) Good morning\n3) Good morning', '3');
INSERT INTO `TrainingContent`(`Content`, `Detail`, `TrainingTypeId`) VALUES ('Good night', '1) Good night\n2) Good night\n3) Good night', '3');
```

### Training Schedule
``` sql
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '1', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '2', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '3', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '4', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '5', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '6', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '7', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '8', '1');
INSERT INTO `TrainingSchedule`(`StartDate`, `EndDate`, `TrainingContentId`, `PatientId`) VALUES ('2022-01-01', '2022-06-01', '9', '1');
```

### Complete Training Progress
``` sql
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('3', '1' ,'86');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('1', '1' ,'78');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('3', '1' ,'96');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('3', '1' ,'67');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('1', '1' ,'97');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('1', '1' ,'76');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('5', '1' ,'49');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('3', '1' ,'63');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('3', '1' ,'99');
INSERT INTO `TrainingProgress`(`TrainingContentId`, `PatientId`, `Grade`) VALUES ('5', '1' ,'79');
```

### Complete Jump For Fun
``` sql
INSERT INTO `JumpForFun`(`Grade`, `PatientId`) VALUES ('22', '1');
INSERT INTO `JumpForFun`(`Grade`, `PatientId`) VALUES ('24', '1');
INSERT INTO `JumpForFun`(`Grade`, `PatientId`) VALUES ('17', '1');
INSERT INTO `JumpForFun`(`Grade`, `PatientId`) VALUES ('26', '1');
INSERT INTO `JumpForFun`(`Grade`, `PatientId`) VALUES ('19', '1');
```

### Complete Cube Hub
``` sql
INSERT INTO `CubeHub`(`Grade`, `PatientId`) VALUES ('15', '1');
INSERT INTO `CubeHub`(`Grade`, `PatientId`) VALUES ('17', '1');
INSERT INTO `CubeHub`(`Grade`, `PatientId`) VALUES ('12', '1');
INSERT INTO `CubeHub`(`Grade`, `PatientId`) VALUES ('19', '1');
INSERT INTO `CubeHub`(`Grade`, `PatientId`) VALUES ('16', '1');
```

### Get All Awards
``` sql
SELECT * FROM `Award`;
```

### Get All Therapists
``` sql
SELECT `Id`, `Username`, `Name`, `Phone`, `Email`, `Profile` FROM `Therapist`;
```

### Get Therapist By Therapist Id
``` sql
SELECT `Id`, `Username`, `Name`, `Phone`, `Email`, `Profile` FROM `Therapist` WHERE `Id` = 1;
```

### Get Therapist By Username
``` sql
SELECT `Id`, `Username`, `Name`, `Phone`, `Email`, `Profile` FROM `Therapist` WHERE `Username` = "olivia";
```

### Get Therapist By Phone
``` sql
SELECT `Id`, `Username`, `Name`, `Phone`, `Email`, `Profile` FROM `Therapist` WHERE `Phone` = "61234578";
```

### Get Therapist By Email
``` sql
SELECT `Id`, `Username`, `Name`, `Phone`, `Email`, `Profile` FROM `Therapist` WHERE `Email` = "olivia@gmail.com";
```

### Get Last Therapist Id
``` sql
SELECT `Id` FROM `Therapist` ORDER BY `Id` DESC LIMIT 1;
```

### Get All Patients
``` sql
SELECT P.Username, P.Name, P.Phone, P.Email, P.Profile, P.Gender, P.Age, P.Hearing, P.Point, A.Award, T.Name FROM `Patient` AS P INNER JOIN `Therapist` AS T ON P.TherapistId = T.Id INNER JOIN `Award` AS A ON P.AwardId = A.Id;
```

### Get Patient By Patient Id
``` sql
SELECT P.Username, P.Name, P.Phone, P.Email, P.Profile, P.Gender, P.Age, P.Hearing, P.Point, A.Award, T.Name FROM `Patient` AS P INNER JOIN `Therapist` AS T ON P.TherapistId = T.Id INNER JOIN `Award` AS A ON P.AwardId = A.Id WHERE P.Id = 1;
```

### Get Patient By Username
``` sql
SELECT P.Username, P.Name, P.Phone, P.Email, P.Profile, P.Gender, P.Age, P.Hearing, P.Point, A.Award, T.Name FROM `Patient` AS P INNER JOIN `Therapist` AS T ON P.TherapistId = T.Id INNER JOIN `Award` AS A ON P.AwardId = A.Id WHERE P.Username = "holmesjj";
```

### Get Patient By Phone
``` sql
SELECT P.Username, P.Name, P.Phone, P.Email, P.Profile, P.Gender, P.Age, P.Hearing, P.Point, A.Award, T.Name FROM `Patient` AS P INNER JOIN `Therapist` AS T ON P.TherapistId = T.Id INNER JOIN `Award` AS A ON P.AwardId = A.Id WHERE P.Phone = "91234567";
```

### Get Patient By Email
``` sql
SELECT P.Username, P.Name, P.Phone, P.Email, P.Profile, P.Gender, P.Age, P.Hearing, P.Point, A.Award, T.Name FROM `Patient` AS P INNER JOIN `Therapist` AS T ON P.TherapistId = T.Id INNER JOIN `Award` AS A ON P.AwardId = A.Id WHERE P.Email = "holmesjj@gmail.com";
```

### Get Last Patient Id
``` sql
SELECT `Id` FROM `Patient` ORDER BY `Id` DESC LIMIT 1;
```

### Get Device Id By Device Id And Patient Id
``` sql
SELECT `DeviceId` FROM `Patient` WHERE `Id` = 1 AND `DeviceId` = "device-id";
```

### Get All Training Types
``` sql
SELECT * FROM `TrainingType`;
```

### Get All Training Contents
``` sql
SELECT * FROM `TrainingContent`;
```

### Get All Training Progress
``` sql
SELECT * FROM `TrainingProgress`;
```

### Get Number Of Training Times For Each Training Content By Patient Id
``` sql
SELECT TC.Content, COUNT(TP.TrainingContentId) AS Count FROM `TrainingProgress` AS TP INNER JOIN `TrainingContent` AS TC ON TP.TrainingContentId = TC.Id WHERE TP.PatientId = 1 GROUP BY TP.TrainingContentId;
```

### Get Training Content By Training Schedule Id
``` sql
SELECT TC.Id, TC.Content, TC.Detail, TT.Type FROM `TrainingSchedule` AS TS INNER JOIN `TrainingContent` AS TC ON TS.TrainingContentId = TC.Id INNER JOIN `TrainingType` AS TT ON TC.TrainingTypeId = TT.Id WHERE TS.Id = 1;
```

### Get Training Content By Training Progress Id
``` sql
SELECT TC.Id, TC.Content, TC.Detail, TT.Type FROM `TrainingProgress` AS TP INNER JOIN `TrainingContent` AS TC ON TP.TrainingContentId = TC.Id INNER JOIN `TrainingType` AS TT ON TC.TrainingTypeId = TT.Id WHERE TP.Id = 1;
```

### Get Training Content By Training Type Id
``` sql
SELECT TC.Id, TC.Content, TC.Detail, TT.Type FROM `TrainingType` AS TT INNER JOIN `TrainingContent` AS TC ON TT.Id = TC.TrainingTypeId WHERE TT.Id = 1;
```

### Get Training Schedule By Patient Id
``` sql
SELECT P.Name, TC.Content, TS.StartDate, TS.EndDate FROM `Patient` AS P INNER JOIN `TrainingSchedule` AS TS ON P.Id = TS.PatientId INNER JOIN `TrainingContent` AS TC ON TS.TrainingContentId = TC.Id WHERE P.Id = 1;
```

### Get Today Training Schedules By Patient Id
``` sql
SELECT P.Name, TC.Content, TS.StartDate, TS.EndDate FROM `Patient` AS P INNER JOIN `TrainingSchedule` AS TS ON P.Id = TS.PatientId INNER JOIN `TrainingContent` AS TC ON TS.TrainingContentId = TC.Id WHERE P.Id = 1 AND TS.StartDate <= CURRENT_TIMESTAMP() AND TS.EndDate >= CURRENT_TIMESTAMP();
```

### Get Training Schedules By Patient Id And Training Content Id
``` sql
SELECT P.Name, TC.Content, TS.StartDate, TS.EndDate FROM `Patient` AS P INNER JOIN `TrainingSchedule` AS TS ON P.Id = TS.PatientId INNER JOIN `TrainingContent` AS TC ON TS.TrainingContentId = TC.Id WHERE P.Id = 1 AND TC.Id = 1;
```

### Get Training Progresses By Patient Id
``` sql
SELECT P.Name, TC.Content, TP.Grade, TP.CreatedAt FROM `Patient` AS P INNER JOIN `TrainingProgress` AS TP ON P.Id = TP.PatientId INNER JOIN `TrainingContent` AS TC ON TP.TrainingContentId = TC.Id WHERE P.Id = 1;
```

### Get Today Training Progresses By Patient Id
``` sql
SELECT P.Name, TC.Content, TP.Grade, TP.CreatedAt FROM `Patient` AS P INNER JOIN `TrainingProgress` AS TP ON P.Id = TP.PatientId INNER JOIN `TrainingContent` AS TC ON TP.TrainingContentId = TC.Id WHERE P.Id = 1 AND DATE(TP.CreatedAt) = CURRENT_DATE();
```

### Get Training Progresses By Patient Id And Training Content Id
``` sql
SELECT P.Name, TC.Content, TP.Grade, TP.CreatedAt FROM `Patient` AS P INNER JOIN `TrainingProgress` AS TP ON P.Id = TP.PatientId INNER JOIN `TrainingContent` AS TC ON TP.TrainingContentId = TC.Id WHERE P.Id = 1 AND TC.Id = 1;
```

### Update Validation Code
``` sql
UPDATE `Patient` SET `ValidationCode` = 'ABCD', `ExpiredTime` = CURRENT_TIMESTAMP() + INTERVAL 10 MINUTE WHERE `Id` = 1;
```

### Reset Password
``` sql
SELECT * FROM `Patient` WHERE `ValidationCode` = "ABCD" AND `ExpiredTime` > CURRENT_TIMESTAMP() AND `Id` = 1;
UPDATE `Patient` SET `Password` = '654321' WHERE `Id` = 1;
```

### Patient Login
``` sql
UPDATE `Patient` SET `LoginTimes` = (SELECT `LoginTimes` FROM `Patient` WHERE `Id` = 1) + 1, `AccountStatus` = CASE WHEN (SELECT `LoginTimes` FROM `Patient` WHERE `Id` = 1) + 1 = 3 THEN "Invalid" ELSE "Valid" END WHERE `Id` = 1;
SELECT * FROM `Patient` WHERE `Username` = "holmesjj" AND `Password` = "123456" AND `AccountStatus` = "Valid";
SELECT * FROM `Patient` WHERE `Username` = "holmesjj" AND `ValidationCode` = "ABCD" AND `ExpiredTime` < CURRENT_TIMESTAMP() AND `AccountStatus` = "Valid";
UPDATE `Patient` SET `LoginTimes` = 0, `AccountStatus` = "Valid" WHERE `Id` = 1;
```

### Therapist Login
``` sql
UPDATE `Therapist` SET `LoginTimes` = (SELECT `LoginTimes` FROM `Therapist` WHERE `Id` = 1) + 1, `AccountStatus` = CASE WHEN (SELECT `LoginTimes` FROM `Therapist` WHERE `Id` = 1) + 1 = 3 THEN "Invalid" ELSE "Valid" END WHERE `Id` = 1;
SELECT * FROM `Therapist` WHERE `Username` = "olivia" AND `Password` = "123456" AND `AccountStatus` = "Valid";
SELECT * FROM `Therapist` WHERE `Username` = "olivia" AND `ValidationCode` = "ABCD" AND `ExpiredTime` < CURRENT_TIMESTAMP() AND `AccountStatus` = "Valid";
UPDATE `Therapist` SET `LoginTimes` = 0, `AccountStatus` = "Valid" WHERE `Id` = 1;
```

### Check Jump For Fun Leaderboard
``` sql
SELECT P.Name, J.Grade FROM `Patient` AS P INNER JOIN `JumpForFun` AS J ON P.Id = J.PatientId ORDER BY J.Grade DESC;
```

### Check Cube Hub Leaderboard
``` sql
SELECT P.Name, C.Grade FROM `Patient` AS P INNER JOIN `CubeHub` AS C ON P.Id = C.PatientId ORDER BY C.Grade DESC;
```
