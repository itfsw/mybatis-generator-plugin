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
  `id` bigint(20) NOT NULL COMMENT '注释1',
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb
-- ----------------------------

-- ----------------------------
-- Table structure for tb_keys
-- ----------------------------
DROP TABLE IF EXISTS `tb_keys`;
CREATE TABLE `tb_keys` (
  `key1` bigint(20) NOT NULL COMMENT '注释1',
  `key2` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  PRIMARY KEY (`key1`,`key2`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_keys
-- ----------------------------

-- ----------------------------
-- Table structure for tb_key_blob
-- ----------------------------
DROP TABLE IF EXISTS `tb_key_blob`;
CREATE TABLE `tb_key_blob` (
  `id` bigint(20) NOT NULL COMMENT '注释1',
  `key1` varchar(20) NOT NULL,
  `field1` longtext COMMENT '注释2',
  `field2` longtext,
  PRIMARY KEY (`id`,`key1`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_key_blob
-- ----------------------------

-- ----------------------------
-- Table structure for tb_lombok
-- ----------------------------
DROP TABLE IF EXISTS `tb_lombok`;
CREATE TABLE `tb_lombok` (
  `id` bigint(20) NOT NULL COMMENT '注释1',
  `key1` varchar(20) NOT NULL,
  `field1` varchar(10) COMMENT '注释2',
  `is_find` tinyint(1),
  `field3` longtext,
  `field4` longtext,
  PRIMARY KEY (`id`,`key1`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_lombok
-- ----------------------------

-- ----------------------------
-- Table structure for tb_only_keys
-- ----------------------------
DROP TABLE IF EXISTS `tb_only_keys`;
CREATE TABLE `tb_only_keys` (
  `key1` bigint(20) NOT NULL,
  `key2` bigint(20) NOT NULL,
  PRIMARY KEY (`key1`,`key2`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_only_keys
-- ----------------------------