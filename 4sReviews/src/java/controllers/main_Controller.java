/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import com.google.gson.Gson;
import connectivity.connection;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
 
import javax.net.ssl.HttpsURLConnection;
/**
 *
 * @author Taha
 */
@WebServlet(name = "main_Controller", urlPatterns = {"/main_Controller"})
public class main_Controller extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
  

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
            throws ServletException, IOException, MalformedURLException {
       // processRequest(request, response);
        PrintWriter out = response.getWriter();
        System.out.println("Get try");
        connection conn=new connection();
        int fn=Integer.parseInt(request.getParameter("fn"));
        String jsonString="";
        Gson gson=new Gson();
        switch(fn)
        {
            case 1: {
                        jsonString=gson.toJson(conn.getVenueIds(), ArrayList.class);
                        
                    }break;
            case 2:{
            try {
                conn.sortLikes();
            } catch (SQLException | ParseException ex) {
                Logger.getLogger(main_Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            }break;
            case 3:{
            try {
                conn.getUserDetails();
            } catch (ParseException | SQLException ex) {
                System.out.println("controller exception"+ex);
                Logger.getLogger(main_Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            }break;
        }
       out.println(jsonString);
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
       // processRequest(request, response);
        System.out.println("Post try");
        connection conn=new connection();
        int fn=Integer.parseInt(request.getParameter("fn"));
        String jsonData=request.getParameter("jsonData");
        //System.out.println("jsondata from POST controller="+jsonData);
        try {
            switch(fn)
            {
                case 1:{conn.addVenues(jsonData);}break;
                case 2:{
                        String venue_id=request.getParameter("venue_id");
                        boolean retur=conn.addTips(jsonData,venue_id);
                        out.println(retur);
                        }break;
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(main_Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(main_Controller.class.getName()).log(Level.SEVERE, null, ex);
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
