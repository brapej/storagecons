package edu.raf.sofkom.console;

import edu.raf.sofkom.FileStorage;
import edu.raf.sofkom.StorageFactory;
import edu.raf.sofkom.UnsuportedTypeException;
import edu.raf.sofkom.connect.ConnectionUtils;
import edu.raf.sofkom.privileges.Privilege;
import edu.raf.sofkom.privileges.PrivilegeException;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;


public class StorageConsoleMain {



    public static void main(String[] args)  {

       FileStorage fs = null;
       //fs = (LocalStorage) Class.forName("edu.raf.sofkom.localstorage.LocalStorage").getDeclaredConstructor().newInstance();
        try {
            fs = StorageFactory.getStorage("edu.raf.sofkom.localstorage.LocalStorage");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            System.err.println("Internal error: Loading failed.");
            System.exit(0);
        }

        //   fs = fs.objCreator();//(FileStorage) Class.forName("edu.raf.sofkom.LocalStorage").getDeclaredConstructor().newInstance();


        Scanner sc = new Scanner(System.in);



        if(args[0].equals("-i")){
            System.out.println(args[0] + args[1]);
            try {
                fs.init(args[1],args[2]);
                System.out.println("Storage initialized @ "+ fs.getPathToStorage());
            } catch (IOException e) {
               System.err.println("Was unable to initialize storage location.");
               System.exit(0);
            }

           System.out.println("Username:");
           String uName = sc.nextLine();
           System.out.println("Password:");
           String password = sc.nextLine();



            fs.setStorageUsers(fs.getStorageUsers().init(uName,password));
        }

        if(args[0].equals("-c")){
            System.out.println("Username:");
            String uName = sc.nextLine();
            System.out.println("Password:");
            String password = sc.nextLine();
            try {
                if(!ConnectionUtils.connect(args[1],uName, password, fs)) {
                    System.err.println("Storage does not exist at specified location.");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Connection error");
            }
        }



        String menu =   "\nCommands:\n_________\n\n"+
                "str <path to> <path from> - Store\n" +
                "ret <path from> - Retrieve\n"+
                "del <path toDelete> - Delete\n"+
                "dcn - Disconnect\n"+
                "strm <path to> <path from> - Store file with meta\n"+
                "peekm <path to> - View file meta\n";

        String suMenu = menu+"\nSuperCommands:\n_________\n\n"+
                "adduser <username> <password>- Add user\n"+
                "userinfo <username> - See user information"+
                "permit <privilege> <username> - Permit to user(Set privileges)\n"+
                "forbid <privilege <username> - Forbid to user(Un-set privileges)\n"+
                "deluser <username> - Delete User\n"+
                "restype <extension> - Restrict filetype."+
                "unrestype <extension> - Restrict filetype.";


        if(fs.getStorageUsers().ifSuperUser())
            listChoices(suMenu);
        else
            listChoices(menu);

        String choice;
        do{
            System.out.println("\n\nChoice (help to see options):");
            choice = sc.nextLine();

            StringTokenizer st = new StringTokenizer(choice," ",false);
            String[] arg = new String[3];

            for(int i = 0;st.hasMoreTokens();i++){
                arg[i] = st.nextToken();
            }
            System.out.println(arg[0] + arg[1]+arg[2]+fs.getStorageUsers().ifSuperUser());



            if(fs.getStorageUsers().ifSuperUser())
               fs = superExec(fs,arg[0],arg[1],arg[2]);
            else
                fs = exec(fs,arg[0],arg[1],arg[2]);

           /* if(choice.equals("help")) {
                if (fs.getStorageUsers().ifSuperUser()) {
                    listChoices(suMenu);
                } else
                    listChoices(menu);
            }

            if(choice.equals("dcn")) {
                ConnectionUtils.disconnect(fs);
            }

            if(fs.getStorageUsers().ifSuperUser()){

                if(choice.startsWith("adduser")){
                    String tok = choice.substring(8);
                    String uname = tok.substring(0,tok.indexOf(' '));
                    String passw = tok.substring(tok.indexOf(' ')+1);

                    if(fs.getStorageUsers().addUser(uname,passw))
                        System.out.println("Successful addition of "+uname);
                    else
                        System.out.println("User "+uname+"could not be added.");
                }
                if(choice.startsWith("userinfo")){
                    String tok = choice.substring(9);
                    System.out.println(fs.getUserString(tok));
                }

                if(choice.startsWith("permit")){
                    String tok = choice.substring(7);
                    String privilege = tok.substring(0,tok.indexOf(' '));
                    String username = tok.substring(tok.indexOf(' ')+1);

                    Privilege p = privilege(privilege);

                    if(p!=Privilege.BAD){
                    fs.getStorageUsers().addUserPrivilege(username,privilege(privilege));
                    }

                }
                if(choice.startsWith("forbid")){
                    System.out.println("forbid");
                }
                if(choice.startsWith("deluser")){
                    String tok = choice.substring(8);
                    if(fs.getStorageUsers().removeUser(tok))
                        System.out.println("Successful deletion of "+tok);
                    else{
                        System.err.println("No user named:"+tok);
                    }
                }
                if(choice.startsWith("restype")){
                    String tok = choice.substring(8);
                    fs.addFiletypeRestriction(tok);

                }
            }*/


        }while(!choice.equals("dcn"));


    }

    private static Privilege privilege(String privilege) {
        if(privilege.equals("str")){
            return Privilege.S;
        }
        if(privilege.equals("ret")){
            return Privilege.R;
        }
        if(privilege.equals("del")){
            return Privilege.D;
        }
        else{
            System.err.println("Privilege not found: "+privilege);
            System.err.println("Privileges: str,ret,del");
            return Privilege.BAD;
        }
    }

    private static void listChoices(String menu){
        System.out.println(menu);
    }

    private static FileStorage exec(FileStorage fs,String command,String... args) {

        System.out.println(command+"\n"+args.toString());

        String menu = "\nCommands:\n_________\n\n" +
                "str <path to> <path from> - Store\n" +
                "ret <path from> - Retrieve\n" +
                "del <path toDelete> - Delete\n" +
                "dcn - Disconnect\n"+
                "strm <path to> <path from> - Store file with meta\n"+
                "peekm <path to> - View file meta\n";;


        System.out.println("***"+args[args.length-1] + " " +args[0]+"***");
        try {
            if (command.equals("str")) {
                for (int i = 0; i < args.length-1; i++) {
                    if(args[args.length-1] == null)
                        args[args.length-1] = "";
                    fs.store(args[args.length-1], args[i]);
                }
            }
            if(command.equals("strm")){
                HashMap<String,String> meta = new HashMap<>();
                boolean more=true;
                Scanner sc = new Scanner(System.in);
                String key,value;
                while(more){
                    System.out.println("key:");
                    key = sc.nextLine();
                    System.out.println("value:");
                    value=sc.nextLine();
                    meta.put(key,value);
                    System.out.println(meta.toString());

                    System.out.println("More (y/n)?");
                    key=sc.nextLine();
                    if(key.trim().equals("y"))
                        continue;
                    else
                        break;
                }

                if(args[1] == null)
                    args[1] = "";

                fs.store(args[1],args[0],meta);
            }
            if(command.equals("peekm")){
                String metastr = fs.readFileMeta(args[0]);
                System.out.println(metastr);
            }
            if (command.equals("ret")) {
                fs.retrieve(args[0]);
            }
            if (command.equals("del")) {
                fs.delete(args[0]);
            }
        } catch (PrivilegeException | UnsuportedTypeException e) {
            System.err.println("Forbidden due to the lack of privileges.");
        } catch (IOException e) {
            System.out.println("IOEX");
        }

        try {
            if (command.equals("dcn")) {

                ConnectionUtils.disconnect(fs);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (command.equals("help")) {
            listChoices(menu);
        }
        return fs;
    }

    private static FileStorage superExec(FileStorage fs,String command,String... args){

        String menu =   "\nCommands:\n_________\n\n"+
                "str <path to> <path from> - Store\n" +
                "ret <path from> - Retrieve\n"+
                "del <path toDelete> - Delete\n"+
                "dcn - Disconnect\n"+
                "strm <path to> <path from> - Store file with meta\n"+
                "peekm <path to> - View file meta\n";

        String suMenu = menu+"\nSuperCommands:\n_________\n\n"+
                "adduser <username> <password>- Add user\n"+
                "userinfo <username> - See user information"+
                "permit <privilege> <username> - Permit to user(Set privileges)\n"+
                "forbid <privilege <username> - Forbid to user(Un-set privileges)\n"+
                "deluser <username> - Delete User\n"+
                "restype <extension> - Restrict filetype."+
                "unrestype <extension> - Restrict filetype.";

        System.out.println("***"+args[args.length-1] + " " +args[0]+"***");
        exec(fs, command, args);
       /* try {
            if (command.equals("str")) {
                for (int i = 0; i < args.length-1; i++) {
                    if(args[args.length-1] == null)
                        args[args.length-1] = "";
                    fs.store(args[args.length-1], args[i]);
                }
            }
            if(command.equals("strm")){
                HashMap<String,String> meta = new HashMap<>();
                boolean more=true;
                Scanner sc = new Scanner(System.in);
                String key,value;
                while(more){
                    System.out.println("key:");
                    key = sc.nextLine();
                    System.out.println("value:");
                    value=sc.nextLine();
                    meta.put(key,value);
                    System.out.println(meta.toString());

                    System.out.println("More (y/n)?");
                    key=sc.nextLine();
                    if(key.trim().equals("y"))
                        continue;
                    else
                        break;
                }

                if(args[1] == null)
                    args[1] = "";

                fs.store(args[1],args[0],meta);
            }
            if(command.equals("peekm")){
                String metastr = fs.readFileMeta(args[0]);
                System.out.println(metastr);
            }
            if (command.equals("ret")) {
                fs.retrieve(args[0]);
            }
            if (command.equals("del")) {
                fs.delete(args[0]);
            }
        } catch (PrivilegeException e) {
            System.err.println("Forbidden due to the lack of privileges.");
        } catch (IOException e) {
           System.out.println("IOEX");
        }

        try {
            if (command.equals("dcn")) {

                ConnectionUtils.disconnect(fs);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        try{
            if(command.equals("adduser")){
                if(fs.getStorageUsers().addUser(args[0],args[1]))
                    System.out.println("Successful addition of "+args[0]);
                else
                    System.out.println("User "+args[0]+"could not be added.");
            }
            if(command.equals("deluser")){
                if(fs.getStorageUsers().removeUser(args[0]))
                System.out.println("Successful deletition of "+args[0]);
                else
                System.out.println("User "+args[0]+"could not be deleted.");
            }
            if(command.equals("restype")){

                if(fs.addFiletypeRestriction(args[0]))
                    System.out.println("Restricted:"+args[0]);
                else
                    System.err.println("Something went wrong or permission exists.");
            }
            if(command.equals("unrestype")){

                if(fs.removeFiletypeRestriction(args[0]))
                    System.out.println("Allowed:"+args[0]);
                else
                    System.err.println("Something went wrong or permission exists.");


            }
        } catch (PrivilegeException e) {
            System.err.println("");
        }
        if(command.equals("userinfo")){
            System.out.println(fs.getUserString(args[0]));
        }
        if(command.equals("permit")){
            Privilege p = privilege(args[0]);

            if(p!=Privilege.BAD){
                fs.getStorageUsers().addUserPrivilege(args[1],privilege(args[0]));
            }
        }
        if(command.equals("forbid")){
            Privilege p = privilege(args[0]);

            if(p!=Privilege.BAD){
                fs.getStorageUsers().removeUserPrivilege(args[1],privilege(args[0]));
            }
        }
            try {
                if(command.equals("dcn")){

                    ConnectionUtils.disconnect(fs);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(command.equals("help")) {
                listChoices(menu);
            }

        if(command.equals("help")) {
            listChoices(suMenu);
        }


        return fs;
    }

}
