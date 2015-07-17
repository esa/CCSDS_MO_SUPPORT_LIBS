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

import esa.mo.com.support.BaseComServer;
import org.ccsds.moims.mo.common.CommonHelper;
import org.ccsds.moims.mo.mal.MALContext;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALElementFactoryRegistry;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.consumer.MALConsumerManager;
import org.ccsds.moims.mo.mal.provider.MALProviderManager;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.URI;

/**
 * Extension of the base COM service provide class for Common based service providers.
 */
public abstract class BaseCommonServer extends BaseComServer
{
  protected final DirectoryServiceWrapper directoryService;

  /**
   * Constructor.
   *
   * @param domain the domain of the service provider.
   * @param network The network of the service provider.
   */
  public BaseCommonServer(IdentifierList domain, Identifier network)
  {
    super(domain, network);

    directoryService = new DirectoryServiceWrapper();
  }

  /**
   * Constructor.
   *
   * @param malFactory The MAL factory to use.
   * @param mal The MAL context to use.
   * @param consumerMgr The consumer manager to use.
   * @param providerMgr The provider manager to use.
   * @param domain the domain of the service provider.
   * @param network The network of the service provider.
   */
  public BaseCommonServer(MALContextFactory malFactory, MALContext mal, MALConsumerManager consumerMgr, MALProviderManager providerMgr, IdentifierList domain, Identifier network)
  {
    super(malFactory, mal, consumerMgr, providerMgr, domain, network);

    directoryService = new DirectoryServiceWrapper();
  }

  @Override
  protected void subInitHelpers(MALElementFactoryRegistry bodyElementFactory) throws MALException
  {
    super.subInitHelpers(bodyElementFactory);

    CommonHelper.deepInit(bodyElementFactory);
  }

  @Override
  protected void subInit() throws MALException, MALInteractionException
  {
    super.subInit();

    String duri = System.getProperty("directory.uri", "rmi://localhost:1024/1024-DirectoryService");
    directoryService.init(consumerMgr, new URI(duri));
  }
}
