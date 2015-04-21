package esa.mo.com.support;

import org.ccsds.moims.mo.com.COMHelper;
import org.ccsds.moims.mo.com.activitytracking.ActivityTrackingHelper;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityAcceptance;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityAcceptanceList;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityExecution;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityExecutionList;
import org.ccsds.moims.mo.com.structures.ObjectDetails;
import org.ccsds.moims.mo.com.structures.ObjectDetailsList;
import org.ccsds.moims.mo.com.structures.ObjectId;
import org.ccsds.moims.mo.com.structures.ObjectKey;
import org.ccsds.moims.mo.com.structures.ObjectType;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.EntityKey;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.Time;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UShort;
import org.ccsds.moims.mo.mal.structures.UpdateHeader;
import org.ccsds.moims.mo.mal.structures.UpdateHeaderList;
import org.ccsds.moims.mo.mal.structures.UpdateType;

/**
 *
 */
public class ActivityTracking
{
  public final static int OBJ_NO_ASE_ACCEPTANCE = 4;
  public final static int OBJ_NO_ASE_EXECUTION = 5;
  public final static int OBJ_NO_ASE_OPERATION_ACTIVITY = 6;
  public final static String OBJ_NO_ASE_ACCEPTANCE_STR = Integer.toString(OBJ_NO_ASE_ACCEPTANCE);
  public final static String OBJ_NO_ASE_EXECUTION_STR = Integer.toString(OBJ_NO_ASE_EXECUTION);
  public static final ObjectType OPERATION_ACTIVITY_OBJECT_TYPE = new ObjectType(COMHelper.COM_AREA_NUMBER, ActivityTrackingHelper.ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper.COM_AREA_VERSION, new UShort(OBJ_NO_ASE_OPERATION_ACTIVITY));

  private final EventServiceHandler eventHandler;
  private int instanceIdentifier = 0;

  public ActivityTracking(EventServiceHandler eventHandler)
  {
    this.eventHandler = eventHandler;
  }

  public void publishAcceptanceEventOperation(MALInteraction interaction, boolean success) throws MALInteractionException, MALException
  {
    publishAcceptanceEvent(interaction, success, null);
  }

  public void publishAcceptanceEvent(MALInteraction interaction, boolean success, ObjectId source) throws MALInteractionException, MALException
  {
    if (source == null)
    {
      System.out.println("ActivityTracking:publishAcceptance malInter = " + interaction);
    }
    else
    {
      System.out.println("ActivityTracking:publishAcceptance malInter = " + interaction + " source " + source);
    }

    // Produce ActivityTransferList
    ActivityAcceptanceList aal = new ActivityAcceptanceList();
    ActivityAcceptance aa = new ActivityAcceptance();
    aa.setSuccess(success);
    aal.add(aa);

    // Produce ObjectDetails
    ObjectDetailsList odl = new ObjectDetailsList();
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);

    // Set source
    if (source == null)
    {
      source = new ObjectId();
      source.setType(OPERATION_ACTIVITY_OBJECT_TYPE);
      System.out.println("ActivityTracking:publishAcceptance source = " + source);

      ObjectKey key = new ObjectKey();
      key.setDomain(interaction.getMessageHeader().getDomain());
      key.setInstId(interaction.getMessageHeader().getTransactionId());
      if (interaction.getMessageHeader().getTransactionId() == null)
      {
        System.out.println("ActivityTracking:getTransactionId = NULL");
      }
      System.out.println("ActivityTracking:key = " + key);
      source.setKey(key);
    }

    objDetails.setSource(source);
    odl.add(objDetails);

    // Produce header
    UpdateHeaderList uhl = new UpdateHeaderList();
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_ACCEPTANCE_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(source.getType()));
    System.out.println("ActivityTracking:eKey = " + ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    UpdateHeader uh = new UpdateHeader(timestamp, interaction.getMessageHeader().getURITo(), UpdateType.DELETION, ekey);
    uhl.add(uh);

    // We can now publish the event
    eventHandler.publish(uhl, odl, aal);
  }

  public void publishExecutionEventSubmitAck(MALInteraction interaction, boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 1);
  }

  public void publishExecutionEventRequestResponse(MALInteraction interaction, boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 1);
  }

  public void publishExecutionEventInvokeAck(MALInteraction interaction, boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 2);
  }

  public void publishExecutionEventInvokeResponse(MALInteraction interaction, boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 2, 2);
  }

  public void publishExecutionEventOperation(MALInteraction interaction, boolean success,
                                             int currentStageCount, int totalStageCount) throws MALInteractionException, MALException
  {
    System.out.println("ActivityTracking:publishexecution malInter = " + interaction);
    // Produce header
    UpdateHeaderList uhl = new UpdateHeaderList();
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_EXECUTION_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, OBJ_NO_ASE_OPERATION_ACTIVITY));

    System.out.println("ActivityTracking:publishexecution ekey = " + ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    uhl.add(new UpdateHeader(timestamp, interaction.getMessageHeader().getURITo(), UpdateType.DELETION, ekey));

    // Produce ActivityTransferList
    ActivityExecutionList ael = new ActivityExecutionList();
    ActivityExecution activityExecutionInstance = new ActivityExecution();
    activityExecutionInstance.setExecutionStage(new UInteger(currentStageCount)); // TBD
    activityExecutionInstance.setStageCount(new UInteger(totalStageCount));
    activityExecutionInstance.setSuccess(success);

    ael.add(activityExecutionInstance);

    // Produce ObjectDetails
    ObjectDetailsList odl = new ObjectDetailsList();
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);

    ObjectKey key = new ObjectKey();
    key.setDomain(interaction.getMessageHeader().getDomain());
    key.setInstId(interaction.getMessageHeader().getTransactionId());
    if (interaction.getMessageHeader().getTransactionId() == null)
    {
      System.out.println("ActivityTracking:getTransactionId = NULL");
    }

    ObjectId source = new ObjectId(OPERATION_ACTIVITY_OBJECT_TYPE, key);
    objDetails.setSource(source);
    odl.add(objDetails);

    // We can now publish the event
    eventHandler.publish(uhl, odl, ael);
  }

  public void publishExecutionEvent(URI uriTo, boolean success,
                                    int currentStageCount, int totalStageCount, ObjectId source) throws MALInteractionException, MALException
  {
    System.out.println("ActivityTracking:publishexecution to (" + uriTo + "), source (" + source + ")");

    // Produce header
    UpdateHeaderList uhl = new UpdateHeaderList();
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_EXECUTION_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(source.getType()));

    System.out.println("ActivityTracking:publishexecution ekey = " + ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    uhl.add(new UpdateHeader(timestamp, uriTo, UpdateType.DELETION, ekey));

    // Produce ActivityTransferList
    ActivityExecutionList ael = new ActivityExecutionList();
    ActivityExecution activityExecutionInstance = new ActivityExecution();
    activityExecutionInstance.setExecutionStage(new UInteger(currentStageCount)); // TBD
    activityExecutionInstance.setStageCount(new UInteger(totalStageCount));
    activityExecutionInstance.setSuccess(success);

    ael.add(activityExecutionInstance);

    // Produce ObjectDetails
    ObjectDetailsList odl = new ObjectDetailsList();
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);

    objDetails.setSource(source);
    odl.add(objDetails);

    // We can now publish the event
    eventHandler.publish(uhl, odl, ael);
  }
}
