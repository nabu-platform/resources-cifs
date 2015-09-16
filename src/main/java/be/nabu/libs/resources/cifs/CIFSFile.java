package be.nabu.libs.resources.cifs;

import java.io.IOException;
import java.net.URI;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class CIFSFile extends CIFSResource implements ReadableResource, WritableResource {

	protected CIFSFile(URI uri, ResourceContainer<?> parent, SmbFile file) {
		super(uri, parent, file);
	}

	@Override
	public WritableContainer<ByteBuffer> getWritable() throws IOException {
		return IOUtils.wrap(new SmbFileOutputStream(getFile()));
	}

	@Override
	public ReadableContainer<ByteBuffer> getReadable() throws IOException {
		return IOUtils.wrap(new SmbFileInputStream(getFile()));
	}
	
}