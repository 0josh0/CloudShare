<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
<script src="http://malsup.github.com/jquery.form.js"></script> 
 <script src="uploadify/jquery.uploadify.js" type="text/javascript"></script>
<link href="uploadify/uploadify.css" rel="stylesheet" /> 
<script> 

	
	$(function() {//encodeURIComponent(filecurrent)
		$("#file_upload_1").uploadify({
			'buttonText' : '上传',
			height : 30,
			swf : 'uploadify/uploadify.swf',
			uploader : "http://localhost:6080/service/api/v2/exts/workspaces/2/files/root/upload?x-ticket=59a98d512c944cb3b134f8421be71f49",
			width : 75,
			simUploadLimit : 3, // 并发上传限制
			//auto:false,   // 选择文件后自动上传
			fileObjName: 'file',
			queueID : 'file_upload_1-queue',//与下面的id对应  文件队列
			//queueSizeLimit:10,
			//checkExisting: true,
			//fileObjName:"Filedata",  // 后台接受参数名称
			"preventCaching" : false, // 设置随机参数，防止缓存
			"progressData" : "speed", // 显示上传进度百分比percentage
			removeCompleted : false, // 上传后是否自动删除记录
			'fileTypeDesc' : "上传的文件", // 'jpg,jpeg'支持的文件格式，写filetypeExts该参数必须写
			'multi' : true, // 是否支持多文件上传
			successTimeout : 1000,//文件上传成功后服务端应返回成功标志，此项设置返回结果的超时时间
			onSelect : function(event, queueID, file) {
				$("#default_row").remove();
			},
			onSWFReady : function() {
			},
			onUploadStart : function(file) {
			},
			"onCancel" : function() {
				// 取消上传事件
				//alert("删除上传");
			},/* 
			onUploadStart: function(file){
				$("#file_upload_1").uploadify('settings', 'uploader', 'http://192.168.1.120:7890/api/v2/exts/workspaces/1/files/63/upload?x-ticket=5f441dcc80b74947af7fdd5a99b092ea');
			}, */
			onUploadSuccess : function(file, data, response) {//在每一个文件上传成功后触发
				console.log("返回的结果:" + response);
				console.log("返回的结果:" + data);
				console.log(file.size);
				if (data == "true") {//上传成功
					//window.parent.fileActor.doRefresh(true);
					//$('#' + file.id).remove();
					$('#' + file.id).find('#progress_size')
							.text('上传成功');//.next().html('<a href="javascript:$(\'#file_upload_1\').uploadify(\'upload\', \''+file.id+'\')">重新上传</a>');
				} else if (data == "false") {//上传成功
					$('#' + file.id).find('#progress_size')
							.text('上传失败');
					//$('#' + file.id).remove();
				} else {
					$('#' + file.id).find('#progress_size')
							.text('上传完成');
					console.log("返回的结果:" + data);
					//$('#' + file.id).remove();
				}
				/* 	var encode = $.toJSON(data);
					console.log(encode);
				    if(isNaN($.evalJSON(encode).fs_id)){
				    	window.parent.fileActor.doRefresh(true);
				    	$('#' + file.id).remove();
				    } */
			},
			onUploadComplete : function(file) {//上传文件成功后触发（每一个文件都触发一次）
				/* console.log("onUploadComplete--------->"+file.name); */
				// $('#' + file.id).find('#progress_size').text('上传完成');
			},
			onQueueComplete : function(queueData) {//在队列中的文件上传完成后触发
				//window.parent.fileActor.doRefresh(true);
				// 所有文件上传成功后触发
				//alert("上传完成");
				/* console.log("onQueueComplete--------->"+queueData.uploadsSuccessful); */
			},
			onUploadError : function(file, errorCode, errorMsg,
					errorString) {
				console.log();
				$('#' + file.id).find('#progress_size').text(
						'上传失败');
			},
			onFallback : function() {
				alert("要使用上传功能，您需要安装Flash player或者一个支持HTML5的浏览器");
			}
		});
	});
</script> 
</head>
<body>
	<input id="fileCurrent" type="hidden" value="" >
	<input id="fileTypeExts" type="hidden" value="*.*">
	<input id="fileSize" type="hidden" value="0" >
	<div class="list-group">
	  <div  class="list-group-item active" style="height:35px;*height:20px;">
	    <span class="glyphicon glyphicon-cloud-upload"></span> 上传文件到云享
	     <button type="button" class="btn btn-default btn-xs" style="float: right;*margin-top: -20px;" onclick="closeUpload()" >关闭</button>
	  </div>
	  <div  class="list-group-item" style="display: block;" id="mainBox">
	选择要上传到 "云享" 文件夹的文件。你每次可以选择多个文件。
	<div>
		<table>
			<tbody>
				<tr>
					<td style="width: 240px;">标题</td>
					<td style="width: 80px;">大小</td>
					<td style="width: 40px;">目录</td>
					<td style="width: 80px;">进度</td>
					<td style="width: 80px;">&nbsp;&nbsp;状态</td>
					<td style="width: 80px;">操作</td>
				</tr>
			</tbody>
		</table>
	</div>
    <div id="file_upload_1-queue1" class="uploadify-queue" style="height:150px;overflow: auto;" >
	        
	  <table  cellspacing="0" cellpadding="0"  id="file_upload_1-queue" class="uploadify-queue"  >
				<tr id="default_row"><td colspan="6" style="width: 700px;">上传队列中暂时没有文件</td></tr>
	  </table>
	</div>
	</div>
	 <div  class="list-group-item" style="height:45px;*margin-top: -20px;*height:40px;">
	 <div style="width:60%; float:left; " > 网页版单文件最大支持 <span id="upload_sizes"></span></div>
	 <div style="width:40%; float:left; ">
	<form>
<!-- <input type="button"  onclick="jQuery('#file_upload_1').uploadifyUpload();" value="fdf"> -->
		<input id="file_upload_1" name="file" type="file" multiple="true">
		<button type="button" class="btn btn-default closeBtn" onclick="closeUpload()">关闭</button>
	</form>
	</div>
  </div>
</div>
	
	<!-- <form id="myForm" action="http://192.168.1.120:7890/service/api/v2/exts/workspaces/1/files/root/upload?x-ticket=3fa3e6a0ea8c40349139c659a2b0c9ae" method="post" enctype="multipart/form-data">
		<input type="text" value="text.txt" name="name">
		<input type="file" name="file">
		<input type="submit" value="提交">
	</form>
</body> -->
</html>