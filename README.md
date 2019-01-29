# doubanspider（豆瓣爬虫）
起因：
------
        学习《JAVA并发编程实践》时，发现里面的很多知识点多而实践很少，对于初学者不容易深入理解JAVA多线程。因为爬虫技术爬取数据量大，使用单线程不现实，因此计划使用JAVA多线程+爬虫技术完成一个高性能的爬虫工具。


最开始的单线程形式
------
①  目前可以爬取电影分类首页（电影-剧情-美国-经典）和电影详情页的描述信息
<br>
<br>使用主线程顺序执行代码
<br>获取从豆瓣服务器上获取5个页面的JSON数据-依次取出json数据并解析为bean-从bean中取出所有的详情地址链接-HttpClient访问url-提取每个html中的剧情描述-存入Bean
<br>② 运行时间 ：2min 15s
<br>
<br>--创建URLspider线程类用于url请求将response解析成HTML，存入entitys中
<br>--创建URLAnalyzer线程类从entitys中取出html，解析出url（解析功能未实现）
<br>--创建HTMLAnalyzer线程类解析html代码中需要的数据(解析功能未实现)
<br>--添加工具类：RegexUtil，HttpClientUtil
<br>--采用ArrayBlockingQueue容器作为存放URL的urls和html代码的entitys
<br>--考虑到多线程安全，用CopyOnWriteArraySet作为存放使用过的url的容器


启用多线程版本
------
版本改动：
<br>①项目使用多线程
<br>②大量使用juc包容器
<br>③添加页面url解析器，html解析器，url爬虫
<br>运行时间 ：1min 17s
<br>
<br>项目不足:
<br>①频繁访问豆瓣url，导致IP地址被封
<br>②爬虫数据量不高
<br>③多线程顺序执行，考虑使用生产者消费者模型
<br>④项目功能过于局限
<br>

解决IP地址被封
------
①爬取豆瓣所有电影详情信息
<br>
<br>描述：
<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;该版本解决了爬虫数据量不高，项目功能局限，未采用生产者消费者模型的问题。由于
<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;频繁访问豆瓣url，导致IP地址被封，爬虫设置了访问时间间隔3s。同时采用了广度优先
<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;遍历算法。
<br>
<br>项目逻辑 ：
![image](https://github.com/buibuibui123/doubanReptile/blob/master/img/p1.PNG)
<br>运行时间 : 预计45小时（假设电影数15w([豆瓣一共收录了多少部电影](https://www.zhihu.com/question/20072525))，项目每秒钟请求1次）
<br>
<br>使用 :
<br>运行cc.heroy.douban.core.ategoryMovie.java类即可。
<br>
<br>遇到的问题：
<br>在测试阶段，我将urls，entitys1，entitys2，分别设置大小为200，200，200。运行过程中出现URLAnalyzer线程出现阻塞的现象，通过在HTMLAnamyzer线程中添加打印容器状态代码后发现，urls达到200后，URLAnalyzer出现阻塞现象，伴随entitys1大小增加到200，URLSpider线程也出现阻塞，因此在网上查询了豆瓣收录电影大致数目后，将容器大小分别设置为40w，20w，20w。

增加爬虫监听器
------
<br>--添加URLSpiderListener类
<br>--在URLSpider类中添加registerListener方法
<br>
<br>描述：
<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;解决socket阻塞导致URLSpider无法继续访问页面的问题。
><br> 前几天在看NIO的视频，老师讲到非阻塞网络通信，然后提到监听器模式，我在网上理解了它的原理，于是
<br>我用这种模式解决之前提到的阻塞难题。
><br>
><br>思路：添加了URLSpiderListener任务类,对多个urlSpider线程注册在URLSpiderListener线程（即持有
<br>监听类的引用），每次urlSpider访问页面后都将调用Listner中keepLive()方法，将Listener的stauts(Map)中
<br>该spider的value置为1，表示存活。而Listener线程会每5分钟检测一次注册的线程类状态，并将状态置为0，若
<br>线程状态为0，即表示线程阻塞，listner线程会创建新的spider线程替代阻塞线程，并移除旧线程。
<br>
<br>数据:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;收集约3.2w条数据
https://github.com/buibuibui123/doubanReptile/blob/master/img/V2d1.png
<br>https://github.com/buibuibui123/doubanReptile/blob/master/img/V2d2.png
<br>时间:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;25小时左右(期间背电脑来回图书馆，用debug模式停止所有线程，累)
<br>平均爬虫速率:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1300/小时(我设置的3个爬虫，每个爬虫3s的等待时长，按理讲每秒1条记录，理想下3600/小时,我猜测是线程切换，IO阻塞，请求时间等因素导致效率底)
<br>数量真实性:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;由于考虑不全，豆瓣的电视剧（如XXX第一季）也被搜集下来了。同时像冷门电影，根本没有标签，基本上不在爬虫范围内。真实性暂时不好说。
<br>
