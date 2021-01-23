import java.io.*;  
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    // Static Variables
    public static ArrayList<String> textFile  = new ArrayList<>();
    public static int version = -1;
    public static Socket s;
    public static DataOutputStream dout;
    public static BufferedReader rd;

    // Methods
	public static void send(String command, String args) throws Exception{
		String temp;
		if(args.equals(""))
			temp = command + "\r\n";
		else
			temp = command + " " + args + "\r\n";
		dout.writeBytes(temp);
		dout.flush();
	}

    public static String[] getResponse() throws Exception{
        String [] response = new String[3];
        String msg = "";
        char ch; 
        int temp;

        temp = rd.read();
        int i = 0;
        while(true){
            ch = (char)temp;
            if ((ch != ' ') && (ch != '\r')){
                msg += ch;
            } else if (ch == ' ') {
                if(i != 2){
                    response[i] = msg;
                    i++;
                    msg = "";
                } else {
                    msg += ch;
                }
            } else if (ch == '\r') {
                response[i] = msg;
                rd.read();
                break;
            }
            temp = rd.read();
        }
        return response;
    }


    public static boolean auth(String user, String pass) throws Exception{
        send("USER", user);
        String userResp = (getResponse())[0];
        if(!userResp.equals("OK"))
            return false;
        send("PASS", pass);
        String passResp = (getResponse())[0];
        return passResp.equals("OK");
    }

    public static void write(String text, String line_number) throws Exception{
		update();
		int ln = Integer.parseInt(line_number);
		if(ln <= textFile.size() && ln > 0){
			String temp = version + " " + (ln - 1) + " " + text;
			send("WRTE", temp);
			String[] responseArray = getResponse();
			String responseMessage = responseArray[0];
			if (!responseMessage.equals("OK"))
				System.out.println(responseArray[1] + " " + responseArray[2]);
			else {
				++version;
				textFile.set(ln - 1, text);
			}
		} else {
			System.out.println("Line number needs to be smaller than file size (" + textFile.size() + ")");
		}
	}

	public static void append(String text) throws Exception {
		update();
		String temp = version + " " + text;
		send("APND", temp);
		String[] responseArray = getResponse();
		String responseMessage = responseArray[0];
		if (!responseMessage.equals("OK")) {
			System.out.println(responseArray[1] + " " + responseArray[2]);
		} else {
			++version;
			textFile.add(text);
		}
	}
    
    public static void update() throws Exception {
        send("UPDT", version + "");
        String[] response = getResponse();
        int server_version = Integer.parseInt(response[1]);
        if(response[0].equals("OK")){
            textFile = new ArrayList<>();
            if (!response[2].equals("")){
                String[] text2write = response[2].split("\n");
                for (int i = 0; i < text2write.length; i++){
                    textFile.add(text2write[i]);
                }
            }
            version = server_version;
        }
    }
    public static  void closeConnection() throws Exception{
        s.close();
        dout.close();
        rd.close();
    }
    
    public static void main(String[] args){
		final String menu = "1) Write to the text file on the server.\n" +
 							"2) Append to the text file on the server.\n" +	
							"3) Read the last version of the text file.\n" +
							"4) Exit.\n";
		Scanner scan = new Scanner(System.in);
		int user_input = 0;
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		try {
			s = new Socket(ip, port);
			dout = new DataOutputStream(s.getOutputStream());
            rd = new BufferedReader(new InputStreamReader(s.getInputStream(), "US-ASCII"));
            System.out.print("Please enter your username: ");
            String username = scan.nextLine();
            System.out.print("Please enter your password: ");
            String password = scan.nextLine();
            boolean check = auth(username, password);
            while(!check){
                s = new Socket(ip, port);
                dout = new DataOutputStream(s.getOutputStream());
                rd = new BufferedReader(new InputStreamReader(s.getInputStream(), "US-ASCII"));
                System.out.println("Connection refused. Wrong username or password.");
                System.out.print("Please enter your username: ");
                username = scan.nextLine();
                System.out.print("Please enter your password: ");
                password = scan.nextLine();
                check = auth(username, password);
            }
            
            System.out.println("Connection established with the server.");
            while(true) {
                System.out.println(menu);
                System.out.print("Enter the selection:");
                try {
                    user_input = Integer.parseInt(scan.nextLine());
                } catch (Exception e) {
                    System.out.println("You should enter a valid selection.");    
                    continue;
                }
                if(user_input == 1){
                    if (textFile.size() != 0){
                        System.out.print("Please enter text to write: ");
                        String text = scan.nextLine();
                        System.out.print("Please enter line number: ");
                        String line = scan.nextLine();
                        int ln;
                        try {
                            ln = Integer.parseInt(line);
                        } catch (Exception e){
                            ln = -1;
                        }
                        while (ln > textFile.size() || ln <= 0 ){
                            System.out.print("Please enter an valid line number (1-" + textFile.size() + "): ");
                            line = scan.nextLine();
                            try {
                                ln = Integer.parseInt(line);
                            } catch (Exception e){
                                ln = -1;
                            }
                        }
                        write(text, line);
                    } else {
                        System.out.println("Textfile is empty. You should append new lines.");
                    }{}
                } else if (user_input == 2){
                    System.out.print("Please enter text to append: ");
                    String text = scan.nextLine();
                    append(text);
                } else if (user_input == 3){
                    update();
                    if (textFile.size() != 0){
                        System.out.println("Textfile:");
                        for(int i = 0; i < textFile.size(); i++){
                            System.out.println((i+1) + " - " + textFile.get(i));
                        }
                    } else {
                        System.out.println("Textfile is empty.");
                    }
                } else if (user_input == 4){
                    break;
                } else {
                    System.out.println("Invalid selection. Try again.");		
            send("EXIT", "");
            closeConnection();
		} catch(Exception e){System.out.println(e);}
		scan.close();
	}
}
