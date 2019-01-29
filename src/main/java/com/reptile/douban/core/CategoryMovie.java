package com.reptile.douban.core;


import com.alibaba.fastjson.JSONObject;
import com.reptile.douban.bean.Movie;
import com.reptile.douban.task.HTMLAnalyzer;
import com.reptile.douban.task.URLAnalyzer;
import com.reptile.douban.task.URLSpider;
import com.reptile.douban.task.URLSpiderListener;
import com.reptile.douban.util.HttpClientUtil;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * 功能 ：爬取豆瓣-分类中的电影信息 内容：电影名称，评分，剧情简介 使用技术：HttpClient , 多线程
 */
public class CategoryMovie {

	//具体电影详情url
	String durl = "https://movie.douban.com/subject/1851857/";
	String durl1 = "https://movie.douban.com/subject/1862151/";
	String durl2 = "https://movie.douban.com/subject/1291561/";

	// 豆瓣详情页的url容器
	BlockingQueue<String> urls = new ArrayBlockingQueue<String>(400000);
	// 获取的页面实体(URLAnalyzer使用)
	BlockingQueue<String> entitys1 = new ArrayBlockingQueue<String>(200000);
	// 获取的页面实体(HTMLAnalyzer使用)
	BlockingQueue<String> entitys2 = new ArrayBlockingQueue<String>(200000);
	// 被使用过的url(去重)
	CopyOnWriteArraySet<String> usedURLS = new CopyOnWriteArraySet<String>();
	// 储存获取的movie对象（理解Vector）
	Vector<Movie> movies = new Vector<Movie>(200);
	// 线程池(后期添加线程日志)
	ExecutorService pool = Executors.newFixedThreadPool(100);
	// URLSpider线程数
	private final int spiderCount = 3;
	// URLAnalyzer线程数
	private final int urlAnalyzerCount = 2;
	// HTMLAnalyzer线程数
	private final int HTMLAnalyzerCount = 2;


	// URLSpider 的 二元闭锁
	int spiderStartGateNum = 1;
	int spiderEndGateNum = spiderStartGateNum * spiderCount;

	// URLAnalyzer的二元闭锁
	int urlAnalyzerStartGateNum = 1;
	int urlAnalyzerEndGateNum = urlAnalyzerStartGateNum * urlAnalyzerCount;

	// HTMLAnalyzer的二元闭锁
	int HTMLAnalyzerStartGateNum = 1;
	int HTMLAnalyzerEndGateNum = HTMLAnalyzerStartGateNum * HTMLAnalyzerCount;


	CountDownLatch urlAnalyzerStartGate = new CountDownLatch(urlAnalyzerStartGateNum);
	CountDownLatch urlAnalyzerEndGate = new CountDownLatch(urlAnalyzerEndGateNum);

	CountDownLatch HTMLAnalyzerStartGate = new CountDownLatch(HTMLAnalyzerStartGateNum);
	CountDownLatch HTMLAnalyzerEndGate = new CountDownLatch(HTMLAnalyzerEndGateNum);

	CountDownLatch urlSpiderStartGate = new CountDownLatch(spiderStartGateNum);
	CountDownLatch urlSpiderEndGate = new CountDownLatch(spiderEndGateNum);

	public void spider() {

		// 开始时间
		long begin_time = System.currentTimeMillis();

		// 获取单个httpClient
		CloseableHttpClient httpClient = HttpClientUtil.getHttpClient();

		try {
			urls.put(durl);
			urls.put(durl1);
			urls.put(durl2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//创建URLSpider监听任务
		URLSpiderListener urlSpiderListener = new URLSpiderListener(httpClient, urls, entitys1, entitys2, pool);

		// 启动URLAnalyzer和HTMLAnalyzer,URLSpider
		for (int i=0;i<urlAnalyzerCount;i++){
			pool.submit(new Thread(new URLAnalyzer(entitys1,entitys2, urls, usedURLS, urlAnalyzerStartGate, urlAnalyzerEndGate)));
		}
		for (int i=0;i<HTMLAnalyzerCount;i++){
			pool.submit(new Thread(new HTMLAnalyzer(entitys1,entitys2, urls ,usedURLS,HTMLAnalyzerStartGate,HTMLAnalyzerEndGate,movies)));
		}
		for (int i=0;i<spiderCount;i++){
			URLSpider spider = new URLSpider(httpClient, urls, entitys1, entitys2, urlSpiderStartGate, urlSpiderEndGate);
			//将spider任务注册到监听器
			spider.registerListner(urlSpiderListener);
			pool.submit(new Thread(spider));
		}


		try {
			//先开启url爬虫爬取一定的url
			urlSpiderStartGate.countDown();
			Thread.sleep(5000);
			urlAnalyzerStartGate.countDown();
			Thread.sleep(5000);
			HTMLAnalyzerStartGate.countDown();
			System.out.println("启动监听器");
			pool.submit(urlSpiderListener);
			//让主线程阻塞开始多线程爬取
			urlSpiderEndGate.await();
			urlAnalyzerEndGate.await();
			HTMLAnalyzerEndGate.await();

			long end_time = System.currentTimeMillis();
			System.out.println("待访问的url数量  ："+urls.size());
			System.out.println("已访问的url数量  ："+usedURLS.size());
			System.out.println("结束时间"+(end_time - begin_time));
			//关闭客户端
			httpClient.close();
			//关闭线程池
			pool.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new CategoryMovie().spider();
	}
}
