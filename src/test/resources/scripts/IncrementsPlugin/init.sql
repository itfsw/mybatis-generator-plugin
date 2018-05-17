/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : mybatis-generator-plugin

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2017-07-05 17:21:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb
-- ----------------------------
DROP TABLE IF EXISTS `tb`;
CREATE TABLE `tb` (
  `id` bigint(20) NOT NULL COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  `inc_f2` bigint(20) DEFAULT '0',
  `inc_f3` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb
-- ----------------------------
INSERT INTO `tb` VALUES ('1', 'fd1', '0', '0', '0');
INSERT INTO `tb` VALUES ('2', 'fd2', '1', '2', '3');
INSERT INTO `tb` VALUES ('3', null, '3', '2', '1');
INSERT INTO `tb` VALUES ('4', 'fd3', '1', '1', '1');

-- ----------------------------
-- Table structure for tb_blobs
-- ----------------------------
DROP TABLE IF EXISTS `tb_blobs`;
CREATE TABLE `tb_blobs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL,
  `field2` longtext COMMENT '注释2',
  `field3` longtext,
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  `inc_f2` bigint(20) NOT NULL DEFAULT '0',
  `inc_f3` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_blobs
-- ----------------------------
INSERT INTO `tb_blobs` VALUES ('1', 'fd1', null, null, '1', '2', '3');
INSERT INTO `tb_blobs` VALUES ('2', null, 'fd2', null, '3', '2', '1');
INSERT INTO `tb_blobs` VALUES ('3', null, null, 'fd3', '1', '1', '1');
INSERT INTO `tb_blobs` VALUES ('4', 'fd4', 'fd5', 'fd6', '0', '0', '0');

-- ----------------------------
-- Table structure for tb_keys
-- ----------------------------
DROP TABLE IF EXISTS `tb_keys`;
CREATE TABLE `tb_keys` (
  `key1` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `key2` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  `inc_f2` bigint(20) NOT NULL DEFAULT '0',
  `inc_f3` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`key1`,`key2`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_keys
-- ----------------------------
INSERT INTO `tb_keys` VALUES ('1', 'k1', 'fd1', null, '1', '2', '3');
INSERT INTO `tb_keys` VALUES ('2', 'k2', null, '2', '3', '2', '1');
INSERT INTO `tb_keys` VALUES ('3', 'k3', null, null, '1', '1', '1');

-- ----------------------------
-- Table structure for tb_single_blob
-- ----------------------------
DROP TABLE IF EXISTS `tb_single_blob`;
CREATE TABLE `tb_single_blob` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field1` longtext COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  `inc_f2` bigint(20) NOT NULL DEFAULT '0',
  `inc_f3` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_single_blob
-- ----------------------------
INSERT INTO `tb_single_blob` VALUES ('1', 'fd1', '0', '1', '2', '3');
INSERT INTO `tb_single_blob` VALUES ('2', null, null, '3', '2', '1');
INSERT INTO `tb_single_blob` VALUES ('3', null, null, '1', '1', '1');

-- ----------------------------
-- Table structure for tb_key_word
-- ----------------------------
DROP TABLE IF EXISTS `tb_key_word`;
CREATE TABLE `tb_key_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `field2` int(11) DEFAULT NULL,
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  `update` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_key_word
-- ----------------------------
INSERT INTO `tb_key_word` VALUES ('1', '0', '0', '1');