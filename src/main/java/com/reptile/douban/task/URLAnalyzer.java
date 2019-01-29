package com.reptile.douban.task;


import com.reptile.douban.util.FilterUtil;
import com.reptile.douban.util.RegexUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

/**
 * URL解析器线程
 * 从entitys中获取entity,解析出url地址
 * 将url进行去重判断，若在usedURLS中存在，则抛弃，否则添加到usedURLS和urls中
 * 
 */
public class URLAnalyzer implements Runnable{

	private BlockingQueue<String> entitys1 ;
	private BlockingQueue<String> entitys2 ;
	private BlockingQueue<String> urls ;
	private CopyOnWriteArraySet<String> usedURLS;
	private CountDownLatch startGate ;
	
	//线程睡眠时间
	long space = 2000L;
	
	CountDownLatch endGate ;
	public URLAnalyzer(BlockingQueue<String> entitys1,BlockingQueue<String> entitys2,BlockingQueue<String> urls, CopyOnWriteArraySet<String> usedURLS,CountDownLatch startGate,CountDownLatch endGate){
		this.entitys1 = entitys1;
		this.entitys2 = entitys2;
		this.urls = urls;
		this.usedURLS = usedURLS;
		this.startGate = startGate;
		this.endGate = endGate;
	}


	@Override
	public void run() {
		try {
			//开启开关，当countDown后开启
			startGate.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("URLAnalyzer启动");
		while(!urls.isEmpty()||!entitys1.isEmpty()||!entitys2.isEmpty()){
			try {
				String content = entitys1.take();
				analyzer(content);
				Thread.sleep(space);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		endGate.countDown();
		System.out.println("URLAnalyzer结束");
	}

	//通过jsoup解析entity的所以url判断并去重
	private void analyzer(String content){

		//将content转换成document对象
		Document doc = Jsoup.parse(content);
		//找到页面所有链接
		Elements links = doc.select("a[href]");

		Set<String> set = new HashSet<String>();
		for(Element e :links){
			String url = e.attr("href");
			set.add(url);
		}
		//判断url是否为电影详情页
		set = RegexUtil.URLRegex(set);

		//去重
		FilterUtil.URLFilter(set, urls, usedURLS);

	}
}
