#BOSH乱码

如果部署在windows的tomcat下，bosh出现乱码，请在catalina.bat中加入
`set JAVA_OPTS=-Djavax.servlet.request.encoding=UTF-8 -Dfile.encoding=UTF-8`