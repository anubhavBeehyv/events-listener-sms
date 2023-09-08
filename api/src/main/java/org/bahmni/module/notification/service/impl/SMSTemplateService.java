package org.bahmni.module.notification.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class SMSTemplateService {

    AppointmentVisitLocationSMS appointmentVisitLocation;

    @Autowired
    public SMSTemplateService(AppointmentVisitLocationSMS appointmentVisitLocation) {
        this.appointmentVisitLocation = appointmentVisitLocation;
    }

    public Map<String, Object> createArgumentsMapForPatientRegistration(Patient patient, Location location) {
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicTime = Context.getAdministrationService().getGlobalPropertyObject("clinic.clinicTimings").getPropertyValue();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("location", location.getName());
        arguments.put("identifier", patient.getPatientIdentifier().getIdentifier());
        arguments.put("patientname", patient.getGivenName() + " " + patient.getFamilyName());
        arguments.put("gender", patient.getGender());
        arguments.put("age", patient.getAge().toString());
        arguments.put("helpdesknumber", helpdeskNumber);
        arguments.put("facilitytimings", clinicTime);
        return arguments;
    }


    public Map<String, Object> createArgumentsForRecurringAppointment(AppointmentDefaultResponse appointment) {
        Map<String, Object> arguments = createArgumentsMapForAppointmentBooking(null,appointment);
//        arguments.put("recurringperiod",appointment.getAppointmentRecurringPattern().getPeriod());
//        arguments.put("recurringtype",appointment.getAppointmentRecurringPattern().getType());
//        arguments.put("recurringdays",appointment.getAppointmentRecurringPattern().getDaysOfWeek());
//        arguments.put("recurringfrequency",appointment.getAppointmentRecurringPattern().getFrequency());

        return arguments;
    }

    public Map<String, Object> createArgumentsMapForAppointmentBooking(Location locationa, AppointmentDefaultResponse appointment) {

        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicTime = Context.getAdministrationService().getGlobalPropertyObject("clinic.clinicTimings").getPropertyValue();
        Map<String, Object> arguments = new HashMap<>();
        String smsTimeZone = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("bahmni.sms.timezone"), null, new Locale("en"));
        String smsDateFormat = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("bahmni.sms.dateformat"), null, new Locale("en"));
        String date = DateUtil.convertUTCToGivenFormat(appointment.getStartDateTime(), smsDateFormat, smsTimeZone);
        Location location=Context.getLocationService().getLocationByUuid((String)appointment.getLocation().get("uuid"));
        String facilityName = getFacilityName(location);
        arguments.put("facilityname", facilityName);
        arguments.put("identifier", appointment.getPatient().get("PatientIdentifier"));
        arguments.put("patientname", appointment.getPatient().get("name"));
        arguments.put("gender", appointment.getPatient().get("gender"));
        arguments.put("helpdesknumber", helpdeskNumber);
        arguments.put("appointmentdate",date);
        arguments.put("teleconsultationlink",appointment.getTeleconsultationLink());
        arguments.put("service",appointment.getService().getName());
        arguments.put("clinicTime",clinicTime);
        return arguments;
    }
    public String getFacilityName(Location location) {
        LocationTag visitLocationTag = Context.getLocationService().getLocationTagByName("Visit Location");
        List<Location> locations = Context.getLocationService().getLocationsHavingAnyTag(Collections.singletonList(visitLocationTag));
        String facilityName = (visitLocationTag != null && !locations.isEmpty()) ? locations.get(0).getName() : "xxxxx";

        if (location != null) {
            String facilityNameFromVisitLocation = appointmentVisitLocation.getFacilityName(location.getUuid());

            if (StringUtils.isNotEmpty(facilityNameFromVisitLocation)) {
                facilityName = facilityNameFromVisitLocation;
            }
        }
        return facilityName;
    }


    public String templateMessage(String smsTemplate, Map<String, Object> arguments) {
        String template = Context.getAdministrationService().getGlobalProperty(smsTemplate);
        String formattedMessage = StringUtils.isBlank(template) ? Context.getMessageSourceService().getMessage(smsTemplate, null, new Locale("en")) : template;

        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(formattedMessage);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String modifiedPlaceholder = placeholder.toLowerCase().replaceAll("\\s", "");
            Object value = arguments.get(modifiedPlaceholder);
            placeholder = String.format("{%s}", placeholder);
            formattedMessage = formattedMessage.replace(placeholder, String.valueOf(value));
        }

        return formattedMessage.replace("\\n", System.lineSeparator());
    }

}
