/* 

This file contains the insert Script for creating the Database related 
resources for the Smart Save App in the mysql database.
The tables used for this application are:

 - registered_users => Mobile Number is an unique identifier for 
   seperating users since the sms is sent via the the lambda function 
   for login and new user registration.
 - req_register => To map the File path as stored in the S3 to the user based on phone number and file name.
 - otp_validator => To keep track of the OTPS generated.
 - no_generator => To maintain a number count for purposes like user id generation.

*/


/*Creating the primary database for the project*/
CREATE DATABASE `infotrends_in` /*!40100 DEFAULT CHARACTER SET latin1 */;


/* Creating the table for storing the user data */
DROP TABLE IF EXISTS `infotrends_in`.`registered_users`;
CREATE TABLE  `infotrends_in`.`registered_users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(45) NOT NULL,
  `email_id` varchar(45) DEFAULT NULL,
  `password` varchar(45) NOT NULL,
  `occupation` varchar(45) DEFAULT NULL,
  `login_time` varchar(45) DEFAULT NULL,
  `login_attempts` varchar(45) NOT NULL,
  `phone_verified` varchar(45) NOT NULL,
  `active_login` varchar(45) DEFAULT 'N',
  `phone_no` varchar(10) NOT NULL,
  `country_code` varchar(5) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/* Creating the data for the files stored in s3 */
DROP TABLE IF EXISTS `infotrends_in`.`req_register`;
CREATE TABLE  `infotrends_in`.`req_register` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `phoneNo` varchar(10) NOT NULL,
  `fileName` varchar(150) NOT NULL,
  `filePath` varchar(150) NOT NULL,
  `fileType` varchar(10) NOT NULL,
  `Timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isImg` varchar(45) NOT NULL DEFAULT 'N',
  `email_id` varchar(50) DEFAULT NULL,
  `isPublic` varchar(2) NOT NULL DEFAULT 'N',
  `sharedNos` varchar(55) NOT NULL DEFAULT '',
  `isSharedFile` varchar(5) DEFAULT 'N',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=latin1;


/* Creating the table used to store the otp data for validations such as - Mobile No. authentication, login Authentication and Authentication key for the Apis*/
DROP TABLE IF EXISTS `infotrends_in`.`otp_validator`;
CREATE TABLE  `infotrends_in`.`otp_validator` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `otp` varchar(45) DEFAULT NULL,
  `user_id` varchar(45) DEFAULT NULL,
  `timestamp` varchar(45) DEFAULT 'NOW()',
  `status` varchar(3) NOT NULL DEFAULT 'A',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=298 DEFAULT CHARSET=latin1;


/* Creating the table to store the number generator table */
DROP TABLE IF EXISTS `infotrends_in`.`no_generator`;
CREATE TABLE  `infotrends_in`.`no_generator` (
  `req_type` varchar(8) NOT NULL,
  `tier` varchar(5) NOT NULL,
  `number` int(11) NOT NULL,
  PRIMARY KEY (`req_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/* Insert Query for inserting the data into the no generator table -- cust_id => Used for Cust Id generation, tier => 1 refers to the SmartSave application */
INSERT INTO no_generator VALUES ('cust_id', '1', 0);


