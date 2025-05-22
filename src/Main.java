import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Loads inventory data from a specified file.
     * If the file does not exist, is empty, or an error occurs during reading,
     * an error message is printed and an empty map is returned.
     *
     * @param file The file from which to load the inventory.
     * @return A map representing the inventory (product code to quantity).
     *         Returns an empty TreeMap if the file is not found, is empty, or in case of a read error.
     */
    private static Map<String, Integer> loadInventory(File file) {
        Map<String, Integer> existencias = new TreeMap<>();
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                existencias = (TreeMap<String, Integer>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error al leer el archivo, se creará un nuevo registro.");
                // Retornar un TreeMap vacío en caso de error para mantener la consistencia
            }
        }
        return existencias;
    }

    /**
     * Main method for the inventory management application.
     * It loads existing inventory, presents a menu for user interaction,
     * and saves the inventory back to file upon exiting.
     *
     * @param args Command line arguments (not used in this application).
     * @throws IOException If an I/O error occurs during file operations (loading or saving inventory).
     */
    public static void main(String[] args) throws IOException {
        File file = new File("existencias.dat");
        Map<String, Integer> existencias = loadInventory(file);

        int opcion;
        do {
            System.out.println("1. Alta Producto");
            System.out.println("2. Baja Producto");
            System.out.println("3. Cambio stock de producto");
            System.out.println("4. Listar existencias");
            System.out.println("5. Salir");
            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (java.util.InputMismatchException e) {
                System.out.println("Error: Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Consume the invalid input
                opcion = 0; // Set to a non-exit option to loop back
            }
            switch (opcion) {
                case 1 -> addProduct(existencias, scanner);
                case 2 -> removeProduct(existencias, scanner);
                case 3 -> changeStock(existencias, scanner);
                case 4 -> listStock(existencias);
                case 5 -> {
                    System.out.println("Saliendo");
                }
                default -> {
                    System.out.println("Opción incorrecta");
                }
            }
        } while (opcion != 5);

        saveInventory(file, existencias);
    }

    /**
     * Adds a new product to the inventory.
     * Prompts the user for a product code and an initial stock quantity.
     * Validates that the product code does not already exist and that the stock quantity is non-negative.
     * Handles invalid input for stock quantity by re-prompting.
     *
     * @param inventory The current inventory map (product code to quantity).
     * @param scanner   The Scanner instance used for user input.
     */
    private static void addProduct(Map<String, Integer> inventory, Scanner scanner) {
        System.out.println("Código de producto");
        String codigo = scanner.next();
        scanner.nextLine(); // Consume newline
        if (!inventory.containsKey(codigo)) {
            System.out.println("Enter initial stock quantity:");
            while (true) {
                try {
                    int stock = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if (stock < 0) {
                        System.out.println("Error: Stock cannot be negative. Please enter a valid stock quantity:");
                        // Loop continues for re-prompt
                    } else {
                        inventory.put(codigo, stock);
                        System.out.println("Product added successfully."); // Optional: confirmation message
                        break; // Exit loop on valid input
                    }
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Error: Entrada inválida. Por favor, ingrese un número para el stock:");
                    scanner.nextLine(); // Consume the invalid input
                    // Loop continues for re-prompt
                }
            }
        } else {
            System.out.println("El código ya existe");
        }
    }

    /**
     * Removes a product from the inventory.
     * Prompts the user for the product code of the item to be removed.
     *
     * @param inventory The current inventory map (product code to quantity).
     * @param scanner   The Scanner instance used for user input.
     */
    private static void removeProduct(Map<String, Integer> inventory, Scanner scanner) {
        System.out.println("Código de producto");
        String codigo = scanner.next();
        scanner.nextLine(); // Consume newline
        inventory.remove(codigo);
    }

    /**
     * Changes the stock quantity of an existing product in the inventory.
     * Prompts the user for the product code and the new stock quantity.
     * Validates that the product code exists and that the new stock quantity is non-negative.
     * Handles invalid input for stock quantity by re-prompting.
     *
     * @param inventory The current inventory map (product code to quantity).
     * @param scanner   The Scanner instance used for user input.
     */
    private static void changeStock(Map<String, Integer> inventory, Scanner scanner) {
        System.out.println("Código de producto");
        String codigo = scanner.next();
        scanner.nextLine(); // Consume newline

        if (!inventory.containsKey(codigo)) {
            System.out.println("Error: Product code not found.");
            return;
        }

        System.out.println("Nuevo stock: ");
        while (true) {
            try {
                int stock = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (stock < 0) {
                    System.out.println("Error: Stock cannot be negative. Please enter a valid stock quantity:");
                    // Loop continues for re-prompt
                } else {
                    inventory.put(codigo, stock);
                    break; // Exit loop on valid input
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Error: Entrada inválida. Por favor, ingrese un número para el stock:");
                scanner.nextLine(); // Consume the invalid input
                // Loop continues for re-prompt
            }
        }
    }

    /**
     * Lists all products currently in the inventory.
     * If the inventory is empty, it prints a message indicating so.
     * Otherwise, it prints each product's code and stock quantity in a user-friendly format.
     *
     * @param inventory The current inventory map (product code to quantity).
     */
    private static void listStock(Map<String, Integer> inventory) {
        if (inventory.isEmpty()) {
            System.out.println("No products in stock.");
        } else {
            System.out.println("Current Stock:");
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                System.out.println("Product Code: " + entry.getKey() + ", Stock: " + entry.getValue());
            }
        }
    }

    /**
     * Saves the current inventory data to a specified file.
     * Uses object serialization to store the inventory map.
     *
     * @param file      The file to which the inventory data will be saved.
     * @param inventory The current inventory map (product code to quantity) to be saved.
     */
    private static void saveInventory(File file, Map<String, Integer> inventory) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(inventory);
        } catch (IOException e) {
            System.err.println("Error al guardar los datos: " + e.getMessage());
        }
    }
}

//Prueba con pull request