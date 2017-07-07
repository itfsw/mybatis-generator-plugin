/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : mybatis-generator-plugin

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2017-06-26 17:30:13
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb
-- ----------------------------
DROP TABLE IF EXISTS `tb`;
CREATE TABLE `tb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of tb
-- ----------------------------
INSERT INTO `tb` VALUES ('1', 'f1');
INSERT INTO `tb` VALUES ('2', 'f2');
INSERT INTO `tb` VALUES ('3', 'f3');
INSERT INTO `tb` VALUES ('4', 'f4');
INSERT INTO `tb` VALUES ('5', 'f5');
INSERT INTO `tb` VALUES ('6', 'f6');
INSERT INTO `tb` VALUES ('7', 'f7');
INSERT INTO `tb` VALUES ('8', 'f8');
INSERT INTO `tb` VALUES ('9', 'f9');
INSERT INTO `tb` VALUES ('10', 'f10');