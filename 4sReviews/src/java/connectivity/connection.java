/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Taha
 */
public class connection {
    private String driver="com.mysql.jdbc.Driver";
   
    private String m_loggerPath;
    
    Gson gson=new Gson();
    Connection con=null;
    private String mysqlurl="jdbc:mysql:///4sreviews";
    private String mysqluser="root";
    private String mysqlpass="";
    PreparedStatement st;
    public connection()
    {
        try
        {
            Class.forName(driver);
            con=DriverManager.getConnection(mysqlurl,mysqluser,mysqlpass);
            System.out.println("connection with database SUCCESSFUL");
        }
        catch(Exception e)
        {
            System.out.println("connection with database failed e="+e);
        }
    }
    public void addVenues(String jsonData) throws ParseException
    {
        System.out.println(jsonData);
        try{
            
        JSONObject json = (JSONObject)new JSONParser().parse(jsonData);
        
        JSONObject responseobj=(JSONObject)new JSONParser().parse((json.get("response").toString()));
        
        System.out.println(json.get("response").toString());
        
            ArrayList venues=(ArrayList) responseobj.get("venues");
            //JSONArray array=(JSONArray)responseobj.get("venues");
            st=con.prepareStatement("INSERT INTO `4sreviews`.`venues` (`venue_id`, `name`, `location`, `menu`, `stats`, `categories`) VALUES (?, ?, ?, ?, ?, ?);");
            for (Object venue : venues) 
            {
                JSONObject obj2 = (JSONObject) venue;
                String venue_id=(String) obj2.get("id");
                String name= obj2.get("name").toString();
                String location= obj2.get("location").toString();
                String stats= obj2.get("stats").toString();
                String categories= obj2.get("categories").toString();
                String menu;
                try
                {
                    menu= obj2.get("menu").toString();
                    
                }
                catch(Exception e)
                {
                    //System.out.println("menu not found e="+e);
                    menu="";
                }
            
            st.setString(1,venue_id);
            st.setString(2,name);
            st.setString(3,location);
            st.setString(4,menu);
            st.setString(5,stats);
            st.setString(6,categories);
            
            st.addBatch();
            
            }
            st.executeBatch();
  
        }
        catch(ParseException | SQLException e)
        {
            System.out.println("error in addVenues e="+e);
        }
        //ud=user.authenticate(json.get("id").toString(),json.get("email").toString(),2);
        
        
        
    }

    public ArrayList getVenueIds() {
        ArrayList venue_id_list=new ArrayList();
        try {
            
            st=con.prepareStatement("Select venue_id from venues");
            ResultSet rs = st.executeQuery();
            while(rs.next())
            {
                String venue_id= rs.getString("venue_id");
                venue_id_list.add(rs.getString("venue_id"));
            }
            String venue_id_list_json=gson.toJson(venue_id_list);
          
        } 
        catch (SQLException ex) {
            Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            
        }
          return venue_id_list;
    }

    public boolean addTips(String jsonData, String venue_id) throws ParseException, SQLException 
    {
        JSONObject json = (JSONObject)new JSONParser().parse(jsonData);
        JSONObject tips=(JSONObject)new JSONParser().parse((json.get("tips").toString()));
        //System.out.println(json.get("tips"));
        ArrayList items=(ArrayList) tips.get("items");
        st=con.prepareStatement("INSERT INTO `tips` (`tip_id`, `canonicalUrl`, `likes`,`likes_content`, `text`, `user_id`, `venue_id`) VALUES (?, ?, ?, ?, ?,?, ?);");
        try
        {
         for (Object item : items) 
            {
                JSONObject obj2 = (JSONObject) item;
                String venueid=(String) obj2.get("id");
                JSONObject likes=(JSONObject)new JSONParser().parse((obj2.get("likes").toString()));
                JSONObject user=(JSONObject)new JSONParser().parse((obj2.get("user").toString()));
                System.out.println(likes.get("count"));
                String ret=addUser(user);
                st.setString(1, obj2.get("id").toString());
                st.setString(2, obj2.get("canonicalUrl").toString());
                st.setLong(3, (long) likes.get("count"));
                st.setString(4, gson.toJson(obj2.get("likes")));
                st.setString(5, obj2.get("text").toString());
                st.setString(6, ret);
                st.setString(7, venue_id);
                st.addBatch();
                
                
            }
         st.executeBatch();
         return true;
         }
        catch (ParseException | SQLException e)
        {
            System.out.println("Error=>connection=>addTips e="+e);
            return false;
        }
    }

    private String addUser(JSONObject user) throws SQLException {
        //System.out.println(user.get("id"));
        PreparedStatement ps=con.prepareStatement("Select user_id from users where user_id=?");
        ps.setString(1, user.get("id").toString());
        ResultSet rs = ps.executeQuery();
        if(rs.next())
        {
            return user.get("id").toString();
        }
        else
        {
        ps=con.prepareStatement("INSERT INTO `users` (`user_id`, `firstname`, `gender`, `photo`) VALUES (?, ?, ?, ?);");
        ps.setString(1, user.get("id").toString());
        ps.setString(2, user.get("firstName").toString());
        ps.setString(3, user.get("gender").toString());
        ps.setString(4, gson.toJson(user.get("photo")));
        ps.addBatch();
        ps.executeBatch();
        return user.get("id").toString();
        }
    }

    public void sortLikes() throws SQLException, ParseException {
        PreparedStatement ps=con.prepareStatement("Select tip_id,likes_content from tips; ");
       
        ResultSet rs = ps.executeQuery();
        ArrayList likes=new ArrayList();
        while(rs.next())
        {
            String [] arr=new String[2];
            arr[0]=rs.getString("tip_id");
            arr[1]=rs.getString("likes_content");
            likes.add(arr);
        }
        //System.out.println(likes);
        for (Object like : likes) {
            try {
                String[] arr = (String[]) like;
                JSONObject json = (JSONObject)new JSONParser().parse(arr[1]);
                //System.out.print(json.get("groups").toString());
                JSONArray groups=(JSONArray)new JSONParser().parse((json.get("groups").toString()));
                //Object [] a=(Object[]) user.get(0);
                JSONObject groups0=(JSONObject)new JSONParser().parse((groups.get(0).toString()));
                JSONArray items=(JSONArray)new JSONParser().parse((groups0.get("items").toString()));
                for (Object item : items) {
                    String ret = addUser((JSONObject) item);
                    ps=con.prepareStatement("INSERT INTO `user_likes_tips` VALUES (?, ?);");
                    ps.setString(1, ret);
                    ps.setString(2,arr[0]);
                    ps.addBatch();
                }
                ps.executeBatch();
                //System.out.print('a');
            }catch (Exception e)
            {
                System.out.println("exception e="+e);
            }
        }
    }

    public void getUserDetails() throws MalformedURLException, IOException, ParseException, SQLException {
        
                PreparedStatement ps=con.prepareStatement("Select user_id from users where lastname is null and homecity is null and complete_profile is null; ");
       
        ResultSet rs = ps.executeQuery();
       
        while(rs.next())
        {
            String url = "https://api.foursquare.com/v2/users/"+rs.getString("user_id")+"?client_id=3TFPJDCOB4NUNEX10RUZAT0EQ5OSQI1A2WZGDLVET2LRVV1I&client_secret=0Y1UEDME1QPXRQICNIHVZGZKSXOLWDMCVLJTPI5ZOXEDTB3I&v=20150317";
            String jsonresp="";
		
                
		//print result
		
                try
                {
                URL obj = new URL(url);
		HttpURLConnection htcon = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		htcon.setRequestMethod("GET");
 
		//System.out.println("\nSending 'GET' request to URL : " + url);
		//System.out.println("Response Code : " + responseCode);
 
                
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(htcon.getInputStream()))) {
                    jsonresp = in.readLine();
                    }    
                JSONObject responsejson=(JSONObject)new JSONParser().parse(jsonresp);
                JSONObject responseObj=(JSONObject)new JSONParser().parse(responsejson.get("response").toString());
                JSONObject User=(JSONObject)new JSONParser().parse(responseObj.get("user").toString());
                //System.out.println(jsonresp);
                String Lastname=(String) User.get("lastName");
                String homeCity=(String) User.get("homeCity");
                String Complete_Profile=User.toJSONString();
                st=con.prepareStatement("UPDATE `users` SET `lastname`=?, `homecity`=?, `complete_profile`=? WHERE `user_id`=?;");
                st.setString(1, Lastname);
                st.setString(2, homeCity);
                st.setString(3, Complete_Profile);
                st.setString(4, rs.getString("user_id"));
                st.execute();
                }
                catch(IOException | ParseException e)
                {
                    System.out.println("Error occured for user_id="+rs.getString("user_id")+" error="+e+" json="+jsonresp);
                    System.out.println("URL="+url);
                }
                catch( SQLException e)
                {
                    System.out.println("Sql Exception occured occured for user_id="+rs.getString("user_id")+" error="+e+" json="+jsonresp);
                    System.out.println("URL="+url);
                }
        }
        
               //st.executeBatch();
                
    }
    
}

    
