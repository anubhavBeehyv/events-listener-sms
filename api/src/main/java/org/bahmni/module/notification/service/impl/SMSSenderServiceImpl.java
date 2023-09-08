package org.bahmni.module.notification.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bahmni.module.notification.properties.NotificationProperties;
import org.bahmni.module.notification.service.SMSSenderService;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.bahmni.module.notification.model.SMSRequest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class SMSSenderServiceImpl implements SMSSenderService {

    @Autowired
    private SMSTemplateService smsTemplateService;
    private final static String SMS_URI = "bahmni.sms.url";
    public static final String PATIENT_REGISTRATION_SMS_TEMPLATE = "sms.registrationSMSTemplate";
    public static final String APPOINTMENT_PROVIDER_TEMPLATE = "sms.providersTemplate";
    public static final String APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE = "sms.teleconsultationLinkTemplate";
    public static final String RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.recurringAppointmentTemplate";
    private final static String APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.appointmentBookingSMSTemplate";
    public static final String HELPDESK_TEMPLATE = "sms.helpdeskTemplate";
    private final Log logger = LogFactory.getLog(this.getClass());


    @Override
    public String getRegistrationMessage(Patient patient, Location location) {
        String template = PATIENT_REGISTRATION_SMS_TEMPLATE;
        return generatePatientMessage(patient,location, template);
    }

    public String getAppointmentBookingMessage(AppointmentDefaultResponse appointment, Location location) {
        String template = appointment.getRecurring() ? RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE : APPOINTMENT_BOOKING_SMS_TEMPLATE;
        return generateAppointmentMessage(appointment, template);
    }


    public String generateAppointmentMessage(AppointmentDefaultResponse appointment, String smsTemplate) {
        Map<String, Object> arguments;
        if (appointment.getRecurring()) {
            arguments = smsTemplateService.createArgumentsForRecurringAppointment(appointment);
        } else {
            arguments = smsTemplateService.createArgumentsMapForAppointmentBooking(null, appointment);
        }

        List<AppointmentProviderDetail> appointmentProviderList = appointment.getProviders();
        List<String> providers = new ArrayList<>();

        for (AppointmentProviderDetail appointmentProvider : appointmentProviderList) {
            providers.add(appointmentProvider.getName());
        }

        String smsTemplateMessage = smsTemplateService.templateMessage(smsTemplate, arguments);
        if (!providers.isEmpty()) {
            arguments.put("providername", org.springframework.util.StringUtils.collectionToCommaDelimitedString(providers));
            smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_PROVIDER_TEMPLATE, arguments);
        }
        if ("Virtual".equals(appointment.getAppointmentKind())) {
            smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments);
        }
        String helpdeskTemplate = smsTemplateService.templateMessage(HELPDESK_TEMPLATE, arguments);
        return smsTemplateMessage + helpdeskTemplate;
    }

    public String generatePatientMessage(Patient patient,Location location, String smsTemplate) {
        Map<String, Object> arguments = smsTemplateService.createArgumentsMapForPatientRegistration(patient, location);
        return smsTemplateService.templateMessage(smsTemplate, arguments);
    }

    @Override
    public String sendSMS(String phoneNumber, String message) {
        try {
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setPhoneNumber(phoneNumber);
            smsRequest.setMessage(message);
            ObjectMapper objMapper = new ObjectMapper();
            String jsonObject = objMapper.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);

            String smsUrl = StringUtils.isBlank(NotificationProperties.getProperty("sms.uri")) ? SMS_URI : NotificationProperties.getProperty("sms.uri");
            HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en")));
            request.addHeader("content-type", "application/json");
            String smsPropertiesPath = NotificationProperties.getProperty("sms.token.path");
            BufferedReader bufferedReader;
            try (FileReader reader = new FileReader(smsPropertiesPath)) {
                bufferedReader = new BufferedReader(reader);
                request.addHeader("Authorization", "Bearer " + bufferedReader.readLine());
            } catch (IOException e) {
                throw new RuntimeException("Error loading SMS properties file.", e);
            }
            request.setEntity(params);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            httpClient.close();
            return response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            logger.error("Exception occured in sending sms ", e);
            throw new RuntimeException("Exception occured in sending sms ", e);
        }
    }

}