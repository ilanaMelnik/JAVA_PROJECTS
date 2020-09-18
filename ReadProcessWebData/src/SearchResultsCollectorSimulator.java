import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class SearchResultsCollectorSimulator {

    private String testFileName     = "SecurityResultGitHub";
    private String getTestFileName() {return testFileName;  }

    private String fullTestFileName = "";
    private void setFullTestFileName(String fullTestFileName) { this.fullTestFileName = fullTestFileName; }
    private String getFullTestFileName() {return fullTestFileName;  }


    public void resetTestFile() {

        File file = new File(getFullTestFileName());
        if (file.exists()) {
            file.delete();
            System.out.println("File deleted: " + file.getName());
        }
    }

    private String getCurrentTime(){

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss.SSSS");
        return formatter.format(date);
    }

    public SearchResultsCollectorSimulator() {

        String fullTestFileName = getTestFileName() + "-" + getCurrentTime() + ".json";
        setFullTestFileName(fullTestFileName);

        resetTestFile();
    }

    public void storeAllSearchResultsInFile(JSONArray allResultsAllPages) throws IOException {

        File file = new File(getFullTestFileName());
        PrintWriter printWriter = null;
        JSONObject searchResult;

        try {
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("File created: " + file.getName() + " will hold all search results");
            }

            printWriter = new PrintWriter(new FileWriter(file, true));

            for (int i = 0; i < allResultsAllPages.size() ; i++) {
                searchResult = (JSONObject) allResultsAllPages.get(i);
                printWriter.printf("%s\r\n", (searchResult).toJSONString());
                printWriter.flush();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing search results to file");
            e.printStackTrace();
        } finally {
            try {
                printWriter.close();
            }catch (Exception e){
                System.out.println("An error occurred while closing search results file");
                e.printStackTrace();
            }
        }
    }
	
	public JSONObject simulateResult(int fileIndex) {

        JSONObject result = new JSONObject();

        result.put("title",       fileIndex + "\\Security");
        result.put("Description", "[Archived] Middleware for security and authorization of web apps.");
        result.put("tags",        "aspnet-product");
        result.put("stars",       "1.2k");
        result.put("language",    "C#");
        result.put("time",        getCurrentTime());

        return result;
    }
	
    public void prepareTestInputFile() throws Exception {

        JSONObject searchResultJson;
        JSONArray allResultsAllPages = new JSONArray();

        //simulate search results - as if they were read from web page :-)
        for (int i = 0; i < 50; i++) {
            if ((searchResultJson = simulateResult(i)) != null) {
                allResultsAllPages.add(searchResultJson);
            }
        }

        if(allResultsAllPages == null || allResultsAllPages.size() == 0) { throw new Exception("Failed to collect results"); }
        storeAllSearchResultsInFile(allResultsAllPages);

        System.out.println("Input File with jason records is ready !!");
    }

    // open json file with all search results and read all lines into JSONArray
    // pass JSONArray to the further processing
    public void fileContentProcess(String fullTestFileName) throws Exception {

        File file = new File(fullTestFileName);
        if (!file.exists()) {
            String errorMessage = "Input File: " + fullTestFileName + ", does not exist!! ";
            System.out.println(errorMessage);
            throw new FileNotFoundException(errorMessage);
        }

        //read file line by line. each line read into json object
        FileReader fileReader = new FileReader(fullTestFileName);
        BufferedReader br = new BufferedReader(fileReader);

        JSONArray allSearchResultLinesArray = new JSONArray();
        String singleLine  = "";

        while( (singleLine = br.readLine()) != null ) {
            JSONObject lineInJsonFormat = new JSONObject();
            String[] lineParts = singleLine.replaceAll("[{}]","") .split(",");

            for(String linePart : lineParts){
                lineInJsonFormat.put(linePart.split(":")[0].replaceAll("\"",""),  //key
                                     linePart.split(":")[1].replaceAll("\"","")); //val
            }
            allSearchResultLinesArray.add(lineInJsonFormat);
        }

        if(allSearchResultLinesArray == null || allSearchResultLinesArray.size() == 0) throw new Exception("Failed to read from File - the jason structures");

        ContentSpreader fileContentManage = new ContentSpreader(getTestFileName());
        fileContentManage.filesSpreader(allSearchResultLinesArray);
    }


    // process is built from two steps:
    // 1. Collect data from site (or simulate it as it was from site)
    // 2. Spread data between files
    public static void main(String[] args) throws Exception {

        try {

            SearchResultsCollectorSimulator collector = new SearchResultsCollectorSimulator();
            collector.prepareTestInputFile();

            collector.fileContentProcess(collector.getFullTestFileName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}