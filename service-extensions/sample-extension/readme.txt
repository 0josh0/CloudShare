扩展开发&集成方式：
1.base-package使用cn.ac.iscas.oncecloudshare.service.extensions.xxx
2.编写一个类继承cn.ac.iscas.oncecloudshare.service.system.extension.Extension
3.编写扩展所需的Listener/Service/Dao/Controller
4.将项目打成jar包放入service webapp的lib目录下（参考方式：eclipse项目右键export，JAR file，勾选add directory entries）
5.编写配置文件（xxx.properties)并放入classpath/extensions下