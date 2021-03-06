package com.apass.esp.nothing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.FeedBack;
import com.apass.esp.domain.enums.FeedBackCategory;
import com.apass.esp.domain.query.FeedBackQuery;
import com.apass.esp.domain.vo.FeedBackVo;
import com.apass.esp.service.feedback.FeedBackService;
import com.apass.esp.utils.FileUtilsCommons;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.esp.web.feedback.EmojiFilter;
import com.apass.esp.web.feedback.FeedbackController;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;

@Path("/v1/feedback")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedbackabsController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackabsController.class);

	@Autowired
	public FeedBackService feedBackService;
	/**
     * 图片服务器地址
     */
    @Value("${nfs.rootPath}")
    private String rootPath;
    /**
     * 意见反馈图片存放地址
     */
    @Value("${nfs.feedback}")
    private String nfsFeedback;
	/**
	 * 意见反馈保存（额度）
	 */
	@POST
    @Path("/saveCredit")
	public Response saveCreditFeedback(Map<String, Object> paramMap) {
		String feedbackType = CommonUtils.getValue(paramMap, "feedbackType");//意见反馈类型
		String comments = CommonUtils.getValue(paramMap, "comments");//意见反馈内容
		String mobile = CommonUtils.getValue(paramMap, "mobile");//反馈者手机号

		String picture1=CommonUtils.getValue(paramMap, "picture1");//图片1
		String picture2=CommonUtils.getValue(paramMap, "picture2");//图片2
		String picture3=CommonUtils.getValue(paramMap, "picture3");//图片3
		String picture1Url="";
		String picture2Url="";
		String picture3Url="";
		String pictureUrl="";
		
		FeedBack fb=new FeedBack();

		if(StringUtils.isBlank(feedbackType)){
			return Response.fail("反馈类型不能为空！");
		}
		if(StringUtils.isBlank(comments)){
			return Response.fail("反馈内容不能为空！");
		}	
		Random radom = new Random();
		int radomNumber = radom.nextInt(10000);
		if (StringUtils.isNotBlank(picture1)) {
			picture1Url = nfsFeedback + mobile + "_" + radomNumber + ".jpg";
			byte[] picture1Byte = Base64Utils.decodeFromString(picture1);
			FileUtilsCommons.uploadByteFilesUtil(rootPath, picture1Url, picture1Byte);
			pictureUrl=picture1Url;
		}
		if (StringUtils.isNotBlank(picture2)) {
			picture2Url = nfsFeedback + mobile + "_" + (radomNumber+1) + ".jpg";
			byte[] picture2Byte = Base64Utils.decodeFromString(picture2);
			FileUtilsCommons.uploadByteFilesUtil(rootPath, picture2Url, picture2Byte);
			pictureUrl=pictureUrl+";"+picture2Url;
		}
		if (StringUtils.isNotBlank(picture3)) {
			picture3Url = nfsFeedback + mobile + "_" + (radomNumber+2) + ".jpg";
			byte[] picture3Byte = Base64Utils.decodeFromString(picture3);
			FileUtilsCommons.uploadByteFilesUtil(rootPath, picture3Url, picture3Byte);
			pictureUrl=pictureUrl+";"+picture3Url;
		}
		if(comments.length()>255){
			LOGGER.error("反馈内容输入的字数过长！");
			return Response.fail("反馈内容输入的字数过长！");
		}
		Boolean falge=EmojiFilter.containsEmoji(comments);
		String csom="";
		if(falge){
			csom=EmojiFilter.filterEmoji(comments);
		}else{
			csom=comments;
		}
		Date date=new Date();
		fb.setType(FeedBackCategory.ANJIAPAI.getCode());
		fb.setPicture(pictureUrl);
		fb.setFeedbackType(feedbackType);
		fb.setComments(csom);
		fb.setMobile(mobile);
		fb.setCreateDate(date);
		fb.setUpdateDate(date);

		Integer result=feedBackService.insert(fb);
		if(result==1){
			return Response.success("提交成功，非常感谢您的反馈！");
		}
		LOGGER.error("意见反馈失败！");
		return Response.fail("意见反馈保存失败!");
	}
	/**
	 * 查询意见反馈
	 */
	@POST
	@Path("/selectFeedback")
	public Response selectFeedback(Map<String, Object> paramMap) {
		try {
			String page = CommonUtils.getValue(paramMap, "page");
			String rows = CommonUtils.getValue(paramMap, "rows");

			String feedbackType = CommonUtils.getValue(paramMap, "feedbackType");// 意见反馈类型
			String mobile = CommonUtils.getValue(paramMap, "mobile");// 反馈者手机号

			String createDateBegin = CommonUtils.getValue(paramMap, "createDateBegin");// 提交时间（开始）
			String createDateEnd = CommonUtils.getValue(paramMap, "createDateEnd");// 提交时间（结束）
			FeedBackQuery feedBackQuery = new FeedBackQuery();
			if(StringUtils.isBlank(page)){
				feedBackQuery.setPage(Integer.parseInt("1"));
			}else{
				feedBackQuery.setPage(Integer.parseInt(page));
			}
			if(StringUtils.isBlank(rows)){
				feedBackQuery.setRows(Integer.parseInt("10"));
			}else{
				feedBackQuery.setRows(Integer.parseInt(rows));
			}
			
			feedBackQuery.setFeedbackType(feedbackType);
			feedBackQuery.setMobile(mobile);
			feedBackQuery.setCreateDateBegin(createDateBegin);
			feedBackQuery.setCreateDateEnd(createDateEnd);
			feedBackQuery.setType(FeedBackCategory.ANJIAPAI.getCode());
			Map<String, Object> result = new HashMap<>();
			ResponsePageBody<FeedBackVo> pagination;

			pagination = feedBackService.getFeedBackListPage(feedBackQuery);

			result.put("FeedBackList", pagination.getRows());
			result.put("total", pagination.getTotal());
			if (CommonCode.SUCCESS_CODE.equals(pagination.getStatus())) {
				return Response.success("查询成功！", result);
			}
			return Response.fail("查询失败！");
		} catch (BusinessException e) {
			return Response.fail("查询失败！");
		}
	}
}
