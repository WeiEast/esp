package com.apass.esp.web.category;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.dto.category.CategoryDto;
import com.apass.esp.domain.vo.CategoryVo;
import com.apass.esp.service.category.CategoryInfoService;
import com.apass.esp.utils.FileUtilsCommons;
import com.apass.esp.utils.ImageTools;
import com.apass.gfb.framework.jwt.common.ListeningRegExpUtils;

/**
 * 商品类别操作类
 */
@Controller
@RequestMapping("/categoryinfo/category")
public class CategoryController {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
	/**
	 * 返回页面
	 */
	private static final String CATEGORYPAGE= "goods/goodCategory";
	@Autowired
	private CategoryInfoService cateService;
	
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public ModelAndView categoryPage(CategoryDto dto) {
	    return  new ModelAndView(CATEGORYPAGE);
	}
	
	
	/**
     * 图片服务器地址
     */
    @Value("${nfs.rootPath}")
    private String                rootPath;
    @Value("${nfs.category}")
    private String                categoryPath;
    
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public List<CategoryVo> listConfig(@RequestBody CategoryDto dto) {
		
	   return  cateService.listCategory(dto);
	}
	
    /**
     * 添加一个分类
     * @param dto
     * @return
     */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
    public Response addCategory(@RequestBody CategoryDto dto){
    	try {
    		//验证传入参数是否符合要求
        	validateAddCategoryDto(dto);
        	cateService.addCategory(dto);
        	return Response.success("添加分类成功！");
		} catch (RuntimeException e) {
			return Response.fail(e.getMessage());
		}
    }
    
    /**
     * 修改一个分类的名称
     * @param dto
     */
    @RequestMapping(value = "/updateName", method = RequestMethod.POST)
    @ResponseBody
    public Response updateCategoryName(@RequestBody CategoryDto dto){
    	try {
    		validateUpdateCategoryDto(dto);
        	cateService.updateCategoryNameById(dto.getCategoryId(), dto.getCategoryName());
        	return Response.success("修改分类名称成功！");
		} catch (RuntimeException e) {
			return Response.fail(e.getMessage());
		}
    	
    }
    
    /**
     * 上传一个三级类目的小图标
     * @return
     * @throws IOException 
     */
    @ResponseBody
    @RequestMapping(value = "/addpic", method = RequestMethod.POST)
    public Response uploadPicFile(MultipartFile file) {
    	String url = "";
    	 try {
	    	if(file.isEmpty()){
	    		throw new RuntimeException("上传图片不能为空!");
	    	}
	    	String fileName  = file.getOriginalFilename();
	    	String imgType = ImageTools.getImgType(file);
	        url = categoryPath +"cate_"+ System.currentTimeMillis() + fileName + "." + imgType;
	    	/**
	         * 图片校验
	         */
	        boolean checkGoodBannerImgSize = ImageTools.checkSiftGoodsImgSize(file);// 尺寸
	        boolean checkImgType = ImageTools.checkImgType(file);// 类型
			int size = file.getInputStream().available();
			if (!(checkGoodBannerImgSize && checkImgType)) {
                file.getInputStream().close();// 284*284px;大小：≤500kb;.jpg .png
                return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：284px,高：284px,格式：.jpg,.png", url);
            } else if (size > 1024 * 300) {
                file.getInputStream().close();
                return Response.fail("文件不能大于300kb!", url);
            }
			FileUtilsCommons.uploadFilesUtil(rootPath, url, file);
			return Response.success("上传图片成功!", url);
		} catch (IOException e) {
			LOGGER.error("三级分类上传图片失败", e);
			return Response.fail("三级分类上传图片失败");
		}
    }
    
    /**
     * 修改一个分类的排序
     */
    @RequestMapping(value = "/updateSort", method = RequestMethod.POST)
    @ResponseBody
    public Response updateCategorySort(@RequestBody CategoryDto dto){
    	try {
    		if(dto.getCategoryId() == 0 || dto.getCategoryId() == null){
        		throw new RuntimeException("传入id为空！");
        	}
    		if(dto.getSortOrder() == null || dto.getSortOrder() == 0){
        		throw new RuntimeException("请明确当前分类的排序！");
        	}
    		cateService.updateCateSortOrder(dto.getCategoryId(), dto.getSortOrder());
    		return Response.success("修改分类排序成功！");
		} catch (RuntimeException e) {
			return Response.fail(e.getMessage());
		}
    }
    
    /**
     * 删除一个分类
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Response deleCategoryById(Long id){
    	try {
    		if(id == null || id == 0){
    			throw new RuntimeException("传入id为空！");
    		}
    		cateService.deleteCategoryById(id);
    		return Response.success("删除分类成功!");
		} catch (RuntimeException e) {
			return Response.fail(e.getMessage());
		}
    }
    
    /**
     * 修改分类验证
     * @param dto
     */
    public void validateUpdateCategoryDto(CategoryDto dto){
    	
    	if(dto.getCategoryId() == 0 || dto.getCategoryId() == null){
    		throw new RuntimeException("传入id为空！");
    	}
    	commonValidate(dto);
    }
    
    /**
     * 新增分类验证
     * @param dto
     */
    public void validateAddCategoryDto(CategoryDto dto){
    	
    	commonValidate(dto);
    	
    	if(dto.getLevel()==3){
    		if(StringUtils.isBlank(dto.getPictureUrl())){
    			throw new RuntimeException("三级必须上传图标！");
    		}
    	}
    	
    	if(dto.getLevel()>1){
    		if(dto.getParentId() ==0 ||dto.getParentId() == null){
    			throw new RuntimeException("请确定上级分类");
    		}
    	}
    	
    	if(dto.getSortOrder() == null || dto.getSortOrder() == 0){
    		throw new RuntimeException("请明确当前分类的排序！");
    	}
    }
    
    /**
     * 修改和新增分类都要用到的验证
     * @param dto
     */
    public void commonValidate(CategoryDto dto){
    	
    	if(dto.getLevel() == 0){
    		throw new RuntimeException("请输入该类目的排列位置");
    	}
    	
    	if(dto.getLevel()<1 || dto.getLevel()>3){
    		throw new RuntimeException("请输入该类目的有效排列位置");
    	}
    	
    	//如果是一级分类 name的值为汉字，切长度为1,5之间
    	if(dto.getLevel() == 1){
    		if(!ListeningRegExpUtils.length(dto.getCategoryName(),1,5) && !ListeningRegExpUtils.isChineseCharacter(dto.getCategoryName())){
    			throw new RuntimeException("类目名称格式不正确，请输入6位以下汉字");
        	}
    	}
    	//如果是二级分类
    	if(dto.getLevel() == 2){
    		String name = dto.getCategoryName();
	    	if(!ListeningRegExpUtils.length(name,1,15)){
	    		throw new RuntimeException("类目名称格式不正确，请输入15位以下汉字和字母！");
	    	}
	    	if(!ListeningRegExpUtils.isChineseOrLetterCharacter(name) && !ListeningRegExpUtils.isLetterCharacter(name) && !ListeningRegExpUtils.isChineseCharacter(name)){
	    		throw new RuntimeException("类目名称格式不正确，请输入15位以下汉字和字母！");
	    	}
    	}
    	//如果是三级分类
    	if(dto.getLevel() == 3){
    		if(!ListeningRegExpUtils.length(dto.getCategoryName(),1,20)){
	    		throw new RuntimeException("类目名称格式不正确，请输入20位以下汉字，字母，数字，特殊字符！");
	    	}
    	}
    	
    }
}
