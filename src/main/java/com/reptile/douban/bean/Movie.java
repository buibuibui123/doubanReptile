package com.reptile.douban.bean;

/**
 * title :电影标题
 * race :评分
 * type :类型
 * story :剧情
 * url :详情页链接
 * director:导演
 * scenarist:编剧
 * starring:演员
 * length:时长
 * releasetime:上映时间
 * frUrl:影评
 */
public class Movie {
	private String title ;
	private String race ;
	private String type ;
	private String story ;
	private String url ;
	private String img;
	private String director;
	private String scenarist;
	private String starring;
	private String length;
	private String releasetime;
	private String frUrl;


	public String getFrUrl() {
		return frUrl;
	}

	public void setFrUrl(String frUrl) {
		this.frUrl = frUrl;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getReleasetime() {
		return releasetime;
	}

	public void setReleasetime(String releasetime) {
		this.releasetime = releasetime;
	}

	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getScenarist() {
		return scenarist;
	}
	public void setScenarist(String scenarist) {
		this.scenarist = scenarist;
	}
	public String getStarring() {
		return starring;
	}
	public void setStarring(String starring) {
		this.starring = starring;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRace() {
		return race;
	}
	public void setRace(String race) {
		this.race = race;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStory() {
		return story;
	}
	public void setStory(String story) {
		this.story = story;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return title + "^" + race + "^" + type + "^" + story + "^" + url
				+ "^" + img + "^" + director + "^" + scenarist + "^" + starring + "^" + length + "^" + releasetime + "^" + frUrl;
	
	}
	
}