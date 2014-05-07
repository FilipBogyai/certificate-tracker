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
package org.jboss.certificate.tracker.core;

import java.util.Date;

/**
 * This class represents certificate information that are obtained by
 * {@link PKIClient} method listCerts from certificate authority. These
 * information are used for certificate synchronization.
 * 
 * @author Filip Bogyai
 */

public class CertificateInfo {

    private String id;
    private String subjectDN;
    private String status;
    private String type;
    private Integer version;
    private Date notValidBefore;
    private Date notValidAfter;
    private Date issuedOn;
    private String issuedBy;

    public CertificateInfo(String id, String subjectDN, String status, String type, Integer version, Date notValidBefore,
            Date notValidAfter, Date issuedOn, String issuedBy) {

        this.id = id;
        this.subjectDN = subjectDN;
        this.status = status;
        this.type = type;
        this.version = version;
        this.notValidBefore = notValidBefore;
        this.notValidAfter = notValidAfter;
        this.issuedOn = issuedOn;
        this.issuedBy = issuedBy;
    }

    public String getAlias() {
        return id;
    }

    public void setAlias(String alias) {
        this.id = alias;
    }

    public String getSubjectDN() {
        return subjectDN;
    }

    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getNotValidBefore() {
        return notValidBefore;
    }

    public void setNotValidBefore(Date notValidBefore) {
        this.notValidBefore = notValidBefore;
    }

    public Date getNotValidAfter() {
        return notValidAfter;
    }

    public void setNotValidAfter(Date notValidAfter) {
        this.notValidAfter = notValidAfter;
    }

    public Date getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(Date issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    @Override
    public String toString() {
        return "CertificateInfo [id=" + id + ", subjectDN=" + subjectDN + ", type=" + type + ", version=" + version
                + ", notValidBefore=" + notValidBefore + ", notValidAfter=" + notValidAfter + ", issuedOn=" + issuedOn + ", issuedBy="
                + issuedBy + "]";
    }

}
