/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package java.security;

import net.rim.device.api.crypto.Digest;
import net.rim.device.api.crypto.MD5Digest;

/**
 *
 * @author ADIKSONLINE
 */
public class MessageDigest {
    private final Digest digest;
    
    private MessageDigest(Digest digest){
        this.digest = digest;
    }
    
    public void update(byte[] input, int offset, int len){
        digest.update(input, offset, len);
    }
    
    public void reset(){
        digest.reset();
    }
    
    public int digest(byte[] buf, int offset, int len) throws DigestException{
        return digest.getDigest(buf, offset);
    }
    
    public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException{
        if ("MD5".equalsIgnoreCase(algorithm)){
            return new MessageDigest(new MD5Digest());
        } else {
            throw new NoSuchAlgorithmException(algorithm + " has not been implemented");
        }
    }
}
