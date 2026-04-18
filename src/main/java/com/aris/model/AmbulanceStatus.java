package com.aris.model;

public enum AmbulanceStatus {
    STANDBY,           // available for dispatch
    TRANSIT_TO_PATIENT, // going to incident/patient
    ON_SCENE,          // at incident location, with patient
    TRANSIT_TO_HOSPITAL, // transporting patient to hospital
    AT_HOSPITAL,       // patient delivered, available
    RETURNING,        // going back to base
    OFF_DUTY           // driver off duty
}
