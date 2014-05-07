/** Copyright 2014 Filip Bogyai
 *
 * This file is part of certificate-tracker.
 *
 * Certificate-tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.jboss.certificate.tracker.client.dogtag;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.resteasy.plugins.providers.atom.Link;

/**
 * Base class for handling of collection of REST transfer objects
 * 
 * @author Endi S. Dewata
 */
public class DataCollection<E> {

    Integer total;
    Collection<E> entries = new ArrayList<E>();
    Collection<Link> links = new ArrayList<Link>();

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Collection<E> getEntries() {
        return entries;
    }

    public void setEntries(Collection<E> entries) {
        this.entries.clear();
        if (entries == null)
            return;
        this.entries.addAll(entries);
    }

    public void addEntry(E entry) {
        entries.add(entry);
    }

    public void removeEntry(E entry) {
        entries.remove(entry);
    }

    @XmlElement(name = "Link")
    public Collection<Link> getLinks() {
        return links;
    }

    public void setLinks(Collection<Link> links) {
        this.links.clear();
        if (links == null)
            return;
        this.links.addAll(links);
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }
}
