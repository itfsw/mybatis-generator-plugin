/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : mybatis-generator-plugin

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2017-06-27 11:17:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
create table sys_company
(
    company_id           int auto_increment comment '公司id'
        primary key,
    company_linkman      varchar(64)                         null comment '联系人名称',
    company_name         varchar(128)                        null comment '名称',
    company_mobile       varchar(11)                         null comment '手机',
    company_email        varchar(128)                        null comment '邮箱',
    company_intro        varchar(512)                        null comment '简介',
    company_logo         varchar(128)                        null comment 'logo',
    company_identity_pic varchar(128)                        null comment '身份证照片',
    company_work_type    varchar(45)                         null comment '工作性质',
    company_business_pic varchar(128)                        null comment '营业执照',
    company_status       varchar(32)                         null comment '状态[disable(disable):禁用,deleted(deleted):删除]',
    company_create_time  timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    company_update_time  timestamp                           null comment '更新时间',
    column_14            int                                 null
)ENGINE=MyISAM DEFAULT CHARSET=utf8;