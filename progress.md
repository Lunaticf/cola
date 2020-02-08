### 无锁缓存框架 Disruptor

使用无锁的方式实现了一个环形队列，环形队列内部为一个普通的数组。
disruptor快的原因一是无锁，二是解决了伪共享，padding用得好。

#### CPU Cache伪共享问题
我们知道CPU有高速缓存，而cache读写数据的最小单位为一个缓存行，
一个缓存行的大小一般为32字节到128字节。
所以如果两个变量存放在同一个缓存行，多线程访问会影响彼此的性能。
比如CPU1上的线程更新了缓存行中的X，那么CPU2上的缓存行就会失效，
同一行的Y即使没有修改，也会无效，导致cache无法命中。

如果CPU经常miss，效率自然会下降。

在Oracle JDK 8 / OpenJDK 8里有一个新功能，叫做 @Contended ，可以用来减少false sharing的情况。

本质上来说就是用户在源码上使用@Contended注解来标注哪些字段要单独处理，避免与其它字段放得太近导致false sharing，然后JVM的实现在计算对象布局的时候就会自动把那些字段拿出来并且插入合适的大小padding。
要在用户代码（非bootstrap class loader或extension class loader所加载的类）中使用@Contended注解的话，需要使用 -XX:-RestrictContended 参数。
(来自R大)

https://zhuanlan.zhihu.com/p/21355046
https://tech.meituan.com/2016/11/18/disruptor.html

## 参考1 老钱 rpckids

redis的事件驱动reactor模型
https://zhuanlan.zhihu.com/p/24305679

epoll原理
https://zhuanlan.zhihu.com/p/63179839
1. 网卡会把接收到的数据写入内存
2. cpu会执行各种系统已经设置好的中断程序 响应中断
比如按下键盘 键盘会给CPU中断引脚发出高电平，CPU执行键盘中断程序
3. 网卡数据写入内存后，网卡向CPU发出中断信号 CPU执行网卡中断程序
4. 进程阻塞的原理？比如进程创建socket后，阻塞在recv函数
- 实际上，socket是一个文件描述符，该socket会维护一个等待队列，当程序执行到recv时，
操作系统会将进程A从工作队列移动到该fd的等待队列中，等socket有数据了就会把该队列重新移入
操作系统的可运行队列。
5. 网卡中断程序会把网络数据移动到socket的接收缓冲区，再唤醒进程A

一个小问题，操作系统如何知道网络数据是哪个socket的，因为网络数据包里有端口号。
当然，为了提高处理速度，操作系统会维护端口号到socket的索引结构，以快速读取。

第二个问题，一个进程A如何监听多个socket的数据，recv只能监听一个socket。

### select
select的实现思路很直接，A可以调用操作系统提供的select函数，传入三个socket，
然后A就会都放到三个socket的等待队列里面，这样任意一个socket有数据，就会唤醒A。
- 缺点1 用户程序需要遍历每个socket，查看是哪个socket有数据
- 缺点2 每次调用select都需要将进程加入到所有监视socket的等待队列，每次唤醒都需要从每个队列中移除。

### epoll
当某个进程调用epoll_create方法时，内核会创建一个eventpoll对象。
eventpoll对象也是文件系统中的一员，和socket一样，它也会有等待队列。

当socket收到数据后，中断程序会操作eventpoll对象，而不是直接操作进程。
当socket收到数据后，中断程序会给eventpoll的“就绪列表”添加socket引用。

eventpoll对象相当于是socket和进程之间的中介，socket的数据接收并不直接影响进程，
而是通过改变eventpoll的就绪列表来改变进程状态。
当程序执行到epoll_wait时，如果rdlist已经引用了socket，那么epoll_wait直接返回，如果rdlist为空，阻塞进程。

假设计算机中正在运行进程A和进程B，在某时刻进程A运行到了epoll_wait语句。内核会将进程A放入eventpoll的等待队列中，阻塞进程。

所以就绪列表应是一种能够快速插入和删除的数据结构。双向链表就是这样一种数据结构，epoll使用双向链表来实现就绪队列

select O(n) epoll O(1)









