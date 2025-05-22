import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

// As Main's methods are static, we don't need to instantiate Main.
// We will call Main.methodName directly.
// We need to access the package-private/public static methods of Main.
// For simplicity in this environment, we'll assume direct access is possible.
// If Main's methods were not public, Main.java would need to be modified or reflection used.

public class MainTest {

    private Map<String, Integer> inventory;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // Helper to create a Scanner from a String
    private Scanner createScanner(String input) {
        return new Scanner(new StringReader(input));
    }

    @BeforeEach
    public void setUp() {
        inventory = new TreeMap<>();
        System.setOut(new PrintStream(outContent)); // Redirect System.out
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut); // Restore System.out
    }

    // --- addProduct Tests ---

    @Test
    public void testAddProduct_newProduct_validStock() {
        String input = "P101\n10\n"; // Product code, then stock
        Scanner scanner = createScanner(input);
        Main.addProduct(inventory, scanner);
        assertTrue(inventory.containsKey("P101"), "Inventory should contain the new product.");
        assertEquals(10, inventory.get("P101"), "Stock quantity should be 10.");
        assertTrue(outContent.toString().contains("Product added successfully."), "Success message should be displayed.");
    }

    @Test
    public void testAddProduct_newProduct_zeroStock() {
        String input = "P102\n0\n";
        Scanner scanner = createScanner(input);
        Main.addProduct(inventory, scanner);
        assertTrue(inventory.containsKey("P102"));
        assertEquals(0, inventory.get("P102"));
        assertTrue(outContent.toString().contains("Product added successfully."));
    }

    @Test
    public void testAddProduct_newProduct_invalidStockThenValid() {
        String input = "P103\n-5\n20\n"; // Invalid stock, then valid stock
        Scanner scanner = createScanner(input);
        Main.addProduct(inventory, scanner);
        assertTrue(inventory.containsKey("P103"));
        assertEquals(20, inventory.get("P103"));
        assertTrue(outContent.toString().contains("Error: Stock cannot be negative."));
        assertTrue(outContent.toString().contains("Product added successfully."));
    }
    
    @Test
    public void testAddProduct_newProduct_nonNumericStockThenValid() {
        String input = "P104\nabc\n25\n"; // Non-numeric stock, then valid stock
        Scanner scanner = createScanner(input);
        Main.addProduct(inventory, scanner);
        assertTrue(inventory.containsKey("P104"));
        assertEquals(25, inventory.get("P104"));
        assertTrue(outContent.toString().contains("Error: Entrada inválida."));
        assertTrue(outContent.toString().contains("Product added successfully."));
    }

    @Test
    public void testAddProduct_existingProduct() {
        inventory.put("P101", 5);
        String input = "P101\n10\n"; // Attempt to add existing product
        Scanner scanner = createScanner(input);
        Main.addProduct(inventory, scanner);
        assertEquals(5, inventory.get("P101"), "Stock of existing product should not change.");
        assertTrue(outContent.toString().contains("El código ya existe"), "Message for existing code should be displayed.");
    }

    // --- removeProduct Tests ---

    @Test
    public void testRemoveProduct_existingProduct() {
        inventory.put("P201", 15);
        String input = "P201\n";
        Scanner scanner = createScanner(input);
        Main.removeProduct(inventory, scanner);
        assertFalse(inventory.containsKey("P201"), "Product should be removed from inventory.");
    }

    @Test
    public void testRemoveProduct_nonExistentProduct() {
        inventory.put("P201", 15);
        String input = "P999\n"; // Non-existent product code
        Scanner scanner = createScanner(input);
        Main.removeProduct(inventory, scanner);
        assertEquals(1, inventory.size(), "Inventory size should remain unchanged.");
        assertTrue(inventory.containsKey("P201"), "Existing product should still be in inventory.");
    }

    // --- changeStock Tests ---

    @Test
    public void testChangeStock_existingProduct_validNewStock() {
        inventory.put("P301", 20);
        String input = "P301\n30\n"; // Product code, then new stock
        Scanner scanner = createScanner(input);
        Main.changeStock(inventory, scanner);
        assertEquals(30, inventory.get("P301"), "Stock should be updated to the new quantity.");
    }

    @Test
    public void testChangeStock_existingProduct_zeroNewStock() {
        inventory.put("P302", 20);
        String input = "P302\n0\n";
        Scanner scanner = createScanner(input);
        Main.changeStock(inventory, scanner);
        assertEquals(0, inventory.get("P302"));
    }
    
    @Test
    public void testChangeStock_existingProduct_invalidStockThenValid() {
        inventory.put("P303", 20);
        String input = "P303\n-5\n25\n"; // Product code, invalid stock, then valid stock
        Scanner scanner = createScanner(input);
        Main.changeStock(inventory, scanner);
        assertEquals(25, inventory.get("P303"));
        assertTrue(outContent.toString().contains("Error: Stock cannot be negative."));
    }

    @Test
    public void testChangeStock_existingProduct_nonNumericStockThenValid() {
        inventory.put("P304", 20);
        String input = "P304\nxyz\n30\n"; // Product code, non-numeric stock, then valid stock
        Scanner scanner = createScanner(input);
        Main.changeStock(inventory, scanner);
        assertEquals(30, inventory.get("P304"));
        assertTrue(outContent.toString().contains("Error: Entrada inválida."));
    }

    @Test
    public void testChangeStock_nonExistentProduct() {
        String input = "P999\n10\n"; // Non-existent product code
        Scanner scanner = createScanner(input);
        Main.changeStock(inventory, scanner);
        assertTrue(inventory.isEmpty(), "Inventory should remain empty.");
        assertTrue(outContent.toString().contains("Error: Product code not found."), "Error message for not found product.");
    }

    @Test
    public void testChangeStock_existingProduct_negativeNewStock() {
        inventory.put("P305", 20);
        String input = "P305\n-5\n"; // Attempt to set negative stock (will re-prompt in actual app, test checks error)
        Scanner scanner = createScanner(input);
        // In the test, the loop for re-prompting will run once with -5
        // The method will print the error, then the input stream ends.
        // So the stock will NOT be updated to -5.
        Main.changeStock(inventory, scanner);
        assertEquals(20, inventory.get("P305"), "Stock should not change if new stock is negative and input ends.");
        assertTrue(outContent.toString().contains("Error: Stock cannot be negative."), "Error message for negative stock.");
    }

    // --- listStock Tests ---

    @Test
    public void testListStock_emptyInventory() {
        Main.listStock(inventory);
        assertTrue(outContent.toString().contains("No products in stock."), "Message for empty stock should be displayed.");
    }

    @Test
    public void testListStock_withProducts() {
        inventory.put("P401", 50);
        inventory.put("P402", 75);
        Main.listStock(inventory);
        String output = outContent.toString();
        assertTrue(output.contains("Current Stock:"), "Header for stock listing should be present.");
        assertTrue(output.contains("Product Code: P401, Stock: 50"), "Product P401 details should be listed.");
        assertTrue(output.contains("Product Code: P402, Stock: 75"), "Product P402 details should be listed.");
    }

    // --- saveInventory and loadInventory Tests ---
    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @Test
    public void testSaveAndLoadInventory_valid() throws IOException {
        inventory.put("P501", 100);
        inventory.put("P502", 200);

        Path tempFile = tempDir.resolve("testInventory.dat");
        
        Main.saveInventory(tempFile.toFile(), inventory);
        assertTrue(Files.exists(tempFile), "Inventory file should have been created by saveInventory.");

        Map<String, Integer> loadedInventory = Main.loadInventory(tempFile.toFile());
        assertNotNull(loadedInventory, "Loaded inventory should not be null.");
        assertEquals(2, loadedInventory.size(), "Loaded inventory should have 2 items.");
        assertEquals(100, loadedInventory.get("P501"), "Stock for P501 should match.");
        assertEquals(200, loadedInventory.get("P502"), "Stock for P502 should match.");
    }
    
    @Test
    public void testLoadInventory_nonExistentFile() {
        File nonExistentFile = new File(tempDir.toFile(), "nonExistent.dat");
        Map<String, Integer> loadedInventory = Main.loadInventory(nonExistentFile);
        assertNotNull(loadedInventory, "Loaded inventory should not be null (should be an empty map).");
        assertTrue(loadedInventory.isEmpty(), "Loaded inventory from non-existent file should be empty.");
        // Check System.err output if Main.loadInventory prints specific error for not found
        // This is harder to test robustly without changing Main.java to use a logger or return error status
    }

    @Test
    public void testLoadInventory_emptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("emptyInventory.dat");
        Files.createFile(emptyFile); // Create an actual empty file

        Map<String, Integer> loadedInventory = Main.loadInventory(emptyFile.toFile());
        assertNotNull(loadedInventory, "Loaded inventory should not be null.");
        assertTrue(loadedInventory.isEmpty(), "Loaded inventory from an empty file should be empty.");
         // Check System.err output as in load from non-existent
    }

    // It's harder to test the IOException/ClassNotFoundException cases for loadInventory
    // without crafting a corrupted file. For this exercise, we'll skip that.

    // Test for main method's menu navigation and option selection (basic)
    // This is more of an integration test and can get complex.
    // Here's a simple one to check if the menu option processing works for "exit".
    @Test
    public void testMain_exitOption() throws IOException {
        String input = "5\n"; // Option 5 is Salir (Exit)
        System.setIn(new ByteArrayInputStream(input.getBytes())); // Simulate System.in
        
        // To test main, we need to ensure it doesn't loop forever if Scanner isn't closed
        // or if the exit condition isn't met.
        // We also need to ensure we don't try to save to "existencias.dat" in the project root.
        // This test is more illustrative. A full test of main() is complex.
        
        // For now, just check if "Saliendo" is printed.
        // Note: Main.main also calls loadInventory and saveInventory which might interact with file system.
        // For a focused unit test, one would refactor Main to make these dependencies injectable.
        // However, adhering to the current structure:
        
        // We need a way to prevent saveInventory from creating existencias.dat in the root
        // For this test, we'll accept it might create it, or we could mock File operations (complex).
        
        // Redirect System.in for the main method
        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Main.main(new String[]{});
        } finally {
            System.setIn(originalIn); // Restore System.in
        }
        
        assertTrue(outContent.toString().contains("Saliendo"), "Exit message should be displayed.");
    }
}
