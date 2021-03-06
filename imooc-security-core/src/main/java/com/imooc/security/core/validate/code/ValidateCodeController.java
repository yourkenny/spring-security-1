package com.imooc.security.core.validate.code;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import com.imooc.security.core.properties.SecurityConstants;
import com.imooc.security.core.properties.SecurityProperties;
import com.imooc.security.core.validate.code.sms.SmsCodeSender;

/**
 * 验证码Control
 * ClassName: ValidateCodeController 
 * @Description: TODO
 * @author lihaoyang
 * @date 2018年3月1日
 */
@RestController
public class ValidateCodeController {
	
//	public static final String SESSION_KEY_IMAGE = "SESSION_KEY_IMAGE_CODE";	
	
//	public static final String SESSION_KEY_SMS = "SESSION_KEY_SMS_CODE";	
	
//	@Autowired
//	private SecurityProperties securityProperties;
	
	@Autowired
	private ValidateCodeGenerator imageCodeGenerator;//图片验证码
	
	@Autowired
	private ValidateCodeGenerator smsCodeGenerator;//短信验证码
	
//	//获取session工具类
	private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();
	
	/**
	 * session存取验证码策略
	 * TODO:重构为调用存储策略存取验证码
	 * 此处注入接口，浏览器和app有不同的实现
	 */
	@Autowired
	private ValidateCodeRepository validateCodeRepository;
	
	
	@Autowired
	private SmsCodeSender smsCodeSender; //短信验证码发送接口
	
	/**
	 * 图片验证码
	 * @Description: TODO
	 * @param @param request
	 * @param @param response
	 * @param @throws IOException   
	 * @return void  
	 * @throws
	 * @author lihaoyang
	 * @date 2018年3月7日
	 */
	@GetMapping(SecurityConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX+"/image")
	public void createCode(HttpServletRequest request,HttpServletResponse response) throws IOException{
		
//		ImageCode imageCode = createImageCode(request, response);
		//调图片生成接口方式
		ImageCode imageCode = (ImageCode) imageCodeGenerator.generator(new ServletWebRequest(request));
		
		/**
		 * 不能存在session里了，应用调接口，具体依赖app还是browser项目，不同的实现
		 */
//		sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_IMAGE, imageCode);
		validateCodeRepository.save(new ServletWebRequest(request), imageCode, ValidateCodeType.IMAGE);
		
		ImageIO.write(imageCode.getImage(), "JPEG", response.getOutputStream());
	}

	
//	private ImageCode createImageCode(HttpServletRequest request, HttpServletResponse response) {
//		//先从request里读取有没有长、宽、字符个数参数，有的话就用，没有用默认的
//		int width  = ServletRequestUtils.getIntParameter(request, "width",securityProperties.getCode().getImage().getWidth());
//    	
//    	int height = ServletRequestUtils.getIntParameter(request, "height",securityProperties.getCode().getImage().getHeight());
//    	
//    	int charLength = this.securityProperties.getCode().getImage().getLength();
//		VerifyCode verifyCode = new VerifyCode(width,height,charLength);
//		return new ImageCode(verifyCode.getImage(),verifyCode.getText(),this.securityProperties.getCode().getImage().getExpireIn());
//	}
	
	/**
	 * 短信验证码
	 * @Description: TODO
	 * @param @param request
	 * @param @param response
	 * @param @throws IOException   
	 * @return void  
	 * @throws ServletRequestBindingException 
	 * @throws
	 * @author lihaoyang
	 * @date 2018年3月7日
	 */
	@GetMapping(SecurityConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX+"/sms")
	public void createSmsCode(HttpServletRequest request,HttpServletResponse response) throws Exception{

		//调验证码生成接口方式
		ValidateCode smsCode = smsCodeGenerator.generator(new ServletWebRequest(request));
		
		/**
		 * 不能把验证码存在session了，调接口，app和browser不同实现
		 */
//		sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_SMS, smsCode);
		
		validateCodeRepository.save(new ServletWebRequest(request) , smsCode, ValidateCodeType.SMS);
		
		//获取手机号
		String mobile = ServletRequestUtils.getRequiredStringParameter(request, "mobile");
		//发送短信验证码
		smsCodeSender.send(mobile, smsCode.getCode());
	}

}
