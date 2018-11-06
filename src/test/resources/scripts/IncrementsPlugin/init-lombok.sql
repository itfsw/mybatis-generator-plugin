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
INSERT INTO `tb` VALUES ('1', 'fd1', '0');
INSERT INTO `tb` VALUES ('2', 'fd2', '1');
INSERT INTO `tb` VALUES ('3', null, '3');

-- ----------------------------
-- Table structure for tb_keys
-- ----------------------------
DROP TABLE IF EXISTS `tb_keys`;
CREATE TABLE `tb_keys` (
  `key1` bigint(20) NOT NULL COMMENT '注释1',
  `key2` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` int(11) DEFAULT NULL,
  `inc_f1` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`key1`,`key2`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_keys
-- ----------------------------
INSERT INTO `tb_keys` VALUES ('1', 'key1', 'fd1', '0', '1');
INSERT INTO `tb_keys` VALUES ('2', 'key2', 'fd2', '1', '2');
INSERT INTO `tb_keys` VALUES ('3', 'key3', null, '3', '3');

-- ----------------------------
-- Table structure for tb_lombok
-- ----------------------------
DROP TABLE IF EXISTS `tb_lombok`;
CREATE TABLE `tb_lombok` (
  `id` bigint(20) NOT NULL COMMENT '注释1',
  `key1` varchar(20) NOT NULL,
  `field1` varchar(10) COMMENT '注释2',
  `inc_f1` smallint(3),
  `field3` longtext,
  `field4` longtext,
  PRIMARY KEY (`id`,`key1`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_lombok
-- ----------------------------
INSERT INTO `tb_lombok` VALUES ('1', 'key1', 'fd1', '0', 'xx1', null);
INSERT INTO `tb_lombok` VALUES ('2', 'key2', 'fd2', '1', 'xx2', 'ss2');
INSERT INTO `tb_lombok` VALUES ('3', 'key3', null, '3', 'xx3', null);