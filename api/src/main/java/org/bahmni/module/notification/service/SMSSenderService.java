package org.bahmni.module.notification.service;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;


public interface SMSSenderService {
    String getRegistrationMessage(Patient patient, Location location);
    String getAppointmentBookingMessage(AppointmentDefaultResponse appointment, Location location);
    String sendSMS(String phoneNumber, String message);
}
