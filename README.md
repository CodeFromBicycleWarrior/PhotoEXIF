# PhotoEXIF
Read and Write EXIF of a photo by Java

##为什么写这个项目

一个刚工作不久的小朋友问了我修改照片`EXIF`的问题，白天太忙，就只能晚上帮他看看。结果自己试了之后，也碰到相同的问题。既然答应了，就要努力去帮忙解决，那么就有了这个项目和这个README。


这个项目根据网上各位大牛的博客、讨论而来，主要是解决网上很多人碰到的使用JAVA修改照片的**EXIF**信息的问题——mediautil.image.jpeg.JPEG cannot be cast to mediautil.image.jpeg.Exif 。
> **EXIF**是 Exchangeable Image File的缩写，这是一种专门为数码相机照片设定的格式。这种格式可以用来记录数字照片的属性信息，例如相机的品牌及型号、相片的拍摄时间、拍摄时所设置的光圈大小、快门速度、ISO等等信息。除此之外它还能够记录拍摄数据，以及照片格式化方式，这样就可以输出到兼容EXIF格式的外设上，例如照片打印机等。


其实我的项目中实现的就是[http://blog.csdn.net/jsnjlc/article/details/2844010](http://blog.csdn.net/jsnjlc/article/details/2844010)这篇博文中的东西，但是博主说的非常模棱两可——“*因为，项目需要修改GPS，其提供的例子后面还提供了个地址，里面有5个java文件，拿出来，在项目中建好。然后在jar包将里面5个同名的文件删除，就OK了。否则你的例子会报错，还有，项目的JDK必须是1.5，编译环境也必须是1.5哦。这2个jar包，前者只能读，不能写，后者呢可以读也可以写，但是使用没有前者方便，因此仍然保留。*”

不仅仅我一人，包括下面的回复都提出了在开始的那个问题——强制类型转换错误！

##具体实现过程

如上面的模棱两可的blog所说，其实，前面的问题在官网已经提供了更新补丁：[http://mediachest.sourceforge.net/mediautil/](http://mediachest.sourceforge.net/mediautil/) 

> #### Important
> Please download [Source Files Updated](http://mediachest.sourceforge.net/mediautil/fixes-1.0.zip) since 1.0 for fixes including an important fix to read large Exif headers properly. Compile these and prepend to your CLASSPATH for the fixes.
(Source files last updated on May 13 2006)

将补丁包下载解压缩之后在`fixes-1.0.zip\fixes-1.0`目录下看到了五个java文件，这就是那个blog说的五个类。根据类中包的要求，我在自己的项目中新建了下面的包路径：

	package mediautil.image.jpeg;

然后在进行EXIF信息修改的`TestExifWriter.java`类中引用我新建包下的文件：

	import mediautil.image.jpeg.Entry;
	import mediautil.image.jpeg.Exif;
	import mediautil.image.jpeg.LLJTran;

前面blog中说的修改jar包什么的，我认为是不需要的，至少现在项目运行起来很正常…

##如何使用
- 环境
	- JDK 1.5
	> 按照上面blog说的我使用了`jdk1.5`，更高级的没有尝试，但是我认为都可以的，感兴趣的朋友可以试试

	- myeclipse
	> eclipse也是可以滴

	- windows 7 
	> 这个……防止高富帅Mac OS和技术屌Linux出现未知问题来喷本屌

- 使用
	- 在桌面（其实是任意地方）放两张图片，这两张图片的exif信息最好是一个有，一个没有，并且拿到图片的位置和名字（我的是放到桌面，路径就是C://Users//DELL//Desktop//1.jpg和C://Users//DELL//Desktop//2.jpg，其中1.jpg是有exif信息的，2.jpg没有）
	- 找到`com.test`包下面的`TestExifReader.java`（读取Exif信息）和`TestExifWriter.java`（修改Exif信息），修改图片的路径和名字之后，直接运行即可。

##题外话
在我自己找项目找栗子的过程中，经常碰到一些比较坑的博主，只贴部分代码，或者根本就不贴代码，完全是考验部分程序屌的想象力嘛！自己既然写博客了，必然有栗子的，为毛不上传让别人下载参考呢？

开源是时代的必然趋势，如果大家看到不错的文章、代码，可以推荐到[码源www.codefrom.com](http://www.codefrom.com)。当然，如果碰到技术问题或者想学习一些技术的朋友，可以关注一下码源。

如果本项目中有任何问题，也欢迎一起讨论。