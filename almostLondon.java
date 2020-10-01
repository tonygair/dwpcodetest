/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dwp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tony
 */
@WebServlet(name = "almostLondon", urlPatterns = {"/almostLondon"})
public class almostLondon extends HttpServlet {

    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public static HttpResponse<String> getAllThePeople() throws IOException, InterruptedException {

        String uriString = "https://bpdts-test-app-v3.herokuapp.com/users";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uriString))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());
        return response;
    }

    private boolean checkDistanceLessThan(Double latitudeOne, Double latitudeTwo,
            Double longitudeOne, Double longitudeTwo, Double totalDistance) {

        /*Radius of Earth in Miles*/
        Double earthRadiusMiles = 3958.8;
        /*using the havferine formula - must remember radians*/
        Double latitudeDistance = Math.toRadians(latitudeTwo - latitudeOne);
        Double longitudeDistance = Math.toRadians(longitudeTwo - longitudeOne);
        Double sideOne = Math.pow(Math.sin(latitudeDistance / 2.0), 2)
                + (Math.cos(Math.toRadians(latitudeOne) * Math.cos(Math.toRadians(latitudeTwo))
                        * Math.pow(Math.sin(longitudeDistance / 2.0), 2)));
        Double curvature = 2 * Math.asin(Math.sqrt(sideOne));
        /* return boolean curvature * Radius of Earth */
        return (totalDistance <= (curvature * earthRadiusMiles));

    }

    private ArrayList<personClass> siftPeople(ArrayList<personClass> inList) {

        Double londonLatitude = 51.5074;
        Double londonLongitude = 0.1278;
        /*allow for london being 10 miles across so distance = 60 +5 */
        Double theDistance = 65.0;

        ArrayList<personClass> outputList = new ArrayList();
        for (personClass P : inList) {
            if (checkDistanceLessThan(londonLatitude, P.latitude,
                    londonLongitude, P.longitude,
                    theDistance)) {
                outputList.add(P);
            }

        }

        return outputList;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, InterruptedException {
        HttpResponse<String> dwpResponse = getAllThePeople();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<personClass>>() {
        }.getType();
        ArrayList<personClass> contactList = gson.fromJson(dwpResponse.body(), type);
        ArrayList<personClass> siftedContactList = siftPeople(contactList);

        /* convert to json string and send back as a response */
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson outGson = gsonBuilder.create();

        String JSONObject = outGson.toJson(siftedContactList);

        response.setContentType("application/json");
        response.getWriter().write(JSONObject);
        response.getWriter().close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (InterruptedException ex) {
            Logger.getLogger(almostLondon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (InterruptedException ex) {
            Logger.getLogger(almostLondon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
