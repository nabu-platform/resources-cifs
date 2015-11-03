package be.nabu.libs.resources.cifs;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;

import jcifs.smb.SmbFile;
import be.nabu.libs.resources.api.LocatableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;

public class CIFSResource implements Resource, Closeable, LocatableResource {

	private SmbFile file;
	private ResourceContainer<?> parent;
	private URI uri;
	
	protected CIFSResource(URI uri, ResourceContainer<?> parent, SmbFile file) {
		this.uri = uri;
		this.parent = parent;
		this.file = file;
		// don't allow infinite timeouts
		if (file.getConnectTimeout() == 0) {
			if (parent instanceof CIFSResource) {
				file.setConnectTimeout(((CIFSResource) parent).getFile().getConnectTimeout());
			}
			else {
				file.setConnectTimeout(60000);
			}
		}
		if (file.getReadTimeout() == 0) {
			if (parent instanceof CIFSResource) {
				file.setReadTimeout(((CIFSResource) parent).getFile().getReadTimeout());
			}
			else {
				file.setReadTimeout(60000);
			}
		}
	}
	
	@Override
	public String getContentType() {
		return URLConnection.guessContentTypeFromName(file.getName());
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public ResourceContainer<?> getParent() {
		return parent;
	}

	protected SmbFile getFile() {
		return file;
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof CIFSResource && ((CIFSResource) object).file.equals(file);
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public void close() throws IOException {
		// do nothing?
	}
}
