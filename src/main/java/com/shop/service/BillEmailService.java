package com.shop.service;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service gửi email hóa đơn sau khi khách đặt hàng (thanh toán) thành công.
 * Gửi tới địa chỉ email đã đăng ký của người dùng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.bill.enabled:true}")
    private boolean billEmailEnabled;

    @Value("${app.mail.bill.from:}")
    private String billEmailFrom;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void sendOrderBill(Order order) {
        if (!billEmailEnabled) {
            log.debug("Gửi bill qua email đang tắt (app.mail.bill.enabled=false).");
            return;
        }
        if (order == null || order.getUser() == null) {
            return;
        }

        User user = order.getUser();
        String toEmail = safeTrim(user.getEmail());
        if (toEmail == null) {
            log.warn("Đơn #{} không có email người nhận — bỏ qua gửi hóa đơn.", order.getId());
            return;
        }

        String senderEmail = safeTrim(billEmailFrom);
        if (senderEmail == null) {
            senderEmail = safeTrim(springMailUsername);
        }

        if (senderEmail == null) {
            log.warn("Thiếu cấu hình email gửi (app.mail.bill.from hoặc spring.mail.username). Bỏ qua gửi bill cho đơn #{}.", order.getId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // multipart = true để gửi đồng thời bản text thuần và HTML (multipart/alternative)
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("[Clothing Shop] Hóa đơn đơn hàng #" + order.getId());

            String plain = buildPlainBill(order);
            String html = buildHtmlBill(order);
            helper.setText(plain, html);

            mailSender.send(mimeMessage);
            log.info("Đã gửi hóa đơn đơn #{} tới {}", order.getId(), toEmail);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể tạo email hóa đơn: " + e.getMessage(), e);
        }
    }

    private String buildPlainBill(Order order) {
        StringBuilder builder = new StringBuilder();
        builder.append("Xin chào ")
                .append(valueOrDefault(order.getUser().getFullName(), order.getUser().getUsername()))
                .append(",\n\n")
                .append("Cảm ơn bạn đã đặt hàng tại Clothing Shop.\n")
                .append("Thông tin hóa đơn:\n")
                .append("- Mã đơn: #").append(order.getId()).append("\n")
                .append("- Thời gian đặt: ").append(formatDate(order)).append("\n")
                .append("- Điện thoại nhận hàng: ").append(valueOrDefault(order.getPhone(), "(không có)")).append("\n")
                .append("- Địa chỉ giao hàng: ").append(valueOrDefault(order.getShippingAddress(), "(không có)")).append("\n")
                .append("- Ghi chú: ").append(valueOrDefault(order.getNote(), "(không có)")).append("\n\n")
                .append("Chi tiết sản phẩm:\n");

        int index = 1;
        for (OrderItem item : order.getOrderItems()) {
            BigDecimal subTotal = item.getSubTotal();
            builder.append(index++)
                    .append(". ")
                    .append(item.getProduct().getName())
                    .append(" | SL: ").append(item.getQuantity())
                    .append(" | Đơn giá: ").append(formatMoney(item.getPrice()))
                    .append(" | Thành tiền: ").append(formatMoney(subTotal))
                    .append("\n");
        }

        builder.append("\nTổng thanh toán: ").append(formatMoney(order.getTotalPrice())).append("\n")
                .append("Trạng thái đơn: ").append(order.getStatus().getDisplayName()).append("\n\n")
                .append("Clothing Shop sẽ liên hệ và giao hàng sớm nhất có thể.\n")
                .append("Trân trọng,\n")
                .append("Clothing Shop");

        return builder.toString();
    }

    private String buildHtmlBill(Order order) {
        String name = escapeHtml(valueOrDefault(order.getUser().getFullName(), order.getUser().getUsername()));
        StringBuilder rows = new StringBuilder();
        int index = 1;
        for (OrderItem item : order.getOrderItems()) {
            rows.append("<tr>")
                    .append("<td>").append(index++).append("</td>")
                    .append("<td>").append(escapeHtml(item.getProduct().getName())).append("</td>")
                    .append("<td style=\"text-align:center\">").append(item.getQuantity()).append("</td>")
                    .append("<td style=\"text-align:right\">").append(escapeHtml(formatMoney(item.getPrice()))).append("</td>")
                    .append("<td style=\"text-align:right\">").append(escapeHtml(formatMoney(item.getSubTotal()))).append("</td>")
                    .append("</tr>");
        }

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Segoe UI,Roboto,Arial,sans-serif;line-height:1.5;color:#222">
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Cảm ơn bạn đã đặt hàng tại <strong>Clothing Shop</strong>.</p>
                <h3 style="margin-bottom:8px">Thông tin đơn hàng #%s</h3>
                <table style="border-collapse:collapse;margin-bottom:16px">
                  <tr><td style="padding:2px 12px 2px 0">Thời gian đặt</td><td>%s</td></tr>
                  <tr><td style="padding:2px 12px 2px 0">Điện thoại</td><td>%s</td></tr>
                  <tr><td style="padding:2px 12px 2px 0">Địa chỉ giao hàng</td><td>%s</td></tr>
                  <tr><td style="padding:2px 12px 2px 0">Ghi chú</td><td>%s</td></tr>
                  <tr><td style="padding:2px 12px 2px 0">Trạng thái</td><td>%s</td></tr>
                </table>
                <table border="1" cellpadding="8" cellspacing="0" style="border-collapse:collapse;width:100%%;max-width:640px">
                  <thead style="background:#f5f5f5">
                    <tr><th>#</th><th>Sản phẩm</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>
                  </thead>
                  <tbody>%s</tbody>
                  <tfoot>
                    <tr><th colspan="4" style="text-align:right">Tổng thanh toán</th><th style="text-align:right">%s</th></tr>
                  </tfoot>
                </table>
                <p style="margin-top:16px">Clothing Shop sẽ liên hệ và giao hàng sớm nhất có thể.</p>
                <p>Trân trọng,<br/>Clothing Shop</p>
                </body>
                </html>
                """.formatted(
                name,
                order.getId(),
                escapeHtml(formatDate(order)),
                escapeHtml(valueOrDefault(order.getPhone(), "—")),
                escapeHtml(valueOrDefault(order.getShippingAddress(), "—")),
                escapeHtml(valueOrDefault(order.getNote(), "—")),
                escapeHtml(order.getStatus().getDisplayName()),
                rows.toString(),
                escapeHtml(formatMoney(order.getTotalPrice())));
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(value);
    }

    private String formatDate(Order order) {
        if (order.getCreatedAt() == null) {
            return "(không rõ)";
        }
        return order.getCreatedAt().format(DATE_TIME_FORMATTER);
    }

    private String valueOrDefault(String value, String defaultValue) {
        String trimmed = safeTrim(value);
        return trimmed != null ? trimmed : defaultValue;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
