import java.util.Random;
import java.util.Scanner;

public class Simulacion_TP6 {

        // Constantes
        private static int MAX_CLIENTES = 50;
        private static int MAX_EMPLEADOS = 10;
        private static int HV_INT = 32767;
        private static Double HV = 9999999999999.0;

        // Variables de tiempo
        private Double t;
        private static Double tf = 57600.0; //160hs al mes por 6 meses (60*160*6)

        //Datos:
        private int ia;
        private int ta;

        //Variables de control:
        private int cc;
        private int ce;

        //Variables de estado
        private Double[] nsc = new Double[MAX_CLIENTES];

        //Variables de resultados
        private Double[] pto = new Double[MAX_EMPLEADOS];
        private Double[] pps = new Double[MAX_CLIENTES];

        //Eventos futuros
        private Double tpll;
        private Double[] tps = new Double[MAX_EMPLEADOS];

        //Variables auxiliares
        private int menorTPS;

        private Double[] ito = new Double[MAX_EMPLEADOS];
        private Double[] sto = new Double[MAX_EMPLEADOS];

        private Double[] stll = new Double[MAX_CLIENTES];
        private Double[] sts = new Double[MAX_CLIENTES];
        private Double[] nti = new Double[MAX_CLIENTES];

        // Array para saber a qué cliente está atendiendo el empleado
        private int[] empleadoCliente = new int[MAX_EMPLEADOS];

    public static void main(String[] args) {
        Simulacion_TP6 simulacion = new Simulacion_TP6();
        simulacion.Simular();
    }

    private void Simular(){

        condicionesIniciales();

        while (t < tf){
            menorTPS = buscarMenorTPS();

            if (tps[menorTPS] < tpll)
                salida();                   
            else
                llegada();                                
        }

        while(!sistemaVacio()){
            tpll = HV;
            menorTPS = buscarMenorTPS();
            salida();
        }

        impresionDeResultados();
    }

    private void condicionesIniciales(){
        t = 0.0;
        tpll = 0.0;

        Scanner term = new Scanner(System.in);
        System.out.println("Ingrese cantidad de Empleados: ");
        ce = term.nextInt();

        System.out.println("Ingrese cantidad de Clientes: ");
        cc = term.nextInt();
        term.close();

        for (int i = 0; i < cc; i++){
            nsc[i] = 0.0;
            stll[i] = 0.0;
            sts[i] = 0.0;
            nti[i] = 0.0;
        }
            

        for (int i = 0; i < ce; i++){
            empleadoCliente[i] = HV_INT;
            tps[i] = HV;
            ito[i] = 0.0;
            sto[i] = 0.0;
        }
    }

    private void salida()
    {
        t = tps[menorTPS];

        int clienteAtendido = buscarClienteAtendidoPor(menorTPS); 
        nsc[clienteAtendido] -= 1;

        sts[clienteAtendido] += t;

        if (nsc[clienteAtendido] >= 1)
        {
            ta = tiempoDeAtencion();
            tps[menorTPS] = t + ta;
        }
        else
        {
            empleadoCliente[menorTPS] = buscarProximoCliente();
            if(empleadoCliente[menorTPS] != HV_INT)
            {
                ta = tiempoDeAtencion();
                tps[menorTPS] = t + ta;
            }
            else
            {
                ito[menorTPS] = t;
                tps[menorTPS] = HV;
            }
        }
    }

    private void llegada()
    {
        t = tpll;
        ia = intervaloEntreArribos();
        tpll = t + ia;
        int clienteLlegada = obtenerClienteLlegada();
        int empleadoActual = obtenerEmpleadoCliente(clienteLlegada);
        
        nsc[clienteLlegada] += 1;

        stll[clienteLlegada] += t;
        nti[clienteLlegada] += 1;

        // Lo atiende si es el primer comprobante, y el empleado está libre
        if (empleadoActual != HV_INT)
            if (nsc[clienteLlegada] == 1 && tps[empleadoActual] == HV)
            {
                sto[empleadoActual] += (t - ito[empleadoActual]);
                ta = tiempoDeAtencion();
                tps[empleadoActual] = t + ta;
            }
    }

    private int buscarMenorTPS()
    {
        int menor = 0;

        for (int i = 0; i < ce; i++)
        {
            if (tps[i] < tps[menor])
                menor = i;
        }

        return menor;
    }

    private int intervaloEntreArribos(){

        int iac, x1;
        double y1, r1, r2, m, fx1 = 1.0;

        m = 1/480; //Valor máximo de f(x)

        Random rndm = new Random();

        do{

            r1 = rndm.nextDouble();
            r2 = rndm.nextDouble();
    
            x1 = (int) (9504*r1) + 96;
            y1 = r2 * m;    

            if (x1 >= 96 && x1 < 192)
                fx1 = 1/480;
            else if(x1 >= 192 && x1 < 480)
                fx1 = 1/960;
            else if(x1 >= 480 && x1 <= 9600)
                fx1 = (-0.0000000120 * x1) + 0.0001154;

        }while(fx1 > y1);

        iac = (int) x1 / cc;
        return iac;

    }

    private int tiempoDeAtencion(){

        int ta, x1;
        double y1, r1, r2, m, fx1 = 1.0;

        m = 0.2; //Valor máximo de f(x)

        Random rndm = new Random();

        do{

            r1 = rndm.nextDouble();
            r2 = rndm.nextDouble();

            x1 = (int) (10*r1) + 5;
            y1 = r2 * m;

            switch(x1){
                case 5:
                case 6:
                    fx1 = (2/25) * x1 - (9/25);
                    break;
                case 7:
                case 8:
                case 9:
                    fx1 = (-4/75) * x1 + (43/75);
                    break;
                case 10:
                case 11:
                    fx1 = (1/25) * x1 - (9/25);
                    break;
                case 12:
                case 13:
                case 14:
                case 15:
                    fx1 = (-2/75) * x1 + (11/25);
                    break;
            }
        }while(fx1 > y1);

    ta = x1;
    return ta;

    }

    // Para obtener el cliente al cual llega un nuevo comprobante
    private int obtenerClienteLlegada(){
        Random random = new Random();
        Double r = random.nextDouble();

        int ret = (int)(r*cc);
        //Console.WriteLine("Random generado " + ret);
        return ret;
    }

    // Para obtener el empleado que está atendiendo a ese cliente
    private int obtenerEmpleadoCliente(int cliente){
        int empleadoActual = HV_INT;

        for (int i = 0; i < ce; i++){
            if (empleadoCliente[i] == cliente)
                empleadoActual = i;

            // Retengo al primer libre para el caso que todos esten libres
            if (empleadoCliente[i] == HV_INT && empleadoActual == HV_INT)
                empleadoActual = i;
        }

        // Está atendiendo a este cliente
        if(empleadoActual != HV_INT)
            empleadoCliente[empleadoActual] = cliente;  

        return empleadoActual;
    }

    private int buscarProximoCliente(){
        boolean atendido = false;

        for(int i = 0; i < cc; i++){
            if(nsc[i] > 0){
                atendido = false;
                for(int j = 0; j < ce; j++){
                    if (empleadoCliente[j] == i)
                        atendido = true;
                }

                if (!atendido)
                    return i;
            }
        }

        return HV_INT;
    }

    private int buscarClienteAtendidoPor(int empleado){
        return empleadoCliente[empleado];
    }

    private boolean sistemaVacio(){
        for(int i = 0; i < cc; i++){
            if (nsc[i] > 0)
                return false;
        }

        return true;
    }

    private void impresionDeResultados(){
        Double pte = 0.0, spps = 0.0;

        for (int i = 0; i < ce; i++)
            pto[i] = (sto[i] * 100 / t);

        for (int i = 0; i < cc; i++){
            pps[i] = (sts[i] - stll[i]) / nti[i];
            spps += pps[i];
        }

        pte = spps / cc;

        for (int i = 0; i < ce; i++){
            int emp = i + 1;
            System.out.println("El porcentaje de tiempo ocioso del empleado " + emp + " es de " + pto[i] + "%");
        }

        for (int i = 0; i < cc; i++){
            int cli = i + 1;
            System.out.println("El promedio de tiempo en sistema de comprobantes del cliente " + cli + " es de " + pps[i]);
        }

        System.out.println("El promedio de permanencia de cada comprobante en minutos es de: " + pte);
    }

}