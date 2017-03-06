package org.lsc.sync;

import java.util.ArrayList;
import java.util.List;

import javax.naming.CommunicationException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lsc.configuration.LscConfiguration;
import org.lsc.exception.LscServiceException;
import org.lsc.jndi.JndiModificationType;
import org.lsc.jndi.JndiModifications;
import org.lsc.jndi.SimpleJndiDstService;
import org.lsc.jndi.SimpleJndiSrcService;
import org.lsc.opendj.LdapServer;

public abstract class AbstractSyncBenchmark {
	
	private static Log LOGGER = LogFactory.getLog(AbstractSyncBenchmark.class);

	protected SimpleJndiSrcService srcJndiServices;
	protected SimpleJndiDstService dstJndiServices;
	
	private static final int MAX_NB_ENTRIES_ALLOWED=100000;
	
	protected void initConf() {
		LscConfiguration.reset();
		LscConfiguration.getInstance();
	}
	
	protected void initLDAPServer(String taskName, int entries) throws Exception {
		
		if (entries > MAX_NB_ENTRIES_ALLOWED) {
			throw new RuntimeException("Entries exceeds " + MAX_NB_ENTRIES_ALLOWED);
		}
		
		LOGGER.info("Starting opendj server ...");
		
		LdapServer.start();
		
		try {
			
			srcJndiServices =  new SimpleJndiSrcService(LscConfiguration.getTask(taskName));
			dstJndiServices =  new SimpleJndiDstService(LscConfiguration.getTask(taskName));
			
			addEntriesInSrc(entries);
			
		} catch (Exception e) {
			try {
				LdapServer.stop();
			} catch (Exception ee) {
				LOGGER.error(ee);
			}
			throw e;
		}
	}
	
	protected void stopLDAPServer() {
		LOGGER.info("Stopping opendj server ...");
		LdapServer.stop();
	}
	
	protected Attribute getAttribute(String name, String value) {
		Attribute attribute = new BasicAttribute(name);
		attribute.add(value);
		return attribute;
	}
	
	private void addEntriesInSrc(int entries) throws CommunicationException, LscServiceException {
		for (int i=1;i<=entries;i++){
			JndiModifications jndiModifications = new JndiModifications(JndiModificationType.ADD_ENTRY);
			jndiModifications.setDistinguishName("uid=test"+i+",ou=src,dc=lsc-project,dc=org");
			List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("objectClass", "inetOrgPerson")));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("mail", "test"+i+"@lsc-project.org")));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("userPassword", "secret")));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("description", "Number three's descriptive text")));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("sn", "Test "+i)));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("cn", "Test "+i)));
			modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, getAttribute("telephoneNumber", "000000, 11111")));
			jndiModifications.setModificationItems(modificationItems);
			srcJndiServices.getJndiServices().apply(jndiModifications);
			
			if (i%(entries/10) == 0) {
				LOGGER.info("Populating server with sample data : " + (double)i * 100 / entries + "% completed.");
			}
		}
		LOGGER.info("Finished populating server with sample data; " + srcJndiServices.getListPivots().size() + " entries in src.");
	}
}
