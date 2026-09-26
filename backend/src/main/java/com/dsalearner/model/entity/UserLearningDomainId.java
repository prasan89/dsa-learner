package com.dsalearner.model.entity;

import java.io.Serializable;
import java.util.UUID;

public record UserLearningDomainId(UUID userId, String domainCode) implements Serializable {}
