# HBase的几个案例
1. 过滤器的使用
/src/com/xizhishi/hbase/filter/
2. 分页的实现
/src/com/xizhishi/hbase/page/
3. 使用MapReduce进行HBase和HDFS之间数据的读写
/src/com/xizhishi/hbase/mr/
### 测试数据
1. student.txt文件用于MapReduce的操作
```
95002,刘晨,女,19,IS
95017,王风娟,女,18,IS
95018,王一,女,19,IS
95013,冯伟,男,21,CS
95014,王小丽,女,19,CS
95019,邢小丽,女,19,IS
95020,赵钱,男,21,IS
95003,王敏,女,22,MA
95004,张立,男,19,IS
95012,孙花,女,20,CS
95010,孔小涛,男,19,CS
95005,刘刚,男,18,MA
95006,孙庆,男,23,CS
95007,易思玲,女,19,MA
95008,李娜,女,18,CS
95021,周二,男,17,MA
95022,郑明,男,20,MA
95001,李勇,男,20,CS
95011,包小柏,男,18,MA
95009,梦圆圆,女,18,MA
95015,王君,男,18,MA
```
2. userinfo表，用于测试过滤器和分页
```
create 'user_info',{NAME=>'base_info' },{NAME=>'extra_info'}
put 'user_info', 'user0001', 'base_info:name', 'zhangsan1'
put 'user_info', 'zhangsan_20180701_0001', 'base_info:name', 'zhangsan1'
put 'user_info', 'zhangsan_20180701_0002', 'base_info:name', 'zhangsan2'
put 'user_info', 'zhangsan_20180701_0003', 'base_info:name', 'zhangsan3'
put 'user_info', 'zhangsan_20180701_0004', 'base_info:name', 'zhangsan4'
put 'user_info', 'zhangsan_20180701_0005', 'base_info:name', 'zhangsan5'
put 'user_info', 'zhangsan_20180701_0006', 'base_info:name', 'zhangsan6'
put 'user_info', 'zhangsan_20180701_0007', 'base_info:name', 'zhangsan7'
put 'user_info', 'zhangsan_20180701_0008', 'base_info:name', 'zhangsan8'
put 'user_info', 'zhangsan_20180701_0001', 'base_info:age', '21'
put 'user_info', 'zhangsan_20180701_0002', 'base_info:age', '22'
put 'user_info', 'zhangsan_20180701_0003', 'base_info:age', '23'
put 'user_info', 'zhangsan_20180701_0004', 'base_info:age', '24'
put 'user_info', 'zhangsan_20180701_0005', 'base_info:age', '25'
put 'user_info', 'zhangsan_20180701_0006', 'base_info:age', '26'
put 'user_info', 'zhangsan_20180701_0007', 'base_info:age', '27'
put 'user_info', 'zhangsan_20180701_0008', 'base_info:age', '28'
put 'user_info', 'zhangsan_20180701_0001', 'extra_info:Hobbies', 'music'
put 'user_info', 'zhangsan_20180701_0002', 'extra_info:Hobbies', 'sport'
put 'user_info', 'zhangsan_20180701_0003', 'extra_info:Hobbies', 'music'
put 'user_info', 'zhangsan_20180701_0004', 'extra_info:Hobbies', 'sport'
put 'user_info', 'zhangsan_20180701_0005', 'extra_info:Hobbies', 'music'
put 'user_info', 'zhangsan_20180701_0006', 'extra_info:Hobbies', 'sport'
put 'user_info', 'zhangsan_20180701_0007', 'extra_info:Hobbies', 'music'
put 'user_info', 'baiyc_20180716_0001', 'base_info:name', 'baiyc1'
put 'user_info', 'baiyc_20180716_0002', 'base_info:name', 'baiyc2'
put 'user_info', 'baiyc_20180716_0003', 'base_info:name', 'baiyc3'
put 'user_info', 'baiyc_20180716_0004', 'base_info:name', 'baiyc4'
put 'user_info', 'baiyc_20180716_0005', 'base_info:name', 'baiyc5'
put 'user_info', 'baiyc_20180716_0006', 'base_info:name', 'baiyc6'
put 'user_info', 'baiyc_20180716_0007', 'base_info:name', 'baiyc7'
put 'user_info', 'baiyc_20180716_0008', 'base_info:name', 'baiyc8'
put 'user_info', 'baiyc_20180716_0001', 'base_info:age', '21'
put 'user_info', 'baiyc_20180716_0002', 'base_info:age', '22'
put 'user_info', 'baiyc_20180716_0003', 'base_info:age', '23'
put 'user_info', 'baiyc_20180716_0004', 'base_info:age', '24'
put 'user_info', 'baiyc_20180716_0005', 'base_info:age', '25'
put 'user_info', 'baiyc_20180716_0006', 'base_info:age', '26'
put 'user_info', 'baiyc_20180716_0007', 'base_info:age', '27'
put 'user_info', 'baiyc_20180716_0008', 'base_info:age', '28'
put 'user_info', 'baiyc_20180716_0001', 'extra_info:Hobbies', 'music'
put 'user_info', 'baiyc_20180716_0002', 'extra_info:Hobbies', 'sport'
put 'user_info', 'baiyc_20180716_0003', 'extra_info:Hobbies', 'music'
put 'user_info', 'baiyc_20180716_0004', 'extra_info:Hobbies', 'sport'
put 'user_info', 'baiyc_20180716_0005', 'extra_info:Hobbies', 'music'
put 'user_info', 'baiyc_20180716_0006', 'extra_info:Hobbies', 'sport'
put 'user_info', 'baiyc_20180716_0007', 'extra_info:Hobbies', 'music'
put 'user_info', 'baiyc_20180716_0008', 'extra_info:Hobbies', 'sport'
put 'user_info', 'rk0001', 'base_info:name', 'luoyufeng'
put 'user_info', 'rk0001', 'base_info:name', 'zhangsan'
```
