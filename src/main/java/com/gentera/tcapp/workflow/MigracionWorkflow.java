package com.gentera.tcapp.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MigracionWorkflow {
    @WorkflowMethod
    String iniciarMigracionCsv(String idProceso);
}