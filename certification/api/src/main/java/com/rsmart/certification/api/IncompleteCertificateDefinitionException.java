package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 12:41:42 PM
 */
public class IncompleteCertificateDefinitionException
    extends CertificationException
{
    public IncompleteCertificateDefinitionException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IncompleteCertificateDefinitionException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IncompleteCertificateDefinitionException(String message, Throwable t) {
        super(message, t);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IncompleteCertificateDefinitionException(Throwable t) {
        super(t);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
