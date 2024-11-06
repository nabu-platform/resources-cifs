/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.resources.cifs;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.TimestampedResource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class CIFSFile extends CIFSResource implements ReadableResource, WritableResource, TimestampedResource {

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

	@Override
	public Date getLastModified() {
		try {
			return new Date(getFile().lastModified());
		}
		catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}
	
}