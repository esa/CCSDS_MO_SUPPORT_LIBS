package esa.mo.com.support;

import java.util.Map;
import org.ccsds.moims.mo.com.event.provider.EventInheritanceSkeleton;
import org.ccsds.moims.mo.com.event.provider.MonitorEventPublisher;
import org.ccsds.moims.mo.com.structures.ObjectDetailsList;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALPublishInteractionListener;
import org.ccsds.moims.mo.mal.structures.ElementList;
import org.ccsds.moims.mo.mal.structures.EntityKey;
import org.ccsds.moims.mo.mal.structures.EntityKeyList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.UpdateHeaderList;
import org.ccsds.moims.mo.mal.transport.MALErrorBody;
import org.ccsds.moims.mo.mal.transport.MALMessageHeader;

/**
 *
 */
public class EventServiceHandler extends EventInheritanceSkeleton
{
  private MonitorEventPublisher monitorEventPublisher = null;

  public EventServiceHandler()
  {
  }

  public void init(IdentifierList domain, Identifier network) throws MALInteractionException, MALException
  {
    System.out.println("EventServiceHandler:init");

    if (null == monitorEventPublisher)
    {
      System.out.println("EventServiceHandler:creating event publisher");

      if (null == domain)
      {
        domain = new IdentifierList();
      }

      if (null == network)
      {
        network = new Identifier("SPACE");
      }

      monitorEventPublisher = createMonitorEventPublisher(domain,
              network,
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              null,
              new UInteger(0));
      final EntityKeyList lst = new EntityKeyList();
      lst.add(new EntityKey(new Identifier("*"), (long) 0, (long) 0, (long) 0));

      monitorEventPublisher.register(lst, new EventPublisher());
    }
  }

  public void publish(UpdateHeaderList updateHeaderList, ObjectDetailsList eventLinks, ElementList eventBody) throws IllegalArgumentException, MALInteractionException, MALException
  {
    monitorEventPublisher.publish(updateHeaderList, eventLinks, eventBody);
  }

  public class EventPublisher implements MALPublishInteractionListener
  {
    public void publishRegisterAckReceived(MALMessageHeader header, Map qosProperties) throws MALException
    {
    }

    public void publishRegisterErrorReceived(MALMessageHeader header, MALErrorBody body, Map qosProperties) throws MALException
    {
    }

    public void publishErrorReceived(MALMessageHeader header, MALErrorBody body, Map qosProperties) throws MALException
    {
      System.out.println("EventPublisher:publishErrorReceived - " + body.toString());
    }

    public void publishDeregisterAckReceived(MALMessageHeader header, Map qosProperties) throws MALException
    {
    }
  }
}
