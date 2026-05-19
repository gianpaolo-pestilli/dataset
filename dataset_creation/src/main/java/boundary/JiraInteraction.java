//package boundary;
//
//import java.io.*;
//import java.net.URL;
//import java.nio.charset.Charset;
//
//import exception.ConfigException;
//import exception.JiraException;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import settings.PropertiesSetter;
//
//public class JiraInteraction {
//
//    private static String readAll(Reader rd) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        int cp;
//        while ((cp = rd.read()) != -1) {
//            sb.append((char) cp);
//        }
//        return sb.toString();
//    }
//
//    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
//        InputStream is = new URL(url).openStream();
//        try {
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            JSONArray json = new JSONArray(jsonText);
//            return json;
//        } finally {
//                is.close();
//            }
//    }
//
//
//    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
//        InputStream is = new URL(url).openStream();
//            try {
//                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//                String jsonText = readAll(rd);
//                JSONObject json = new JSONObject(jsonText);
//                return json;
//            } finally {
//                is.close();
//            }
//    }
//
//
//    public static void printTicket() throws IOException, JSONException, JiraException {
//        String projName = null;
//        try {
//            projName = PropertiesSetter.getProjectName();
//        } catch (ConfigException e){
//            throw new JiraException(e.getMessage());
//        }
//
//        Integer j = 0, i = 0, total = 1;
//            //Get JSON API for closed bugs w/ AV in the project
//            do {
//                //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
//                j = i + 1000;
//                String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
//                        + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
//                        + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
//                        + i.toString() + "&maxResults=" + j.toString();
//                JSONObject json = readJsonFromUrl(url);
//                JSONArray issues = json.getJSONArray("issues");
//                total = json.getInt("total");
//                for (; i < total && i < j; i++) {
//                    //Iterate through each bug
//                    String key = issues.getJSONObject(i%1000).get("key").toString();
//
//                    // Still understanding where to store this information
//                    System.out.println(key);
//                }
//            } while (i < total);
//        }
//
//
//
//        import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.*;
//import java.net.URL;
//import java.nio.charset.Charset;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//
//
//
//        public static HashMap<LocalDateTime, String> releaseNames;
//        public static HashMap<LocalDateTime, String> releaseID;
//        public static ArrayList<LocalDateTime> releases;
//        public static Integer numVersions;
//
//        public static void main(String[] args) throws IOException, JSONException {
//
//            String projName ="QPID";
//            //Fills the arraylist with releases dates and orders them
//            //Ignores releases with missing dates
//            releases = new ArrayList<LocalDateTime>();
//            Integer i;
//            String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
//            JSONObject json = readJsonFromUrl(url);
//            JSONArray versions = json.getJSONArray("versions");
//            releaseNames = new HashMap<LocalDateTime, String>();
//            releaseID = new HashMap<LocalDateTime, String> ();
//            for (i = 0; i < versions.length(); i++ ) {
//                String name = "";
//                String id = "";
//                if(versions.getJSONObject(i).has("releaseDate")) {
//                    if (versions.getJSONObject(i).has("name"))
//                        name = versions.getJSONObject(i).get("name").toString();
//                    if (versions.getJSONObject(i).has("id"))
//                        id = versions.getJSONObject(i).get("id").toString();
//                    addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
//                            name,id);
//                }
//            }
//            // order releases by date
//            Collections.sort(releases, new Comparator<LocalDateTime>(){
//                //@Override
//                public int compare(LocalDateTime o1, LocalDateTime o2) {
//                    return o1.compareTo(o2);
//                }
//            });
//            if (releases.size() < 6)
//                return;
//            FileWriter fileWriter = null;
//            try {
//                fileWriter = null;
//                String outname = projName + "VersionInfo.csv";
//                //Name of CSV for output
//                fileWriter = new FileWriter(outname);
//                fileWriter.append("Index,Version ID,Version Name,Date");
//                fileWriter.append("\n");
//                numVersions = releases.size();
//                for ( i = 0; i < releases.size(); i++) {
//                    Integer index = i + 1;
//                    fileWriter.append(index.toString());
//                    fileWriter.append(",");
//                    fileWriter.append(releaseID.get(releases.get(i)));
//                    fileWriter.append(",");
//                    fileWriter.append(releaseNames.get(releases.get(i)));
//                    fileWriter.append(",");
//                    fileWriter.append(releases.get(i).toString());
//                    fileWriter.append("\n");
//                }
//
//            } catch (Exception e) {
//                System.out.println("Error in csv writer");
//                e.printStackTrace();
//            } finally {
//                try {
//                    fileWriter.flush();
//                    fileWriter.close();
//                } catch (IOException e) {
//                    System.out.println("Error while flushing/closing fileWriter !!!");
//                    e.printStackTrace();
//                }
//            }
//            return;
//        }
//
//
//        public static void addRelease(String strDate, String name, String id) {
//            LocalDate date = LocalDate.parse(strDate);
//            LocalDateTime dateTime = date.atStartOfDay();
//            if (!releases.contains(dateTime))
//                releases.add(dateTime);
//            releaseNames.put(dateTime, name);
//            releaseID.put(dateTime, id);
//            return;
//        }
//
//
//        public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
//            InputStream is = new URL(url).openStream();
//            try {
//                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//                String jsonText = readAll(rd);
//                JSONObject json = new JSONObject(jsonText);
//                return json;
//            } finally {
//                is.close();
//            }
//        }
//
//        private static String readAll(Reader rd) throws IOException {
//            StringBuilder sb = new StringBuilder();
//            int cp;
//            while ((cp = rd.read()) != -1) {
//                sb.append((char) cp);
//            }
//            return sb.toString();
//        }
//
//
//    }
//}
