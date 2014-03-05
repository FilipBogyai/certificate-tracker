//--- BEGIN COPYRIGHT BLOCK ---
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; version 2 of the License.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License along
//with this program; if not, write to the Free Software Foundation, Inc.,
//51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//(C) 2012 Red Hat, Inc.
//All rights reserved.
//--- END COPYRIGHT BLOCK ---
package org.jboss.certificate.tracker.client.cert;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CertDataInfos")
public class CertDataInfos {

    Integer total;
    Collection<CertDataInfo> entries = new ArrayList<CertDataInfo>();

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @XmlElementRef
    public Collection<CertDataInfo> getEntries() {
        return entries;
    }

    public void setEntries(Collection<CertDataInfo> entries) {
        this.entries.clear();
        if (entries == null)
            return;
        this.entries.addAll(entries);
    }

    public void addEntry(CertDataInfo entry) {
        entries.add(entry);
    }

    public void removeEntry(CertDataInfo entry) {
        entries.remove(entry);
    }

}
