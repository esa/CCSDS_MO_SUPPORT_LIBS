package esa.mo.com.support;

import esa.mo.mal.support.BaseMalServer;
import org.ccsds.moims.mo.com.COMHelper;
import org.ccsds.moims.mo.com.event.EventHelper;
import org.ccsds.moims.mo.mal.MALContext;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALElementFactoryRegistry;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.consumer.MALConsumerManager;
import org.ccsds.moims.mo.mal.provider.MALProviderManager;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;

/**
 *
 */
public abstract class BaseComServer extends BaseMalServer
{
  protected EventServiceHandler eventService;
  protected ActivityTracking activityService;

  public BaseComServer(IdentifierList domain, Identifier network)
  {
    super(domain, network);
  }

  public BaseComServer(MALContextFactory malFactory, MALContext mal, MALConsumerManager consumerMgr, MALProviderManager providerMgr, IdentifierList domain, Identifier network)
  {
    super(malFactory, mal, consumerMgr, providerMgr, domain, network);
  }

  @Override
  protected void subInitHelpers(MALElementFactoryRegistry bodyElementFactory) throws MALException
  {
    COMHelper.deepInit(bodyElementFactory);
  }

  @Override
  protected void subInit() throws MALException, MALInteractionException
  {
    eventService = new EventServiceHandler();
    activityService = new ActivityTracking(eventService);

    createProvider(EventHelper.EVENT_SERVICE, eventService, true);
    
    eventService.init(domain, network);
  }
}
