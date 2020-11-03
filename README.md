# SudokuADC - Progetto di Architetture Distribuite per il Cloud

Studente: **Piccolo Gianluca**

Matricola: **0522500596**

Esame: **Architetture Distribuite per il Cloud - A.A. 2019-20**

Docenti: **Alberto Negro, Gennaro Cordasco e Carmine Spagnuolo**

Dipartimento di Informatica - Università degli Studi di Salerno




## Descrizione del progetto

In questo progetto è stato utilizzato OpenMPI per l'implementazione di "Game of Life".  
Si tratta di un gioco nel quale è presente una griglia bidimensionale di celle, dove ogni cella ha due possibili stati: viva oppure morta.  
Ogni cella interagisce con i suoi 8 vicini adiacenti (orizzontalmente, verticalmente e diagonalmente).  

Ad ogni generazione nel tempo, lo stato di una cella cambia secondo le seguenti regole:
- una cella viva con meno di 2 vicini vivi, muore per sottopopolazione
- una cella viva con 2 o 3 vicini vivi, vive per la prossima generazione
- una cella viva con più di 3 vicini vivi, muore per sovrappopolazione
- una cella morta con esattamente 3 vicini vivi, rinasce nella prossima generazione




## Descrizione dell'algoritmo

L'algoritmo è composto dai seguenti passi:
1.	Il master crea una griglia casuale di M righe ed N colonne
2.	Il master invia ad ogni processore la sottomatrice per la quale calcolare la prossima generazione 
3.	Il master e gli slave calcolano la sottomatrice della generazione successiva  
Ogni processore deve calcolare M/numProcs righe ed N colonne della prossima generazione
4.	Gli slave inviano al master le sottomatrici aggiornate
5.	Il master riceve le sottomatrici aggiornate e si occupa quindi di aggiornare la griglia
6.	Il procedimento viene ripetuto per il numero di generazioni GEN 

NOTA: Vengono gestiti i casi in cui M % numProcs != 0




## Dettagli di implementazione

### Funzionalità MPI

In questo progetto sono state utilizzate le seguenti funzionalità MPI:
-	MPI_Send
-	MPI_Recv



### Funzionamento dell'algoritmo

L'implementazione prevede una gestione toroidale della matrice.  
Ciò significa che la riga precedente alla prima riga coincide con l'ultima e la riga successiva all'ultima coincide con la prima riga.  
Questo avviene anche per le colonne, la colonna precedente alla prima colonna coincide con l'ultima e la colonna successiva all'ultima coincide con la prima colonna.

Il processore i, per calcolare correttamente la sottomatrice della prossima generazione, ha bisogno delle due righe agli estremi in modo da calcolare tutti i vicini.  


In questo caso:
- p0 deve calcolare le righe r0, r1, r2  ed allora ha bisogno anche di r11 (per i vicini in alto nella riga r0) e r3 (per i vicini in basso nella riga r2)
- p1 deve calcolare le righe r3, r4, r5 ed allora ha bisogno di r2 (per i vicini in alto di r3) e di r6 (per i vicini in basso di r5)
- p2 deve calcolare le righe r6, r7, r8 ed allora ha bisogno di r5 (per i vicini in alto di r6) e di r9 (per i vicini in basso di r8)
- p3 deve calcolare le righe r9, r10, r11 ed allora ha bisogno di r8 (per i vicini in alto di r9) e di r0 (per i vici in basso di r11)

NOTA:  
Nel caso in cui M % numProcs != 0, sia rem il resto della divisione   
i primi rem processori gestiscono una riga in più



### Correttezza dell'algoritmo
Ecco alcuni esempi per verificare la correttezza dell'algoritmo, presenti anche all'interno dell'implementazione:


## Test

I test sono stati eseguiti su un cluster Amazon AWS EC2, composto da 8 istanze di tipo t2.large.  
Ogni istanza ha 2 vCPU, quindi i test sono stati eseguiti aumentando gradualmente sia il numero di processori che il numero dello istanze.  
In particolare, i test cominciano con 1 istanza con 1 processore fino ad arrivare ad 8 istanze con 16 processori.


### Strong Scalability

Dimensione del problema:
- numRighe: 32000
- numColonne: 8000
- numGenerazioni: 5

Risultati:


Passando dall’algoritmo sequenziale all'algoritmo parallelo con 2 processori, il tempo di esecuzione viene ridotto di circa il 30%.  
Non si tratta di una riduzione ottimale in quanto non si arriva al dimezzamento del tempo di esecuzione. Ciò probabilmente è dovuto all'overhead generato dalla comunicazione di un elevato numero di righe.  

Eseguendo il programma con **8 processori** il tempo viene diminuito del 50% rispetto all'algoritmo sequenziale, segno che il calcolo delle sottomatrici di ogni processore, riesce a bilanciare il tempo generato dall'overhead di comunicazione.  

In generale, con l'utilizzo di **16 processori** il tempo viene ridotto di circa il 60% rispetto all'algoritmo sequenziale. Si passa infatti da **165.24 sec** a **61.69 sec**




### Weak Scalability

Risultati:

A parità di numero di generazioni, si può notare che al crescere della dimensione del problema, il programma non risulta propriamente efficiente. I tempi di esecuzione infatti crescono sensibilmente, nonostante l'aumento del numero di processori.  
Questo può essere dovuto al maggior overhead delle comunicazioni per via dell'elevato numero di righe che ogni processore deve inviare e ricevere.



## Istruzioni di compilazione ed esecuzione

### Esecuzione locale -- Sequenziale

Eseguire i seguenti comandi:

    gcc seq.c -o seq
    ./seq <numRighe> <numColonne> <numGenerazioni>

Es.  

    ./seq 1000 500 100
     
     
- **numRighe**: numero delle righe della matrice
- **numColonne**: numero delle colonne della matrice
- **numGenerazioni**: numero delle generazioni da calcolare




### Esecuzione locale -- Parallela

Eseguire i seguenti comandi:

    mpicc par.c -o par
    mpirun -np <numProcs> ./par <numRighe> <numColonne> <numGenerazioni>

Es.

    mpirun -np 2 ./par 5000 2000 20


- **numProcs**: numero dei processori che devono lavorare
- **numRighe**: numero delle righe della matrice
- **numColonne**: numero delle colonne della matrice
- **numGenerazioni**: numero delle generazioni da calcolare



### Esecuzione su Cluster AWS	

Una volta lanciate le istanze su AWS, bisogna creare un machinefile:

    nano machinefile

All'interno del machinefile bisogna scrivere gli ip privati delle istanze che devono lavorare, seguiti dal numero degli slots (vCPU) che ognuna deve utilizzare:
    
    localhost slots=2
    IP_PRIVATO_IST1 slots=2
    IP_PRIVATO_IST2 slots=2
    ...
    IP_PRIVATO_ISTN slots=2


Per lanciare il programma, dal master digitare:

    mpirun -np <numProcs> --hostfile machinefile ./par <numRighe> <numColonne> <numGenerazioni>
