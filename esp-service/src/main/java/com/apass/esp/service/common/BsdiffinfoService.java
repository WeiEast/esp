package com.apass.esp.service.common;

import com.apass.esp.domain.entity.BsdiffInfoEntity;
import com.apass.esp.domain.entity.BsdiffVo;
import com.apass.esp.mapper.BsdiffInfoEntityMapper;
import com.apass.esp.utils.FileUtilsCommons;
import com.apass.lib.utils.BsdiffUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class BsdiffinfoService {
	@Autowired
	private BsdiffInfoEntityMapper bsdiffInfoEntityMapper;

	private static final Logger LOGGER = LoggerFactory.getLogger(BsdiffinfoService.class);
	@Value("${nfs.rootPath}")
	private String rootPath;
	@Value("${nfs.bsdiff}")
	private String nfsBsdiffPath;

	private static final String VERPATH = "/verzip";
	private static final String PATCHPATH = "/patchzip";


    @Transactional
    public void bsdiffUpload(BsdiffVo bsdiffVo, BsdiffInfoEntity bsdiffInfoEntity) throws IOException {
        StringBuffer sb = new StringBuffer();

		//如果版本号已存在，给出提示
        String bsdiffVer = bsdiffVo.getBsdiffVer();

        List<BsdiffInfoEntity> bsdiffInfoEntities = listAll();
        if(CollectionUtils.isNotEmpty(bsdiffInfoEntities)){
            for (BsdiffInfoEntity bsEn: bsdiffInfoEntities) {
                if(StringUtils.equals(bsdiffVer,bsEn.getBsdiffVer())){
                    throw new RuntimeException("版本号已经存在，请重新填写版本号!");
                }
            }
        }

        MultipartFile bsdiffFile = bsdiffVo.getBsdiffFile();
        String[] split = bsdiffFile.getOriginalFilename().split("\\.");
        if(!StringUtils.equals("zip",split[1])){
            throw new RuntimeException("请上传zip文件 .");
        }
        if(!StringUtils.equals(bsdiffVer,split[0])){
            throw new RuntimeException("版本要与zip文件名一致.");
        }

		String originalFilename = bsdiffFile.getOriginalFilename();

		bsdiffInfoEntity.setBsdiffVer(bsdiffVer);

		//判断服务器端是否有更早版本的文件，如果没有 直接上传，如果有 增量拆分
		File directory = new File(rootPath+nfsBsdiffPath+VERPATH);
		if(!directory.exists()){//如果目录不存在创建目录
			directory.mkdirs();
		}
		if(!(directory.listFiles().length > 0)){//第一次上传
			int count = bsdiffInfoEntityMapper.insertSelective(bsdiffInfoEntity);//先操作数据库，再上传文件。
			//没有增量拆分操作
			if(count == 1){
				FileUtilsCommons.uploadFilesUtil(rootPath, nfsBsdiffPath+VERPATH+"/"+originalFilename, bsdiffFile);
			}
		}else{//增量拆分
			for(int i=Integer.valueOf(bsdiffVer)-1;i>0;i--){
				if(i>1){
					sb.append(bsdiffVer+"_"+i+",");
				}else {
					sb.append(bsdiffVer+"_"+i);
				}
			}
			int count = bsdiffInfoEntityMapper.insertSelective(bsdiffInfoEntity);//先操作数据库，再上传文件。
			if(count == 1){
				FileUtilsCommons.uploadFilesUtil(rootPath, nfsBsdiffPath+VERPATH+"/"+originalFilename, bsdiffFile);
				File directoryPatch = new File(rootPath+nfsBsdiffPath+PATCHPATH);
				if(!directoryPatch.exists()){//如果目录不存在创建目录
					directoryPatch.mkdirs();
				}
				for(int i=Integer.valueOf(bsdiffVer)-1;i>0;i--){
//					File oldFile = new File(rootPath+nfsBsdiffPath+VERPATH+"/"+i+".zip");
//					File newFile = new File(rootPath+nfsBsdiffPath+VERPATH+"/"+originalFilename);
//					File diffFile = new File(rootPath+nfsBsdiffPath+PATCHPATH+"/"+bsdiffVer+"_"+i+".zip");

					BsdiffUtils.getInstance().bsdiff(rootPath+nfsBsdiffPath+VERPATH+"/"+i+".zip",rootPath+nfsBsdiffPath+VERPATH+"/"+originalFilename,rootPath+nfsBsdiffPath+PATCHPATH+"/"+bsdiffVer+"_"+i+".zip");
				}
			}
		}
    }

	public List<BsdiffInfoEntity> listAll(){
		return bsdiffInfoEntityMapper.selectAllBsdiff();
	}
}
