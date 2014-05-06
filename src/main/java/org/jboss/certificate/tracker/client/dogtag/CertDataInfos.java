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

import java.util.Collection;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CertDataInfos")
public class CertDataInfos extends DataCollection<CertDataInfo> {

    @Override
    @XmlElementRef
    public Collection<CertDataInfo> getEntries() {
        return super.getEntries();
    }
}
