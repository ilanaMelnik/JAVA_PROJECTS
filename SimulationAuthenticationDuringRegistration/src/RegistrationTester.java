import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegistrationTester {

    private static Registration tester;

    @BeforeClass
    public static void testSetup() {
        tester = new Registration();
    }

    @Test
    public void noDelayViaAuthentication() {
        System.out.println("Test - noDelayViaAuthentication - started");
        try {
            tester.first_registration("12er34",
                                        "12er34R%",
                                        2,
                                        0);
            System.out.println("Test - noDelayViaAuthentication - Passed");

        } catch (Exception e) {
            System.out.println("Test - noDelayViaAuthentication - Failed");
            e.printStackTrace();
        }
    }

    @Test
    public void delayViaAuthentication() {
        System.out.println("Test - noDelayViaAuthentication - started");
        try {
            tester.first_registration("12er34",
                                        "12er34R%",
                                        2,
                                        10);
            System.out.println("Test - noDelayViaAuthentication - Passed");

        } catch (Exception e) {
            System.out.println("Test - noDelayViaAuthentication - Failed");
            e.printStackTrace();
        }
    }

    @Test
    public void invalidUsername() {
        System.out.println("Test - invalidUsername - started");
        try {
            tester.first_registration("12634",
                                        "12er34R%",
                                        2,
                                        0);
            System.out.println("Test - invalidUsername - Passed");

        } catch (Exception e) {
            System.out.println("Test - invalidUsername - Failed");
            e.printStackTrace();
        }
    }

    @Test
    public void shortUsername() {
        System.out.println("Test - shortUsername - started");
        try {
            tester.first_registration("12r",
                                        "12er34R%",
                                        2,
                                        0);
            System.out.println("Test - shortUsername - Passed");

        } catch (Exception e) {
            System.out.println("Test - shortUsername - Failed");
            e.printStackTrace();
        }
    }

    @Test
    public void onlyLettersUsername() {
        System.out.println("Test - onlyLettersUsername - started");
        try {
            tester.first_registration("rttuopji",
                                        "12er34R%",
                                        2,
                                        0);
            System.out.println("Test - onlyLettersUsername - Passed");

        } catch (Exception e) {
            System.out.println("Test - onlyLettersUsername - Failed");
            e.printStackTrace();
        }
    }

    @Test
    public void specialCharsUsername() {
        System.out.println("Test - onlyLettersUsername - started");
        try {
            tester.first_registration("rt##tuop",
                    "12er34R%",
                    2,
                    0);
            System.out.println("Test - onlyLettersUsername - Passed");

        } catch (Exception e) {
            System.out.println("Test - onlyLettersUsername - Failed");
            e.printStackTrace();
        }
    }
}
