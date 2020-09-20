import java.io.*;
import java.security.InvalidParameterException;
import java.util.InputMismatchException;
import java.util.concurrent.TimeoutException;

import static java.lang.Integer.parseInt;


public class Registration {

    private String token = "";
    private String AuthentifiedToken = ""; //will be set by Authentication Thread !!
    private String filePath = "";
    public Printer printer;
    private boolean wasSignalled = false;

    // volatile means read from computer main memory and not from CPU cash.
    // It is specially important in multi thread program that use shared objects, where each thread can run on different
    // CPU and can copy the variables into its cash.
    private volatile MonitorObject myMonitorObject;


    private String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAuthentifiedToken() {
        return AuthentifiedToken;
    }

    public void setAuthentifiedToken(String authentifiedToken) {
        AuthentifiedToken = authentifiedToken;
    }

    private String getFilePath() {
        return filePath;
    }

    public Registration() {
        this.token = "tytye456er94k";
        this.filePath = System.getProperty("user.dir")+"\\Authentication.txt";
        this.myMonitorObject = new MonitorObject();
        this.printer = new Printer(Printer.WhoIAmType.REG);
    }

    // create file, write at the first line the Token number
    private void createFileWithToken() {

        PrintWriter printWriter = null;
        File file = new File(getFilePath());

        printer.print("Preparation of Authentication Token started ...");
        try {
            if (!file.exists()) {
                file.createNewFile();
                printer.print("File created: " + file.getName());
            }

            printWriter = new PrintWriter(new FileWriter(file));
            printWriter.printf("%s\r\n", (getToken()));
            printWriter.flush();

        } catch (IOException e) {
            printer.print("An error occurred while writing token: " + getToken() + " to file: " + getFilePath());
            e.printStackTrace();
        } finally {
            try {
                printWriter.close();
                printer.print("File with Token is ready !");
            }catch (Exception e){
                printer.print("An error occurred while closing file: " + getFilePath());
                e.printStackTrace();
            }
        }

    }

    private void analyseResults(long actualAuthTimeSec,
                                long expectedAuthenticationTimeSec,
                                String receivedToken, String origToken) throws Exception {
        try {
            if (actualAuthTimeSec > expectedAuthenticationTimeSec) {
                printer.print("Actual authentication time (" + actualAuthTimeSec + " Sec) " +
                        "> than expected (" + expectedAuthenticationTimeSec + " Sec)");
                throw new TimeoutException(printer.getWhoI() + ":" + " " + "Exception Exceeded Authentication time !");
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new Exception();
        }

        try {
            if (!receivedToken.equals(origToken)) {
                printer.print("Received invalid token !");
                throw new InvalidParameterException(printer.getWhoI() + ":" + " " + "Exception Received invalid token !");
            }
        }catch (InvalidParameterException e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 1. get + check username and password
    // 2. run Authentication thread that WAITS for file with token. (When Token is ready it validates him and NOTIFIES upon success)
    // 3. prepares file with Token and NOTIFIES the Authentication thread
    public void first_registration( String username,
                                    String password,
                                    int expectedAuthenticationTimeSec,
                                    int authenticationDelayTimeSecs) throws Exception {

        InputManagerer inputManager = new InputManagerer(printer.getWhoI());


        try {
            inputManager.checkCredentials(username, password);

            printer.print("User credentials are correct: \n" +
                          "username: " + inputManager.getUsername() + "\n" +
                          "password: " + inputManager.getPassword());

            printer.print("Expected authentication time: " + expectedAuthenticationTimeSec + " Sec, " +
                          "Authentication delay time: " + authenticationDelayTimeSecs + " Sec");

            Thread authentication = new AuthenticationTokenWaiter(getToken(),
                                                                  getFilePath(),
                                                                  authenticationDelayTimeSecs);
            printer.print("Starting Authentication thread...");

            long startTimeMillis = System.currentTimeMillis();
            authentication.start();

            createFileWithToken();

            synchronized(myMonitorObject){
                try {
                    wasSignalled = true;
                    myMonitorObject.notify();

                    printer.print("Waiting till token is verified ...");
                    myMonitorObject.wait();
                    wasSignalled = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

            printer.print("Waiting Authentication to finish ...");
            authentication.join(expectedAuthenticationTimeSec);

            long actualAuthTimeSec = (System.currentTimeMillis()-startTimeMillis)/1000;

            analyseResults(actualAuthTimeSec,
                           expectedAuthenticationTimeSec,
                           getAuthentifiedToken(), getToken());

            printer.print("Authentication time & token are OK");

        } catch (Exception e) {
            throw new Exception("Exception during Registration/Authentication process");
        }
    }


    public class MonitorObject{}


    // class responsible for: receive + check user's input
    // inner class - nobody else outside the MainClass can see it and use it. Main class defines a class to check input
    private class InputManagerer  {
        private String username = "";
        private String password = "";
        private Printer printer;

        private String getUsername() {return this.username; }

        private void setUsername(String username) {this.username = username; }

        private String getPassword() {return this.password; }

        private void setPassword(String password) {this.password = password; }

        public InputManagerer(Printer.WhoIAmType whoIam){this.printer = new Printer(whoIam); }

        private void checkLength(String input,int min_len, int max_len) {

            if (input == null || input.isEmpty() || (input.length() < min_len) || (input.length() > max_len)) {
                printer.print("Exception, incorrect length");
                throw new InputMismatchException();
            }
        }

        private void checkUsernameIllegalCharacters(String username){
            // username must contains:
            // at least 1 number, at least 1 letter (upper case or lower case)

            Boolean digit_flag             = false;
            Boolean lower_letter_flag      = false;
            Boolean upper_letter_flag      = false;

            char[] input_by_chars = username.toCharArray();

            for(char c : input_by_chars){
                if(!Character.isAlphabetic(c) && !Character.isDigit(c) ) {
                    printer.print("Exception, illegal characters in username ");
                    throw new InputMismatchException();
                }

                if(Character.isLetter(c)/*isAlphabetic(c)*/ && Character.isUpperCase(c)) { upper_letter_flag = true;}
                else if(Character.isLetter(c)) {lower_letter_flag = true;}
                else if(Character.isDigit(c)) { digit_flag = true;}
            }

            //check all condition fulfilled
            if ((!lower_letter_flag && !upper_letter_flag) || !digit_flag){
                printer.print("Exception, illegal username format ");
                throw new InputMismatchException();
            }
        }

        private void checkPasswordIllegalCharacters(String inputString) {
            // contains at least 1 number
            // contains at least 1 letter

            Boolean digit_flag             = false;
            Boolean lower_letter_flag      = false;
            Boolean upper_letter_flag      = false;
            Boolean special_character_flag = false;

            char[] input_by_chars = inputString.toCharArray();

            for(char c : input_by_chars){
                if(!Character.isAlphabetic(c) && !Character.isDigit(c) ){                       special_character_flag = true;}
                else if(Character.isLetter(c)/*isAlphabetic(c)*/ && Character.isUpperCase(c)) { upper_letter_flag      = true;}
                else if(Character.isLetter(c)) {                                                lower_letter_flag      = true;}
                else if(Character.isDigit(c)) {                                                 digit_flag             = true;}
            }

            //check all condition fulfilled
            if (!lower_letter_flag || !upper_letter_flag || !digit_flag || !special_character_flag){
                printer.print("Exception, illegal password format ");
                throw new InputMismatchException();
            }
        }

        public void checkCredentials(String username,
                                     String password) throws InputMismatchException{
            printer.print("Checking username ...");
            checkLength(username, 5 ,8 );
            checkUsernameIllegalCharacters(username);
            setUsername(username);

            printer.print("Checking password ...");
            checkLength(password, 5 ,8 );
            checkPasswordIllegalCharacters(password);
            setPassword(password);
        }
    }


    private class AuthenticationTokenWaiter extends Thread {

        private String token = "";
        private String filePath = "";
        private int delayTimeSec = 0;
        private Printer printer;

        private String getToken() {
            return token;
        }

        private String getFilePath() {
            return filePath;
        }

        private int getDelayTimeSec() {
            return delayTimeSec;
        }

        public AuthenticationTokenWaiter(String expectedToken,
                                         String filePath,
                                         int authenticationDelayTimeSecs){
            this.token        = expectedToken;
            this.filePath     = filePath;
            this.delayTimeSec = authenticationDelayTimeSecs;
            this.printer      = new Printer(Printer.WhoIAmType.AUTH);
        }


        // perform:
        // 1. wait for file with token to be created
        // 2. read file, extract token. check for token correctness
        // 3. notify
        public void run() {
            printer.print("Authentication started");

            synchronized (myMonitorObject) {
                if (!wasSignalled) {
                    try {
                        printer.print("Waiting till file with token - " + getFilePath() + " is ready... ");
                        myMonitorObject.wait();
                        wasSignalled = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            printer.print("File ready, going to read token ...");

            //simulation of possible delay during the token supply by user
            if(getDelayTimeSec() > 0) {
                try {
                    Thread.sleep(getDelayTimeSec() * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            File file = new File(this.getFilePath());

            try {
                if(!file.exists())
                    throw new FileNotFoundException(printer.getWhoI() + ":" + " " + "File - " + getFilePath() + " was not created");

                FileReader fileReader = new FileReader(getFilePath());
                BufferedReader buffReader = new BufferedReader(fileReader);
                String extractedToken  = "";
                if ((extractedToken = buffReader.readLine()) == null)
                    throw new IOException(printer.getWhoI() + ":" + " " + "Empty file - " + getFilePath());

                if(!extractedToken.equals(getToken()))
                    throw new InputMismatchException(printer.getWhoI() + ":" + " " + "Invalid Token !!");

                printer.print("Token is: " + extractedToken + ", notifying ... ");

                setAuthentifiedToken(extractedToken);

                synchronized(myMonitorObject){
                    wasSignalled = true;
                    myMonitorObject.notify();
                }
            } catch (IOException e) {
                e.printStackTrace();
                printer.print("An error occurred during creation of file or writing single search result into file");
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Registration reg;

        String username = args[0];
        String password = args[1];

        int expectedAuthenticationTime = parseInt(args[2]);
        int authenticationDelayTime = parseInt(args[3]);

        reg = new Registration();
        try{
            reg.first_registration(username,
                                   password,
                                   expectedAuthenticationTime,
                                   authenticationDelayTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

