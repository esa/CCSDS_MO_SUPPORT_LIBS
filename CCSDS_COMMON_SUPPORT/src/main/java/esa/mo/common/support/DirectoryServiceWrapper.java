/* ----------------------------------------------------------------------------
 * Copyright (C) 2014      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO Common Support library
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.common.support;

import esa.mo.mal.support.BaseMalServer;
import org.ccsds.moims.mo.common.directory.DirectoryHelper;
import org.ccsds.moims.mo.common.directory.consumer.DirectoryStub;
import org.ccsds.moims.mo.common.directory.structures.LocalNode;
import org.ccsds.moims.mo.common.directory.structures.NodeDetails;
import org.ccsds.moims.mo.common.directory.structures.NodeDetailsList;
import org.ccsds.moims.mo.common.directory.structures.ProviderInformation;
import org.ccsds.moims.mo.common.directory.structures.ProviderInformationList;
import org.ccsds.moims.mo.common.directory.structures.ServiceAddress;
import org.ccsds.moims.mo.common.directory.structures.ServiceAddressList;
import org.ccsds.moims.mo.common.directory.structures.ServiceDetails;
import org.ccsds.moims.mo.common.directory.structures.ServiceDetailsList;
import org.ccsds.moims.mo.common.directory.structures.ServiceFilter;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALService;
import org.ccsds.moims.mo.mal.consumer.MALConsumer;
import org.ccsds.moims.mo.mal.consumer.MALConsumerManager;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.IntegerList;
import org.ccsds.moims.mo.mal.structures.NamedValueList;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.QoSLevelList;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UShort;

/**
 *
 */
public class DirectoryServiceWrapper
{
  protected DirectoryStub directoryService;

  public void init(MALConsumerManager consumerMgr, URI uri) throws MALInteractionException, MALException
  {
    BaseMalServer.LOGGER.fine("DirectoryServiceWrapper:init");

    if (null == directoryService)
    {
      IdentifierList domain = new IdentifierList();
      Identifier network = new Identifier("GROUND");
      SessionType session = SessionType.LIVE;
      Identifier sessionName = new Identifier("LIVE");

      MALConsumer consumer = consumerMgr.createConsumer((String) null,
              uri,
              uri,
              DirectoryHelper.DIRECTORY_SERVICE,
              new Blob("".getBytes()),
              domain,
              network,
              session,
              sessionName,
              QoSLevel.BESTEFFORT,
              System.getProperties(),
              new UInteger(0));

      directoryService = new DirectoryStub(consumer);
    }
  }

  public void publishProvider(Identifier providerName, IdentifierList domain, Identifier network, MALService service, IntegerList supportedCapabilities, URI serviceURI, URI brokerURI) throws MALException, MALInteractionException
  {
    directoryService.publishService(createNodeDetailsList(providerName,
            domain,
            network,
            service,
            supportedCapabilities,
            serviceURI,
            brokerURI));
  }

  public void withdrawProvider(Identifier providerName, IdentifierList domain, Identifier network, MALService service, IntegerList supportedCapabilities, URI serviceURI, URI brokerURI) throws MALException, MALInteractionException
  {
    directoryService.withdrawService(createNodeDetailsList(providerName,
            domain,
            network,
            service,
            supportedCapabilities,
            serviceURI,
            brokerURI));
  }
  
  public ServiceDetailsList lookupService(Identifier providerName, IdentifierList domain, Identifier network, MALService service, IntegerList supportedCapabilities) throws MALException, MALInteractionException
  {
    UShort areaNumber = null;
    UShort serviceNumber = null;
    UOctet areaVersion = null;
    
    if (null != service)
    {
      areaNumber = service.getArea().getNumber();
      serviceNumber = service.getNumber();
      areaVersion = service.getArea().getVersion();
    }
    
    ServiceFilter filter = new ServiceFilter(domain, network, SessionType.LIVE, new Identifier("LIVE"), areaNumber, serviceNumber, areaVersion, supportedCapabilities, providerName);
    
    return directoryService.lookupService(filter);
  }
  
  public static NodeDetailsList createNodeDetailsList(Identifier providerName, IdentifierList domain, Identifier network, MALService service, IntegerList supportedCapabilities, URI serviceURI, URI brokerURI)
  {
    ServiceAddress sa = new ServiceAddress(new QoSLevelList(), new NamedValueList(), 1, serviceURI, brokerURI, null);
    ServiceAddressList sal = new ServiceAddressList();
    sal.add(sa);
    
    ProviderInformation pi = new ProviderInformation(providerName, supportedCapabilities, new NamedValueList(), sal);
    ProviderInformationList pil = new ProviderInformationList();
    pil.add(pi);
    
    ServiceDetails sd = new ServiceDetails(service.getArea().getNumber(), service.getNumber(), service.getArea().getVersion(), pil);
    ServiceDetailsList sdl = new ServiceDetailsList();
    sdl.add(sd);
    
    LocalNode node = new LocalNode(SessionType.LIVE, new Identifier("LIVE"), sdl);
    NodeDetails nd = new NodeDetails(domain, network, node);
    
    NodeDetailsList ndl = new NodeDetailsList();
    ndl.add(nd);
    
    return ndl;
  }
}
