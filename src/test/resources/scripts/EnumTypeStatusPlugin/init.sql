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
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` smallint(3) COMMENT '注释[success(0):禁用, fail_type(1):启用]发士大夫大事发生的',
  `field3` varchar(255) COMMENT '注释111[success(成都):禁用, type(成都11):启用]'
);

-- ----------------------------
-- Records of tb
-- ----------------------------