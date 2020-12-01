# database_diff
# 数据库表结构对比
#### 为的是解决每次部署项目时，还需要想想之前执行了哪些或者忘了记哪些DCL语句


可以做到
* [x] 缺少的表获取创建表语句
* [x] 缺少的字段生成DCL语句

待完成
* [ ] 外键和索引
* [ ] 不同视图，视图缺少
* [ ] 不同存储过程，存储过程缺少
* [x] 换一种获取结果的方式
* [ ] 将结果输出到一个.sql文件中

操作方式
1. 更改application.yml配置文件
2. 启动项目