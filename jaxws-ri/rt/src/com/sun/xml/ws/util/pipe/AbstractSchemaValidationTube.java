package com.sun.xml.ws.util.pipe;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.developer.SchemaValidationFeature;
import com.sun.xml.ws.developer.ValidationErrorHandler;
import org.w3c.dom.*;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation.
 *
 * @author Jitendra Kotamraju
 */
public abstract class AbstractSchemaValidationTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(AbstractSchemaValidationTube.class.getName());

    protected final WSBinding binding;
    protected final SchemaValidationFeature feature;

    public AbstractSchemaValidationTube(WSBinding binding, Tube next) {
        super(next);
        this.binding = binding;
        feature = binding.getFeature(SchemaValidationFeature.class);
    }

    protected AbstractSchemaValidationTube(AbstractSchemaValidationTube that, TubeCloner cloner) {
        super(that, cloner);
        this.binding = that.binding;
        this.feature = that.feature;
    }

    protected abstract Validator getValidator();

    protected abstract boolean isNoValidation();

    /**
     * Recursively visit ancestors and build up {@link org.xml.sax.helpers.NamespaceSupport} oject.
     */
    private void buildNamespaceSupport(NamespaceSupport nss, Node node) {
        if(node==null || node.getNodeType()!=Node.ELEMENT_NODE)
            return;

        buildNamespaceSupport( nss, node.getParentNode() );

        nss.pushContext();
        NamedNodeMap atts = node.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( "xmlns".equals(a.getPrefix()) ) {
                nss.declarePrefix( a.getLocalName(), a.getValue() );
                continue;
            }
            if( "xmlns".equals(a.getName()) ) {
                nss.declarePrefix( "", a.getValue() );
                continue;
            }
        }
    }

    /**
     * Adds inscope namespaces as attributes to  <xsd:schema> fragment nodes.
     *
     * @param nss namespace context info
     * @param elem that is patched with inscope namespaces
     */
    private @Nullable void patchDOMFragment(NamespaceSupport nss, Element elem) {
        NamedNodeMap atts = elem.getAttributes();
        for( Enumeration en = nss.getPrefixes(); en.hasMoreElements(); ) {
            String prefix = (String)en.nextElement();

            for( int i=0; i<atts.getLength(); i++ ) {
                Attr a = (Attr)atts.item(i);
                if (!"xmlns".equals(a.getPrefix()) || !a.getLocalName().equals("prefix")) {
                    LOGGER.fine("Patching with xmlns:"+prefix+"="+nss.getURI(prefix));
                    elem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"+prefix, nss.getURI(prefix));
                }
            }
        }
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isNoValidation() || !request.getMessage().hasPayload() || request.getMessage().isFault()) {
            return super.processRequest(request);
        }
        doProcess(request);
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (isNoValidation() || response.getMessage() == null || !response.getMessage().hasPayload() || response.getMessage().isFault()) {
            return super.processResponse(response);
        }
        doProcess(response);
        return super.processResponse(response);
    }

    private void doProcess(Packet packet) {
        getValidator().reset();
        Class<? extends ValidationErrorHandler> handlerClass = feature.getErrorHandler();
        ValidationErrorHandler handler;
        try {
            handler = handlerClass.newInstance();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        handler.setPacket(packet);
        getValidator().setErrorHandler(handler);
        Message msg = packet.getMessage().copy();
        Source source = msg.readPayloadAsSource();
        try {
            // Validator javadoc allows ONLY SAX, and DOM Sources
            // But the impl seems to handle all kinds.
            getValidator().validate(source);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    protected DOMSource toDOMSource(Source source) {
        if (source instanceof DOMSource) {
            return (DOMSource)source;
        }
        Transformer trans = XmlUtil.newTransformer();
        DOMResult result = new DOMResult();
        try {
            trans.transform(source, result);
        } catch(TransformerException te) {
            throw new WebServiceException(te);
        }
        return new DOMSource(result.getNode());
    }

    protected static void printDOM(DOMSource src) {
        try {
            ByteArrayBuffer bos = new ByteArrayBuffer();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(src, sr);
            LOGGER.info("**** src ******"+bos.toString());
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}