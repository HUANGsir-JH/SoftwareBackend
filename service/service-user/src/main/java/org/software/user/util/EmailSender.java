package org.software.user.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件注入发件人邮箱
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 发送纯文本邮件。
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    @Async // 异步执行此方法，避免阻塞调用者
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // 从配置中获取发件人
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);

            log.info("验证码成功发到: " + to);
        } catch (MailException e) {

            log.error("验证码没能发到: " + to + ".错误信息: " + e.getMessage());
        }
    }

    /**
     * 发送验证码邮件的专用方法(HTML格式)
     * @param to 收件人邮箱
     * @param verificationCode 验证码
     */
    @Async
    public void sendVerificationCodeEmail(String to, String verificationCode) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("验证码 - 请查收");
            
            // 构建美观的HTML邮件内容
            String htmlContent = buildVerificationCodeHtml(verificationCode);
            helper.setText(htmlContent, true); // true表示发送HTML格式
            
            mailSender.send(mimeMessage);
            log.info("HTML验证码邮件成功发送到: " + to);
        } catch (MessagingException e) {
            log.error("HTML验证码邮件发送失败到: " + to + ". 错误信息: " + e.getMessage());
        } catch (MailException e) {
            log.error("邮件发送失败到: " + to + ". 错误信息: " + e.getMessage());
        }
    }

    /**
     * 构建验证码的HTML模板
     * @param verificationCode 验证码
     * @return HTML内容
     */
    private String buildVerificationCodeHtml(String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>验证码</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; background-color: #f5f5f5; font-family: Arial, \"Microsoft YaHei\", sans-serif;'>" +
                "    <div style='max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden;'>" +
                "        <!-- 头部 -->" +
                "        <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px; text-align: center;'>" +
                "            <h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>验证码确认</h1>" +
                "            <p style='margin: 10px 0 0 0; color: #f0f0f0; font-size: 14px;'>Verification Code</p>" +
                "        </div>" +
                "        " +
                "        <!-- 主体内容 -->" +
                "        <div style='padding: 40px 30px;'>" +
                "            <p style='margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;'>" +
                "                您好," +
                "            </p>" +
                "            <p style='margin: 0 0 30px 0; color: #666666; font-size: 14px; line-height: 1.8;'>" +
                "                您正在进行身份验证操作,请使用以下验证码完成验证:" +
                "            </p>" +
                "            " +
                "            <!-- 验证码显示区域 -->" +
                "            <div style='background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; padding: 30px; text-align: center; margin: 0 0 30px 0;'>" +
                "                <div style='background-color: #ffffff; display: inline-block; padding: 15px 40px; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>" +
                "                    <span style='font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: \"Courier New\", monospace;'>" +
                verificationCode +
                "                    </span>" +
                "                </div>" +
                "            </div>" +
                "            " +
                "            <!-- 提示信息 -->" +
                "            <div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px 20px; margin: 0 0 20px 0; border-radius: 4px;'>" +
                "                <p style='margin: 0; color: #856404; font-size: 14px; line-height: 1.6;'>" +
                "                    ⏰ <strong>有效期:</strong> 该验证码将在 <strong>5分钟</strong> 内有效" +
                "                </p>" +
                "            </div>" +
                "            " +
                "            <div style='background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px 20px; margin: 0 0 30px 0; border-radius: 4px;'>" +
                "                <p style='margin: 0; color: #721c24; font-size: 14px; line-height: 1.6;'>" +
                "                    🔒 <strong>安全提示:</strong> 请勿将验证码泄露给他人" +
                "                </p>" +
                "            </div>" +
                "            " +
                "            <p style='margin: 0; color: #999999; font-size: 13px; line-height: 1.6;'>" +
                "                如果这不是您本人的操作,请忽略此邮件,您的账户仍然是安全的。" +
                "            </p>" +
                "        </div>" +
                "        " +
                "        <!-- 底部 -->" +
                "        <div style='background-color: #f8f9fa; padding: 25px 30px; border-top: 1px solid #e9ecef;'>" +
                "            <p style='margin: 0 0 10px 0; color: #6c757d; font-size: 12px; text-align: center; line-height: 1.6;'>" +
                "                这是一封系统自动发送的邮件,请勿直接回复" +
                "            </p>" +
                "            <p style='margin: 0; color: #adb5bd; font-size: 12px; text-align: center;'>" +
                "                © 2025 Your Company. All rights reserved." +
                "            </p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}