package org.jboss.certificate.tracker.core;

import java.util.Date;

public class CertificateInfo {

    private String alias;
    private String subjectDN;
    private String status;
    private String type;
    private Integer version;
    private Date notValidBefore;
    private Date notValidAfter;
    private Date issuedOn;
    private String issuedBy;

    public CertificateInfo(String alias, String subjectDN, String status, String type, Integer version, Date notValidBefore,
            Date notValidAfter,
            Date issuedOn, String issuedBy) {

        this.alias = alias;
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
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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
        return "CertificateInfo [alias=" + alias + ", subjectDN=" + subjectDN + ", type=" + type + ", version=" + version
                + ", notValidBefore=" + notValidBefore + ", notValidAfter=" + notValidAfter + ", issuedOn=" + issuedOn + ", issuedBy="
                + issuedBy + "]";
    }

}
