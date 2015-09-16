package be.nabu.libs.resources.cifs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import be.nabu.libs.resources.api.ResourceResolver;
import be.nabu.libs.resources.api.ResourceRoot;
import be.nabu.libs.resources.api.principals.BasicPrincipal;
import be.nabu.libs.resources.api.principals.DomainBasicPrincipal;

/**
 * Important: I had some problems with a particular client where i had an UNC:
 * 		\\client.example.com\path\to\file
 *
 * And a domain called "CLIENT"
 * 
 * It kept failing to resolve the server (but failed to specify which server) so after some debugging, 
 * it turns out that jcifs will actually try to resolve the "CLIENT" as well which was actually returning an ip that I could not connect to
 * 
 * In the end I updated the domain to "client.example.com" as well and they both connected to the same server, solving the problem
 */
public class CIFSResourceResolver implements ResourceResolver {
	
	private static List<String> defaultSchemes = Arrays.asList(new String[] { "smb", "cifs", "samba" });

	@Override
	public ResourceRoot getResource(URI uri, Principal principal) {
		try {
			String domain = null;
			String user = null;
			String password = null;
			
			// hardcoded gets precendence
			if (uri.getUserInfo() != null) {
				int index = uri.getUserInfo().indexOf(':');
				user = index < 0 ? uri.getUserInfo() : uri.getUserInfo().substring(0, index);
				password = index < 0 ? null : uri.getUserInfo().substring(index + 1);
			}
			else if (principal instanceof DomainBasicPrincipal) {
				DomainBasicPrincipal domainPrincipal = (DomainBasicPrincipal) principal;
				domain = domainPrincipal.getDomain();
				user = domainPrincipal.getName();
				password = domainPrincipal.getPassword();
			}
			else if (principal instanceof BasicPrincipal) {
				BasicPrincipal basicPrincipal = (BasicPrincipal) principal;
				user = basicPrincipal.getName();
				password = basicPrincipal.getPassword();
			}
			
			if (user != null) {
				user = user.replace('/', ';');
				int index = user.indexOf(';');
				if (index >= 0) {
					domain = user.substring(0, index);
					user = user.substring(index + 1);
				}
			}
			NtlmPasswordAuthentication ntlmAuthentication = user == null ? null : new NtlmPasswordAuthentication(domain, user, password);
			// it might be necessary sometimes
//			Config.setProperty("jcifs.netbios.wins", "10.0.0.0");
			
			String authority = uri.getHost();
			if (uri.getPort() >= 0) {
				authority += ":" + uri.getPort();
			}
			uri = new URI("smb", authority, uri.getPath(), uri.getQuery(), uri.getFragment());
			
			SmbFile file = new SmbFile(uri.toString(), ntlmAuthentication);
			file.setConnectTimeout(10000);
			file.setReadTimeout(10000);
			if (file.exists()) {
				if (file.isFile())
					return new CIFSFile(uri, null, file);
				else if (file.isDirectory())
					return new CIFSDirectory(uri, null, file);
				else {
					throw new RuntimeException("Unknown type: " + file);
				}
			}
		}
		catch (SmbException e) {
			e.printStackTrace();
			// ignore
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			// ignore
		}
		return null;
	}

	@Override
	public List<String> getDefaultSchemes() {
		return defaultSchemes;
	}
}
