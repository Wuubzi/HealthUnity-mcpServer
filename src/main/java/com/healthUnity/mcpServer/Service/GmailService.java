package com.healthUnity.mcpServer.Service;


import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class GmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String email;

    @Autowired
    public GmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    @Async
    public void sendConfirmAppointment(String gmail,
                                       String nombre_paciente,
                                       String nombre_doctor,
                                       String direccion_doctor,
                                       String especialidad_doctor,
                                       LocalDate fecha_cita,
                                       LocalTime hora_cita,
                                       String razon_cita,
                                       String UrlImagen){
        MimeMessage message = javaMailSender.createMimeMessage();

        String body = "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Confirmación de Cita</title>\n" +
                "    <style>\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
                "            background-color: #f5f7fa;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        \n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: #ffffff;\n" +
                "            border-radius: 16px;\n" +
                "            overflow: hidden;\n" +
                "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);\n" +
                "        }\n" +
                "        \n" +
                "        .header {\n" +
                "            background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);\n" +
                "            padding: 40px 30px;\n" +
                "            text-align: center;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        .header h1 {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: 700;\n" +
                "            margin-bottom: 8px;\n" +
                "        }\n" +
                "        \n" +
                "        .header p {\n" +
                "            font-size: 16px;\n" +
                "            opacity: 0.95;\n" +
                "        }\n" +
                "        \n" +
                "        .content {\n" +
                "            padding: 35px 30px;\n" +
                "        }\n" +
                "        \n" +
                "        .greeting {\n" +
                "            font-size: 18px;\n" +
                "            color: #1a1a1a;\n" +
                "            margin-bottom: 20px;\n" +
                "            font-weight: 500;\n" +
                "        }\n" +
                "        \n" +
                "        .appointment-card {\n" +
                "            background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);\n" +
                "            border-radius: 12px;\n" +
                "            padding: 25px;\n" +
                "            margin: 25px 0;\n" +
                "            color: white;\n" +
                "            box-shadow: 0 6px 20px rgba(74, 144, 226, 0.3);\n" +
                "        }\n" +
                "        \n" +
                "        .doctor-info {\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            margin-bottom: 20px;\n" +
                "            padding-bottom: 20px;\n" +
                "            border-bottom: 1px solid rgba(255, 255, 255, 0.3);\n" +
                "        }\n" +
                "        \n" +
                "        .doctor-photo {\n" +
                "            width: 70px;\n" +
                "            height: 70px;\n" +
                "            border-radius: 50%;\n" +
                "            background-color: white;\n" +
                "            margin-right: 15px;\n" +
                "            overflow: hidden;\n" +
                "            flex-shrink: 0;\n" +
                "            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);\n" +
                "        }\n" +
                "        \n" +
                "        .doctor-photo img {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            object-fit: cover;\n" +
                "        }\n" +
                "        \n" +
                "        .doctor-details h2 {\n" +
                "            font-size: 22px;\n" +
                "            font-weight: 700;\n" +
                "            margin-bottom: 4px;\n" +
                "        }\n" +
                "        \n" +
                "        .doctor-details p {\n" +
                "            font-size: 15px;\n" +
                "            opacity: 0.9;\n" +
                "        }\n" +
                "        \n" +
                "        .appointment-details {\n" +
                "            display: grid;\n" +
                "            gap: 15px;\n" +
                "        }\n" +
                "        \n" +
                "        .detail-item {\n" +
                "            display: flex;\n" +
                "            align-items: flex-start;\n" +
                "        }\n" +
                "        \n" +
                "        .detail-icon {\n" +
                "            font-size: 20px;\n" +
                "            margin-right: 12px;\n" +
                "            margin-top: 2px;\n" +
                "        }\n" +
                "        \n" +
                "        .detail-text strong {\n" +
                "            display: block;\n" +
                "            font-size: 13px;\n" +
                "            opacity: 0.9;\n" +
                "            margin-bottom: 3px;\n" +
                "        }\n" +
                "        \n" +
                "        .detail-text span {\n" +
                "            font-size: 16px;\n" +
                "            font-weight: 500;\n" +
                "        }\n" +
                "        \n" +
                "        .important-notice {\n" +
                "            background-color: #FFF3E0;\n" +
                "            border-left: 4px solid #FF9800;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 8px;\n" +
                "            margin: 25px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .important-notice h3 {\n" +
                "            color: #E65100;\n" +
                "            font-size: 16px;\n" +
                "            margin-bottom: 10px;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        \n" +
                "        .important-notice p {\n" +
                "            color: #5D4037;\n" +
                "            font-size: 14px;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        \n" +
                "        .tips {\n" +
                "            background-color: #F5F9FF;\n" +
                "            border-radius: 8px;\n" +
                "            padding: 20px;\n" +
                "            margin: 25px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .tips h3 {\n" +
                "            color: #4A90E2;\n" +
                "            font-size: 16px;\n" +
                "            margin-bottom: 12px;\n" +
                "        }\n" +
                "        \n" +
                "        .tips ul {\n" +
                "            list-style: none;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        \n" +
                "        .tips li {\n" +
                "            color: #424242;\n" +
                "            font-size: 14px;\n" +
                "            line-height: 1.8;\n" +
                "            padding-left: 24px;\n" +
                "            position: relative;\n" +
                "            margin-bottom: 8px;\n" +
                "        }\n" +
                "        \n" +
                "        .tips li:before {\n" +
                "            content: \"✓\";\n" +
                "            position: absolute;\n" +
                "            left: 0;\n" +
                "            color: #4A90E2;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        \n" +
                "        .footer {\n" +
                "            background-color: #f8f9fb;\n" +
                "            padding: 25px 30px;\n" +
                "            text-align: center;\n" +
                "            color: #666;\n" +
                "            font-size: 13px;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        \n" +
                "        .footer p {\n" +
                "            margin-bottom: 8px;\n" +
                "        }\n" +
                "        \n" +
                "        .button {\n" +
                "            display: inline-block;\n" +
                "            background-color: #4A90E2;\n" +
                "            color: white;\n" +
                "            padding: 14px 35px;\n" +
                "            text-decoration: none;\n" +
                "            border-radius: 8px;\n" +
                "            font-weight: 600;\n" +
                "            margin: 20px 0;\n" +
                "            font-size: 15px;\n" +
                "            transition: background-color 0.3s;\n" +
                "        }\n" +
                "        \n" +
                "        .button:hover {\n" +
                "            background-color: #357ABD;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>\uD83D\uDC4B ¡Cita Confirmada!</h1>\n" +
                "            <p>Tu cita médica ha sido agendada exitosamente</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"content\">\n" +
                "            <p class=\"greeting\">Hola <strong>Carlos</strong>,</p>\n" +
                "            <p style=\"color: #666; line-height: 1.6; margin-bottom: 20px;\">\n" +
                "                Nos complace confirmar tu cita médica. A continuación encontrarás todos los detalles importantes:\n" +
                "            </p>\n" +
                "            \n" +
                "            <div class=\"appointment-card\">\n" +
                "                <div class=\"doctor-info\">\n" +
                "                    <div class=\"doctor-photo\">\n" +
                "                        <img src=\"https://via.placeholder.com/200x200/4A90E2/FFFFFF?text=Doctor\" alt=\"Dr. Juan Perez\">\n" +
                "                    </div>\n" +
                "                    <div class=\"doctor-details\">\n" +
                "                        <h2>Dr. Juan Perez</h2>\n" +
                "                        <p>Medicina General</p>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"appointment-details\">\n" +
                "                    <div class=\"detail-item\">\n" +
                "                        <div class=\"detail-icon\">\uD83D\uDCC5</div>\n" +
                "                        <div class=\"detail-text\">\n" +
                "                            <strong>Fecha</strong>\n" +
                "                            <span>15 de Noviembre, 2025</span>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    \n" +
                "                    <div class=\"detail-item\">\n" +
                "                        <div class=\"detail-icon\">\uD83D\uDD50</div>\n" +
                "                        <div class=\"detail-text\">\n" +
                "                            <strong>Hora</strong>\n" +
                "                            <span>9:00 - 9:30 AM</span>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    \n" +
                "                    <div class=\"detail-item\">\n" +
                "                        <div class=\"detail-icon\">\uD83D\uDCCD</div>\n" +
                "                        <div class=\"detail-text\">\n" +
                "                            <strong>Dirección</strong>\n" +
                "                            <span>Calle 85 #15-32, Consultorio 301, Soledad, Atlántico</span>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    \n" +
                "                    <div class=\"detail-item\">\n" +
                "                        <div class=\"detail-icon\">\uD83D\uDCCB</div>\n" +
                "                        <div class=\"detail-text\">\n" +
                "                            <strong>Motivo de Consulta</strong>\n" +
                "                            <span>Chequeo General de Rutina</span>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"important-notice\">\n" +
                "                <h3>⏰ Importante</h3>\n" +
                "                <p>\n" +
                "                    Por favor, llega <strong>15 minutos antes</strong> de tu cita para completar el proceso de registro y admisión. \n" +
                "                    Esto nos ayudará a atenderte puntualmente y brindarte el mejor servicio.\n" +
                "                </p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"tips\">\n" +
                "                <h3>\uD83D\uDCCC Recuerda traer:</h3>\n" +
                "                <ul>\n" +
                "                    <li>Documento de identidad</li>\n" +
                "                    <li>Carnet de tu EPS o seguro médico</li>\n" +
                "                    <li>Resultados de exámenes previos (si aplica)</li>\n" +
                "                    <li>Lista de medicamentos que estés tomando actualmente</li>\n" +
                "                </ul>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div style=\"text-align: center;\">\n" +
                "                <a href=\"#\" class=\"button\">Ver Detalles de la Cita</a>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style=\"color: #666; font-size: 14px; line-height: 1.6; margin-top: 25px;\">\n" +
                "                Si necesitas cancelar o reprogramar tu cita, por favor contáctanos con al menos 24 horas de anticipación.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            <p><strong>¿Necesitas ayuda?</strong></p>\n" +
                "            <p>Contáctanos: (324) 231 3243 | info@healthunity.com</p>\n" +
                "            <p style=\"margin-top: 15px; opacity: 0.8;\">\n" +
                "                © 2025 HealthUnity. Todos los derechos reservados.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        body = body.replace("Carlos", nombre_paciente);
        body = body.replace("Dr. Juan Perez", nombre_doctor);
        body = body.replace("Medicina General", especialidad_doctor);
        body = body.replace("15 de Noviembre, 2025", fecha_cita.toString());
        body = body.replace("9:00 - 9:30 AM", hora_cita.toString());
        body = body.replace("Calle 85 #15-32, Consultorio 301, Soledad, Atlántico", direccion_doctor);
        body = body.replace("Chequeo General de Rutina", razon_cita);
        body = body.replace("https://via.placeholder.com/200x200/4A90E2/FFFFFF?text=Doctor", UrlImagen);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(email);
            helper.setTo(gmail);
            helper.setSubject("Cita Confirmada");
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
