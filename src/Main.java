import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) throws IOException {
        Map<String, Integer> existencias = new TreeMap<>();

        // Intentar cargar el archivo, si no existe o está vacío, crear un nuevo TreeMap
        File file = new File("existencias.dat");
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                existencias = (TreeMap<String, Integer>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error al leer el archivo, se creará un nuevo registro.");
                existencias = new TreeMap<>();
            }
        }

        int opcion;
        do {
            System.out.println("1. Alta Producto");
            System.out.println("2. Baja Producto");
            System.out.println("3. Cambio stock de producto");
            System.out.println("4. Listar existencias");
            System.out.println("5. Salir");
            opcion = new Scanner(System.in).nextInt();
            switch (opcion) {
                case 1 -> {
                    System.out.println("Código de producto");
                    String codigo = new Scanner(System.in).next();
                    if (!existencias.containsKey(codigo)) {
                        existencias.put(codigo, 0);
                    } else {
                        System.out.println("El código ya existe");
                    }
                }
                case 2 -> {
                    System.out.println("Código de producto");
                    String codigo = new Scanner(System.in).next();
                    existencias.remove(codigo);
                }
                case 3 -> {
                    System.out.println("Código de producto");
                    String codigo = new Scanner(System.in).next();
                    System.out.println("Nuevo stock: ");
                    int stock = new Scanner(System.in).nextInt();
                    existencias.put(codigo, stock);
                }
                case 4 -> {
                    System.out.println(existencias);
                }
                case 5 -> {
                    System.out.println("Saliendo");
                }
                default -> {
                    System.out.println("Opción incorrecta");
                }
            }
        } while (opcion != 5);

        // Guardar los datos en el archivo
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("existencias.dat"))) {
            out.writeObject(existencias);
        } catch (IOException e) {
            System.err.println("Error al guardar los datos: " + e.getMessage());
        }
    }
}

//Prueba con pull request