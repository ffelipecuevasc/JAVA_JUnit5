package org.fcuevas.junit;

import org.fcuevas.junit.excepciones.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

//import static org.junit.jupiter.api.Assertions.*;

/*Con las pruebas unitarias hay que considerar que tanto las clases como los métodos tienen el nivel
 * de encapsulación DEFAULT (PACKAGE) para no ser llamados desde ningún otro lado del proyecto excepto
 * desde el contexto de las pruebas. Por eso no se anota el nivel de encapsulación como PUBLIC por ejemplo*/
class CuentaTest {

    Cuenta cuenta;

    /*Las anotaciones @BeforeAll y @AfterAll se ejecutan 1 sola vez en todo el ciclo de vida de la
    * clase de prueba. Por defecto, JUnit5 no crea una instancia de la clase de pruebas, por lo tanto
    * estos métodos con estas anotaciones deben ser estáticos para poder ejecutarse sin depender de
    * una instancia.
    *
    * Esto se puede evitar colocando la anotación @TestInstance(TestInstance.Lifecycle.PER_CLASS) antes
    * de declarar la clase (o sea antes de la línea 13 de código acá). Por defecto, JUnit5 viene
    * configurado con @TestInstance(TestInstance.Lifecycle.PER_METHOD) para crear una instancia por cada
    * método declarado con la anotación @Test, por eso es posible ejecutar los métodos por separado.
    *
    * Pero no crea una instancia para ejecutar @BeforeAll y @AfterAll, porque esas anotaciones están
    * pensadas para ejecutarse una sola vez por clase, no por método.*/
    @BeforeAll
    static void antesTodo(){
        System.out.println("Iniciando las pruebas con JUnit 5.");
        System.out.println("----------------------------------");
    }

    //Anotación para establecer una acción a ejecutar antes de cada método @Test
    @BeforeEach
    //Se utiliza inyección de dependencias (TestInfo) donde JUnit5 se hace cargo de inyectar el objeto
    void inicializandoMetodos(TestInfo testInfo){
        this.cuenta = new Cuenta("Felipe Cuevas", new BigDecimal(1500.000));
        System.out.println("Inicializando la prueba respectiva. Test = " + testInfo.getDisplayName());
    }

    @AfterAll
    static void despuesTodo(){
        System.out.println("----------------------------------");
        System.out.println("Finalizando pruebas con JUnit 5.");
    }

    @AfterEach
    void finalizandoMetodos(){
        System.out.println("Finalizando la prueba (test) correctamente.");
    }

    @Test
    @DisplayName("Prueba (test) para revisar el nombre de una cuenta")
    void pruebaNombreCuenta() {
        String esperado = "Felipe Cuevas";
        String realidad = cuenta.getCliente();
        Assertions.assertEquals(esperado, realidad);
        /*En el caso de los métodos de prueba, pueden haber varios 'assert' dentro del método pero solo
        falta que 1 solo falle para decretar que el método completo falló*/
    }

    @Test
    @DisplayName("Prueba (test) para revisar el saldo de una cuenta")
    void pruebaSaldoCuenta() {
        Assertions.assertEquals(1500.000, this.cuenta.getSaldo().doubleValue());
        Assertions.assertFalse(this.cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
    }

    /*Este test originalmente usa por defecto el método equals() que la clase Cliente hereda de Object,
     * por lo que inicialmente arroja error porque los objetos no son iguales aunque los atributos son
     * los mismos. Pero si editamos el método equals() de la clase Cliente y comparamos en base a los
     * atributos entonces ahí esta prueba dará como resultado TRUE.
     *
     * Por este motivo se llama TDD porque primero se realizan las pruebas para detectar una deficiencia
     * en el código (la ausencia del método equals()) y después se arregla la clase Cuenta para superar
     * la prueba.*/
    @Test
    @DisplayName("Prueba (test) para revisar la creación de cuentas")
    void pruebaReferenciaCuenta() {
        Cuenta cuenta1 = new Cuenta("Felipe", new BigDecimal(2500.12345));
        Cuenta cuenta2 = new Cuenta("Felipe", new BigDecimal(2500.12345));
        Assertions.assertEquals(cuenta1, cuenta2);

    }

    @Test
    @DisplayName("Prueba (test) para revisar el método DÉBITO de una cuenta")
    void pruebaDebitoCuenta() {
        this.cuenta.debito(new BigDecimal(100.000));
        Assertions.assertNotNull(this.cuenta.getSaldo());
        Assertions.assertEquals(1400.000, this.cuenta.getSaldo().intValue());
    }

    @Test
    @DisplayName("Prueba (test) para revisar el método CRÉDITO de una cuenta")
    void pruebaCreditoCuenta() {
        this.cuenta.credito(new BigDecimal(100));
        Assertions.assertNotNull(this.cuenta.getSaldo());
        Assertions.assertEquals(1600, this.cuenta.getSaldo().intValue());
    }

    @Test
    @DisplayName("Prueba (test) para revisar el saldo suficiente de una cuenta más una excepción")
    void pruebaDineroInsuficienteExcepcionCuenta() {
        Cuenta cuentaE = new Cuenta("Cliente", new BigDecimal(1000.500));
        Exception excepcion = Assertions.assertThrows(DineroInsuficienteException.class, () -> {
            cuentaE.debito(new BigDecimal(1500));
        });
        String actual = excepcion.getMessage();
        String esperado = "Dinero Insuficiente";
        Assertions.assertEquals(esperado, actual);
    }

    @Test
    @DisplayName("Prueba (test) para revisar la transferencia de montos entre cuentas")
    void pruebaTransferenciaMontosCuentas() {
        Cuenta cuenta1 = new Cuenta("Cliente 1", new BigDecimal(1000));
        Cuenta cuenta2 = new Cuenta("Cliente 2", new BigDecimal(1000));
        Banco banco = new Banco();
        banco.setNombre("Banco Estado");
        banco.transferir(cuenta1, cuenta2, new BigDecimal(500));

        Assertions.assertEquals("500", cuenta1.getSaldo().toPlainString());
        Assertions.assertEquals("1500", cuenta2.getSaldo().toPlainString());
    }

    /*A continuación se necesita crear la clase Banco para ir probando la relación entre Cliente-Banco
     * y se procede a realizar el test en formato TDD para evaluar si la relación es bidireccional
     * y funciona correctamente. Por ejemplo se agrega el atributo BANCO en el Cliente para relacionarlos
     * y en el caso de Banco se crea una lista de clientes con un método addCliente().*/
    @Test
    @DisplayName("Prueba (test) para revisar la relación entre cuentas y un banco")
    void pruebaRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("Cliente 1", new BigDecimal(1000));
        Cuenta cuenta2 = new Cuenta("Cliente 2", new BigDecimal(1000));
        Banco banco = new Banco();
        banco.setNombre("Banco Estado");
        banco.addCuentas(cuenta1);
        banco.addCuentas(cuenta2);
        banco.transferir(cuenta1, cuenta2, new BigDecimal(500));

        Assertions.assertEquals("500", cuenta1.getSaldo().toPlainString());
        Assertions.assertEquals("1500", cuenta2.getSaldo().toPlainString());

        Assertions.assertEquals(2, banco.getCuentas().size());
        Assertions.assertEquals("Banco Estado", cuenta1.getBanco().getNombre());

        Assertions.assertEquals("Cliente 1", banco.getCuentas().stream()
                .filter(c -> c.getCliente().equals("Cliente 1"))
                .findFirst()
                .get()
                .getCliente()
        );

        Assertions.assertTrue(banco.getCuentas().stream()
                .filter(c -> c.getCliente().equals("Cliente 1"))
                .findFirst()
                .isPresent()
        );
    }

    /*Acá introducimos el método assertAll() que permite agrupar varias pruebas (test) y así, en caso
     * de que alguna prueba falle, el método assertAll() te permite ver cuál prueba falló, a diferencia
     * de la prueba anterior que tenía varias pruebas (métodos assert()) por separado y si alguno
     * fallaba, no ejecutaba las siguientes prueba y daba por fallido todo el test. Con el assertAll()
     * se pueden ejecutar todas las pruebas y saber con certeza cuáles fallaron.*/
    @Test
    @DisplayName("Prueba (test) para revisar la relación entre cuentas y un banco con ASSERTALL()")
    void pruebaRelacionBancoCuentasAssertAll() {
        Cuenta cuenta1 = new Cuenta("Cliente 1", new BigDecimal(1000));
        Cuenta cuenta2 = new Cuenta("Cliente 2", new BigDecimal(1000));
        Banco banco = new Banco();
        banco.setNombre("Banco Estado");
        banco.addCuentas(cuenta1);
        banco.addCuentas(cuenta2);
        banco.transferir(cuenta1, cuenta2, new BigDecimal(500));

        /*Acá te pide como argumento ingresar objetos de la clase Executable, o una colección de los
          mismos, o implementar esto con expresiones lambda*/
        Assertions.assertAll(
                () -> Assertions.assertEquals("500", cuenta1.getSaldo().toPlainString()),
                () -> Assertions.assertEquals("1500", cuenta2.getSaldo().toPlainString()),
                () -> Assertions.assertEquals(2, banco.getCuentas().size()),
                () -> Assertions.assertEquals("Banco Estado", cuenta1.getBanco().getNombre()),
                () -> Assertions.assertEquals("Cliente 1", banco.getCuentas().stream()
                        .filter(c -> c.getCliente().equals("Cliente 1"))
                        .findFirst()
                        .get()
                        .getCliente()
                ),
                () -> Assertions.assertTrue(banco.getCuentas().stream()
                        .filter(c -> c.getCliente().equals("Cliente 2"))
                        .findFirst()
                        .isPresent()
                ));
    }

    //Agregando un parámetro final se puede personalizar el mensaje de error de la prueba
    @Test
    @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje personalizado")
    void pruebaNombreCuentaMensajePersonalizado() {
        String esperado = "Felipe Cuevas";
        String realidad = this.cuenta.getCliente();
        Assertions.assertEquals(esperado, realidad,"Se esperaba un cliente particular.");
    }

    //Dando el mensaje en expresión lambda el string se consume solo si efectivamente hay un error
    @Test
    @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje LAMBDA")
    void pruebaNombreCuentaMensajePersonalizadoLambda() {
        String esperado = "Felipe Cuevas";
        String realidad = this.cuenta.getCliente();
        Assertions.assertEquals(esperado, realidad,() -> "Se esperaba un cliente particular: " + esperado);
    }

    //Acá se aplica la anotación @RepeatedTest en vez de @Test
    @RepeatedTest(3)
    @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje personalizado REPETIR")
    void pruebaNombreCuentaMensajePersonalizadoRepetir(RepetitionInfo info) {
        String esperado = "Felipe Cuevas";
        String realidad = this.cuenta.getCliente();
        Assertions.assertEquals(esperado, realidad,"Se esperaba un cliente particular.");
        if(info.getCurrentRepetition() == 2) System.out.println("\tMensaje desde la repetición = " + info.getCurrentRepetition());
    }

    /*Etiqueta que se puede colocar a todo, al colocar a la clase interna se asigna a todos los métodos
    * y así se pueden ejecutar solamente aquellos que tengan un determinado tag editando la configuración de
    * ejecución en IntelliJ > JUnit5 > Build and Run, en vez de ejecutar la clase escogemos ejecutar por tags*/
    @Tag("sistema_operativo")
    //Clase interna para agrupar pruebas (tests), deben ir con la anotación @Nested
    @Nested
    class SOTest{
        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje LAMBDA - WINDOWS")
        void pruebaParaWindows(){
            String esperado = "Felipe Cuevas";
            String realidad = cuenta.getCliente();
            Assertions.assertEquals(esperado, realidad,() -> "Se esperaba un cliente particular: " + esperado);
        }

        //El siguiente test no se va a ejecutar porque estamos en Windows, o sea, es como si tuviera la anotación @Disabled
        @Test
        @EnabledOnOs(OS.LINUX)
        @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje LAMBDA - LINUX")
        void pruebaParaLinux(){
            String esperado = "Felipe Cuevas";
            String realidad = cuenta.getCliente();
            Assertions.assertEquals(esperado, realidad,() -> "Se esperaba un cliente particular: " + esperado);
        }

        //Acá se deshabilita la prueba si el SO tiene arquitectura de 32 bits
        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje LAMBDA - 64 BITS")
        void pruebaSoloParaArquitectura32Bits(){
            String esperado = "Felipe Cuevas";
            String realidad = cuenta.getCliente();
            Assertions.assertEquals(esperado, realidad,() -> "Se esperaba un cliente particular: " + esperado);
        }
    }

    @Tag("parametrizadas")
    @Nested
    class PruebasParametrizadas{

        //Acá se utiliza @ParameterizedTest y con @ValueSource se coloca un arreglo de strings que debe ser
        //declarado como parámetro del método de la prueba, JUnit5 inyectará el arreglo de strings al parámetro
        @ParameterizedTest
        @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje personalizado - VALUE SOURCE")
        @ValueSource(strings = {"100","200","300"})
        void pruebaValueSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            Assertions.assertNotNull(cuenta.getSaldo());
            Assertions.assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest
        @DisplayName("Prueba (test) para revisar el nombre de una cuenta con mensaje personalizado - CSV SOURCE")
        @CsvSource({"1,100","2,200","3,300"})
        void pruebaCsvSource(String indice, String monto) {
            System.out.println("Indice = " + indice + " | Monto = " + monto);
            cuenta.debito(new BigDecimal(monto));
            Assertions.assertNotNull(cuenta.getSaldo());
            Assertions.assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Nested
    @Tag("timeOut")
    class TimeOut{

        @Test
        @DisplayName("Prueba (test) para trabajar con TIME OUT")
        @Timeout(5)
        void pruebaTimeOut() throws InterruptedException{
            TimeUnit.SECONDS.sleep(4);
        }

        @Test
        @DisplayName("Prueba (test) para trabajar con TIME OUT - Parametrizado")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void pruebaTimeOutParametros() throws InterruptedException{
            TimeUnit.SECONDS.sleep(4);
        }

        @Test
        @DisplayName("Prueba (test) para trabajar con TIME OUT - Assertions")
        @Timeout(5)
        void pruebaTimeOutAssertions() throws InterruptedException{
            Assertions.assertTimeout(Duration.ofSeconds(5),()->{TimeUnit.SECONDS.sleep(4);});
        }
    }
}