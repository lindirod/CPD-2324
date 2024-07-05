import java.util.Scanner;

public class matrixMult{
    public static void initializeMatrices(double[] pha, double[] phb, double[] phc, int m_ar, int m_br){
        int i, j;
        for(i=0; i<m_ar; i++)
		    for(j=0; j<m_ar; j++)
			    pha[i*m_ar + j] = 1.0;

	    for(i=0; i<m_br; i++)
		    for(j=0; j<m_br; j++)
			    phb[i*m_br + j] = i+1;

	    for(i=0; i<m_ar; i++)
		    for(j=0; j<m_ar; j++)
			    phc[i*m_ar + j] = 0.0;

    }
    
    public static double OnMult(int m_ar, int m_br){
        double[] pha, phb, phc;
        int i, j, k;
        long Time1, Time2;
        double duration;

        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];

        initializeMatrices(pha, phb, phc, m_ar, m_br);

        Time1 = System.currentTimeMillis();

        for(i = 0; i < m_ar; i++)
		    for(j = 0; j < m_br; j++)
			    for(k = 0; k < m_ar; k++)
				    phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

        Time2 = System.currentTimeMillis();

        duration = (Time2 - Time1) / 1000.0;
        System.out.printf("Time: %.3f seconds%n%n", duration);

        for(i=0; i<1; i++)
		    for(j=0; j<Math.min(10,m_br); j++)
			    System.out.printf("%.2f ", phc[j]);
        
        return duration;
    }

    public static double OnMultLine(int m_ar, int m_br){
        double[] pha, phb, phc;
        int i, j, k;
        long Time1, Time2;
        double duration;

        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];

        initializeMatrices(pha, phb, phc, m_ar, m_br);

        Time1 = System.currentTimeMillis();

        for(i = 0; i < m_ar; i++)
			for(k = 0; k < m_ar; k++)
		        for(j = 0; j < m_br; j++)
				    phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

        Time2 = System.currentTimeMillis();

        duration = (Time2 - Time1) / 1000.0;
        System.out.printf("Time: %.3f seconds%n%n", duration);

        for(i=0; i<1; i++)
		    for(j=0; j<Math.min(10,m_br); j++)
			    System.out.printf("%.2f ", phc[j]);
        
        return duration;
    }
        
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int op, size;
        double duration;

        while (true) {
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("Selection?: ");
            op = scanner.nextInt();
            if (op == 0)
                break;

            System.out.println("Enter size of matrices: ");
            size = scanner.nextInt();

            if (op == 1) {
                duration = matrixMult.OnMult(size, size);
                System.out.println("\n");
            } else if (op == 2) {
                duration = matrixMult.OnMultLine(size, size);
                System.out.println("\n");
            } else {
                System.out.println("Invalid option. Please try again.\n");
            }
        }

        scanner.close();
    }
    
}