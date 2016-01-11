<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>

<form action="doSend" method="post">
	<textarea name="msg" rows="10" cols="50"></textarea>
	<br/><br/>
	接受者id（逗号隔开，如果为空，则发送给所有用户）：<br/>
	<input type="text" name="to" />
	<br/><br/>
	<input type="submit" value="发送" />
</form>
