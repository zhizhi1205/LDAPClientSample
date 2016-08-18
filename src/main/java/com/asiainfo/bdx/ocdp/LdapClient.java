package com.asiainfo.bdx.ocdp;

import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.ldap.*;

/**
 * Created by baikai on 8/17/16.
 */
public class LdapClient {

    private String ldapUrl;
    private String ldapUserDN;
    private String ldapPwd;

    public LdapClient(String ldapUrl, String ldapUserDN, String ldapPwd){
        this.ldapUrl = ldapUrl;
        this.ldapUserDN = ldapUserDN;
        this.ldapPwd = ldapPwd;
    }

    /**
     * Create LDAP user
     * @param userName
     * @param password
     * @param uidNumber
     * @param gidNumber
     */
    public void createLDAPUser(String userName, String password, String uidNumber, String gidNumber){
        LdapContext context = this.initLDAPContext();
        Attributes matchAttrs = new BasicAttributes(true);
        BasicAttribute objclassSet = new BasicAttribute("objectClass");
        objclassSet.add("account");
        objclassSet.add("posixAccount");
        matchAttrs.put(objclassSet);
        matchAttrs.put(new BasicAttribute("uid", userName));
        matchAttrs.put(new BasicAttribute("cn", userName));
        matchAttrs.put(new BasicAttribute("uidNumber", uidNumber));
        matchAttrs.put(new BasicAttribute("gidNumber", gidNumber));
        matchAttrs.put(new BasicAttribute("homeDirectory", "/home/" + userName));
        matchAttrs.put(new BasicAttribute("userpassword", password));
        matchAttrs.put(new BasicAttribute("description", "LDAP user."));

        try {
            context.bind("uid=" + userName + ",ou=People,dc=asiainfo,dc=com", null, matchAttrs);
        } catch (NamingException e) {
            e.printStackTrace();
        }finally {
            this.closeLdapContext(context);
        }
    }

    /**
     * Create LDAP user group
     * @param groupName
     * @param password
     * @param gidNumber
     */
    public void createLDAPUserGroup(String groupName, String password, String gidNumber){
        LdapContext context = this.initLDAPContext();
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute("objectclass", "posixGroup"));
        matchAttrs.put(new BasicAttribute("cn", groupName));
        matchAttrs.put(new BasicAttribute("gidNumber", gidNumber));
        matchAttrs.put(new BasicAttribute("userPassword", password));
        try {
            context.bind("cn=" + groupName + ",ou=People,dc=asiainfo,dc=com", null, matchAttrs);
        } catch (NamingException e) {
            e.printStackTrace();
        }finally {
            this.closeLdapContext(context);
        }
    }

    /**
     * Delete LDAP user
     * @param userName
     */
    public void deleteLDAPUser(String userName){
        LdapContext context = this.initLDAPContext();
        try {
            context.unbind(userName);
        } catch (NamingException e) {
            e.printStackTrace();
        }finally {
            this.closeLdapContext(context);
        }
    }

    /**
     * Delete LDAP user group
     * @param groupName
     */
    public void deleteLDAPUserGroup(String groupName){
        this.deleteLDAPUser(groupName);
    }

    /**
     * Modify LDAP user attribute with new value
     * @param userName
     * @param attributeName
     * @param attributeNewValue
     */
    public void updateLDAPUserAttribute(String userName, String attributeName, String attributeNewValue){
        LdapContext context = this.initLDAPContext();
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(context.REPLACE_ATTRIBUTE, new BasicAttribute(attributeName, attributeNewValue));
        try{
            context.modifyAttributes(userName, mods);
        }catch (NamingException e) {
            e.printStackTrace();
        }finally {
            this.closeLdapContext(context);
        }
    }

    /**
     * Search LDAP users by user dn and filter
     * @param userName
     * @param filter
     * @return NamingEnumeration<SearchResult>
     */
    public NamingEnumeration<SearchResult> searchLDAPUser(String userName, String filter){
        NamingEnumeration<SearchResult> searchResults = null;
        LdapContext context = this.initLDAPContext();
        SearchControls ctrl = new SearchControls();
        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            searchResults = context.search(userName, filter, ctrl);
        } catch (NamingException e) {
            e.printStackTrace();
        }finally {
            this.closeLdapContext(context);
        }
        return searchResults;
    }

    private LdapContext initLDAPContext(){
        LdapContext context = null;
        Properties mEnv = new Properties();
        mEnv.put(LdapContext.AUTHORITATIVE, "true");
        mEnv.put(LdapContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        mEnv.put(LdapContext.PROVIDER_URL, this.ldapUrl);
        mEnv.put(LdapContext.SECURITY_AUTHENTICATION, "simple");
        mEnv.put(LdapContext.SECURITY_PRINCIPAL, this.ldapUserDN);
        mEnv.put(LdapContext.SECURITY_CREDENTIALS, this.ldapPwd);
        try {
            context = new InitialLdapContext(mEnv,null);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return context;
    }

    private void closeLdapContext(LdapContext context){
        try {
            context.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
