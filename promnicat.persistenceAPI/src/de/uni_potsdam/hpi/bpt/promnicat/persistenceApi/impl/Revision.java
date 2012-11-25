/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IModel;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IRepresentation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IRevision;

/**
 * A {@link Revision} represents one version of a {@link Model}, with one {@link Revision} being the latest/newest.
 * Each {@link Revision} can hold several {@link Representation}s. 
 * Each {@link Revision} has a unique number, author, language and metadata with any key/values pairs.
 * 
 * @author Andrina Mascher, Tobias Hoppe
 *
 */
public class Revision extends AbstractPojo implements IRevision {

	//the revision number
	protected Integer revisionNumber = null;
	//connected model
	protected transient IModel model = null;
	//connected representations
	protected Set<IRepresentation> representations = new HashSet<IRepresentation>();
	//metadata with key/values, values are separated by MD_SPLIT to store only key/value in database
	protected Map<String, String> metadata = new HashMap<String, String>();
	//separator used to distinguish metadata values for 1 key
	protected final String MD_SPLIT = "\t####\t";
	//is true if this is the latest revision of the model
	protected boolean latestRevision = false;
	//name of the authors
	protected String author = "";

	protected Revision() {
	}

	protected Revision(Integer number) {
		this.revisionNumber = number;
	}

	@Override
	public String toString() {
		return "Revision [dbId=" + getDbId() + ", revisionNumber=" + getRevisionNumber() + ", latestRevison=" + isLatestRevision()
				+ ", modelTitle=" + getTitle()
				+ ", #representations=" + getNrOfRepresentations()
				+ ", author=" + getAuthor()
				+ ", #metadata=" + getMetadata().size() + "]"
				;
	}
	
	@Override
	public boolean isCompletelyLoaded() {
		if(getModel() == null) {
			return false;
		}
		return getModel().getCompletelyLoaded();
	}

	@Override
	public void connectModel(IModel newModel) {
		//defer responsibility
		if(newModel != null) {
			newModel.connectRevision(this); 
		}
	}

	@Override
	public void connectRepresentation(IRepresentation representation) {
		if(representation != null) {
			if(!this.representations.contains(representation)) {
				this.representations.add((Representation) representation);				
				((Representation) representation).setRevision(this);
			}
		}
	}

	@Override
	public String getTitle() {
		if (getModel() == null) {
			return null;
		}
		return getModel().getTitle();
	}
	
	@Override
	public int getNrOfRepresentations() {
		return representations.size();
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public boolean isLatestRevision() {
		return latestRevision;
	}

	@Override
	public void setLatestRevision(boolean latestRevision) {
		this.latestRevision = latestRevision;
	}

	@Override
	public Integer getRevisionNumber() {
		return revisionNumber;
	}

	@Override
	public void setRevisionNumber(Integer number) {
		this.revisionNumber = number;
	}

	@Override
	public IModel getModel() {
		return model;
	}

	@Override
	public Set<IRepresentation> getRepresentations() {
		return representations;
	}

	@Override
	public void setRepresentations(Set<IRepresentation> representations) {
		this.representations.clear();
		if(representations == null) {
			return;
		}
		for (IRepresentation rep : representations) {
			this.representations.add(rep);
			connectRepresentation(rep);
		}
	}

	@Override
	public HashMap<String, String[]> getMetadata() {
		HashMap<String,String[]> newMd = new HashMap<String,String[]>();
		for(Entry<String,String> e : metadata.entrySet()) {
			newMd.put(e.getKey(), convertMetadataValueToArray(e.getValue()));
		}
		return newMd;
	}

	@Override
	public String[] getMetadataAtKey(String key) {
		return convertMetadataValueToArray(metadata.get(key));
	}
	
	/**
	 * Convert a string into an array by splitting
	 * @param value
	 * @return the converted array
	 */
	private String[] convertMetadataValueToArray(String value) {
		return value.split(MD_SPLIT);
	}
	
	/**
	 * Convert a string array into a string. 
	 * This is a work around because key/value pairs can be stored in OrientDb, but key/values not.
	 * @param array
	 * @return the converted string
	 */
	private String convertMetadataValueFromArray(String[] array) {
		String s = "";
		for(int i=0; i<array.length; i++) {
			s += array[i] + MD_SPLIT;	
		}
		//don't start with MD_SPLIT
		s = s.substring(0, s.length() - MD_SPLIT.length());
		return s;
	}
	
	@Override
	public void setMetadata(Map<String, String[]> metadata) {
		if(metadata == null) {
			this.metadata.clear();
			return;
		}
		for(Entry<String,String[]> e : metadata.entrySet()) {
			setMetadataAtKey(e.getKey(), e.getValue());
		}
	}
	
	@Override
	public void setMetadataAtKey(String key, String[] values) {
		if(key == null || key.isEmpty()) {
			return;
		}
		this.metadata.put(key, convertMetadataValueFromArray(values));
	}
	
	@Override
	public void addMetadataAtKey(String key, String value) {
		if(key == null || key.isEmpty()) {
			return;
		} else if (!metadata.containsKey(key)) {
			metadata.put(key, value);
		} else {
			String s = metadata.get(key);
			if(s.length() == 0) {
				s = value;
			} else {
				s += MD_SPLIT + value;
			}
			metadata.put(key, s);
		}
	}

	public void setModel(IModel modelToSet) {
		model = modelToSet;
	}
}
