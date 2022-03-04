# 项目说明

## 项目结构
包含2个基本module：universal（公共sdk）、app（主要模块）

## 主要的类说明
### universal目录说明
#### base：基类组件
- BaseActivity：基类Activity
- BaseFragment：基类Fragment
- BaseListActivity：列表数据对应的基类Activity
- BaseListFragment：列表数据对应的基类Fragment
- IBaseView：基类Activity和基类Fragment共同调用的方法集合
- IBaseListView：列表数据对应的基类Activity和基类Fragment共同调用的方法集合
- BaseViewModel：基类ViewModel
- BaseViewModelWithData：基类ViewModel的进一步封装，默认包含一个接口请求
- BaseListViewModel：列表数据对应的基类ViewModel
- CommonBaseActivity：不需要额外声明ViewModel的Activity的父类
- CommonBaseFragment：不需要额外声明ViewModel的Fragment的父类
- WebviewActivity：H5 Activity基类
- WebviewFragment：H5 Fragment基类
- DontSwipeBack：不需要侧滑退出的Activity可以选择继承这个类
- liveDataEntity.DialogConfig：加载对话框配置信息
- liveDataEntity.ErrorResponse：接口请求失败结果封装
- app模块下的ui目录：各个Activity/Fragment所在目录
#### http：网络请求相关
- Config：配置开发和生产环境
- RetrofitFactory：配置Retrofit
- CustomObserver：自定义Observer类
#### util：通用的工具
#### widget：自定义控件

### app目录说明
#### adapter：所有的adapter适配器
#### entity：所有的Bean实体类
#### func：功能组件
#### server：接口定义
#### ui：界面相关
##### entry：应用入口，包括启动页、引导页、登录页等
##### main：主页
#### util：业务相关的工具类，通用的工具可以定义在common库下的util目录
#### widget：业务相关的控件类

## 注意点
### 开发须知
1、适配器adapter新建在adapter目录  
2、bean类新建在entity目录  
3、用户数据（SharedPreferences）在UserInfo中存储  
4、接口地址在ApiService中定义，参数在ApiHelper中封装，外部类唯一通过ApiHelper访问接口  
5、图片资源放在drawable-xhdpi目录  
6、三方库的依赖以api形式放在common module下的build.gradle  
### 替换套用项目需要修改的地方
1、app下build.gradle配置自动签名，替换为项目自己的签名文件  
2、app下build.gradle修改applicationId，更改为项目自己的包名  
3、app下build.gradle的versionCode和versionName遵循项目自己的命名规则  
4、app下build.gradle中修改生成的APK名字  
5、settings.gradle修改rootProject.name  
6、project下build.gradle确定最小、目标和编译SDK版本  

# 特别声明：本项目只做学习用，不做商业用途！！！