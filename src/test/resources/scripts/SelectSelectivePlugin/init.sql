/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : mybatis-generator-plugin

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2017-07-03 17:34:11
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb
-- ----------------------------
DROP TABLE IF EXISTS `tb`;
CREATE TABLE `tb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb
-- ----------------------------
INSERT INTO `tb` VALUES ('1', 'fd1', null);
INSERT INTO `tb` VALUES ('2', null, '2');
INSERT INTO `tb` VALUES ('3', 'fd3', '3');
INSERT INTO `tb` VALUES ('4', 'fd3', '4');

-- ----------------------------
-- Table structure for tb_blobs
-- ----------------------------
DROP TABLE IF EXISTS `tb_blobs`;
CREATE TABLE `tb_blobs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL,
  `field2` longtext COMMENT '注释2',
  `field3` longtext,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_blobs
-- ----------------------------
INSERT INTO `tb_blobs` VALUES ('1', 'fd1', 'L1', null);
INSERT INTO `tb_blobs` VALUES ('2', null, 'L2', 'LL2');
INSERT INTO `tb_blobs` VALUES ('3', 'fd3', 'L3', 'LL3');
INSERT INTO `tb_blobs` VALUES ('4', 'fd3', 'L4', 'LL4');

-- ----------------------------
-- Table structure for tb_keys
-- ----------------------------
DROP TABLE IF EXISTS `tb_keys`;
CREATE TABLE `tb_keys` (
  `key1` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `key2` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  PRIMARY KEY (`key1`,`key2`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_keys
-- ----------------------------
INSERT INTO `tb_keys` VALUES ('1', '2', 'fd1', null);
INSERT INTO `tb_keys` VALUES ('2', '3', null, '2');
INSERT INTO `tb_keys` VALUES ('3', '4', 'fd2', '3');

-- ----------------------------
-- Table structure for tb_single_blob
-- ----------------------------
DROP TABLE IF EXISTS `tb_single_blob`;
CREATE TABLE `tb_single_blob` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` longtext COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_single_blob
-- ----------------------------
