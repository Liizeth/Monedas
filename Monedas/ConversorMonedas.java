import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

// Fuente de la API gratuita: https://www.exchangerate-api.com/
// Registrate, obtené tu API key gratis y reemplazá "TU_API_KEY" abajo

public class ConversorMonedas {

    // ─────────────────────────────────────────────────────────────
    // CONFIGURACIÓN: reemplazá con tu propia API key
    // ─────────────────────────────────────────────────────────────
    static final String API_KEY = "TU_API_KEY";

    // URL base de la API
    static final String URL_BASE = "https://v6.exchangerate-api.com/v6/" + API_KEY;

    public static void main(String [] args) {

        //no se porque aparece la imgen de arriba, intente sacarla pero no se pudo :(

        // Scanner para leer lo que escribe el usuario en la consola
        Scanner scanner = new Scanner(System.in);

        // Variable para guardar la opción elegida en el menú
        int opcion = 0;

        System.out.println("===========================================");
        System.out.println("   BIENVENIDO AL CONVERSOR DE MONEDAS");
        System.out.println("===========================================");

        // El programa sigue corriendo hasta que el usuario elija salir (opción 3)
        while (opcion != 3) {

            // Mostramos el menú de opciones
            mostrarMenu();

            System.out.print("Ingresá tu opción: ");

            // Leemos la opción ingresada y la guardamos
            opcion = leerEntero(scanner);

            // Según la opción elegida, ejecutamos una acción distinta
            switch (opcion) {
                case 1:
                    // Si eligió 1, hacemos una conversión de moneda
                    hacerConversion(scanner);
                    break;

                case 2:
                    // Si eligió 2, mostramos la lista de monedas disponibles
                    mostrarMonedasDisponibles();
                    break;

                case 3:
                    // Si eligió 3, salimos del programa
                    System.out.println("\n¡Gracias por usar el conversor! Hasta luego.");
                    break;

                default:
                    // Si ingresó un número que no existe en el menú
                    System.out.println("Opción inválida. Por favor elegí 1, 2 o 3.");
            }
        }

        // Cerramos el scanner al terminar (buena práctica)
        scanner.close();
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: mostrarMenu
    // Imprime las opciones del menú en pantalla
    // ─────────────────────────────────────────────────────────────
    static void mostrarMenu() {
        System.out.println("\n------- MENÚ -------");
        System.out.println("1. Convertir moneda");
        System.out.println("2. Ver monedas disponibles");
        System.out.println("3. Salir");
        System.out.println("--------------------");
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: mostrarMonedasDisponibles
    // Muestra una lista fija de monedas que el usuario puede usar
    // ─────────────────────────────────────────────────────────────
    static void mostrarMonedasDisponibles() {
        System.out.println("\n── Monedas disponibles ──");
        System.out.println("ARS - Peso argentino");
        System.out.println("USD - Dólar estadounidense");
        System.out.println("EUR - Euro");
        System.out.println("BRL - Real brasileño");
        System.out.println("CLP - Peso chileno");
        System.out.println("COP - Peso colombiano");
        System.out.println("BOB - Boliviano boliviano");
        System.out.println("MXN - Peso mexicano");
        System.out.println("(Podés ingresar cualquier código de moneda válido)");
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: hacerConversion
    // Le pide al usuario los datos necesarios y consulta la API
    // para hacer la conversión de moneda
    // ─────────────────────────────────────────────────────────────
    static void hacerConversion(Scanner scanner) {

        System.out.println("\n── Conversión de moneda ──");

        // Pedimos el código de la moneda de origen (ej: ARS, USD)
        System.out.print("Ingresá el código de la moneda de ORIGEN (ej: ARS): ");
        String monedaOrigen = scanner.next().toUpperCase(); // .toUpperCase() convierte a mayúsculas

        // Pedimos el código de la moneda destino
        System.out.print("Ingresá el código de la moneda DESTINO (ej: USD): ");
        String monedaDestino = scanner.next().toUpperCase();

        // Verificamos que las dos monedas no sean iguales
        if (monedaOrigen.equals(monedaDestino)) {
            System.out.println("Las monedas de origen y destino son iguales. No tiene sentido convertir.");
            return; // Salimos del método sin hacer nada más
        }

        // Pedimos el monto a convertir
        System.out.print("Ingresá el monto a convertir: ");
        double monto = leerDecimal(scanner);

        // Verificamos que el monto sea mayor a cero
        if (monto <= 0) {
            System.out.println("El monto debe ser mayor a cero.");
            return;
        }

        // Construimos la URL de la API con los datos ingresados
        // Formato: /pair/ORIGEN/DESTINO/MONTO
        String url = URL_BASE + "/pair/" + monedaOrigen + "/" + monedaDestino + "/" + monto;

        System.out.println("\nConsultando la API...");

        // Llamamos a la API y obtenemos el resultado
        String respuesta = consultarAPI(url);

        // Si hubo un error al consultar la API, respuesta será null
        if (respuesta == null) {
            System.out.println("No se pudo conectar a la API. Verificá tu API key y conexión a internet.");
            return;
        }

        // Extraemos el valor convertido del JSON de respuesta
        double resultado = extraerResultado(respuesta);

        // Si el resultado es -1, hubo un error en la respuesta
        if (resultado == -1) {
            System.out.println("Error en la respuesta de la API. Verificá que los códigos de moneda sean válidos.");
            return;
        }

        // Mostramos el resultado final de la conversión
        System.out.printf("\nResultado: %.2f %s = %.2f %s%n", monto, monedaOrigen, resultado, monedaDestino);
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: consultarAPI
    // Recibe una URL, hace la petición HTTP y devuelve la respuesta
    // como texto (JSON). Si falla, devuelve null.
    // ─────────────────────────────────────────────────────────────
    static String consultarAPI(String url) {
        try {
            // Creamos el cliente HTTP (el que hace las peticiones a internet)
            HttpClient cliente = HttpClient.newHttpClient();

            // Construimos la petición con la URL que recibimos
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            // Enviamos la petición y recibimos la respuesta como String
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

            // Devolvemos el cuerpo de la respuesta (el JSON con los datos)
            return respuesta.body();

        } catch (Exception e) {
            // Si algo falla (sin internet, URL mala, etc.) devolvemos null
            System.out.println("Error al consultar la API: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: extraerResultado
    // Recibe el JSON como texto y busca el valor de "conversion_result"
    // Devuelve el número convertido, o -1 si no lo encuentra.
    //
    // NOTA: Hacemos esto manualmente (sin librerías como Gson)
    // para que sea más simple de entender.
    // ─────────────────────────────────────────────────────────────
    static double extraerResultado(String json) {
        try {
            // Buscamos el texto "conversion_result" dentro del JSON
            String clave = "\"conversion_result\":";
            int inicio = json.indexOf(clave);

            // Si no encontramos la clave, algo salió mal
            if (inicio == -1) return -1;

            // Nos posicionamos justo después de la clave y la coma/espacio
            inicio += clave.length();

            // Buscamos donde termina el número (puede terminar con , o })
            int fin = json.indexOf(",", inicio);
            int finLlave = json.indexOf("}", inicio);

            // Tomamos el que esté más cerca
            if (fin == -1 || finLlave < fin) fin = finLlave;

            // Extraemos el número como texto y lo convertimos a double
            String numeroTexto = json.substring(inicio, fin).trim();
            return Double.parseDouble(numeroTexto);

        } catch (Exception e) {
            // Si hubo algún error al parsear, devolvemos -1
            return -1;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: leerEntero
    // Lee un número entero del scanner. Si el usuario ingresa algo
    // que no es un número, devuelve -1 para evitar que el programa crashee.
    // ─────────────────────────────────────────────────────────────
    static int leerEntero(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.next());
        } catch (NumberFormatException e) {
            System.out.println("Eso no es un número válido.");
            return -1; // Valor que no coincide con ninguna opción del menú
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODO: leerDecimal
    // Lee un número decimal del scanner. Si el usuario ingresa algo
    // que no es un número, devuelve 0.
    // ─────────────────────────────────────────────────────────────
    static double leerDecimal(Scanner scanner) {
        try {
            return Double.parseDouble(scanner.next().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Eso no es un número válido.");
            return 0;
        }
    }
}
