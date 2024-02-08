package Es2.srcProp2;

import java.io.*;
import java.net.*;


public class MPutServerSeq {
	public static final int PORT = 55555; // porta default per server

	public static void main(String[] args) throws IOException {
		// Porta sulla quale ascolta il server
		int port = -1;

		/* controllo argomenti */
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out
					.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
				System.exit(1);
			}
		} //try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
				.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
			System.exit(1);
		}

		/* preparazione socket e in/out stream */
		ServerSocket serverSocket = null;
		try {
//			serverSocket = new ServerSocket(port,2);
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("MPutServerSeq: avviato ");
			System.out.println("Creata la server socket: " + serverSocket);
		}
		catch (Exception e) {
			System.err.println("Problemi nella creazione della server socket: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		try {
			//ciclo infinito server
			while (true) {
				Socket clientSocket = null;
				DataInputStream inSock = null;
				DataOutputStream outSock = null;
                String nomeFile;
				
				System.out.println("\nIn attesa di richieste...");
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(30000); //timeout altrimenti server sequenziale si sospende
					System.out.println("Connessione accettata: " + clientSocket + "\n");
				}
				catch (SocketTimeoutException te) {
					System.err
						.println("Non ho ricevuto nulla dal client per 30 sec., interrompo "
								+ "la comunicazione e accetto nuove richieste.");
					// il server continua a fornire il servizio ricominciando dall'inizio
					continue;
				}
				catch (Exception e) {
					System.err.println("Problemi nella accettazione della connessione: "
							+ e.getMessage());
					e.printStackTrace();
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo, se ci sono stati problemi
					continue;
				}

				//stream I/O e ricezione dimensione Max
				int sizeMax;
				try {
					inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
					sizeMax = Integer.parseInt(inSock.readUTF());
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
                catch(NumberFormatException e){
                    System.out.println("Dimensione non ricevuta correttamente");
                    e.printStackTrace();
                    clientSocket.close();
                    continue;
                }
				

				//elaborazione e comunicazione esito dimensione Max
                try {
                    //dimensione passata correttamente
                    outSock.writeUTF("ok");
                }
                catch (Exception e) {
                    System.out.println("Problemi nella notifica di file esistente: ");
                    e.printStackTrace();
                    continue;
                }
                
				try {
					nomeFile = inSock.readUTF();
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nel passaggio del nome del file "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
                
                //elaborazione e comunicazione esito
				String esito;
                FileOutputStream outFile = null;
				if (nomeFile == null) {
					System.out.println("Problemi nella ricezione del nome del file: ");
					clientSocket.close();
					continue;
				} else {
					File curFile = new File(nomeFile);
					// controllo su file
					if (curFile.exists()) {
						try {
							esito = "salta";
							// distruggo il file da sovrascrivere
							outSock.writeUTF(esito);
						}
						catch (Exception e) {
							System.out.println("Problemi nella notifica di file esistente: ");
							e.printStackTrace();
							continue;
						}
					} 
					else {
                        try {
							esito = "attiva";
							// distruggo il file da sovrascrivere
							outSock.writeUTF(esito);
                            outFile = new FileOutputStream(nomeFile);
						}
						catch (Exception e) {
							System.out.println("Problemi nella notifica di attiva: ");
							e.printStackTrace();
							continue;
						}
                    }
				}
        
                //ricezione dimensione file
                int size;
                String esitoDim = null;
                try {
					size = Integer.parseInt(inSock.readUTF());
                    if(size < sizeMax){
                        try {
							esitoDim = "salta";
							// distruggo il file da sovrascrivere
							outSock.writeUTF(esitoDim);
						}
						catch (Exception e) {
							System.out.println("Problemi nella notifica di file esistente: ");
							e.printStackTrace();
							continue;
						}
                    }
                    else {
                        try {
							esitoDim = "attiva";
							// distruggo il file da sovrascrivere
							outSock.writeUTF(esitoDim);
						}
						catch (Exception e) {
							System.out.println("Problemi nella notifica di attiva: ");
							e.printStackTrace();
							continue;
						}
                    }

		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
                catch(NumberFormatException e){
                    System.out.println("Dimensione non ricevuta correttamente");
                    e.printStackTrace();
                    clientSocket.close();
                    continue;
                }
				
				//ricezione file
				try {
					System.out.println("Ricevo il file " + nomeFile + ": \n");
					/**NOTA: la funzione consuma l'EOF*/
					// ciclo di lettura da sorgente e scrittura su destinazione
                    int buffer;    
                    try {
                        // esco dal ciclo all lettura di un valore negativo -> EOF
                        // N.B.: la funzione consuma l'EOF
                        while ((buffer=inSock.read()) >= 0) {
                            outFile.write(buffer);
                        }
                            outFile.flush();
                    }
                    catch (EOFException e) {
                        System.out.println("Problemi, i seguenti: ");
                        e.printStackTrace();
                    }
					System.out.println("\nRicezione del file " + nomeFile
							+ " terminata\n");
					outFile.close();				// chiusura file 
					clientSocket.shutdownInput();	//chiusura socket (downstream)
					// ritorno esito positivo al client
					outSock.writeUTF("File salvato");
					clientSocket.shutdownOutput();	//chiusura socket (upstream)
					System.out.println("\nTerminata connessione con " + clientSocket);
					clientSocket.close();
				}
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					continue;          
				}        
				catch (Exception e) {
					System.err
						.println("\nProblemi durante la ricezione e scrittura del file: "
								+ e.getMessage());
					e.printStackTrace();
					clientSocket.close();
					System.out.println("Terminata connessione con " + clientSocket);
					continue;
				}
			} // while (true)
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Errore irreversibile, MPutServerSeq: termino...");
			System.exit(3);
		}
	} // main
} // MPutServerSeq
