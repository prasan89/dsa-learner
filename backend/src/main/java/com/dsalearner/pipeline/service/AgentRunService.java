package com.dsalearner.pipeline.service;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final CfAgentRunRepository agentRunRepository;

    public List<CfAgentRun> runsForLesson(UUID lessonId, int version) {
        return agentRunRepository.findByLessonIdAndLessonVersion(lessonId, version);
    }
}
