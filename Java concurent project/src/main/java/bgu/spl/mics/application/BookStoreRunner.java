package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */




public class BookStoreRunner implements Serializable {

    //file name, holds path of output text
    //allCustomers, holds customer to print
    private static void print(String filename, Object object) {
        try {

            FileOutputStream fileOutputStream=new FileOutputStream(filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {

        HashMap<Integer,Customer> allCustomers=new HashMap<>();
        LinkedList<Thread> threadLinkedList=new LinkedList<>();
        LinkedList<MicroService> servicesLinkedList=new LinkedList<>();

        Object lock=new Object();
        int servicesCounter=0;//remembers the number off the services in order to assure that every service finish initializing befor trying to start the time service
        // hold all selling services
        SellingService [] SellingServices;
        // hold all Inventory service
        InventoryService [] InventoryServices;
        // hold all Logistic services
        LogisticsService [] LogisticsServices;
        // hold all ResourceService services
        ResourceService[] ResourceServices;
        // hold the customers
        APIService [] APIServices;
        JsonObject object = null;

        //address to the json file
        String thePath=args[0];
        //String thePath = "/users/studs/bsc/2019/henmor/Desktop/input.json";

        try {
            object = (JsonObject) new JsonParser().parse(new FileReader(thePath)); //contain all the text from file
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //if file do exist
        if (object != null) {


            //Inventory:initialInventory

            // Json's book section
            JsonElement books= object.get("initialInventory");

            //creating an array of books which has slots as the number of books in Json file
            //later, []inventory gonna be loaded
            BookInventoryInfo[] inventory = new BookInventoryInfo[books.getAsJsonArray().size()];

            for(int i=0;i<books.getAsJsonArray().size();i++)
            {
                //parse book details
                String bookName=books.getAsJsonArray().get(i).getAsJsonObject().get("bookTitle").getAsString();
                int amount=books.getAsJsonArray().get(i).getAsJsonObject().get("amount").getAsInt();
                int price=books.getAsJsonArray().get(i).getAsJsonObject().get("price").getAsInt();

                //creating the book
                BookInventoryInfo bookToInsert=new BookInventoryInfo(bookName,amount,price);
                inventory[i]=bookToInsert;
            }
            //load inventory
            Inventory.getInstance().load(inventory);
//----------------------------------------------------------------------------------------------------------------------
            //ResourceHolder
            //Json's vehicles section
            JsonElement cars=object.get("initialResources");
            // creating a vehicles array which will be loaded latter
            DeliveryVehicle[] vehicles = new DeliveryVehicle[cars.getAsJsonArray().get(0).getAsJsonObject().get("vehicles").getAsJsonArray().size()];
            //going thorough cars in JJson file
            for(int i=0;i<cars.getAsJsonArray().get(0).getAsJsonObject().get("vehicles").getAsJsonArray().size();i++)
            {
                int license=cars.getAsJsonArray().get(0).getAsJsonObject().get("vehicles").getAsJsonArray().get(i).getAsJsonObject().get("license").getAsInt();
                int speed=cars.getAsJsonArray().get(0).getAsJsonObject().get("vehicles").getAsJsonArray().get(i).getAsJsonObject().get("speed").getAsInt();

                DeliveryVehicle vehicle=new DeliveryVehicle(license,speed);
                vehicles[i]=vehicle;
            }
            //loading
            ResourcesHolder.getInstance().load(vehicles);
//----------------------------------------------------------------------------------------------------------------------
            // time service construct
            JsonElement services= object.get("services");

            int speed=object.get("services").getAsJsonObject().get("time").getAsJsonObject().get("speed").getAsInt();
            int duration=object.get("services").getAsJsonObject().get("time").getAsJsonObject().get("duration").getAsInt();

            TimeService timer=new TimeService(speed,duration);
//----------------------------------------------------------------------------------------------------------------------
            //SellingServices Constructor
            int numOfSellingServices=(object.get("services").getAsJsonObject().get("selling").getAsInt());
            SellingServices=new SellingService[numOfSellingServices];

            for (int i=0;i<numOfSellingServices;i++)
            {
                String numOfService=String.valueOf(i+1);
                String serviceName="selling "+numOfService;
                SellingService sellingService=new SellingService(serviceName);
                SellingServices[i]=sellingService; //adding Selling service to the linked list
                servicesLinkedList.add(sellingService);
            }
            servicesCounter+=numOfSellingServices;//adds the number of the selling services to the services counter
//----------------------------------------------------------------------------------------------------------------------
            // Inventory services Constructor
            int numOfInventoryService=object.get("services").getAsJsonObject().get("inventoryService").getAsInt();
            InventoryServices= new InventoryService[numOfInventoryService];

            for(int i=0;i<numOfInventoryService;i++)
            {
                String numOfService=String.valueOf(i+1);
                String inventoryServiceName="inventory "+numOfService;
                InventoryService inventoryToCreate= new InventoryService(inventoryServiceName);
                InventoryServices[i]=inventoryToCreate;
                servicesLinkedList.add(inventoryToCreate);
            }
            servicesCounter+=numOfInventoryService;//adds the number of the inventory services to the services counter
//----------------------------------------------------------------------------------------------------------------------
            // logistics service
            int numOflogisticsService=object.get("services").getAsJsonObject().get("logistics").getAsInt();
            LogisticsServices=new LogisticsService[numOflogisticsService];
            for(int i=0;i<numOflogisticsService;i++)
            {
                String numOfService=String.valueOf(i+1);
                String LogisticServiceName="logistics "+numOfService;
                LogisticsService logisticToInsert= new LogisticsService(LogisticServiceName); //create service
                LogisticsServices[i]=logisticToInsert; //add service to the the array
                servicesLinkedList.add(logisticToInsert);
            }
            servicesCounter+=numOflogisticsService;//adds the number of the logistics services to the services counter
//----------------------------------------------------------------------------------------------------------------------
            // ReourceHolder Service
            int numOfResourcesService=object.get("services").getAsJsonObject().get("resourcesService").getAsInt();
            ResourceServices=new ResourceService [numOfResourcesService];
            for(int i=0;i<numOfResourcesService;i++)
            {
                String numOfService=String.valueOf(i+1);
                String resourceServiceName="resource "+numOfService;
                ResourceService resourceService =new ResourceService(resourceServiceName);
                ResourceServices[i]=resourceService;
                servicesLinkedList.add(resourceService);
            }
            servicesCounter+=numOfResourcesService;//adds the number of the resource services to the services counter
            //----------------------------------------------------------------------------------------------------------
            // Customers
            int numOfAPIServices=object.get("services").getAsJsonObject().get("customers").getAsJsonArray().size();
            APIServices=new APIService[numOfAPIServices];
            for(int i=0;i<object.get("services").getAsJsonObject().get("customers").getAsJsonArray().size();i++)
            {
                JsonElement customerToCreate=object.get("services").getAsJsonObject().get("customers").getAsJsonArray().get(i);
                int customerId=customerToCreate.getAsJsonObject().get("id").getAsInt();
                String customerName=customerToCreate.getAsJsonObject().get("name").getAsString();
                String customerAdress=customerToCreate.getAsJsonObject().get("address").getAsString();
                int customerDistance=customerToCreate.getAsJsonObject().get("distance").getAsInt();
                int creditNUmber=((JsonObject)customerToCreate).getAsJsonObject("creditCard").get("number").getAsInt();
                int creditAmount=((JsonObject)customerToCreate).getAsJsonObject("creditCard").get("amount").getAsInt();


                Customer customer= new Customer(customerId,customerName,customerAdress,customerDistance,creditNUmber,creditAmount);
                allCustomers.put(customerId,customer);

                LinkedBlockingQueue<Pair<String,Integer>> orderSchedule=new LinkedBlockingQueue<>();
                for(int j=0;j<(customerToCreate).getAsJsonObject().get("orderSchedule").getAsJsonArray().size();j++)
                {
                    String bookName=(customerToCreate).getAsJsonObject().get("orderSchedule").getAsJsonArray().get(j).getAsJsonObject().get("bookTitle").getAsString();
                    int tick=(customerToCreate).getAsJsonObject().get("orderSchedule").getAsJsonArray().get(j).getAsJsonObject().get("tick").getAsInt();
                    Pair<String,Integer> pair=new Pair<>(bookName,tick);
                    orderSchedule.add(pair);
                }
                String serviceName="API"+String.valueOf(i);
                APIServices[i]=new APIService(orderSchedule,customer,serviceName);
                servicesLinkedList.add(APIServices[i]);

            }
            servicesCounter+=numOfAPIServices;//adds the number of the API services to the services counter


//                                    have to complete

//----------------------------------------------------------------------------------------------------------------------


            for(MicroService microService:servicesLinkedList)
            {
                Thread thread=new Thread(microService);
                threadLinkedList.add(thread);
                thread.start();
            }

            ThreadCounter threadCounter=ThreadCounter.getInstance();

            while(threadCounter.getCounter()!=servicesCounter);
            {//waiting until every thread finish initializing
                try{
                    Thread.sleep((long)(100));
                }
                catch
                (Exception exception)
                {
                }
            }

            Thread thread=new Thread(timer);
            thread.start();
        }


        for(Thread thread:threadLinkedList)
        {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        MoneyRegister moneyRegister=MoneyRegister.getInstance();
        Inventory inventory=Inventory.getInstance();


        print(args[1],allCustomers);
        inventory.printInventoryToFile(args[2]);
        moneyRegister.printOrderReceipts(args[3]);
        print(args[4],moneyRegister);
        
    }


}
