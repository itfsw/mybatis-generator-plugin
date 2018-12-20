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
  `field2` bigint(20) NOT NULL COMMENT '注释3',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of tb
-- ----------------------------
INSERT INTO `tb` VALUES ('1', 'f1', '1');
INSERT INTO `tb` VALUES ('2', 'f2', '1');
INSERT INTO `tb` VALUES ('3', 'f3', '1');
INSERT INTO `tb` VALUES ('4', 'f4', '1');
INSERT INTO `tb` VALUES ('5', 'f5', '1');
INSERT INTO `tb` VALUES ('6', 'f6', '100');
INSERT INTO `tb` VALUES ('7', 'f7', '100');
INSERT INTO `tb` VALUES ('8', 'f8', '100');
INSERT INTO `tb` VALUES ('9', 'f9', '100');
INSERT INTO `tb` VALUES ('10', 'f10', '100');

-- ----------------------------
-- Table structure for tb_all
-- ----------------------------
DROP TABLE IF EXISTS `tb_all`;
CREATE TABLE `tb_all` (
  `key1` bigint(20) NOT NULL COMMENT '注释1',
  `key2` varchar(20) NOT NULL COMMENT '注释2',
  `field1` varchar(255) DEFAULT NULL COMMENT '注释3',
  `field2` bigint(20) NOT NULL COMMENT '注释4',
  `field3` longtext COMMENT '注释5',
  PRIMARY KEY (`key1`, `key2`)
);

-- ----------------------------
-- Records of tb_all
-- ----------------------------
INSERT INTO `tb_all` VALUES ('1', 'key1', 'f1', '1', 'b1');
INSERT INTO `tb_all` VALUES ('2', 'key2', 'f2', '1', 'b2');
INSERT INTO `tb_all` VALUES ('3', 'key3', 'f3', '1', 'b3');