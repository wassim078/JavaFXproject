package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategoryForum;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.time.LocalDateTime;


import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "LiveCycle";
    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    // private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);
// Modifiez les SCOPES pour permettre l'écriture
    //private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR);
    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR,
            CalendarScopes.CALENDAR_EVENTS
    );

    private static Credential getCredentials() throws Exception {
        // Load credentials from the test.json file
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new InputStreamReader(GoogleCalendarService.class.getResourceAsStream("/com/example/livecycle/test.json")));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static Calendar getCalendarService() throws Exception {
        Credential credential = getCredentials();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Method to fetch Google Calendar events
    public static List<Event> getGoogleCalendarEvents(String queryFilter) throws Exception {
        Calendar service = getCalendarService();

        Events events = service.events().list("primary")
                .setTimeMin(new DateTime(System.currentTimeMillis()))
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setQ(queryFilter) // Filtrage par nom de catégorie
                .execute();

        return events.getItems();
    }
    // Ajoutez cette méthode
    public static void createEvent(Event event) throws Exception {
        Calendar service = getCalendarService();
        service.events().insert("primary", event).execute();
    }

}