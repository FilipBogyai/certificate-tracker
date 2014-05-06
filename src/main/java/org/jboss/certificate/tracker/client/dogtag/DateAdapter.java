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

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * The DateAdapter class provides custom marshaling for Date.
 *
 * @author Endi S. Dewata
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String value) throws Exception {
        return value.isEmpty() ? null : new Date(Long.parseLong(value));
    }

    @Override
    public String marshal(Date value) throws Exception {
        return value == null ? null : Long.toString(value.getTime());
    }
}
