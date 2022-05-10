# 前言
因为觉得哔哩哔哩的播放器UI和设计挺不错的，所以就模仿哔哩哔哩播放器部分搭建了一个大体的框架，自己做了一个视频应用，先看一下效果。  
# 效果图

| ![screenshot1](/screenshot/1_首页_推荐1.webp) | ![screenshot2](/screenshot/2_首页_推荐2.webp) | ![screenshot3](/screenshot/4_首页_社区.webp) | 
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ![screenshot4](/screenshot/5_首页_周榜.webp) | ![screenshot5](/screenshot/6_动态_综合.webp) | ![screenshot6](/screenshot/7_动态_视频.webp) | 
| ![screenshot7](/screenshot/8_动态_推荐.webp) |  ![screenshot8](/screenshot/11_视频详情_简介.webp) | ![screenshot9](/screenshot/12_视频详情_评论.webp) |
| ![screenshot10](/screenshot/13_短视频详情1.webp) |  ![screenshot11](/screenshot/15_图片详情.webp) | ![screenshot12](/screenshot/16_图片查看.webp) |
  
如果图片加载不出来的话，可以直接查看screenshot目录里面的截图，在screenshot目录里面也有一个BiliPlayer.apk，可以直接安装体验。  

# 项目说明
项目采用MVVM开发模式，使用Kotlin + Retrofit + 协程 + ViewModel + LiveData进行开发，视频播放接入GSYVideoPlayer，视频Api使用了开眼的Api，特此感谢！
## 项目结构
包含3个模块：universal（公共sdk）、app（主要模块）、video（视频模块）

## 主要的类说明
### universal目录说明
- base：基类组件
~~~
BaseActivity：基类Activity
BaseFragment：基类Fragment
BaseListActivity：列表数据对应的基类Activity
BaseListFragment：列表数据对应的基类Fragment
IBaseView：基类Activity和基类Fragment共同调用的方法集合
IBaseListView：列表数据对应的基类Activity和基类Fragment共同调用的方法集合
BaseViewModel：基类ViewModel
BaseViewModelWithData：基类ViewModel的进一步封装，默认包含一个接口请求
BaseListViewModel：列表数据对应的基类ViewModel
CommonBaseActivity：不需要额外声明ViewModel的Activity的父类
CommonBaseFragment：不需要额外声明ViewModel的Fragment的父类
WebviewActivity：H5 Activity基类
WebviewFragment：H5 Fragment基类
DontSwipeBack：不需要侧滑退出的Activity可以选择继承这个类
liveDataEntity.DialogConfig：加载对话框配置信息
liveDataEntity.ErrorResponse：接口请求失败结果封装
~~~
- http：网络请求相关
~~~
Config：配置开发和生产环境
RetrofitFactory：配置Retrofit
BaseListResponse：列表类型数据对应的基类Bean
interceptor目录：自定义拦截器
~~~
- util：通用的工具
- widget：自定义控件

### app目录说明
- adapter：所有的adapter适配器
- dialog：自定义对话框
- entity：所有的Bean实体类
- func：功能组件
- server：接口定义
~~~
ApiService：定义接口地址
ApiHelper：封装接口参数
~~~
- ui：界面相关
~~~
common：通用页面基类

detail：各类详情页
VideoDetailActivity：视频详情页
VideoIntroduceFragment：视频详情页-简介
VideoReplyFragment：视频详情页-评论
VideoReplyFragment：视频详情页-评论
SwitchDetailActivity：短视频详情页
PhotoDetailActivity：图片详情页
PhotoRelayFragment：图片详情页-转发
PhotoReplyFragment：图片详情页-评论
PhotoPraiseFragment：图片详情页-赞
PhotoViewActivity：图片查看器Activity
WebDetailActivity：网页

entry：应用入口，包括启动页、引导页、登录页等
find：发现
home：首页
main：主页
trends：动态
~~~
- util：业务相关的工具类，通用的工具可以定义在common库下的util目录
- widget：业务相关的控件类

### video目录说明
- adapter：视频全屏播放结束后相关推荐列表对应adapter
- entity：视频Bean类
- player：各类播放器控件
~~~
DetailVideoPlayer：详情页播放器
ListVideoPlayer：列表播放器
SwitchVideoPlayer：上下切换播放器
TrendsVideoPlayer：动态播放器
~~~
- util：视频相关工具类
- widget：视频相关控件类

## 项目Api
### 首页
- 热门：http://baobab.kaiyanapp.com/api/v5/index/tab/allRec
- 推荐：http://baobab.kaiyanapp.com/api/v5/index/tab/feed
- 推荐-轮播图：http://baobab.kaiyanapp.com/api/v7/index/tab/discovery
- 社区：http://baobab.kaiyanapp.com/api/v7/community/tab/rec
- 周榜：http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=weekly
- 月榜：http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=monthly
- 总榜：http://baobab.kaiyanapp.com/api/v4/rankList/videos?strategy=historical
### 动态
- 综合/视频：http://baobab.kaiyanapp.com/api/v7/community/tab/rec
- 推荐：http://baobab.kaiyanapp.com/api/v4/tabs/follow
- 主题：http://baobab.kaiyanapp.com/api/v7/tag/tabList
### 视频详情页
- 相关推荐：如http://baobab.kaiyanapp.com/api/v4/video/related?id=125863，125863为播放视频的id
- 评论：http://baobab.kaiyanapp.com/api/v2/replies/video?videoId=125863，125863为播放视频的id

# 特别声明：本项目只做学习用，不做其他用途！！！