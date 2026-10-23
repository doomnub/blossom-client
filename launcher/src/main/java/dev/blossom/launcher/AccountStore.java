package dev.blossom.launcher;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
/** Stable, owner-only refresh-token storage. Never stores passwords or game tokens. */
final class AccountStore {
    private final Path file;
    AccountStore(Path root){file=root.resolve("account.properties");}
    String read(String clientId)throws IOException {
        if(!Files.exists(file))return null;if(Files.size(file)>65536)throw new IOException("Saved account file exceeds size limit");
        Properties p=new Properties();try(Reader r=Files.newBufferedReader(file)){try{p.load(r);}catch(IllegalArgumentException e){throw new IOException("Saved account file is invalid; remove account.properties and sign in again",e);}}return clientId.equals(p.getProperty("clientId"))?p.getProperty("refreshToken"):null;
    }
    void save(String clientId,String token)throws IOException {
        if(token==null||token.isEmpty())return;Files.createDirectories(file.getParent());
        Path temp=Files.createTempFile(file.getParent(),"account-",".tmp");
        try{
            PosixFileAttributeView posix=Files.getFileAttributeView(temp,PosixFileAttributeView.class);
            if(posix!=null)Files.setPosixFilePermissions(temp,PosixFilePermissions.fromString("rw-------"));
            else {AclFileAttributeView acl=Files.getFileAttributeView(temp,AclFileAttributeView.class);if(acl==null)throw new IOException("Owner-only account storage unavailable");UserPrincipal owner=Files.getOwner(temp);acl.setAcl(Collections.singletonList(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()));}
            Properties p=new Properties();p.setProperty("clientId",clientId);p.setProperty("refreshToken",token);try(Writer w=Files.newBufferedWriter(temp)){p.store(w,"Blossom remembered account — private; do not share");}Net.move(temp,file);
        }finally{Files.deleteIfExists(temp);}
    }
    void clear()throws IOException{Files.deleteIfExists(file);}
}
