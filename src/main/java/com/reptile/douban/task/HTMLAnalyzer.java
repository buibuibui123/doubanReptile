package com.reptile.douban.task;


import com.reptile.douban.bean.Movie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML页面解析线程
 * 从entitys中取出html源代码进行数据提取(电影名，评分，类型，剧情，链接)
 * 将movie存到vector容器
 */
public class HTMLAnalyzer implements Runnable{

	private final BlockingQueue<String> entitys1 ;
	private final BlockingQueue<String> entitys2 ;
	private final CountDownLatch startGate ;
	private final CountDownLatch endGate ;
//	private final Vector<Movie> movies ;
	private final BlockingQueue<String> urls;
	CopyOnWriteArraySet<String> usedUrls = new CopyOnWriteArraySet<String>();
	//线程睡眠时间
	long space = 2000L;
	
	public HTMLAnalyzer(BlockingQueue<String> entitys1,BlockingQueue<String> entitys2 ,BlockingQueue<String> urls,CopyOnWriteArraySet<String> usedUrls ,CountDownLatch startGate , CountDownLatch endGate,Vector<Movie> movies){
		this.entitys1 = entitys1;
		this.entitys2 = entitys2;
		this.usedUrls = usedUrls;
		this.startGate = startGate;
		this.endGate = endGate;
//		this.movies = movies;
		this.urls = urls;
	}
	
	@Override
	public void run() {
//创建目录
		String path = "F:/movie";
		File p = new File(path);
		if(!p.exists()) {
			//创建文件目录
			p.mkdir();
		}
		FileWriter fw = null;
		File f = new File(p,UUID.randomUUID()+".txt");
		if(f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//等待startGate
System.out.println(entitys2.size());
		try {
			startGate.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//启动
System.out.println("HTMLAnalyzer启动");
try {
	fw = new FileWriter(f);
} catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
		
		while(!urls.isEmpty()||!entitys1.isEmpty()||!entitys2.isEmpty()){
			try {
				Set<String> set1 = new HashSet<String>();
				String content = entitys2.take();
				Movie movie = new Movie();
				Document doc = Jsoup.parse(content);
				//电影url
				String url = content.substring(0,content.indexOf("#"));
				movie.setUrl(url);
				//电影名
				Elements titles = doc.getElementsByTag("title");
				String title = titles.get(0).text();
				movie.setTitle(title);
				//电影评分
				Elements races = doc.getElementsByClass("ll rating_num");
				String race = races.get(0).text();
				movie.setRace(race);
				//电影标签
				Elements types = doc.getElementsByClass("tags-body");
				String type ="";
				for(Element e : types){
					type += e.text()+" ";
				}
				movie.setType(type);
				//电影图片
				Elements img = doc.select("div.grid-16-8");
				Elements elements = img.select("a.nbgnbg").select("img");
				String imgUrl = "";
				imgUrl = elements.attr("src");
				movie.setImg(imgUrl);
				//导演编剧主演
				Elements attrs = doc.getElementsByClass("attrs");
				movie.setDirector(attrs.get(0).text());
				movie.setScenarist(attrs.get(1).text());
				movie.setStarring(attrs.get(2).text());
				//片长 上映时间
				Elements allSpan = doc.select("div.subject").select("span");
				for(Element a : allSpan){
					if (!(a.attr("content").equals(""))){
						set1.add(a.attr("content"));
					}
				}
				String releasetime ="";
				String regex="^-?\\d+$";
				Pattern p1 =Pattern.compile(regex);
				for(String s : set1){
					Matcher m=p1.matcher(s);
					if(m.matches()){
						movie.setLength(s);
					}else {
						releasetime = s;
					}
				}
				movie.setReleasetime(releasetime);

				Elements frUrl = doc.select("div.main-bd");//
				Elements elements2 = frUrl.select("a");
				String frUrl1 = "";
				frUrl1 = elements2.attr("href");
				movie.setFrUrl(frUrl1);

				//剧情(有些网页剧情显示不完全，需要判断完整剧情位置)
				String story = "";
				Elements indents = doc.getElementsByClass("indent");
				Elements allhidden = doc.getElementsByClass("all hidden");
				if(indents!=null&&indents.get(1)!=null){
					story = indents.get(1).text();
				}else{
					if(allhidden!=null&&allhidden.get(0)!=null){
						story =allhidden.get(0).text();
					}
				}	
					
				movie.setStory(story);
				fw.append(movie.toString()+"\r\n");
				System.out.println("写入："+movie.getTitle()+"  当前状态：urls :"+urls.size()+" ,entitys1 :"+entitys1.size()+" ,entitys2 :"+entitys2.size()+" ,usedUrl :"+(usedUrls.size()-urls.size()));
				Thread.sleep(space);

			} catch (Exception e) {
				System.out.println("页面解析失败");
			}
		}
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		endGate.countDown();
System.out.println("HTMLAnalyzer结束");
		
	}
	
	

}
