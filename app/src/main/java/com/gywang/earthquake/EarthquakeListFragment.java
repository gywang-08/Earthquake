package com.gywang.earthquake;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeListFragment extends ListFragment {

    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();

    ArrayAdapter<Quake> aa;
    ArrayList<Quake>earthquakes = new ArrayList<Quake>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<Quake>(getActivity(),layoutID,earthquakes);
        setListAdapter(aa);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthquakes();
            }
        });

        t.start();
    }

    public void refreshEarthquakes(){
        URL url;
        try{
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
            int responseCode = httpURLConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = httpURLConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                earthquakes.clear();

                NodeList nl = docEle.getElementsByTagName("entry");
                if(nl != null && nl.getLength()>0){
                    for(int i = 0; i < nl.getLength();i ++){
                        Element entry = (Element)nl.item(i);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkStr = hostname + link.getAttribute("href");

                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy--MM--dd'T'hh:mm:ss'Z'");
                        Date qdata = new GregorianCalendar(0,0,0).getTime();
                        try {
                            qdata = sdf.parse(dt);
                        } catch (ParseException e) {
                            Log.d(TAG,"Date parsing exception .",e);
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeStr = details.split(" ")[1];
                        int end = magnitudeStr.length()-1;
                        double magnitude = Double.parseDouble(magnitudeStr.substring(0,end));

                        if(details.split(",").length  > 1)
                            details = details.split(",")[1].trim();
                        else {
                            details = details.split("-")[1].trim();
                        }

                        final Quake quake = new Quake(qdata,details,l,magnitude,linkStr);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                addNewQuake(quake);
                            }
                        });
                    }
                }

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        finally {

        }
    }

    private void addNewQuake(Quake quake){
        MainActivity activity = (MainActivity)getActivity();
        if(quake.getMagnitude() > activity.getMinimumMagnitude()){
            earthquakes.add(quake);
        }
        aa.notifyDataSetChanged();
    }
}
