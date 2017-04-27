$(function(){
	$('#addMerchantInfor').window('close');
	$('#editMerchantInfor').window('close');
	
	//Grid 列表
	$("#merchantInforList").datagrid({
        title : '商户信息',
        fit : true,
        fitColumns : true,
        rownumbers : true,
        pagination : true,
        singleSelect : true,
        striped:true,
        toolbar : '#tb',
        columns : [[{
            title : '商户编码',
            field : 'merchantCode',
            width : 150,
            align : 'center'
        }, {
            title : '商户名称',
            field : 'merchantName',
            width : 150,
            align : 'center'
        }, {
            title : '商户昵称',
            field : 'merchantNickname',
            width : 150,
            align : 'center'
        },{
            title : '商户类型',
            field : 'merchantType',
            width : 150,
            align : 'center',
            formatter:function(value,row,index){
            	if(value=="1"){
            		return "个人";
            	}else if(value=="2"){
            		return "企业";
            	}
            }
        },{
            title : '结算日期',
            field : 'merchantSettlementDate',
            width : 150,
            align : 'center',
            formatter:function(value,row,index){
            	if(value!=null){
            		return value+"号";
            	}
            }
        },{
            title : '结算银行名称',
            field : 'settlementBankName',
            width : 120,
            align : 'center'
        },{
            title : '商户状态',
            field : 'status',
            width : 150,
            align : 'center',
            formatter:function(value,row,index){
            	if(value=="1"){
            		return "正常";
            	}else if(value=="-1"){
            		return "无效";
            	}else if(value=="0"){
            		return "待审核";
            	}
            }
        },{
            title : '操作',
            field : 'opt',
            width : 150,
            align : 'center',
            formatter : function(value, row, index) {
            	// 授权标示
            	var grantedAuthority=$('#grantedAuthority').val();
            	var content = "";
            	if(grantedAuthority=='permission'){
            	if(row.status=="1"){
                    content += "<a href='javascript:void(0);' class='easyui-linkedbutton' onclick=\"$.Edit('"
        				+ encodeURI(JSON.stringify(row) )+ "');\">编辑</a>&nbsp;&nbsp;";
                    content += "<a href='javascript:void(0);' class='easyui-linkedbutton' onclick=\"$.setStatus('"
                    	+ row.merchantCode+"','"+row.status + "');\">设为无效</a>&nbsp;&nbsp;";
            	}//encodeURI(JSON.stringify(row) )
            	if(row.status=="-1"){
            	    content += "<a href='javascript:void(0);' class='easyui-linkedbutton' onclick=\"$.setStatus('"
						+ row.merchantCode+"','"+row.status + "');\">设为正常</a>&nbsp;&nbsp;";
            		}
            	}else{
            		content +="权限不足";
            	}
            	return content;
            }
        }]],
        loader : function(param, success, error) {
            $.ajax({
                url : ctx + '/application/merchantinfor/merchant/query',
                data : param,
                type : "post",
                dataType : "json",
                success : function(data) {
                	//console.log(data);
                    $.validateResponse(data, function() {
                    	  success(data);
                    });
                }
            })
        }
	});
	
	//添加商户信息
	$("#add").click(function(){
		//清空弹出框中的数据
		$("#addMerchantCode").textbox('setValue','');
		$("#addMerchantName").textbox('setValue','');
		$("#addMerchantProvince").combobox('setValue','');
		$("#addMerchantCity").combobox('setValue','');
		$("#addMerchantAddress").textbox('setValue','');
		$("#addMerchantReturnAddress").textbox('setValue','');
		$("#addMerchantReturnName").textbox('setValue','');
		$("#addMerchantReturnPhone").textbox('setValue','');
		$("#addMerchantReturnPostCode").textbox('setValue','');
		$("#addMerchantPostcode").textbox('setValue','');
		$("#addMerchantType").combobox('setValue','');
		$("#addMerchantNickname").textbox('setValue','');
		$("#addIsContainFreight").combobox('setValue','');
		$("#addMerchantSettlementDate").combobox('setValue','');
		$("#addSettlementBankName").textbox('setValue','');
		$("#addSettlementCardNo").textbox('setValue','');
		$("#addManageType").textbox('setValue','');
		$("#addOrgCode").textbox('setValue','');
		//打开弹出框
		$('#addMerchantInfor').window({
            shadow: true,
            minimizable:false,
            maximizable:false,
            collapsible:false,
            modal:true 
		});
		//加载省份 和城市
		loadDirect("addMerchantProvince","addMerchantCity");
		$('#addMerchantInfor').window('open');
	});
	
	
	//邮编校验
	$("input",$("#addMerchantPostcode").next("span")).blur(function(){  
	    debugger; 
		var postCode = $("#addMerchantPostcode").textbox('getValue');
		//正则对象的标准声明方式，RegExp  
		var pattern=new RegExp(/^\d{6}$/);  //只能输入6位数字
		if(!regExp_pattern(postCode,pattern)){
			$.messager.alert("<span style='color: black;'>提示</span>","邮政编码只能是6位数字！",'info');
			 $("#addMerchantPostcode").textbox('setValue','');
			return;
		}
	})  
	//商户编码校验
//	$("input",$("#addMerchantCode").next("span")).blur(function(){  
//		debugger;
//		var merchantCode = $("#addMerchantCode").textbox('getValue');
//		var pattern=new RegExp(/^.{4}$/);  //只能输入4位
//		if(!regExp_pattern(merchantCode,pattern)){
//			$.messager.alert("<span style='color: black;'>提示</span>","商户编码必须为4位！",'info');
//			$("#addMerchantCode").textbox('clear');
//			return;
//		}
//	})  
	
	//确认   添加商户信息
	$("#agreeAdd").click(function(){
		debugger;
		var merchantCode = $("#addMerchantCode").textbox('getValue');
		if (null == merchantCode || ("") == merchantCode) {
			$.messager.alert("<span style='color: black;'>提示</span>","商户编码不能为空！",'info');
			return;
		}
//		if(merchantCode.length != 4){
//			$.messager.alert("<span style='color: black;'>提示</span>","商户编码必须为4位！",'info');
//			return;
//		}
		
		var merchantName= $("#addMerchantName").textbox('getValue');
		
		if (null == merchantName || ("") == merchantName) {
			// "<span style='color: red;'>提示</span>"
			$.messager.alert("<span style='color: black;'>提示</span>","商户名称不能为空！",'info');
			return;
		}
		
		if(merchantName.length>15){   //长度可自定义
	    	$.messager.alert("<span style='color: black;'>警告</span>","商户名称长度不能超过15个字！",'warning');
	    	return;
	    }
		var merchantReturnAddress = $("#addMerchantReturnAddress").textbox('getValue');//二期后台电商需要添加商家退货地址
		var merchantReturnName = $("#addMerchantReturnName").textbox('getValue');
		var merchantReturnPhone = $("#addMerchantReturnPhone").textbox('getValue');
		var merchantReturnPostCode = $("#addMerchantReturnPostCode").textbox('getValue');
		
		if(null ==merchantReturnAddress || merchantReturnAddress.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","商户退货地址不能为空！",'info');
	    	return;
	    }
		if(merchantReturnAddress.length>80){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","商户退货地址长度不能超过80！",'warning');
	    	return;
	    }
		if(null ==merchantReturnName || merchantReturnName.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","收货人姓名长度不能为空！",'info');
	    	return;
	    }
		if(merchantReturnName.length>12){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","收货人姓名长度不能超过12！",'warning');
	    	return;
	    }
		if(null ==merchantReturnPhone || merchantReturnPhone.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","收货人联系电话长度不能为空！",'info');
	    	return;
	    }
		if(merchantReturnPhone.length>11){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","收货人联系电话长度不能超过11！",'warning');
	    	return;
	    }
		var merchantReturnPhoneFalge=/^1\d{10}$/.test(merchantReturnPhone);
		if(!merchantReturnPhoneFalge){
			$.messager.alert("<span style='color: black;'>警告</span>","收货人联系电话填写错误！",'warning');
	    	return;
		}
		
		var merchantProvince = $("#addMerchantProvince").combobox('getValue');//数据库存储对应省市的code更加灵活
//		var merchantProvince = $("#addMerchantProvince").combobox('getText');
		var merchantCity = $("#addMerchantCity").combobox('getValue');
		var merchantAddress = $("#addMerchantAddress").textbox('getValue');
		var merchantPostcode = $("#addMerchantPostcode").textbox('getValue');
		var merchantType = $("#addMerchantType").combobox('getValue');
		var merchantNickname = $("#addMerchantNickname").textbox('getValue');
		var isContainFreight = $("#addIsContainFreight").combobox('getValue');
		if (null == isContainFreight || ("") == isContainFreight) {
			$.messager.alert("<span style='color: black;'>提示</span>","是否含运费不能为空！",'info');
			return;
		}
		var merchantSettlementDate = $("#addMerchantSettlementDate").combobox('getValue');
		var settlementBankName = $("#addSettlementBankName").textbox('getValue');
		var settlementCardNo = $("#addSettlementCardNo").textbox('getValue');
		var manageType = $("#addManageType").textbox('getValue');
		var orgCode = $("#addOrgCode").textbox('getValue');
        
		var params={
			merchantCode: merchantCode,//商户编码
			merchantName: merchantName,//商户名称
			merchantProvince: merchantProvince,
			merchantCity: merchantCity,
			merchantAddress: merchantAddress,
			merchantReturnAddress:merchantReturnAddress,//商家退货地址
			merchantReturnName :merchantReturnName,
			merchantReturnPhone :merchantReturnPhone,
			merchantReturnPostCode :merchantReturnPostCode,
			merchantPostcode: merchantPostcode,//邮政编码
			merchantType: merchantType,//商户类型（个人、企业）
		    merchantNickname:merchantNickname,//商户昵称
			isContainFreight:isContainFreight,//是否
			merchantSettlementDate: merchantSettlementDate,//结算日期
			settlementBankName: settlementBankName,//结算银行名称
			settlementCardNo: settlementCardNo,//结算银行卡号
			manageType: manageType,//经营类型
			orgCode: orgCode//企业机构代码
		};
		$.ajax({
            url : ctx + '/application/merchantinfor/merchant/add',
            data : params,
            type : "post",
            dataType : "json",
            success : function(data) {
            	debugger;
            	console.log(data);
            	if (data.result == false && data.message == 'timeout')
			    {
//            		$.messager.alert("提示", "超时，请重新登录", "info");
//            		parent.location.reload();//刷新整个页面
            		$.messager.alert("操作提示", "登录超时, 请重新登录", "info");
    				window.top.location = ctx + "/logout";
    				return false;
			    }
            	if(data.status=="1"){
            		$.messager.alert("<span style='color: black;'>提示</span>",data.msg,'info'); 
            		//关闭弹出框
            		$('#addMerchantInfor').window('close');
            		var params={};
            		$('#merchantInforList').datagrid('load', params);
            	}else if(data.status=="0"){
            		$.messager.alert("<span style='color: black;'>警告</span>",data.msg,'warning');
            	}else{
            		$.messager.alert("<span style='color: black;'>错误</span>","添加商户信息失败！",'error');  
            	}
            }
		});
		
	});
	//取消  添加商户信息
	$("#cancelAdd").click(function(){
		$('#addMerchantInfor').window('close');
	});
	
	//监听事件,当页面省(直辖市)回显时加载对应二级区域
	$("#editMerchantProvince").combobox({
		onChange: function (n,o) {
			var code=$("#editMerchantProvince").combobox('getValue');
			$('#editMerchantCity').combobox({
				method:"get",
				url:ctx + "/application/nation/queryNations?districtCode="+code,
			    valueField:'code',
			    textField:'name'
			});
		}
	});
	
	
	//编辑   商户信息
	$.Edit=function(row){
		debugger;
		//加载省份 和城市
		loadDirect("editMerchantProvince","editMerchantCity");
		//string转json对象
		var row3=JSON.parse(decodeURI(row));
		console.log(row3);
		//回显编辑弹出框中的数据
		$("#merchantId").val(row3.id);
		$("#editMerchantCode").textbox('setValue',row3.merchantCode);
		$("#editMerchantName").textbox('setValue',row3.merchantName);
		$("#editMerchantProvince").combobox('setValue',row3.merchantProvince);
		$("#editMerchantCity").combobox('setValue',row3.merchantCity);
		$("#editMerchantAddress").textbox('setValue',row3.merchantAddress);
		$("#editMerchantReturnAddress").textbox('setValue',row3.merchantReturnAddress);//商家退货地址
		$("#editMerchantReturnName").textbox('setValue',row3.merchantReturnName);
		$("#editMerchantReturnPhone").textbox('setValue',row3.merchantReturnPhone);
		$("#editMerchantReturnPostCode").textbox('setValue',row3.merchantReturnPostCode);
		$("#editMerchantPostcode").textbox('setValue',row3.merchantPostcode);
		//alert(row3.merchantType);
		$("#editMerchantType").combobox('setValue',row3.merchantType);
		debugger;
		if(null ==row3.merchantSettlementDate || ''==row3.merchantSettlementDate){
			$("#editMerchantSettlementDate").combobox('setValue','');
		}else{
			$("#editMerchantSettlementDate").combobox('setValue',row3.merchantSettlementDate);
		}
		$("#editMerchantNickname").textbox('setValue',row3.merchantNickname);
		$("#editIsContainFreight").combobox('setValue',row3.isContainFreight);
		$("#editSettlementBankName").textbox('setValue',row3.settlementBankName);
		$("#editSettlementCardNo").textbox('setValue',row3.settlementCardNo);
		$("#editManageType").textbox('setValue',row3.manageType);
		$("#editOrgCode").textbox('setValue',row3.orgCode);
		debugger;
		$("#message").textbox('setValue',row3.remark);
		
		
		//打开编辑弹出框
		$('#editMerchantInfor').window({
            shadow: true,
            minimizable:false,
            maximizable:false,
            collapsible:false,
            modal:true 
		});
		
		$('#editMerchantInfor').window('open');
		
	}
	
	//确认  编辑商户信息
	$("#agreeEdit").click(function(){
		debugger;
		var id=$("#merchantId").val();
		if(null==id || ("")==id){
			$.messager.alert("<span style='color: black;'>提示</span>","商户id不能为空!",'info');  
			return;
		}
		
		var merchantCode = $("#editMerchantCode").textbox('getValue');
		if (null == merchantCode || ("") == merchantCode) {
			$.messager.alert("<span style='color: black;'>提示</span>","商户编码不能为空!",'info');  
			return;
		}
		var merchantName = $("#editMerchantName").textbox('getValue');
		if (null == merchantName || ("") == merchantName) {
			$.messager.alert("<span style='color: black;'>提示</span>","商户名称不能为空!",'info');  
			return;
		}
		var isContainFreight = $("#editIsContainFreight").combobox('getValue');
		if (null == isContainFreight || ("") == isContainFreight) {
			$.messager.alert("<span style='color: black;'>提示</span>","结算是否含运费不能为空!",'info');  
			return;
		}
		var merchantReturnAddress = $("#editMerchantReturnAddress").textbox('getValue');//商家退货地址
		var merchantReturnName = $("#editMerchantReturnName").textbox('getValue');
		var merchantReturnPhone = $("#editMerchantReturnPhone").textbox('getValue');
		var merchantReturnPostCode = $("#editMerchantReturnPostCode").textbox('getValue');
		
		if(null ==merchantReturnAddress || merchantReturnAddress.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","商户退货地址不能为空！",'info');
	    	return;
	    }
		if(merchantReturnAddress.length>80){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","商户退货地址长度不能超过80！",'warning');
	    	return;
	    }
		if(null ==merchantReturnName || merchantReturnName.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","收货人姓名长度不能为空！",'info');
	    	return;
	    }
		if(merchantReturnName.length>12){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","收货人姓名长度不能超过12！",'warning');
	    	return;
	    }
		if(null ==merchantReturnPhone || merchantReturnPhone.length==0){ 
	    	$.messager.alert("<span style='color: black;'>提示</span>","收货人联系电话长度不能为空！",'info');
	    	return;
	    }
		if(merchantReturnPhone.length>11){ 
	    	$.messager.alert("<span style='color: black;'>警告</span>","收货人联系电话长度不能超过11！",'warning');
	    	return;
	    }
		var merchantReturnPhoneFalge=/^1\d{10}$/.test(merchantReturnPhone);
		if(!merchantReturnPhoneFalge){
			$.messager.alert("<span style='color: black;'>警告</span>","收货人联系电话填写错误！",'warning');
	    	return;
		}
		
		var merchantProvince = $("#editMerchantProvince").combobox('getValue');
		var merchantCity = $("#editMerchantCity").combobox('getValue');
//		var merchantCity = $("#editMerchantCity").combobox('getText');
		var merchantAddress = $("#editMerchantAddress").textbox('getValue');
		var merchantPostcode = $("#editMerchantPostcode").textbox('getValue');
		var merchantType = $("#editMerchantType").combobox('getValue');
		var merchantNickname = $("#editMerchantNickname").textbox('getValue');
		var merchantSettlementDate = $("#editMerchantSettlementDate").combobox('getValue');
		var settlementBankName = $("#editSettlementBankName").textbox('getValue');
		var settlementCardNo = $("#editSettlementCardNo").textbox('getValue');
		var manageType = $("#editManageType").textbox('getValue');
		var orgCode = $("#editOrgCode").textbox('getValue');
		debugger;
		var remark = $("#message").textbox('getValue');
		
		var params={
			id: id,//商户id
			merchantCode: merchantCode,//商户编码
			merchantName: merchantName,//商户名称
			merchantProvince: merchantProvince,
			merchantCity: merchantCity,
			merchantAddress: merchantAddress,
			merchantReturnAddress:merchantReturnAddress,
			merchantReturnName :merchantReturnName,
			merchantReturnPhone :merchantReturnPhone,
			merchantReturnPostCode :merchantReturnPostCode,
			merchantPostcode: merchantPostcode,//邮政编码
			merchantType: merchantType,//商户类型（个人、企业）
		    merchantNickname: merchantNickname,//商户昵称
			isContainFreight: isContainFreight,//是否
			merchantSettlementDate: merchantSettlementDate,//结算日期
			settlementBankName: settlementBankName,//结算银行名称
			settlementCardNo: settlementCardNo,//结算银行卡号
			manageType: manageType,//经营类型
			orgCode: orgCode,//企业机构代码
			remark:remark//备注
		};
		
		$.ajax({
            url : ctx + '/application/merchantinfor/merchant/edit',
            data : params,
            type : "post",
            dataType : "json",
            success : function(data) {
            	if(data.status=="1"){
            		$.messager.alert("<span style='color: black;'>提示</span>",data.msg,'info');  
            		//关闭弹出窗
            		$('#editMerchantInfor').window('close');
            		var params={};
            		$('#merchantInforList').datagrid('load', params);
            	}else{
            		$.messager.alert("<span style='color: black;'>错误</span>","编辑商户信息失败！",'error');  
            	}
            }
		});
		
	});
	//取消  编辑商户信息
	$("#cancelEdit").click(function(){
		$('#editMerchantInfor').window('close');
	});
	//设置商户状态
	$.setStatus=function(merchantCode,status){
		debugger;
		var nowStatus=null;
		if(status=="1"){
			$.messager.confirm("<span style='color: black;'>操作提示</span>", "您确定将该条商户信息设为无效？", function (r) {  
	            if (r) { 
	            	nowStatus="-1"
	            	setStatusMethod(merchantCode,nowStatus);
	            }	
	        });  
		}else if(status=="-1"){
			nowStatus="1"
			setStatusMethod(merchantCode,nowStatus);
		}
		
	}
	
	//设置商户状态的具体方法
	function setStatusMethod(merchantCode,nowStatus){
		var params={};
		params['merchantCode']=merchantCode;
		params['status']=nowStatus;
		$.ajax({
            url : ctx + '/application/merchantinfor/merchant/editStatus',
            data : params,
            type : "post",
            dataType : "json",
            success : function(data) {
            	if(data.status=="1"){
            		$.messager.alert("<span style='color: black;'>提示</span>",data.msg,'info');  
            		var params={};
            		$('#merchantInforList').datagrid('load', params);
            	}else{
            		$.messager.alert("<span style='color: black;'>错误</span>",'编辑商户状态失败！','error'); 
            	}
            }
		});
	}
	
	//刷新
	$("#flush").click(function(){
		$("#merchantName").textbox('setValue','');
		$("#merchantType").combobox('setValue','');
		var params = {};
		$('#merchantInforList').datagrid('load', params);
	});
	//查询
	$(".search-btn").click(function(){
		var params = {};
		params['merchantName'] = $("#merchantName").textbox('getValue');
		params['merchantType'] = $("#merchantType").combobox('getValue');
		
		$('#merchantInforList').datagrid('load', params);
	});
	
});


function loadDirect(provinceId,cityId){
	debugger;
	$('#'+provinceId).combobox({
		method:"get",
	    url:ctx + "/application/nation/queryNations",
	    valueField:'code',
	    textField:'name',
   	    onSelect:function(data){
	    	debugger;
	    	console.log(data);
			$('#'+cityId).combobox({
			    //url:ctx + "/application/merchantinfor/merchant/queryCity?provinceId="+data.code,
				method:"get",
				url:ctx + "/application/nation/queryNations?districtCode="+data.code,
			    valueField:'code',
			    textField:'name'
			});
	    }
		
	});
}


function regExp_pattern(str,pattern){  
	   return pattern.test(str);  
}  