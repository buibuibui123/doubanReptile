package com.reptile.douban.task;

import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *URLSpider监听器，由于URLSpider端口常常阻塞，因此创建该类定时监听URLSpider类的状态，若URLSpider阻塞，将创建新的URLSpider替代
 */
public class URLSpiderListener implements Runnable{
	
	private Map<String,Integer> status ;
	private final CloseableHttpClient httpClient ;
	private final BlockingQueue<String> urls;
	private final BlockingQueue<String> entitys1 ;
	private final BlockingQueue<String> entitys2 ;
	private final ExecutorService pool ;
	//监听周期： 5分钟
	private final long ListenerTime = 300000;
	
	public URLSpiderListener(CloseableHttpClient httpClient, BlockingQueue<String> urls,
                             BlockingQueue<String> entitys1, BlockingQueue<String> entitys2, ExecutorService pool) {
		super();
		this.status = new ConcurrentHashMap<String,Integer>();
		this.httpClient = httpClient;
		this.urls = urls;
		this.entitys1 = entitys1;
		this.entitys2 = entitys2;
		this.pool = pool;
	}

	@Override
	public void run() {
		while (!urls.isEmpty()||!entitys1.isEmpty()||!entitys2.isEmpty()){
			//设置监听周期
			try {
				Thread.sleep(ListenerTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//判断URLSpider线程是否存活
			Set<String> keys = status.keySet();
			for (String key : keys){
				//若线程状态为0 ，表示已经阻塞
				if (status.get(key) == 0){
					//移除该Key-Value
					status.remove(key);
					//新建线程
					createURLSpiderTask();
				}else {
					//存活，将状态置为0，为下次检测
					status.put(key, 0);
				}
			}
		}
	}
	
	/*
	 * 保持存活，将status中对应的Value变为1
	 */
	public void keepLive(String name) {
		status.put(name,1);
	}

	/**
	 *创建新的爬虫任务
	 */
	private void createURLSpiderTask() {
		URLSpider urlSpider = new URLSpider(httpClient,urls,entitys1,entitys2,new CountDownLatch(0),new CountDownLatch(0));
		//注册到监听器
		urlSpider.registerListner(this);
		pool.submit(new Thread(urlSpider));
		System.out.println("新建URL爬虫 ！");
	}
}
