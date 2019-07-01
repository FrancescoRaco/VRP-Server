# VRP-Server
Trasporto pubblico nella città di Roma: un approccio metaeuristico all'ottimizzazione delle rotte degli autobus

Istruzioni per importare il progetto server nell'ambiente di sviluppo:

- Creare un nuovo progetto Java denominato "VRP-Server"
- Scaricare il file .zip di questa Github repository ed importarlo nel progetto appena creato
- Assicurarsi che la cartella "src" sia "source folder" (da usare per i file sorgente) 
- Assicurarsi che "core", "server" e "test" (all'interno di "src") siano importati come package
- Scaricare il file .zip al seguente URL: https://drive.google.com/open?id=1i79DTvzXBMgfTH2_M1SY1fBmM63MOgjG
- Estrarre il precedente file .zip e inserire la cartella estratta "data" nella directory principale del progetto
- Includere tale cartella "data" nel classpath del progetto tramite le relative configurazioni build path
- Includere le librerie contenute nella cartella "VRP-Server_lib" nel module path tramite le stesse configurazioni build path del progetto
- Creare una libreria utente denominata "commons.math3" che includa il file .jar memorizzato nella cartella "CommonsMath_lib" del progetto ed aggiungerla al module path del progetto
- Utilizzare versioni JDK 11+ per assicurarsi che il programma funzioni
- Effettuare un cleaning del progetto
- Eseguire la classe "Server", all'interno del package "server" (percorso: server.Server)

Nota: è consigliato modificare le impostazioni di run del server.Server, aggiungendo il seguente argomento alla VM per incrementare la RAM massima assegnata al processo: "-Xmx2048m"

Non è stato possibile caricare tutti i file direttamente su questa repository a causa dei limiti di spazio
