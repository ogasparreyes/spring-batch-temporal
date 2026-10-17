package com.gentera.tcapp.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface BatchActivity {
    @ActivityMethod
    void ejecutarMigracionCsv(String idProceso);
}