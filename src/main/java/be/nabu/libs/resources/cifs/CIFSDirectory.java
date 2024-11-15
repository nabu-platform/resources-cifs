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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;

public class CIFSDirectory extends CIFSResource implements ManageableContainer<CIFSResource> {

	protected CIFSDirectory(URI uri, ResourceContainer<?> parent, SmbFile file) {
		super(uri, parent, file);
	}

	@SuppressWarnings("resource")
	@Override
	public CIFSResource getChild(String name) {
		try {
			SmbFile file = new SmbFile(getFile(), name);
			if (!file.exists()) {
				return null;
			}
			return file.isFile() ? new CIFSFile(URIUtils.getChild(getUri(), name), this, file) : new CIFSDirectory(URIUtils.getChild(getUri(), name), this, file);
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		catch (SmbException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterator<CIFSResource> iterator() {
		List<CIFSResource> children = new ArrayList<CIFSResource>();
		try {
			for (SmbFile child : getFile().listFiles()) {
				children.add(child.isFile() ? new CIFSFile(URIUtils.getChild(getUri(), child.getName()), this, child) : new CIFSDirectory(URIUtils.getChild(getUri(), child.getName()), this, child));	
			}
		}
		catch (SmbException e) {
			throw new RuntimeException(e);
		}
		return children.iterator();
	}

	@Override
	public CIFSResource create(String name, String contentType) throws IOException {
		if (getChild(name) != null) {
			throw new IOException("The file with name '" + name + "' already exists");
		}
		SmbFile smbFile = new SmbFile(getFile(), name);
		if (Resource.CONTENT_TYPE_DIRECTORY.equals(contentType)) {
			smbFile.mkdir();
			return new CIFSFile(URIUtils.getChild(getUri(), name), this, smbFile);
		}
		else {
			smbFile.createNewFile();
			return new CIFSDirectory(URIUtils.getChild(getUri(), name), this, smbFile);
		}
	}

	@Override
	public void delete(String name) throws IOException {
		CIFSResource child = getChild(name);
		if (child != null) {
			child.getFile().delete();
		}
	}

	@Override
	public String getContentType() {
		return Resource.CONTENT_TYPE_DIRECTORY;
	}
}
