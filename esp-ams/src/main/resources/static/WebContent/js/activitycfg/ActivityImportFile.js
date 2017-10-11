$(function(){
	$("#addGoodsToGroup").window('close');

    $('#importFileList').datagrid({
        title : '商品池',
        fit : true,
        rownumbers : true,
        pagination : true,
        singleSelect: false, //允许选择多行  
        selectOnCheck: true,   
        checkOnSelect: false,
        striped:true,
        toolbar : '#tb',
        rowStyler:function(rowIndex,rowData){
        	if(rowData.colFalgt=='1'){
        		return 'background-color:#6293BB;';
        	}
        },
        columns : [[ { field: 'ck', checkbox: true, width: '30' },  //复选框 
            {
                title : '商品编号',
                field : 'goodsCode',
                width : 150,
                align : 'center'
            }, {
                title : 'skuid',
                field : 'skuId',
                width : 150,
                align : 'center'
            },{
                title : '商品名称',
                field : 'goodsName',
                width : 120,
                align : 'center',
    			formatter : function(value, row, index) {
                 	if(null == value || "null"==value)
                 		value = "";
                 	var msg = value+"";
                 	return "<div  title='" + value + "'>" + value + "</div>";
                 }
            },
            {
                title : '商品状态',
                field : 'goodsStatus',
                width : 120,
                align : 'center',
                formatter : function(value, row, index) {
                	if(value=='G00'){
                		return "待上架";
                	}else if(value=='G01'){
                		return "待审核";
                	}else if(value=='G02'){
                		return "已上架";
                	}else if(value=='G03'){
                		return "已下架";
                	}else if(value=='G04'){
                		return "待审核";
                	}
                }
            },{
                title : '商品类目（三级）',
                field : 'goodsCategory',
                width : 120,
                align : 'center'
            },{
                title : '成本价',
                field : 'goodsCostPrice',
                width : 120,
                align : 'center'
            },{
                title : '售价',
                field : 'goodsPrice',
                width : 120,
                align : 'center'
            },{
                title : '市场价',
                field : 'marketPrice',
                width : 120,
                align : 'center'
            },{
                title : '活动价',
                field : 'activityPrice',
                width : 120,
                align : 'center'
            },{
                title : '分组',
                field : 'groupName',
                width : 120,
                align : 'center'
            },{
                title : '备注',
                field : 'detailDesc',
                width : 120,
                align : 'center',
                formatter : function(value, row, index) {
                	if(value=='1'){
                		return "导入成功";
                	}else{
                		return "导入失败";
                	}
                }
            },{
				title : '操作',
				field : 'opt',
				width : 120,
				align : 'center',
				formatter : function(value, row, index) {
					var content = "";
					if(row.detailDesc=='1' && row.status !='S') {
					  	 content +="<a href='javascript:void(0);' class='easyui-linkedbutton' onclick=\"$.editGoodsAndActivity('"
                             + row.goodsId +"','"+ row.activityId+ "');\">添加至</a>&nbsp;&nbsp;";
					}
				 return content;
			}}]],
        loader : function(param, success, error) {
            $.ajax({
                url : ctx + '/application/activity/list',
                data : param,
                type : "post",
                dataType : "json",
                success : function(data) {
                	console.log(data);
                    $.validateResponse(data, function() {
                        success(data);
                    });
                }
            })
        }
    });

	$("#createGroup").click(function () {
		$("#groupNameAdd").textbox('clear');
		$("#sordGroupAdd").textbox('clear');
		$("#addGroupDiv").dialog({
			modal : true,
			title : "创建分组",
			resizable:true,
			width : 400,
			buttons : [ {
				text : "确定",
				handler : function() {
					var groupNameAdd = $("#groupNameAdd").textbox('getValue');
					var sordGroupAdd = $("#sordGroupAdd").textbox('getValue');
					var params = {
						"groupName":groupNameAdd,
						"orderSort":sordGroupAdd,
					}

					$.ajax({
						type : "POST",
						url : ctx + '/application/activity/addGroup',
						data : params,
						success : function(data) {
							ifLogout(data);
							if (data.status == 1) {
								$("#addGroupDiv").dialog("close");
								$.messager.alert('提示',data.msg,'success');
							} else {
								$.messager.alert('提示',data.msg,'error');
							}
						}
					});

				}
			}, {
				text : "取消",
				handler : function() {
					$("#addGroupDiv").dialog("close");
				}
			} ]
		});
	});

    //加载商品类目信息
    goodsCategoryComboFun();
    // 查询列表
    $("#searchGoods").click(function() {
        var params = {};
        params['goodsCode'] = $("#goodsCode").textbox('getValue');
        params['skuId'] = $("#skuId").textbox('getValue');
        var goodsCategoryCombo=$("#goodsCategoryCombo").combotree('getValue');
        if("请选择"==goodsCategoryCombo){
        	goodsCategoryCombo="";
        }
        params['goodsCategory']=goodsCategoryCombo;
        $('#importFileList').datagrid('load', params);
    });
    // 刷新列表
    $("#resetGoods").click(function() {
    	$("#goodsCode").textbox('setValue','');
    	$("#skuId").textbox('setValue','');
    	$("#goodsCategoryCombo").combotree('setValue','');
    	$("#goodsCategoryCombo").combotree('setValue', '请选择');
    	var params = {};
    	$('#importFileList').datagrid('load', params);
    });
    
    //导入
    $("#import").click(function(){
    	$("#activityId").val("4");
    	var form = $("#ExcelFileForm");
    	var file = $("#Excelfile").val();
    	if(file == null || file == ''){
    		$.messager.alert("<font color='black'>提示</font>","请选择要上传的Excel文件","info");
    		return;
    	}
    	form.form("submit",{
    		url : ctx + '/application/activity/importFile',
    		success : function(response) {
    			var data = JSON.parse(response);
    			if(data.status == '1'){
    				$.messager.alert("<font color='black'>提示</font>", data.msg, "info");
    				$('#Excelfile').val('');
    				var params = {};
    		    	$('#importFileList').datagrid('load', params);
    			}else{
    				$.messager.alert("<font color='black'>提示</font>", data.msg, "info");
    			}
    		}
    	});
    });
	//单个商品添加至
	$.editGoodsAndActivity = function(goodsId,activityId) {
		/**加载该活动的分组**/
		var params = {};
		params['activityId']=activityId;
		$('#groupName').combobox({
		    url:ctx + '/application/activity/loalgroupIds',
		    queryParams:params,
		    onLoadSuccess:function(object){		 
		    	var l=object.length;
		    	if(l>0){
		    		$("#addGoodsToGroup").window('open');
		    		$("#addGoodsToGroupActivityId").val(activityId);
		    		$("#addGoodsToGroupGoodsId").val(goodsId);
		    	}else{
		    		alert("请为该活动添加分组！");
		    	}
		    },
		    valueField:'id',
		    textField:'text'
		});
	};
	//按取消键 关闭弹框
	 $("#addGoodsToGroupOppo").click(function() {
 		$("#addGoodsToGroup").window('close');
	 });
		//确定关联分组
	 $("#addGoodsToGroupAgree").click(function() {
 		$("#addGoodsToGroup").window('close');
 		 var params = {};
 		 var activityId=$("#addGoodsToGroupActivityId").val();
 		 var goodsId=   $("#addGoodsToGroupGoodsId").val();
 		 var groupNameId=$("#groupName").textbox('getValue');
 		 if(null ==groupNameId || groupNameId ==""){
 			 alert("请选择分组！");
 			 return;
 		 }
 		 params['activityId']= activityId;
 		 params['goodsId']= goodsId
         params['groupNameId'] = groupNameId;
 		$.ajax({
 			type : "POST",
 			url : ctx + '/application/activity/addOneGoods',
 			data : params,
 			success : function(data) {
 				if(data.status=='1'){
 					alert(data.msg);
 				}else{
 					alert("添加失败！");
 				}
 				var params = {};
		    	$('#importFileList').datagrid('load', params);
 			}
 		});
	 });
    // 批量商品添加至
    $("#addGoods").click(function() {
    	var selRow = $('#importFileList').datagrid('getChecked');
		if(selRow.length==0){  
			 $.messager.alert("提示", "至少勾选一条数据！","info");  
			 return ;  
		}else{
//			var activityId= $("#addGoodsToGroupActivityId").val();
			var activityId='4';
			var goodsIdsString=selRow[0].goodsId;
			for (var i = 1; i < selRow.length; i++) {
				goodsIdsString=goodsIdsString+','+selRow[i].goodsId;
		      } 
			/**加载该活动的分组**/
			var params = {};
			params['activityId']=activityId;
			$('#groupName').combobox({
			    url:ctx + '/application/activity/loalgroupIds',
			    queryParams:params,
			    onLoadSuccess:function(object){		 
			    	var l=object.length;
			    	if(l>0){
			    		$("#addGoodsToGroup").window('open');
			    		$("#addGoodsToGroupActivityId").val(activityId);
			    	    $("#addGoodsToGroupGoodsId").val(goodsIdsString);
			    	}else{
			    		alert("请为该活动添加分组！");
			    	}
			    },
			    valueField:'id',
			    textField:'text'
			});
		}
    });
    
});

//判断是否超时
function ifLogout(data){
	if(data.message=='timeout' && data.result==false){
		$.messager.alert("操作提示", "登录超时, 请重新登录", "info");
		window.top.location = ctx + "/logout";
		return false;
	}
}