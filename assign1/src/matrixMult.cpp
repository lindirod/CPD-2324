#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

void initializeMatrices(double *pha, double *phb, double *phc, int m_ar, int m_br)
{
	int i, j;
	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			phc[i*m_ar + j] = (double)0.0;
}
 
void OnMult(int m_ar, int m_br) 
{
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	initializeMatrices(pha, phb, phc, m_ar, m_br);


    Time1 = clock();

	for(i = 0; i < m_ar; i++)
		for(j = 0; j < m_br; j++)
			for(k = 0; k < m_ar; k++)
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	initializeMatrices(pha, phb, phc, m_ar, m_br);


    Time1 = clock();

	for(i = 0; i < m_ar; i++)
		for(k = 0; k < m_ar; k++)
			for(j = 0; j < m_br; j++)
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	int bki, bkj, bkk, i, j, k;

	double *pha, *phb, *phc;
	
		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	initializeMatrices(pha, phb, phc, m_ar, m_br);


    Time1 = clock();

	for (bki = 0; bki < m_ar; bki += bkSize)
		for (bkj = 0; bkj < m_br; bkj += bkSize)
			for (bkk = 0; bkk < m_ar; bkk += bkSize)
				for(i = bki; i < ((bki + bkSize) > m_ar ? m_ar : (bki + bkSize)); i++)
					for(k = bkk; k < ((bkk + bkSize) > m_ar ? m_ar : (bkk + bkSize)); k++)
						for(j = bkj; j < ((bkj + bkSize) > m_br ? m_br : (bkj + bkSize)); j++)
							phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLineParallelFirst(int m_ar, int m_br)
{
    double Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	initializeMatrices(pha, phb, phc, m_ar, m_br);


    Time1 = omp_get_wtime();

	#pragma omp parallel for
	for(i = 0; i < m_ar; i++)
		for(k = 0; k < m_ar; k++)
			for(j = 0; j < m_br; j++)
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

    Time2 = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", Time2 - Time1);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLineParallelSecond(int m_ar, int m_br)
{
    double Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	initializeMatrices(pha, phb, phc, m_ar, m_br);


    Time1 = omp_get_wtime();

	#pragma omp parallel private(i, k)
	for(i = 0; i < m_ar; i++)
		for(k = 0; k < m_ar; k++)
	#pragma omp for
			for(j = 0; j < m_br; j++)
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

    Time2 = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", Time2 - Time1);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Line Multiplication Parallel First" << endl;
		cout << "5. Line Multiplication Parallel Second" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, col, blockSize);  
				break;
			case 4:
				OnMultLineParallelFirst(lin, col);
				break;
			case 5:
				OnMultLineParallelSecond(lin, col);
			default:
				break;

		}

  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}
