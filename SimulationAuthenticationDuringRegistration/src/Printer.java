public class Printer {
    private WhoIAmType whoIam;

    public WhoIAmType getWhoI() {
        return whoIam;
    }

    public Printer(WhoIAmType whoIam){
        this.whoIam = whoIam;
    }

    public void print(String msg){
        System.out.println(getWhoI() + ":" + " " + msg);
    }

    public enum WhoIAmType {
        REG,
        AUTH;

        public static String getWhoIAmIndicatorByType(WhoIAmType type) {
            switch (type) {
                case REG:
                    return "Main_Thread";
                case AUTH:
                    return "Authentication_Thread";
            }
            return null;
        }

        WhoIAmType() {
        }
    }
}
