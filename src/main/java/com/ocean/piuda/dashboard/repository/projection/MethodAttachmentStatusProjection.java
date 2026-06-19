package com.ocean.piuda.dashboard.repository.projection;

public interface MethodAttachmentStatusProjection {
    String getMethodName();  // TransplantMethod enum name (e.g. ROPE)
    String getStatusName();  // SpeciesAttachmentStatus enum name (e.g. GOOD)
}
