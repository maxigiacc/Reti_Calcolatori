package Es2.srcProp2;

// PutFileClient.java

import java.net.*;

import java.io.*;

public class MPutClient {

	public static void main(String[] args) throws IOException {
   
		InetAddress addr = null;
		int port = -1;
        int sizeMax = 0;
		
		try{ //check args
			if(args.length == 3){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
                sizeMax = Integer.parseInt(args[2]);
			} else{
				System.out.println("Usage: java PutFileClient serverAddr serverPort sizeMax");
				System.exit(1);
			}
		} //try
		// Per esercizio si possono dividere le diverse eccezioni
		catch(Exception e){
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java PutFileClient serverAddr serverPort sizeMax");
			System.exit(2);
		}
		
		// oggetti utilizzati dal client per la comunicazione e la lettura del file
		// locale
		Socket socket = null;
		FileInputStream inFile = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		String nomeFile = null;
        long size = 0;
        File fileCurr = null;

		// creazione stream di input da tastiera
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out
		    .print("MPutClient Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");

		try{
			while ( (nomeFile=stdIn.readLine()) != null){
                //se la dimensione massima è maggiore di zero creo la socket
                // se il file esiste creo la socket
                fileCurr = new File(nomeFile);
                size = fileCurr.length();
				if(fileCurr.exists()){
					// creazione socket
					try{
						socket = new Socket(addr, port);
						socket.setSoTimeout(30000);
						System.out.println("Creata la socket: " + socket);
					}
					catch(Exception e){
						System.out.println("Problemi nella creazione della socket: ");
						e.printStackTrace();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

					// creazione stream di input/output su socket
					try{
						inSock = new DataInputStream(socket.getInputStream());
						outSock = new DataOutputStream(socket.getOutputStream());
					}
					catch(IOException e){
						System.out
							.println("Problemi nella creazione degli stream su socket: ");
						e.printStackTrace();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}
				}
				// se la richiesta non � corretta non proseguo
				else{
					System.out.println("File non presente nel direttorio corrente");
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;
				}

                //invio dimensione massima
                try {
                    outSock.writeUTF("" + sizeMax);
                }
                catch(Exception e) {
                    System.out
		        		.println("Problemi nel passaggio del nome della dimensione massima");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
                }

                //ricezione esito dimensione massima
                String esitoDimMax = null;
                try {
                    esitoDimMax = inSock.readUTF();
                    if(!esitoDimMax.equalsIgnoreCase("ok")) {
                        System.out.println("Problemi nella comunicazione dell'esito dim max");
                        continue;
                    }
                    //invio nome File
                    else {
                        try {
                            outSock.writeUTF(nomeFile);
                        }
                        catch(Exception e) {
                            System.out.println("Problemi nella comunicazione del nome del file " + nomeFile);
                            e.printStackTrace();
                            continue;
                        }
                        
                    } 
                }
                catch(Exception e) {
                    System.out.println("Problemi comunicazione esito dimensione massima");
                    continue;
                }

                //ricezione esito nome file
                String esitoNome = null;
                try {
                    esitoNome = inSock.readUTF();
                    if(esitoNome.equalsIgnoreCase("salta")) {
                        System.out.println("File saltato");
                        continue;
                    }
                    //invio nome File
                    else {
                        if(esitoNome.equalsIgnoreCase("attiva")) {
                            //invio dimensione del file
                            try {
                                outSock.writeUTF(""+ size);
                            }
                            catch(Exception e) {
                                System.out.println("Problemi nella comunicazione della dimensione del file " + nomeFile);
                                e.printStackTrace();
                                continue;
                            }
                        }
                    } 
                }
                catch(Exception e) {
                    System.out.println("Problemi comunicazione esito nome file");
                    continue;
                }
				
                //ricezione esito dimensione del file
                String esitoDim = null;
                try {
                    esitoDim = inSock.readUTF();
                    if(!esitoDim.equalsIgnoreCase("ok")) {
                        System.out.println("File troppo piccolo");
                        continue;
                    }
                    //invio nome File
                    else {
                        if(esitoNome.equalsIgnoreCase("attiva")) {
                            //invio dimensione del file
                            try {
                                outSock.writeUTF(""+ size);
                            }
                            catch(Exception e) {
                                System.out.println("Problemi nella comunicazione della dimensione del file " + nomeFile);
                                e.printStackTrace();
                                continue;
                            }
                        }
                    } 
                }
                catch(Exception e) {
                    System.out.println("Problemi comunicazione esito nome file");
                    continue;
                }

                System.out.println("Inizio la trasmissione di " + nomeFile);

				/* Invio file richiesto e attesa esito dal server */
				// creazione stream di input da file
				try{
					inFile = new FileInputStream(nomeFile);
				}
				/*
				 * abbiamo gia' verificato che esiste, a meno di inconvenienti, es.
				 * cancellazione concorrente del file da parte di un altro processo, non
				 * dovremmo mai incorrere in questa eccezione.
				 */
				catch(FileNotFoundException e){
					System.out
						.println("Problemi nella creazione dello stream di input da "
								+ nomeFile + ": ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;
				}
				
				
				// trasferimento file
				try{
					// ciclo di lettura da sorgente e scrittura su destinazione
                    int buffer = 0;   
                    DataInputStream src = new DataInputStream(inFile); 
                    try {
                        // esco dal ciclo all lettura di un valore negativo -> EOF
                        // N.B.: la funzione consuma l'EOF
                        while ((buffer=src.read()) >= 0) {
                            outSock.write(buffer);
                        }
                        outSock.flush();
                    }
                    catch (EOFException e) {
                        System.out.println("Problemi, i seguenti: ");
                        e.printStackTrace();
                    }
                    inFile.close(); 			// chiusura file
					socket.shutdownOutput(); 	// chiusura socket in upstream, invio l'EOF al server
					System.out.println("Trasmissione di " + nomeFile + " terminata ");
				}
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					socket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch(Exception e){
					System.out.println("Problemi nell'invio di " + nomeFile + ": ");
					e.printStackTrace();
					socket.close();
					System.out
				      	.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;
				}
				
				// ricezione esito
				String esito;
				try{
					esito = inSock.readUTF();
					System.out.println("Esito trasmissione: " + esito);
					// chiudo la socket in downstream
					socket.shutdownInput();
					System.out.println("Terminata la chiusura della socket: " + socket);
				}
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					socket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch(Exception e){
					System.out
						.println("Problemi nella ricezione dell'esito, i seguenti: ");
					e.printStackTrace();
					socket.close();
					System.out
				      	.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
				// tutto ok, pronto per nuova richiesta
				System.out
				    .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
				
			}
			socket.close();
			System.out.println("PutFileClient: termino...");
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// quali per esempio la caduta della connessione con il server
		// in seguito alle quali il client termina l'esecuzione
		catch(Exception e){
			System.err.println("Errore irreversibile, il seguente: ");
			e.printStackTrace();
			System.err.println("Chiudo!");
			System.exit(3); 
	    }
	} // main
} // MPutClient
