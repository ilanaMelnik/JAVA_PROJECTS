import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ContentSpreader{
    private String title    = "";
    public void   setTitle(String data){ this.title = data; }
    public String getTitle(){ return this.title; }

    private String tags     = "";
    public void   setTags(String tags){ this.tags = tags; }
    public String getTags(){ return this.tags; }

    private String language = "";
    public void   setLanguage(String language){ this.language = language; }
    public String getLanguage(){ return this.language; }

    private int numOfThreads = 0;
    public void setNumOfThreads(int coresAmount){ this.numOfThreads = coresAmount; }
    public int  getNumOfThreads(){ return(this.numOfThreads); }

    private String basefileName;
    public void setBaseFileName(String basefileName){
        this.basefileName = basefileName;
    }
    public String  getBaseFileName(){ return(this.basefileName); }


    public ContentSpreader(String baseFileName) {

        setNumOfThreads(getCores());
        setBaseFileName(baseFileName);
    }

    public int getCores() {

        int numOfCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of Cores on current machine is: " + numOfCores);
        return numOfCores;
    }

    // prepare tasks to be done by workers - each line treated by separate worker
    // implemented a pool thread. Amount of threads that will do the work is basing on amount of CPU cores
    // 1. create Thread pool
    // 2. Run on results array, per result (consider it as incoming request that need to be processed) create and run worker,
    // add the worker to the execution queue. executor will find available worker thread and give the task to it for processing it.
    public void filesSpreader(JSONArray allSearchResultLinesArray) {

        System.out.println("Prepare separate json files for all search results");

        ExecutorService executorPool = Executors.newFixedThreadPool(getNumOfThreads());

        int lineIndex = 0;

        for (Object line : allSearchResultLinesArray) {

            String title    = (String) ((JSONObject) line).get("title");
            String tags     = (String) ((JSONObject) line).get("tags") != "" ? (String) ((JSONObject) line).get("tags") : null;
            String language = (String) ((JSONObject) line).get("language");

            Runnable workerUnit = new WorkerThread(title,
                                                    tags,
                                                    language,
                                                    lineIndex++,
                                                    getBaseFileName());
            executorPool.execute(workerUnit);
        }
        executorPool.shutdown(); //executor pool will shut down when all tasks will be processed

        while (!executorPool.isTerminated()) {} //wait till all threads finish processing their runnable objects (tasks)

        System.out.println("Finished, all lines written into dedicated json files");
    }
}


// runnable object is a tasks / request, will be executed as thread, work to be done:
// 1. create file with name format: baseName-OrderOfResult-timestamp
// 2. create message of format: title, tags, language
class WorkerThread implements Runnable {

    String title        = "";
    String tags         = "";
    String language     = "";
    int    lineIndex    = 0;
    String baseFileName = "";

    public WorkerThread(String title,
                        String tags,
                        String language,
                        int    lineIndex,     //needed for file name
                        String baseFileName){ //needed for file name

        this.title        = title;
        this.tags         = tags;
        this.language     = language;
        this.lineIndex    = lineIndex;
        this.baseFileName = baseFileName;
    }


    private String getCurrentTime(){

        Date date = new Date();
        long milisecs = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH_mm_ss.SSSS");

        return formatter.format(milisecs);
    }

    private String buildFileName(){

        return baseFileName + "-" + lineIndex + "-" + getCurrentTime() + ".json";
    }

    //actual work to be done (request/task)
    //1. create json file
    //2. create JSON obj, with input data and write it to the file
   public void run() {

       JSONObject singleResult = new JSONObject();
       singleResult.put("title",    title);
       singleResult.put("tags",     tags);
       singleResult.put("language", language);

       File file = new File(buildFileName());
       PrintWriter my_writer = null;

       try {
           if (!file.exists()) {
               file.createNewFile();
               System.out.println("New json file created: " + file.getName());
           }

           my_writer = new PrintWriter(new FileWriter(file));
           my_writer.printf("%s\r\n", singleResult.toJSONString());

       } catch (IOException e) {
           e.printStackTrace();
           System.out.println("An error occurred during creation of file or writing single search result into file");
       } finally {
           try {
               my_writer.flush();
               my_writer.close();
           }catch (Exception e){
               e.printStackTrace();
           }
       }
    }
}
