package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import bgu.spl.mics.application.passiveObjects.Inventory;

import static bgu.spl.mics.application.passiveObjects.Inventory.option.NOT_IN_STOCK;
import static bgu.spl.mics.application.passiveObjects.Inventory.option.SUCCESSFULLY_TAKEN;
import static org.junit.Assert.*;

public class InventoryTest {


    private BookInventoryInfo [] bookInventoryInfo;
    private Inventory inventory;
    @Before
    public void setUp() throws Exception
    {
         inventory=Inventory.getInstance(); //instance

        BookInventoryInfo b1=new BookInventoryInfo("Harry potter",1,80);
        BookInventoryInfo b2=new BookInventoryInfo("Kipa aduma",2,70);
        BookInventoryInfo b3=new BookInventoryInfo("Shrek",3,60);
        bookInventoryInfo=new BookInventoryInfo[3]; //holds books
        bookInventoryInfo[0]=b1;
        bookInventoryInfo[1]=b2;
        bookInventoryInfo[2]=b3;
    }

    @After
    public void tearDown() throws Exception {
        this.inventory=null;

    }

    @Test
    public void getInstance()
    {
        //checking if inventory exist
        assertTrue(inventory!=null); // cheek if instance built
        //checking if same instance
        assertTrue(inventory==Inventory.getInstance()); // check if inventory is singelton
    }



    @Test
    public void load()
    {
        inventory.load(bookInventoryInfo); //loading inventory with books

        //checking if the first book there
        assertTrue(inventory.checkAvailabiltyAndGetPrice("Harry potter")==80);
        //checking if the first book there
        assertTrue(inventory.checkAvailabiltyAndGetPrice("Kipa aduma")==70);
        //checking if the third book there
        assertTrue(inventory.checkAvailabiltyAndGetPrice("Shrek")==60);

        //checking if recognize book not in the inventory
         assertTrue(inventory.checkAvailabiltyAndGetPrice("Dictionary")==-1);
    }

    @Test
    public void take()
    {
        //checking if book was taken successfully
        assertEquals(inventory.take("Harry potter"),SUCCESSFULLY_TAKEN);

        // inverient 1
        //checking if amount updated correctly
        assertEquals(inventory.take("Harry potter"),NOT_IN_STOCK);
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        //checking the correctness of function return price
        assertEquals(inventory.checkAvailabiltyAndGetPrice("Harry potter"),10);
        assertEquals(inventory.checkAvailabiltyAndGetPrice("Kipa aduma"),20);
        assertEquals(inventory.checkAvailabiltyAndGetPrice("shrek"),30);

        inventory.take("Harry potter");
        // there are no books
        assertEquals(inventory.checkAvailabiltyAndGetPrice("Harry potter"),-1);
    }

    @Test
    public void printInventoryToFile() {
    }
}