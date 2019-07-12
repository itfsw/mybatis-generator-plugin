# 这是 MyBatis Generator 插件的拓展插件包  
应该说使用Mybatis就一定离不开[MyBatis Generator](https://github.com/mybatis/generator)这款代码生成插件，而这款插件自身还提供了插件拓展功能用于强化插件本身，官方已经提供了一些[拓展插件](http://www.mybatis.org/generator/reference/plugins.html)，本项目的目的也是通过该插件机制来强化Mybatis Generator本身，方便和减少我们平时的代码开发量。  
>因为插件是本人兴之所至所临时发布的项目（本人已近三年未做JAVA开发，代码水平请大家见谅），但基本插件都是在实际项目中经过检验的请大家放心使用，但因为项目目前主要数据库为MySQL，Mybatis实现使用Mapper.xml方式，所以代码生成时对于其他数据库和注解方式的支持未予考虑，请大家见谅。    
  
>V1.3.x版本的测试基准基于mybatis-3.5.0，同时向下兼容V3.4.0(某些插件需要context节点配置mybatis版本信息[[issues#70](https://github.com/itfsw/mybatis-generator-plugin/issues/70)])。老版本参见分支[V1.2.x](https://github.com/itfsw/mybatis-generator-plugin/tree/V1.2)；  
```xml
<context>
    <!-- 
        解决 批量插入插件（BatchInsertPlugin）在mybatis3.5.0以下版本无法返回自增主键的问题
        指定mybatis版本，让插件指定您所使用的mybatis版本生成对应代码
     -->
    <property name="mybatisVersion" value="3.4.0"/>
</context>
```

---------------------------------------
插件列表：  
* [查询单条数据插件（SelectOneByExamplePlugin）](#1-查询单条数据插件)
* [MySQL分页插件（LimitPlugin）](#2-mysql分页插件)
* [数据Model链式构建插件（ModelBuilderPlugin）](#3-数据model链式构建插件)
* [Example Criteria 增强插件（ExampleEnhancedPlugin）](#4-example-增强插件exampleandiforderby)
* [Example 目标包修改插件（ExampleTargetPlugin）](#5-example-目标包修改插件)
* [批量插入插件（BatchInsertPlugin）](#6-批量插入插件)
* [逻辑删除插件（LogicalDeletePlugin）](#7-逻辑删除插件)
* [数据Model属性对应Column获取插件（ModelColumnPlugin）](#8-数据model属性对应column获取插件)
* [存在即更新插件（UpsertPlugin）](#9-存在即更新插件)
* [Selective选择插入更新增强插件（SelectiveEnhancedPlugin）](#10-selective选择插入更新增强插件)
* [~~Table增加前缀插件（TablePrefixPlugin）~~](#11-table增加前缀插件)
* [~~Table重命名插件（TableRenamePlugin）~~](#12-table重命名插件)
* [自定义注释插件（CommentPlugin）](#13-自定义注释插件)
* [~~增量插件（IncrementsPlugin）~~](#14-增量插件)
* [查询结果选择性返回插件（SelectSelectivePlugin）](#15-查询结果选择性返回插件)
* [~~官方ConstructorBased配置BUG临时修正插件（ConstructorBasedBugFixPlugin）~~](#16-官方constructorbased配置bug临时修正插件)
* [乐观锁插件（OptimisticLockerPlugin）](#17-乐观锁插件)
* [表重命名配置插件（TableRenameConfigurationPlugin）](#18-表重命名配置插件)
* [Lombok插件（LombokPlugin）](#19-Lombok插件)
* [数据ModelCloneable插件（ModelCloneablePlugin）](#20-数据ModelCloneable插件)
* [状态枚举生成插件（EnumTypeStatusPlugin）](#21-状态枚举生成插件)
* [增量插件（IncrementPlugin）](#22-增量插件)
* [Mapper注解插件（MapperAnnotationPlugin）](#23-Mapper注解插件)

---------------------------------------
Maven引用：  
```xml
<dependency>
  <groupId>com.itfsw</groupId>
  <artifactId>mybatis-generator-plugin</artifactId>
  <version>1.3.5</version>
</dependency>
```
---------------------------------------
MyBatis Generator 参考配置（插件依赖应该配置在mybatis-generator-maven-plugin插件依赖中[[issues#6]](https://github.com/itfsw/mybatis-generator-plugin/issues/6)）
```xml
<!-- mybatis-generator 自动代码插件 -->
<plugin>
    <groupId>org.mybatis.generator</groupId>
    <artifactId>mybatis-generator-maven-plugin</artifactId>
    <version>1.3.7</version>
    <configuration>
        <!-- 配置文件 -->
        <configurationFile>src/main/resources/mybatis-generator.xml</configurationFile>
        <!-- 允许移动和修改 -->
        <verbose>true</verbose>
        <overwrite>true</overwrite>
    </configuration>
    <dependencies>
        <!-- jdbc 依赖 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.driver.version}</version>
        </dependency>
        <dependency>
            <groupId>com.itfsw</groupId>
            <artifactId>mybatis-generator-plugin</artifactId>
            <version>${mybatis.generator.plugin.version}</version>
        </dependency>
    </dependencies>
</plugin>
```
---------------------------------------
gradle集成[[issues#41]](https://github.com/itfsw/mybatis-generator-plugin/issues/41)），感谢[masa-kunikata](https://github.com/masa-kunikata)提供的脚本。
```gradle
// https://gist.github.com/masa-kunikata/daaf0f51a8ab9b808f61805407e1654c
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "gradle.plugin.com.arenagod.gradle:mybatis-generator-plugin:1.4"
    }
}

apply plugin: 'java-library'
apply plugin: "com.arenagod.gradle.MybatisGenerator"
apply plugin: 'eclipse'

sourceCompatibility = 1.8
targetCompatibility = 1.8


def mybatisGeneratorCore = 'org.mybatis.generator:mybatis-generator-core:1.3.7'
def itfswMybatisGeneratorPlugin = 'com.itfsw:mybatis-generator-plugin:1.3.5'

mybatisGenerator {
  verbose = false
  configFile = "config/mybatisGenerator/generatorConfig.xml"

  dependencies {
    mybatisGenerator project(':')
    mybatisGenerator itfswMybatisGeneratorPlugin
    mybatisGenerator mybatisGeneratorCore
  }
}

repositories {
    mavenCentral()
}

dependencies {
    compile mybatisGeneratorCore
    compile itfswMybatisGeneratorPlugin
    testCompile 'junit:junit:4.12'
}

def defaultEncoding = 'UTF-8'

compileJava {
    options.encoding = defaultEncoding
}
compileTestJava {
    options.encoding = defaultEncoding
}
```
---------------------------------------
### 1. 查询单条数据插件
对应表Mapper接口增加了方法  
插件：
```xml
<!-- 查询单条数据插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.SelectOneByExamplePlugin"/>
```
使用：  
```java
public interface TbMapper {    
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    Tb selectOneByExample(TbExample example);
    
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    // Model WithBLOBs 时才有
    TbWithBLOBs selectOneByExampleWithBLOBs(TbExample example);
}
```
### 2. MySQL分页插件
对应表Example类增加了Mysql分页方法，limit(Integer rows)、limit(Integer offset, Integer rows)和page(Integer page, Integer pageSize)  
>warning: 分页默认从0开始，目前网上流行的大多数前端框架分页都是从0开始，插件保持这种方式（可通过配置startPage参数修改）； 

插件：
```xml
<!-- MySQL分页插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.LimitPlugin">
    <!-- 通过配置startPage影响Example中的page方法开始分页的页码，默认分页从0开始 -->
    <property name="startPage" value="0"/>
</plugin>
```
使用：  
```java
public class TbExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    protected Integer offset;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    protected Integer rows;
    
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    public TbExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    public TbExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    public TbExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        // !!! 如果配置了startPage且不为0
        // this.offset = (page - startPage) * pageSize;
        this.rows = pageSize;
        return this;
    }
    
    // offset 和 rows 的getter&setter
    
    // 修正了clear方法
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb
     *
     * @mbg.generated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
        rows = null;
        offset = null;
    }
}
public class Test {
    public static void main(String[] args) {
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1GreaterThan(1)
                .example()
                .limit(10)  // 查询前10条
                .limit(10, 10)  // 查询10~20条
                .page(1, 10)    // 查询第2页数据（每页10条）
        );
    }
}
```
### 3. 数据Model链式构建插件
这个是仿jquery的链式调用强化了表的Model的赋值操作  
插件：
```xml
<!-- 数据Model链式构建插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.ModelBuilderPlugin"/>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 直接new表Model的内部Builder类，赋值后调用build()方法返回对象
        Tb table = new Tb.Builder()
               .field1("xx")
               .field2("xx")
               .field3("xx")
               .field4("xx")
               .build();
        // 或者使用builder静态方法创建Builder
        Tb table = Tb.builder()
               .field1("xx")
               .field2("xx")
               .field3("xx")
               .field4("xx")
               .build();
    }
}
```
### 4. Example 增强插件(example,andIf,orderBy)
* Criteria的快速返回example()方法。  
* ~~Criteria链式调用增强，以前如果有按条件增加的查询语句会打乱链式查询构建，现在有了andIf(boolean ifAdd, CriteriaAdd add)方法可一直使用链式调用下去。~~
* Example增强了setOrderByClause方法，新增orderBy(String orderByClause)、orderBy(String ... orderByClauses)方法直接返回example，增强链式调用，配合数据Model属性对应Column获取插件（ModelColumnPlugin）使用效果更佳。 
* 增加基于column的操作，当配置了[数据Model属性对应Column获取插件（ModelColumnPlugin）](#8-数据model属性对应column获取插件)插件时，提供column之间的比对操作。  
* 增加createCriteria静态方法newAndCreateCriteria简写example的创建。
* 增加when方法（Example和Criteria都有），方便根据不同条件附加对应操作。

插件：
```xml
<!-- Example Criteria 增强插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.ExampleEnhancedPlugin">
    <!-- 是否支持已经过时的andIf方法（推荐使用when代替），默认支持 -->
    <property name="enableAndIf" value="true"/>
</plugin>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // -----------------------------------example-----------------------------------
        // 表Example.Criteria增加了工厂方法example()支持，使用后可链式构建查询条件使用example()返回Example对象
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1EqualTo(1)
                .andField2EqualTo("xxx")
                .example()
        );
        
        // ----------------- andIf （@Deprecated 尽量使用when代替）  ---------------------
        // Criteria增强了链式调用，现在一些按条件增加的查询条件不会打乱链式调用了
        // old
        TbExample oldEx = new TbExample();
        TbExample.Criteria criteria = oldEx
                .createCriteria()
                .andField1EqualTo(1)
                .andField2EqualTo("xxx");
        // 如果随机数大于0.5，附加Field3查询条件
        if (Math.random() > 0.5){
            criteria.andField3EqualTo(2)
                    .andField4EqualTo(new Date());
        }
        this.tbMapper.selectByExample(oldEx);

        // new
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1EqualTo(1)
                .andField2EqualTo("xxx")
                // 如果随机数大于0.5，附加Field3查询条件
                .andIf(Math.random() > 0.5, new TbExample.Criteria.ICriteriaAdd() {
                    @Override
                    public TbExample.Criteria add(TbExample.Criteria add) {
                        return add.andField3EqualTo(2)
                                .andField4EqualTo(new Date());
                    }
                })
                // 当然最简洁的写法是采用java8的Lambda表达式，当然你的项目是Java8+
                .andIf(Math.random() > 0.5, add -> add
                        .andField3EqualTo(2)
                        .andField4EqualTo(new Date())
                )
                .example()
        );
        
        // -----------------------------------when-----------------------------------
        this.tbMapper.selectByExample(
                TbExample.newAndCreateCriteria()
                // 如果随机数大于1，附加Field3查询条件
                .when(Math.random() > 1, new TbExample.ICriteriaWhen() {
                    @Override
                    public void criteria(TbExample.Criteria criteria) {
                        criteria.andField3EqualTo(2);
                    }
                })
                // 当然最简洁的写法是采用java8的Lambda表达式，当然你的项目是Java8+
                .when(Math.random() > 1, criteria -> criteria.andField3EqualTo(2))
                // 也支持 if else 这种写法
                .when(Math.random() > 1, criteria -> criteria.andField3EqualTo(2), criteria -> criteria.andField3EqualTo(3))
                .example()
                // example上也支持 when 方法
                .when(true, example -> example.orderBy("field1 DESC"))
        );
        
        // -----------------------------------orderBy-----------------------------------
        // old
        TbExample ex = new TbExample();
        ex.createCriteria().andField1GreaterThan(1);
        ex.setOrderByClause("field1 DESC");
        this.tbMapper.selectByExample(ex);

        // new
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1GreaterThan(1)
                .example()
                .orderBy("field1 DESC")
                // 这个配合数据Model属性对应Column获取插件（ModelColumnPlugin）使用
                .orderBy(Tb.Column.field1.asc(), Tb.Column.field3.desc())
        );
        
        // -----------------------------------column-----------------------------------
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1EqualToColumn(Tb.Column.field2)   // where field1 = field2
                .andField1NotEqualToColumn(Tb.Column.field2)    // where field1 <> field2
                .andField1GreaterThanColumn(Tb.Column.field2)   // where field1 > field2
                .andField1GreaterThanOrEqualToColumn(Tb.Column.field2)  // where field1 >= field2
                .andField1LessThanColumn(Tb.Column.field2)  // where field1 < field2
                .andField1LessThanOrEqualToColumn(Tb.Column.field2) // where field1 <= field2
                .example()
        );
        
        // ---------------------------- static createCriteria  -----------------------
        // simple
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .example()
        );
        // new
        this.tbMapper.selectByExample(
                TbExample.newAndCreateCriteria()
                .example()
        );
    }
}
```
### 5. Example 目标包修改插件
Mybatis Generator 插件默认把Model类和Example类都生成到一个包下，这样该包下类就会很多不方便区分，该插件目的就是把Example类独立到一个新包下，方便查看。  
插件：
```xml
<!-- Example 目标包修改插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.ExampleTargetPlugin">
    <!-- 修改Example类生成到目标包下 -->
    <property name="targetPackage" value="com.itfsw.mybatis.generator.dao.example"/>
</plugin>
```
### 6. 批量插入插件
提供了批量插入batchInsert和batchInsertSelective方法，需配合数据Model属性对应Column获取插件（ModelColumnPlugin）插件使用，实现类似于insertSelective插入列！    
>warning: 插件生成的batchInsertSelective方法在使用时必须指定selective列，因为插件本身是预编译生成sql,对于批量数据是无法提供类似insertSelective非空插入的方式的;    

插件：
```xml
<!-- 批量插入插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.BatchInsertPlugin">
    <!-- 
    开启后可以实现官方插件根据属性是否为空决定是否插入该字段功能
    ！需开启allowMultiQueries=true多条sql提交操作，所以不建议使用！插件默认不开启
    -->
    <property name="allowMultiQueries" value="false"/>
</plugin>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 构建插入数据
        List<Tb> list = new ArrayList<>();
        list.add(
                Tb.builder()
                .field1(0)
                .field2("xx0")
                .field3(0)
                .createTime(new Date())
                .build()
        );
        list.add(
                Tb.builder()
                .field1(1)
                .field2("xx1")
                .field3(1)
                .createTime(new Date())
                .build()
        );
        // 普通插入，插入所有列
        this.tbMapper.batchInsert(list);
        // !!!下面按需插入指定列（类似于insertSelective），需要数据Model属性对应Column获取插件（ModelColumnPlugin）插件
        this.tbMapper.batchInsertSelective(list, Tb.Column.field1, Tb.Column.field2, Tb.Column.field3, Tb.Column.createTime);
        // 或者排除某些列
        this.tbMapper.batchInsertSelective(list, Tb.Column.excludes(Tb.Column.id, Tb.Column.delFlag));
    }
}
```
### 7. 逻辑删除插件
因为很多实际项目数据都不允许物理删除，多采用逻辑删除，所以单独为逻辑删除做了一个插件，方便使用。  
- 增加logicalDeleteByExample和logicalDeleteByPrimaryKey方法；
- 增加selectByPrimaryKeyWithLogicalDelete方法（[[pull#12]](https://github.com/itfsw/mybatis-generator-plugin/pull/12)）；
- 查询构造工具中增加逻辑删除条件andLogicalDeleted(boolean)；
- 数据Model增加逻辑删除条件andLogicalDeleted(boolean)；
- 增加逻辑删除常量IS_DELETED（已删除 默认值）、NOT_DELETED（未删除 默认值）（[[issues#11]](https://github.com/itfsw/mybatis-generator-plugin/issues/11)）；
- 增加逻辑删除枚举；

>warning: 注意在配合[状态枚举生成插件（EnumTypeStatusPlugin）](#21-状态枚举生成插件)使用时的注释格式，枚举数量必须大于等于2，且逻辑删除和未删除的值能在枚举中找到。
 
插件：
```xml
<xml>
    <!-- 逻辑删除插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.LogicalDeletePlugin">
        <!-- 这里配置的是全局逻辑删除列和逻辑删除值，当然在table中配置的值会覆盖该全局配置 -->
        <!-- 逻辑删除列类型只能为数字、字符串或者布尔类型 -->
        <property name="logicalDeleteColumn" value="del_flag"/>
        <!-- 逻辑删除-已删除值 -->
        <property name="logicalDeleteValue" value="9"/>
        <!-- 逻辑删除-未删除值 -->
        <property name="logicalUnDeleteValue" value="0"/>
        
        <!-- 是否生成逻辑删除常量(只有开启时 logicalDeleteConstName、logicalUnDeleteConstName 才生效) -->
        <property name="enableLogicalDeleteConst" value="true"/>
        <!-- 逻辑删除常量名称，不配置默认为 IS_DELETED -->
        <property name="logicalDeleteConstName" value="IS_DELETED"/>
        <!-- 逻辑删除常量（未删除）名称，不配置默认为 NOT_DELETED -->
        <property name="logicalUnDeleteConstName" value="NOT_DELETED"/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这里可以单独表配置逻辑删除列和删除值，覆盖全局配置 -->
        <property name="logicalDeleteColumn" value="del_flag"/>
        <property name="logicalDeleteValue" value="1"/>
        <property name="logicalUnDeleteValue" value="-1"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 1. 逻辑删除ByExample
        this.tbMapper.logicalDeleteByExample(
                new TbExample()
                .createCriteria()
                .andField1EqualTo(1)
                .example()
        );

        // 2. 逻辑删除ByPrimaryKey
        this.tbMapper.logicalDeleteByPrimaryKey(1L);

        // 3. 同时Example中提供了一个快捷方法来过滤逻辑删除数据
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1EqualTo(1)
                // 新增了一个andDeleted方法过滤逻辑删除数据
                .andLogicalDeleted(true)
                // 当然也可直接使用逻辑删除列的查询方法，我们数据Model中定义了一个逻辑删除常量DEL_FLAG
                .andDelFlagEqualTo(Tb.IS_DELETED)
                .example()
        );
        
        // 4. 逻辑删除和未删除常量
        Tb tb = Tb.builder()
                .delFlag(Tb.IS_DELETED)   // 删除
                .delFlag(Tb.NOT_DELETED)    // 未删除
                .build()
                .andLogicalDeleted(true);   // 也可以在这里使用true|false设置逻辑删除

        // 5. selectByPrimaryKeyWithLogicalDelete V1.0.18 版本增加
        // 因为之前觉得既然拿到了主键这种查询没有必要，但是实际使用中可能存在根据主键判断是否逻辑删除的情况，这种场景还是有用的
        this.tbMapper.selectByPrimaryKeyWithLogicalDelete(1, true);
        
        // 6. 使用逻辑删除枚举
        Tb tb = Tb.builder()
                .delFlag(Tb.DelFlag.IS_DELETED)   // 删除
                .delFlag(Tb.DelFlag.NOT_DELETED)    // 未删除
                .build()
                .andLogicalDeleted(true);   // 也可以在这里使用true|false设置逻辑删除
    }
}
```
通过注解覆盖逻辑删除配置
```sql
CREATE TABLE `tb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注释1',
  `del_flag` smallint(3) COMMENT '注释[enable(1):第一项必须是代表未删除, disable(0):第二项必须是代表已删除, other(2):当然还可以附加其他状态]',
  PRIMARY KEY (`id`)
);
```
```java
/**
 * 生成的Tb会根据注释覆盖逻辑删除配置
 */
public class Tb {
    public static final Short ENABLE = DelFlag.ENABLE.value();
    public static final Short DISABLE = DelFlag.DISABLE.value();
    
    public enum DelFlag {
        ENABLE(new Short("1"), "第一项必须是代表未删除"),
        DISABLE(new Short("0"), "第二项必须是代表已删除"),
        OTHER(new Short("2"), "当然还可以附加其他状态");
        
        private final Short value;
        private final String name;
        
        DelFlag(Short value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public Short getValue() {
            return this.value;
        }
        public Short value() {
            return this.value;
        }
        public String getName() {
            return this.name;
        }
    }
}
```

### 8. 数据Model属性对应Column获取插件
项目中我们有时需要获取数据Model对应数据库字段的名称，一般直接根据数据Model的属性就可以猜出数据库对应column的名字，可是有的时候当column使用了columnOverride或者columnRenamingRule时就需要去看数据库设计了，所以提供了这个插件获取model对应的数据库Column。  
* 配合Example Criteria 增强插件（ExampleEnhancedPlugin）使用，这个插件还提供了asc()和desc()方法配合Example的orderBy方法效果更佳。
* 配合批量插入插件（BatchInsertPlugin）使用，batchInsertSelective(@Param("list") List<XXX> list, @Param("selective") XXX.Column ... insertColumns)。  
* 提供静态excludes方法来进行快速反选。  

插件：
```xml
<!-- 数据Model属性对应Column获取插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.ModelColumnPlugin"/>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 1. 获取Model对应column
        String column = Tb.Column.createTime.value();

        // 2. 配合Example Criteria 增强插件（ExampleEnhancedPlugin）使用orderBy方法
        // old
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1GreaterThan(1)
                .example()
                .orderBy("field1 DESC, field3 ASC")
        );

        // better
        this.tbMapper.selectByExample(
                new TbExample()
                .createCriteria()
                .andField1GreaterThan(1)
                .example()
                .orderBy(Tb.Column.field1.desc(), Tb.Column.field3.asc())
        );
        
        // 3. 配合批量插入插件（BatchInsertPlugin）使用实现按需插入指定列
        List<Tb> list = new ArrayList<>();
        list.add(
                Tb.builder()
                .field1(0)
                .field2("xx0")
                .field3(0)
                .field4(new Date())
                .build()
        );
        list.add(
                Tb.builder()
                .field1(1)
                .field2("xx1")
                .field3(1)
                .field4(new Date())
                .build()
        );
        // 这个会插入表所有列
        this.tbMapper.batchInsert(list);
        // 下面按需插入指定列（类似于insertSelective）
        this.tbMapper.batchInsertSelective(list, Tb.Column.field1, Tb.Column.field2, Tb.Column.field3, Tb.Column.createTime);
        
        // 4. excludes 方法
        this.tbMapper.batchInsertSelective(list, Tb.Column.excludes(Tb.Column.id, Tb.Column.delFlag));
    }
}
```
### 9. 存在即更新插件
1. 使用MySQL的[“insert ... on duplicate key update”](https://dev.mysql.com/doc/refman/5.7/en/insert-on-duplicate.html)实现存在即更新操作，简化数据入库操作（[[issues#2]](https://github.com/itfsw/mybatis-generator-plugin/issues/2)）。  
2. 在开启allowMultiQueries=true（默认不会开启）情况下支持upsertByExample，upsertByExampleSelective操作，但强力建议不要使用（需保证团队没有使用statement提交sql,否则会存在sql注入风险）（[[issues#2]](https://github.com/itfsw/mybatis-generator-plugin/issues/2)）。

插件：
```xml
<!-- 存在即更新插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.UpsertPlugin">
    <!-- 
    支持upsertByExample，upsertByExampleSelective操作
    ！需开启allowMultiQueries=true多条sql提交操作，所以不建议使用！插件默认不开启
    -->
    <property name="allowMultiQueries" value="false"/>
    <!-- 
    开启批量功能，支持batchUpsert,batchUpsertWithBLOBs,batchUpserSelective 
    ！这几个方法中无法支持IncrementsPlugin的方法！插件默认不开启
    -->
    <property name="allowBatchUpsert" value="fasle"/>
</plugin>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 1. 未入库数据入库，执行insert
        Tb tb = Tb.builder()
                .field1(1)
                .field2("xx0")
                .delFlag(Tb.DEL_FLAG_ON)
                .build();
        int k0 = this.tbMapper.upsert(tb);
        // 2. 已入库数据再次入库,执行update（！！需要注意如触发update其返回的受影响行数为2）
        tb.setField2("xx1");
        int k1 = this.tbMapper.upsert(tb);

        // 3. 类似insertSelective实现选择入库
        Tb tb1 = Tb.builder()
                .field1(1)
                .field2("xx0")
                .build();
        int k2 = this.tbMapper.upsertSelective(tb1);
        tb1.setField2("xx1");
        int k3 = this.tbMapper.upsertSelective(tb1);

        // --------------------------------- allowMultiQueries=true ------------------------------
        // 4. 开启allowMultiQueries后增加upsertByExample，upsertByExampleSelective但强力建议不要使用（需保证团队没有使用statement提交sql,否则会存在sql注入风险）
        Tb tb2 = Tb.builder()
                .field1(1)
                .field2("xx0")
                .field3(1003)
                .delFlag(Tb.DEL_FLAG_ON)
                .build();
        int k4 = this.tbMapper.upsertByExample(tb2,
                new TbExample()
                        .createCriteria()
                        .andField3EqualTo(1003)
                        .example()
        );
        tb2.setField2("xx1");
        // !!! upsertByExample,upsertByExampleSelective触发更新时，更新条数返回是有问题的，这里只会返回0
        // 这是mybatis自身问题，也是不怎么建议开启allowMultiQueries功能原因之一
        int k5 = this.tbMapper.upsertByExample(tb2,
                new TbExample()
                        .createCriteria()
                        .andField3EqualTo(1003)
                        .example()
        );
        // upsertByExampleSelective 用法类似
        
        // 当Model WithBLOBs 存在时上述方法增加对应的 WithBLOBs 方法，举例如下：
        TbWithBLOBs tb3 = Tb.builder()
                            .field1(1)
                            .field2("xx0")
                            .delFlag(Tb.DEL_FLAG_ON)
                            .build();
        int k6 = this.tbMapper.upsertWithBLOBs(tb);
        
        // --------------------------------- allowBatchUpsert=true ------------------------------
        List<Tb> list = new ArrayList<>();
        list.add(
                Tb.builder()
                .field1(0)
                .field2("xx0")
                .field3(0)
                .field4(new Date())
                .build()
        );
        list.add(
                Tb.builder()
                .field1(1)
                .field2("xx1")
                .field3(1)
                .field4(new Date())
                .build()
        );
        this.tbMapper.batchUpsert(list);    // 对于BLOBs 有batchUpsertWithBLOBs方法
        this.tbMapper.batchUpsertSelective(list, Tb.Column.field1, Tb.Column.field2, Tb.Column.field3, Tb.Column.createTime);
        this.tbMapper.batchUpsertSelective(list, Tb.Column.excludes(Tb.Column.id, Tb.Column.delFlag)); // 排除某些列
    }
}
```
### 10. Selective选择插入更新增强插件
项目中往往需要指定某些字段进行插入或者更新，或者把某些字段进行设置null处理，这种情况下原生xxxSelective方法往往不能达到需求，因为它的判断条件是对象字段是否为null，这种情况下可使用该插件对xxxSelective方法进行增强。  
>warning: 以前老版本（1.1.x）插件处理需要指定的列时是放入Model中指定的，但在实际使用过程中有同事反馈这个处理有点反直觉，导致某些新同事不能及时找到对应方法，而且和增强的SelectSelectivePlugin以及UpsertSelective使用方式都不一致，所以统一修改之。  

插件：
```xml
<!-- Selective选择插入更新增强插件 -->
<plugin type="com.itfsw.mybatis.generator.plugins.SelectiveEnhancedPlugin"/>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // ------------------------------ 新版本（SelectiveEnhancedPlugin）--------------------------------
        // 1. 指定插入或更新字段
        Tb tb = Tb.builder()
                .field1(1)
                .field2("xx2")
                .field3(1)
                .createTime(new Date())
                .build();
        // 只插入或者更新field1,field2字段
        this.tbMapper.insertSelective(tb, Tb.Column.field1, Tb.Column.field2);
        this.tbMapper.updateByExampleSelective(
                tb,
                new TbExample()
                        .createCriteria()
                        .andIdEqualTo(1l)
                        .example(),
                Tb.Column.field1, Tb.Column.field2
        );
        this.tbMapper.updateByPrimaryKeySelective(tb, Tb.Column.field1, Tb.Column.field2);
        this.tbMapper.upsertSelective(tb, Tb.Column.field1, Tb.Column.field2);
        this.tbMapper.upsertByExampleSelective(
                tb,
                new TbExample()
                        .createCriteria()
                        .andField3EqualTo(1)
                        .example(),
                Tb.Column.field1, Tb.Column.field2
        );

        // 2. 更新某些字段为null
        this.tbMapper.updateByPrimaryKeySelective(
                Tb.builder()
                .id(1l)
                .field1(null)   // 方便展示，不用设也可以
                .build(),
                Tb.Column.field1
        );
        
        // 3. 排除某些列
        this.tbMapper.insertSelective(tb, Tb.Column.excludes(Tb.Column.id, Tb.Column.delFlag));
    }
}
```
### 11. Table增加前缀插件
项目中有时会遇到配置多数据源对应多业务的情况，这种情况下可能会出现不同数据源出现重复表名，造成异常冲突。
该插件允许为表增加前缀，改变最终生成的Model、Mapper、Example类名以及xml名。  
>warning: 使用[Table重命名插件](12-table重命名插件)可以实现相同功能！  
>warning: 官方最新版本中已提供domainObjectRenamingRule支持(可以配合[表重命名配置插件](#18-表重命名配置插件)进行全局配置)！  
```xml
<table tableName="tb">
    <domainObjectRenamingRule searchString="^" replaceString="DB1" />
</table>
```
插件：
```xml
<xml>
    <!-- Table增加前缀插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.TablePrefixPlugin">
        <!-- 这里配置的是全局表前缀，当然在table中配置的值会覆盖该全局配置 -->
        <property name="prefix" value="Cm"/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这里可以单独表配置前缀，覆盖全局配置 -->
        <property name="suffix" value="Db1"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // Tb 表对应的Model、Mapper、Example类都增加了Db1的前缀
        // Model类名: Tb -> Db1Tb
        // Mapper类名: TbMapper -> Db1TbMapper
        // Example类名: TbExample -> Db1TbExample
        // xml文件名: TbMapper.xml -> Db1TbMapper.xml
    }
}
```
### 12. Table重命名插件
插件由来：  
记得才开始工作时某个隔壁项目组的坑爹项目，由于某些特定的原因数据库设计表名为t1~tn,字段名为f1~fn。
这种情况下如何利用Mybatis Generator生成可读的代码呢，字段可以用columnOverride来替换，Model、Mapper等则需要使用domainObjectName+mapperName来实现方便辨识的代码。  
>该插件解决：domainObjectName+mapperName?好吧我想简化一下，所以直接使用tableOverride（仿照columnOverride）来实现便于配置的理解。 

某些DBA喜欢在数据库设计时使用t_、f_这种类似的设计（[[issues#4]](https://github.com/itfsw/mybatis-generator-plugin/issues/4)），
这种情况下我们就希望能有类似[columnRenamingRule](http://www.mybatis.org/generator/configreference/columnRenamingRule.html)这种重命名插件来修正最终生成的Model、Mapper等命名。
>该插件解决：使用正则替换table生成的Model、Example、Mapper等命名。
 
项目中有时会遇到配置多数据源对应多业务的情况，这种情况下可能会出现不同数据源出现重复表名，造成异常冲突。
该插件可以实现和[Table增加前缀插件](11-table增加前缀插件)相同的功能，仿照如下配置。  
```xml
<property name="searchString" value="^"/>
<property name="replaceString" value="DB1"/>
```
>warning: 官方最新版本中已提供domainObjectRenamingRule支持(可以配合[表重命名配置插件](#18-表重命名配置插件)进行全局配置)！  
```xml
<table tableName="tb">
    <domainObjectRenamingRule searchString="^T" replaceString="" />
</table>
``` 

插件：
```xml
<xml>
    <!-- Table重命名插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.TableRenamePlugin">
        <!-- 可根据具体需求确定是否配置 -->
        <property name="searchString" value="^T"/>
        <property name="replaceString" value=""/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这个优先级最高，会忽略searchString、replaceString的配置 -->
        <property name="tableOverride" value="TestDb"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 1. 使用searchString和replaceString时，和Mybatis Generator一样使用的是java.util.regex.Matcher.replaceAll去进行正则替换
        // 表： T_USER
        // Model类名: TUser -> User
        // Mapper类名: TUserMapper -> UserMapper
        // Example类名: TUserExample -> UserExample
        // xml文件名: TUserMapper.xml -> UserMapper.xml
        
        // 2. 使用表tableOverride时，该配置优先级最高
        // 表： T_BOOK
        // Model类名: TBook -> TestDb
        // Mapper类名: TBookMapper -> TestDbMapper
        // Example类名: TBookExample -> TestDbExample
        // xml文件名: TBookMapper.xml -> TestDbMapper.xml
    }
}
```
### 13. 自定义注释插件
Mybatis Generator是原生支持自定义注释的（commentGenerator配置type属性），但使用比较麻烦需要自己实现CommentGenerator接口并打包配置赖等等。  
该插件借助freemarker极佳的灵活性实现了自定义注释的快速配置。  
>warning: 下方提供了一个参考模板，需要注意${mgb}的输出，因为Mybatis Generator就是通过该字符串判断是否为自身生成代码进行覆盖重写。  

>warning: 请注意拷贝参考模板注释前方空格，idea等工具拷贝进去后自动格式化会造成格式错乱。 

>warning: 模板引擎采用的是freemarker所以一些freemarker指令参数（如：<#if xx></#if>、${.now?string("yyyy-MM-dd HH:mm:ss")}）都是可以使用的，请自己尝试。

>warning: [默认模板](https://github.com/itfsw/mybatis-generator-plugin/blob/master/src/main/resources/default-comment.ftl)

| 注释ID | 传入参数 | 备注 |
| ----- | ----- | ---- |
| addJavaFileComment | mgb<br>[compilationUnit](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/CompilationUnit.java) | Java文件注释   |
| addComment | mgb<br>[xmlElement](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/xml/XmlElement.java) | Xml节点注释  |
| addRootComment | mgb<br>[rootElement](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/xml/XmlElement.java) | Xml根节点注释  |
| addFieldComment | mgb<br>[field](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/Field.java)<br>[introspectedTable](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/IntrospectedTable.java)<br>[introspectedColumn](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/IntrospectedColumn.java) | Java 字段注释(非生成Model对应表字段时，introspectedColumn可能不存在)  |
| addModelClassComment | mgb<br>[topLevelClass](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/TopLevelClass.java)<br>introspectedTable | 表Model类注释  |
| addClassComment | mgb<br>[innerClass](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/InnerClass.java)<br>introspectedTable | 类注释  |
| addEnumComment | mgb<br>[innerEnum](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/InnerEnum.java)<br>introspectedTable | 枚举注释  |
| addInterfaceComment | mgb<br>[innerInterface](https://github.com/itfsw/mybatis-generator-plugin/blob/master/src/main/java/com/itfsw/mybatis/generator/plugins/utils/enhanced/InnerInterface.java)<br>introspectedTable | 接口注释(itfsw插件新增类型)  |
| addGetterComment | mgb<br>[method](https://github.com/mybatis/generator/blob/master/core/mybatis-generator-core/src/main/java/org/mybatis/generator/api/dom/java/Method.java)<br>introspectedTable<br>introspectedColumn | getter方法注释(introspectedColumn可能不存在)  |
| addSetterComment | mgb<br>method<br>introspectedTable<br>introspectedColumn | setter方法注释(introspectedColumn可能不存在)  |
| addGeneralMethodComment | mgb<br>method<br>introspectedTable | 方法注释  |

插件：
```xml
<xml>
    <!-- 自定义注释插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.CommentPlugin">
        <!-- 自定义模板路径 -->
        <property name="template" value="src/main/resources/mybatis-generator-comment.ftl" />
    </plugin>
</xml>
```
使用（[参考模板](https://github.com/itfsw/mybatis-generator-plugin/blob/master/src/main/resources/default-comment.ftl)）：  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<template>
    <!-- #############################################################################################################
    /**
     * This method is called to add a file level comment to a generated java file. This method could be used to add a
     * general file comment (such as a copyright notice). However, note that the Java file merge function in Eclipse
     * does not deal with this comment. If you run the generator repeatedly, you will only retain the comment from the
     * initial run.
     * <p>
     *
     * The default implementation does nothing.
     *
     * @param compilationUnit
     *            the compilation unit
     */
    -->
    <comment ID="addJavaFileComment"></comment>

    <!-- #############################################################################################################
    /**
     * This method should add a suitable comment as a child element of the specified xmlElement to warn users that the
     * element was generated and is subject to regeneration.
     *
     * @param xmlElement
     *            the xml element
     */
    -->
    <comment ID="addComment"><![CDATA[
<!--
  WARNING - ${mgb}
  This element is automatically generated by MyBatis Generator, do not modify.
  @project https://github.com/itfsw/mybatis-generator-plugin
-->
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * This method is called to add a comment as the first child of the root element. This method could be used to add a
     * general file comment (such as a copyright notice). However, note that the XML file merge function does not deal
     * with this comment. If you run the generator repeatedly, you will only retain the comment from the initial run.
     * <p>
     *
     * The default implementation does nothing.
     *
     * @param rootElement
     *            the root element
     */
    -->
    <comment ID="addRootComment"></comment>

    <!-- #############################################################################################################
    /**
     * This method should add a Javadoc comment to the specified field. The field is related to the specified table and
     * is used to hold the value of the specified column.
     * <p>
     *
     * <b>Important:</b> This method should add a the nonstandard JavaDoc tag "@mbg.generated" to the comment. Without
     * this tag, the Eclipse based Java merge feature will fail.
     *
     * @param field
     *            the field
     * @param introspectedTable
     *            the introspected table
     * @param introspectedColumn
     *            the introspected column
     */
    -->
    <comment ID="addFieldComment"><![CDATA[
<#if introspectedColumn??>
/**
    <#if introspectedColumn.remarks?? && introspectedColumn.remarks != ''>
 * Database Column Remarks:
        <#list introspectedColumn.remarks?split("\n") as remark>
 *   ${remark}
        </#list>
    </#if>
 *
 * This field was generated by MyBatis Generator.
 * This field corresponds to the database column ${introspectedTable.fullyQualifiedTable}.${introspectedColumn.actualColumnName}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
<#else>
/**
 * This field was generated by MyBatis Generator.
 * This field corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
</#if>
    ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds a comment for a model class.  The Java code merger should
     * be notified not to delete the entire class in case any manual
     * changes have been made.  So this method will always use the
     * "do not delete" annotation.
     *
     * Because of difficulties with the Java file merger, the default implementation
     * of this method should NOT add comments.  Comments should only be added if
     * specifically requested by the user (for example, by enabling table remark comments).
     *
     * @param topLevelClass
     *            the top level class
     * @param introspectedTable
     *            the introspected table
     */
    -->
    <comment ID="addModelClassComment"><![CDATA[
/**
<#if introspectedTable.remarks?? && introspectedTable.remarks != ''>
 * Database Table Remarks:
<#list introspectedTable.remarks?split("\n") as remark>
 *   ${remark}
</#list>
</#if>
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb} do_not_delete_during_merge
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the inner class comment.
     *
     * @param innerClass
     *            the inner class
     * @param introspectedTable
     *            the introspected table
     * @param markAsDoNotDelete
     *            the mark as do not delete
     */
    -->
    <comment ID="addClassComment"><![CDATA[
/**
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb}<#if markAsDoNotDelete?? && markAsDoNotDelete> do_not_delete_during_merge</#if>
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the enum comment.
     *
     * @param innerEnum
     *            the inner enum
     * @param introspectedTable
     *            the introspected table
     */
    -->
    <comment ID="addEnumComment"><![CDATA[
/**
 * This enum was generated by MyBatis Generator.
 * This enum corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the interface comment.
     *
     * @param innerInterface
     *            the inner interface
     * @param introspectedTable
     *            the introspected table
     */
    -->
    <comment ID="addInterfaceComment"><![CDATA[
/**
 * This interface was generated by MyBatis Generator.
 * This interface corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the getter comment.
     *
     * @param method
     *            the method
     * @param introspectedTable
     *            the introspected table
     * @param introspectedColumn
     *            the introspected column
     */
    -->
    <comment ID="addGetterComment"><![CDATA[
<#if introspectedColumn??>
/**
 * This method was generated by MyBatis Generator.
 * This method returns the value of the database column ${introspectedTable.fullyQualifiedTable}.${introspectedColumn.actualColumnName}
 *
 * @return the value of ${introspectedTable.fullyQualifiedTable}.${introspectedColumn.actualColumnName}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
<#else>
/**
 * This method was generated by MyBatis Generator.
 * This method returns the value of the field ${method.name?replace("get","")?replace("is", "")?uncap_first}
 *
 * @return the value of field ${method.name?replace("get","")?replace("is", "")?uncap_first}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
</#if>
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the setter comment.
     *
     * @param method
     *            the method
     * @param introspectedTable
     *            the introspected table
     * @param introspectedColumn
     *            the introspected column
     */
    -->
    <comment ID="addSetterComment"><![CDATA[
<#if introspectedColumn??>
/**
 * This method was generated by MyBatis Generator.
 * This method sets the value of the database column ${introspectedTable.fullyQualifiedTable}.${introspectedColumn.actualColumnName}
 *
 * @param ${method.parameters[0].name} the value for ${introspectedTable.fullyQualifiedTable}.${introspectedColumn.actualColumnName}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
<#else>
/**
 * This method was generated by MyBatis Generator.
 * This method sets the value of the field ${method.name?replace("set","")?uncap_first}
 *
 * @param ${method.parameters[0].name} the value for field ${method.name?replace("set","")?uncap_first}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
</#if>
        ]]></comment>

    <!-- #############################################################################################################
    /**
     * Adds the general method comment.
     *
     * @param method
     *            the method
     * @param introspectedTable
     *            the introspected table
     */
    -->
    <comment ID="addGeneralMethodComment"><![CDATA[
/**
 * This method was generated by MyBatis Generator.
 * This method corresponds to the database table ${introspectedTable.fullyQualifiedTable}
 *
 * ${mgb}
 * @project https://github.com/itfsw/mybatis-generator-plugin
 */
        ]]></comment>
</template>
```
### 14. 增量插件
为更新操作生成set filedxxx = filedxxx +/- inc 操作，方便某些统计字段的更新操作，常用于某些需要计数的场景；  

>warning：该插件在整合LombokPlugin使用时会生成大量附加代码影响代码美观，强力建议切换到新版插件[IncrementPlugin](#22-增量插件);    

插件：
```xml
<xml>
    <!-- 增量插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.IncrementsPlugin" />
    
    <table tableName="tb">
        <!-- 配置需要进行增量操作的列名称（英文半角逗号分隔） -->
        <property name="incrementsColumns" value="field1,field2"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 在构建更新对象时，配置了增量支持的字段会增加传入增量枚举的方法
        Tb tb = Tb.builder()
                .id(102)
                .field1(1, Tb.Builder.Inc.INC)  // 字段1 统计增加1
                .field2(2, Tb.Builder.Inc.DEC)  // 字段2 统计减去2
                .field4(new Date())
                .build();
        // 更新操作，可以是 updateByExample, updateByExampleSelective, updateByPrimaryKey
        // , updateByPrimaryKeySelective, upsert, upsertSelective等所有涉及更新的操作
        this.tbMapper.updateByPrimaryKey(tb);
    }
}
```
### 15. 查询结果选择性返回插件
一般我们在做查询优化的时候会要求查询返回时不要返回自己不需要的字段数据，因为Sending data所花费的时间和加大内存的占用
，所以我们看到官方对于大数据的字段会拆分成xxxWithBLOBs的操作，但是这种拆分还是不能精确到具体列返回。  
所以该插件的作用就是精确指定查询操作所需要返回的字段信息，实现查询的精确返回。  
>warning: 因为采用的是resultMap进行的属性绑定（即时设置了constructorBased=true也无效，因为参数个数不一致会导致异常，该插件也会另外生成一个基于属性绑定的resultMap），
所以可能会出现list中存在null元素的问题，这个是mybatis自身机制造成的当查询出来的某行所有列都为null时不会生成对象（PS：其实这个不能算是错误，mybatis这样处理也说的通，但是在使用时还是要注意null的判断，当然如果有什么配置或者其他处理方式欢迎反馈哦）。  

插件：
```xml
<xml>
    <!-- 查询结果选择性返回插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.SelectSelectivePlugin" />
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 查询操作精确返回需要的列
        this.tbMapper.selectByExampleSelective(
            new TbExample()
            .createCriteria()
            .andField1GreaterThan(1)
            .example(),
            Tb.Column.field1,
            Tb.Column.field2
        );
        // 同理还有 selectByPrimaryKeySelective，selectOneByExampleSelective（SelectOneByExamplePlugin插件配合使用）方法
        
        // 当然可以使用excludes
        this.tbMapper.selectByExampleSelective(
            new TbExample()
            .createCriteria()
            .andField1GreaterThan(1)
            .example(),
            Tb.Column.excludes(Tb.Column.id, Tb.Column.delFlag)
        );
    }
}
```
### 16. 官方ConstructorBased配置BUG临时修正插件
当javaModelGenerator配置constructorBased=true时，如果表中只有一个column类型为“blob”时java model没有生成BaseResultMap对应的构造函数，
这个bug已经反馈给官方[issues#267](https://github.com/mybatis/generator/issues/267)。  
> 官方V1.3.6版本将解决这个bug,老版本的可以使用这个插件临时修正问题。  

插件：
```xml
<xml>
    <!-- 官方ConstructorBased配置BUG临时修正插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.ConstructorBasedBugFixPlugin" />
</xml>
```
### 17. 乐观锁插件
为并发操作引入乐观锁，当发生删除或者更新操作时调用相应的WithVersion方法传入版本号，插件会在相应的查询条件上附加上版本号的检查，防止非法操作的发生。  
同时在更新操作中支持自定义nextVersion或者利用sql 的“set column = column + 1”去维护版本号。   

插件：
```xml
<xml>
    <!-- 乐观锁插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.OptimisticLockerPlugin">
        <!-- 是否启用自定义nextVersion，默认不启用(插件会默认使用sql的 set column = column + 1) -->
        <property name="customizedNextVersion" value="false"/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这里可以单独表配置，覆盖全局配置 -->
        <property name="customizedNextVersion" value="true"/>
        <!-- 指定版本列 -->
        <property name="versionColumn" value="version"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // ============================ 带版本号的删除更新等操作 ===============================
        int result = this.tbMapper.deleteWithVersionByExample(
                        100, // 版本号
                        new TbExample()
                           .createCriteria()
                           .andField1GreaterThan(1)
                           .example()
                     );
        if (result == 0){
            throw new Exception("没有找到数据或者数据版本号错误！");
        }
        // 对应生成的Sql: delete from tb WHERE version = 100 and ( ( field1 > 1 ) )
        
        // 带版本号的方法有：
        // deleteWithVersionByExample、deleteWithVersionByPrimaryKey、
        // updateWithVersionByExampleSelective、updateWithVersionByExampleWithBLOBs、updateWithVersionByExample
        // updateWithVersionByPrimaryKeySelective、updateWithVersionByPrimaryKeyWithBLOBs、updateWithVersionByPrimaryKey
        
        // ============================= 使用默认版本号生成策略 ===========================
        this.tbMapper.updateWithVersionByPrimaryKey(
                100,    // 版本号
                Tb.builder()
                  .id(102)
                  .field1("ts1")
                  .build()
        );
        // 对应生成的Sql: update tb set version = version + 1, field1 = 'ts1' where version = 100 and id = 102
        
        // ============================= 使用自定义版本号生成策略 ===========================
        this.tbMapper.updateWithVersionByPrimaryKey(
                100,    // 版本号
                Tb.builder()
                  .id(102)
                  .field1("ts1")
                  .build()
                  .nextVersion(System.currentTimeMillis())    // 传入nextVersion
        );
        // 对应生成的Sql: update tb set version = 1525773888559, field1 = 'ts1' where version = 100 and id = 102
    }
}
```
### 18. 表重命名配置插件
官方提供了domainObjectRenamingRule(官方最新版本已提供)、columnRenamingRule分别进行生成的表名称和对应表字段的重命名支持，但是它需要每个表单独进行配置，对于常用的如表附带前缀“t_”、字段前缀“f_”这种全局性替换会比较麻烦。   
该插件提供了一种全局替换机制，当表没有单独指定domainObjectRenamingRule、columnRenamingRule时采用全局性配置。   
同时插件提供clientSuffix、exampleSuffix、modelSuffix来修改对应生成的类和文件的结尾（之前issue中有用户希望能把Mapper替换成Dao）。       
- 全局domainObjectRenamingRule  
```xml
<xml>
    <!-- 表重命名配置插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.TableRenameConfigurationPlugin">
        <property name="domainObjectRenamingRule.searchString" value="^T"/>
        <property name="domainObjectRenamingRule.replaceString" value=""/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这里可以禁用全局domainObjectRenamingRule配置 -->
        <property name="domainObjectRenamingRule.disable" value="true"/>
    </table>
    
    <table tableName="tb_ts1">
        <!-- 当然你也可以使用官方domainObjectRenamingRule的配置来覆盖全局配置 -->
        <domainObjectRenamingRule searchString="^Tb" replaceString="B"/>
    </table>
</xml>
```
- 全局columnRenamingRule  
```xml
<xml>
    <!-- 表重命名配置插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.TableRenameConfigurationPlugin">
        <!-- 需要注意，这里的正则和官方一样是比对替换都是原始表的column名称 -->
        <property name="columnRenamingRule.searchString" value="^f_"/>
        <property name="columnRenamingRule.replaceString" value="_"/>
    </plugin>
    
    <table tableName="tb">
        <!-- 这里可以禁用全局columnRenamingRule配置 -->
        <property name="columnRenamingRule.disable" value="true"/>
    </table>
    
    <table tableName="tb_ts1">
        <!-- 当然你也可以使用官方domainObjectRenamingRule的配置来覆盖全局配置 -->
        <columnRenamingRule searchString="^f_" replaceString="_"/>
    </table>
</xml>
```
- clientSuffix、exampleSuffix、modelSuffix  
```xml
<xml>
    <!-- 表重命名配置插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.TableRenameConfigurationPlugin">
        <!-- TbMapper -> TbDao, TbMapper.xml -> TbDao.xml -->
        <property name="clientSuffix" value="Dao"/>
        <!-- TbExmaple -> TbQuery -->
        <property name="exampleSuffix" value="Query"/>
        <!-- Tb -> TbEntity -->
        <property name="modelSuffix" value="Entity"/>
    </plugin>
</xml>
```
### 19. Lombok插件
使用Lombok的使用可以减少很多重复代码的书写，目前项目中已大量使用。
但Lombok的@Builder对于类的继承支持很不好，最近发现新版(>=1.18.2)已经提供了对@SuperBuilder的支持，所以新增该插件方便简写代码。

>warning1: @Builder注解在Lombok 版本 >= 1.18.2 的情况下才能开启，对于存在继承关系的model会自动替换成@SuperBuilder注解(目前IDEA的插件对于SuperBuilder的还不支持（作者已经安排上更新日程）, 可以开启配置supportSuperBuilderForIdea使插件在遇到@SuperBuilder注解时使用ModelBuilderPlugin替代该注解)。  

>warning2: 配合插件IncrementsPlugin（已不推荐使用，请使用新版[IncrementPlugin](#22-增量插件)解决该问题） 并且 @Builder开启的情况下，因为@SuperBuilder的一些限制，
插件模拟Lombok插件生成了一些附加代码可能在某些编译器上会提示错误，请忽略（Lombok = 1.18.2 已测试）。

```xml
<xml>
    <!-- Lombok插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.LombokPlugin">
        <!-- @Data 默认开启,同时插件会对子类自动附加@EqualsAndHashCode(callSuper = true)，@ToString(callSuper = true) -->
        <property name="@Data" value="true"/>
        <!-- @Builder 必须在 Lombok 版本 >= 1.18.2 的情况下开启，对存在继承关系的类自动替换成@SuperBuilder -->
        <property name="@Builder" value="false"/>
        <!-- @NoArgsConstructor 和 @AllArgsConstructor 使用规则和Lombok一致 -->
        <property name="@AllArgsConstructor" value="false"/>
        <property name="@NoArgsConstructor" value="false"/>
        <!-- @Getter、@Setter、@Accessors 等使用规则参见官方文档 -->
        <property name="@Accessors(chain = true)" value="false"/>
        <!-- 临时解决IDEA工具对@SuperBuilder的不支持问题，开启后(默认未开启)插件在遇到@SuperBuilder注解时会调用ModelBuilderPlugin来生成相应的builder代码 -->
        <property name="supportSuperBuilderForIdea" value="false"/>
    </plugin>
</xml>
```
### 20. 数据ModelCloneable插件
数据Model实现Cloneable接口。

```xml
<xml>
    <!-- 数据ModelCloneable插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.ModelCloneablePlugin"/>
</xml>
```
### 21. 状态枚举生成插件
数据库中经常会定义一些状态字段，该工具可根据约定的注释格式生成对应的枚举类，方便使用。
>warning：插件1.2.18版本以后默认开启自动扫描，根据约定注释格式自动生成对应枚举类
```xml
<xml>
    <!-- 状态枚举生成插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.EnumTypeStatusPlugin">
        <!-- 是否开启自动扫描根据约定注释格式生成枚举，默认true -->
        <property name="autoScan" value="true"/>
        <!-- autoScan为false,这里可以定义全局需要检查生成枚举类的列名 -->
        <property name="enumColumns" value="type, status"/>
    </plugin>
    <table tableName="tb">
        <!-- autoScan为false,也可以为单独某个table增加配置 -->
        <property name="enumColumns" value="user_type"/>
    </table>
</xml>
```
>warning: 约定的注释检查规则的正则表达式如下
```java
public class EnumTypeStatusPlugin {
    public final static String REMARKS_PATTERN = ".*\\s*\\[\\s*(\\w+\\s*\\(\\s*[\\u4e00-\\u9fa5_-a-zA-Z0-9]+\\s*\\)\\s*:\\s*[\\u4e00-\\u9fa5_-a-zA-Z0-9]+\\s*\\,?\\s*)+\\s*\\]\\s*.*";
}

```
使用
```sql
CREATE TABLE `tb` (
  `type` smallint(3) COMMENT '注释[success(0):成功, fail(1):失败]',
  `status` bigint(3) COMMENT '换行的注释
                                         [
                                           login_success(0):登录成功,
                                           login_fail(1):登录失败
                                         ]',
  `user_type` varchar(20) COMMENT '具体注释的写法是比较宽泛的，只要匹配上面正则就行
   [    success (   我是具体值  )    : 我是值的描述_我可以是中英文数字和下划线_xxx_123, fail_xx_3
    (1  ) :  失败] 后面也可以跟注释'                                       
);
```
```java
public class Tb {
    public enum Type {
        SUCCESS((short)0, "成功"),
        FAIL((short)1, "失败");
        
        private final Short value;
        private final String name;
        
        Type(Short value, String name) {
            this.value = value;
            this.name = name;
        }
        public Short getValue() {
            return this.value;
        }
        public Short value() {
            return this.value;
        }
        public String getName() {
            return this.name;
        }
    }

    public enum Status {
        LOGIN_SUCCESS(0L, "登录成功"),
        LOGIN_FAIL(1L, "登录失败");

        private final Long value;
        private final String name;

        Status(Long value, String name) {
            this.value = value;
            this.name = name;
        }
        public Long getValue() {
            return this.value;
        }
        public Long value() {
            return this.value;
        }
        public String getName() {
            return this.name;
        }
    }

    public enum UserType {
        SUCCESS("我是具体值", "我是值的描述_我可以是中英文数字和下划线_xxx_123"),
        FAIL_XX_3("1", "失败");

        private final String value;
        private final String name;

        UserType(String value, String name) {
            this.value = value;
            this.name = name;
        }
        public String getValue() {
            return this.value;
        }
        public String value() {
            return this.value;
        }
        public String getName() {
            return this.name;
        }
    }
}
```
### 22. 增量插件
为更新操作生成set filedxxx = filedxxx +/- inc 操作，方便某些统计字段的更新操作，常用于某些需要计数的场景,需配合（[ModelColumnPlugin](#8-数据model属性对应column获取插件)）插件使用；     

插件：
```xml
<xml>
    <!-- 增量插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.IncrementPlugin" />
    
    <table tableName="tb">
        <!-- 配置需要进行增量操作的列名称（英文半角逗号分隔） -->
        <property name="incrementColumns" value="field1,field2"/>
    </table>
</xml>
```
使用：  
```java
public class Test {
    public static void main(String[] args) {
        // 在构建更新对象时，配置了增量支持的字段会增加传入增量枚举的方法
        Tb tb = Tb.builder()
                .id(102)
                .field4(new Date())
                .build()
                .increment(Tb.Column.field1.inc(1)) // 字段1 统计增加1
                .increment(Tb.Column.field2.dec(2)); // 字段2 统计减去2
        // 更新操作，可以是 updateByExample, updateByExampleSelective, updateByPrimaryKey
        // , updateByPrimaryKeySelective, upsert, upsertSelective等所有涉及更新的操作
        this.tbMapper.updateByPrimaryKey(tb);
    }
}
```
### 23. Mapper注解插件
对官方的（[MapperAnnotationPlugin](http://www.mybatis.org/generator/reference/plugins.html)）增强，可自定义附加@Repository注解（IDEA工具对@Mapper注解支持有问题，使用@Autowired会报无法找到对应bean，附加@Repository后解决）；     

插件：
```xml
<xml>
    <!-- Mapper注解插件 -->
    <plugin type="com.itfsw.mybatis.generator.plugins.MapperAnnotationPlugin">
        <!-- @Mapper 默认开启 -->
        <property name="@Mapper" value="true"/>
        <!-- @Repository 默认关闭，开启后解决IDEA工具@Autowired报错 -->
        <property name="@Repository" value="false"/>
    </plugin>
</xml>
```