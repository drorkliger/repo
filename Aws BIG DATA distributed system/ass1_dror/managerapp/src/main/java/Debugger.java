
public class Debugger {

    private String className;
    private String scopeDescription;
    private String whatHasJustHappened;
    private String line;


    public Debugger(String className)
    {
        this.className = className;
        this.scopeDescription = "";
        this.whatHasJustHappened = "";
        this.line="";
    }

    public void printHere(String whatHasJustHappened, String scopeDescription)
    {
        System.out.println(whatHasJustHappened + "\t\t\t" + "from " + this.className + "\t\t\t"+"in: " + scopeDescription);
    }
}
