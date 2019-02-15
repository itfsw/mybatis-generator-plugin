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
  `field2` smallint(3) COMMENT '注释[success(0):禁用, fail_type(1):启用]',
  `field3` smallint(3) COMMENT '注释[success(0):禁用, fail_type(1):启用]',
  `field3_str` varchar(255) COMMENT '注释111[success(成都):禁用, type(成都11):启用]',
  `status` smallint(3) COMMENT '注释[success(0):禁用, fail(1):启用]',
  `type` bigint(20) COMMENT '注释       [    success (   0   )           :  禁用    ,   fail_type (  1  )  :  启用  ]  阿斯顿覅就就',
  `break_line` bigint(20) COMMENT '换行的注释
            [
              success (   0   )           :  禁用    ,
              fail_type (  1  )  :  启用
            ]
            发士大夫撒旦法'
);

-- ----------------------------
-- Records of tb
-- ----------------------------

-- ----------------------------
-- Table structure for tb_unsupport_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_unsupport_type`;
CREATE TABLE `tb_unsupport_type` (
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` blob COMMENT '注释[success(0):禁用, fail_type(1):启用]发士大夫大事发生的'
);

-- ----------------------------
-- Records of tb_unsupport_type
-- ----------------------------

-- ----------------------------
-- Table structure for tb_wrong_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_wrong_comment`;
CREATE TABLE `tb_wrong_comment` (
  `field1` varchar(255) DEFAULT NULL COMMENT '注释2',
  `field2` smallint(3) COMMENT '注释success(0):禁用, fail_type(1):启用]发士大夫大事发生的'
);

-- ----------------------------
-- Records of tb_wrong_comment
-- ----------------------------