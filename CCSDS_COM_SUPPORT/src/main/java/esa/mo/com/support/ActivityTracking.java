/* ----------------------------------------------------------------------------
 * Copyright (C) 2014      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO COM Support library
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
package esa.mo.com.support;

import esa.mo.mal.support.BaseMalServer;
import java.util.logging.Level;
import org.ccsds.moims.mo.com.COMHelper;
import org.ccsds.moims.mo.com.activitytracking.ActivityTrackingHelper;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityAcceptance;
import org.ccsds.moims.mo.com.activitytracking.structures.ActivityExecution;
import org.ccsds.moims.mo.com.structures.ObjectDetails;
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
import org.ccsds.moims.mo.mal.structures.UpdateType;

/**
 *
 */
public class ActivityTracking
{
  public static final int OBJ_NO_ASE_ACCEPTANCE = 4;
  public static final int OBJ_NO_ASE_EXECUTION = 5;
  public static final int OBJ_NO_ASE_OPERATION_ACTIVITY = 6;
  public static final String OBJ_NO_ASE_ACCEPTANCE_STR = Integer.toString(OBJ_NO_ASE_ACCEPTANCE);
  public static final String OBJ_NO_ASE_EXECUTION_STR = Integer.toString(OBJ_NO_ASE_EXECUTION);
  public static final ObjectType OPERATION_ACTIVITY_OBJECT_TYPE = new ObjectType(COMHelper.COM_AREA_NUMBER, ActivityTrackingHelper.ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper.COM_AREA_VERSION, new UShort(OBJ_NO_ASE_OPERATION_ACTIVITY));

  private final EventServiceHandler eventHandler;
  private int instanceIdentifier = 0;

  public ActivityTracking(final EventServiceHandler eventHandler)
  {
    this.eventHandler = eventHandler;
  }

  public void publishAcceptanceEventOperation(final MALInteraction interaction, final boolean success) throws MALInteractionException, MALException
  {
    publishAcceptanceEvent(interaction, success, null);
  }

  public void publishAcceptanceEvent(final MALInteraction interaction, final boolean success, final ObjectId src) throws MALInteractionException, MALException
  {
    if (src == null)
    {
      BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishAcceptance malInter = {0}", interaction);
    }
    else
    {
      BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishAcceptance malInter = {0} source {1}", new Object[]
      {
        interaction, src
      });
    }

    // Produce ActivityTransferList
    ActivityAcceptance aa = new ActivityAcceptance();
    aa.setSuccess(success);

    // Produce ObjectDetails
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);

    // Set source
    ObjectId source = src;
    if (source == null)
    {
      source = new ObjectId();
      source.setType(OPERATION_ACTIVITY_OBJECT_TYPE);
      BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishAcceptance source = {0}", source);

      ObjectKey key = new ObjectKey();
      key.setDomain(interaction.getMessageHeader().getDomain());
      key.setInstId(interaction.getMessageHeader().getTransactionId());
      if (interaction.getMessageHeader().getTransactionId() == null)
      {
        BaseMalServer.LOGGER.fine("ActivityTracking:getTransactionId = NULL");
      }
      BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:key = {0}", key);
      source.setKey(key);
    }

    objDetails.setSource(source);

    // Produce header
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_ACCEPTANCE_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(source.getType()));
    BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:eKey = {0}", ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    UpdateHeader uh = new UpdateHeader(timestamp, interaction.getMessageHeader().getURITo(), UpdateType.DELETION, ekey);

    // We can now publish the event
    eventHandler.publishSingle(uh, objDetails, aa);
  }

  public void publishExecutionEventSubmitAck(final MALInteraction interaction, final boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 1);
  }

  public void publishExecutionEventRequestResponse(final MALInteraction interaction, final boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 1);
  }

  public void publishExecutionEventInvokeAck(final MALInteraction interaction, final boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 1, 2);
  }

  public void publishExecutionEventInvokeResponse(final MALInteraction interaction, final boolean success) throws MALInteractionException, MALException
  {
    publishExecutionEventOperation(interaction, success, 2, 2);
  }

  public void publishExecutionEventOperation(final MALInteraction interaction, final boolean success,
          final int currentStageCount, final int totalStageCount) throws MALInteractionException, MALException
  {
    BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishexecution malInter = {0}", interaction);
    
    // Produce header
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_EXECUTION_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, OBJ_NO_ASE_OPERATION_ACTIVITY));

    BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishexecution ekey = {0}", ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    UpdateHeader uh = new UpdateHeader(timestamp, interaction.getMessageHeader().getURITo(), UpdateType.DELETION, ekey);

    // Produce ActivityTransferList
    ActivityExecution activityExecutionInstance = new ActivityExecution();
    activityExecutionInstance.setExecutionStage(new UInteger(currentStageCount)); // TBD
    activityExecutionInstance.setStageCount(new UInteger(totalStageCount));
    activityExecutionInstance.setSuccess(success);

    // Produce ObjectDetails
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);

    ObjectKey key = new ObjectKey();
    key.setDomain(interaction.getMessageHeader().getDomain());
    key.setInstId(interaction.getMessageHeader().getTransactionId());
    if (interaction.getMessageHeader().getTransactionId() == null)
    {
      BaseMalServer.LOGGER.fine("ActivityTracking:getTransactionId = NULL");
    }

    ObjectId source = new ObjectId(OPERATION_ACTIVITY_OBJECT_TYPE, key);
    objDetails.setSource(source);

    // We can now publish the event
    eventHandler.publishSingle(uh, objDetails, activityExecutionInstance);
  }

  public void publishExecutionEvent(final URI uriTo, final boolean success,
          final int currentStageCount,
          final int totalStageCount,
          final ObjectId source) throws MALInteractionException, MALException
  {
    BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishexecution to ({0}), source ({1})", new Object[]
    {
      uriTo, source
    });

    // Produce header
    final EntityKey ekey = new EntityKey(
            new Identifier(OBJ_NO_ASE_EXECUTION_STR),
            ComStructureHelper.generateSubKey(COMHelper._COM_AREA_NUMBER, ActivityTrackingHelper._ACTIVITYTRACKING_SERVICE_NUMBER, COMHelper._COM_AREA_VERSION, 0),
            new Long(instanceIdentifier++),
            ComStructureHelper.generateSubKey(source.getType()));

    BaseMalServer.LOGGER.log(Level.FINE, "ActivityTracking:publishexecution ekey = {0}", ekey);
    final Time timestamp = new Time(System.currentTimeMillis());
    UpdateHeader uh = new UpdateHeader(timestamp, uriTo, UpdateType.DELETION, ekey);

    // Produce ActivityTransferList
    ActivityExecution activityExecutionInstance = new ActivityExecution();
    activityExecutionInstance.setExecutionStage(new UInteger(currentStageCount)); // TBD
    activityExecutionInstance.setStageCount(new UInteger(totalStageCount));
    activityExecutionInstance.setSuccess(success);

    // Produce ObjectDetails
    ObjectDetails objDetails = new ObjectDetails();
    objDetails.setRelated(null);
    objDetails.setSource(source);

    // We can now publish the event
    eventHandler.publishSingle(uh, objDetails, activityExecutionInstance);
  }
}
