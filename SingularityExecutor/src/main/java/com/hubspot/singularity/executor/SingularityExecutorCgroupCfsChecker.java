package com.hubspot.singularity.executor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.hubspot.singularity.executor.task.SingularityExecutorTask;
import com.hubspot.singularity.runner.base.shared.WatchServiceHelper;

public class SingularityExecutorCgroupCfsChecker extends WatchServiceHelper {
  private static final Logger LOG = LoggerFactory.getLogger(SingularityExecutorCgroupCfsChecker.class);

  private static final String CGROUP_CFS_QUOTA_FILE = "cpu.cfs_quota_us";
  private static final String CGROUP_CFS_PERIOD_FILE = "cpu.cfs_period_us";

  private final String taskId;
  private final long desiredCfsQuota;
  private final long desiredCfsPeriod;

  public SingularityExecutorCgroupCfsChecker(SingularityExecutorTask task, int cpuHardLimit, long desiredCfsPeriod) throws IOException {
    super(1000, getCpuCgroupDirectory(task), ImmutableList.of(StandardWatchEventKinds.ENTRY_MODIFY));
    this.taskId = task.getTaskId();
    this.desiredCfsQuota = cpuHardLimit * desiredCfsPeriod;
    this.desiredCfsPeriod = desiredCfsPeriod;
  }

  private static Path getCpuCgroupDirectory(SingularityExecutorTask task) throws IOException {
    List<String> cgroups = Files.readAllLines(Paths.get(String.format("/proc/%s/cgroup", task.getTaskDefinition().getExecutorPid())));
    for (String cgroup : cgroups) {
      if (cgroup.contains(":cpu:")) {
        String[] segments = cgroup.split(":");
        String cgroupPath = segments[segments.length - 1];
        return Paths.get(getBaseCgroupPath() + cgroupPath);
      }
    }
    throw new RuntimeException(String.format("Found no cpu cgroup from output %s", cgroups));
  }

  private static String getBaseCgroupPath() {
    if (Files.isDirectory(Paths.get("/cgroup"))) {
      return "/cgroup";
    } else {
      return "/sys/fs/cgroup";
    }
  }

  @Override
  public boolean processEvent(WatchEvent.Kind<?> kind, Path filename) throws IOException {
    try {
      if (filename.toString().endsWith(CGROUP_CFS_QUOTA_FILE)) {
        long cfsQuota = Long.parseLong(new String(Files.readAllBytes(filename), StandardCharsets.US_ASCII));
        if (cfsQuota != desiredCfsQuota) {
          FileOutputStream overwriteFileStream = new FileOutputStream(filename.toFile(), false);
          overwriteFileStream.write(Long.toString(desiredCfsQuota).getBytes(StandardCharsets.US_ASCII));
          overwriteFileStream.close();
          LOG.info("Updated cfsQuota from {} to {} for task {}", cfsQuota, desiredCfsQuota, taskId);
        }
      }
      if (filename.toString().endsWith(CGROUP_CFS_PERIOD_FILE)) {
        long cfsPeriod = Long.parseLong(new String(Files.readAllBytes(filename), StandardCharsets.US_ASCII));
        if (cfsPeriod != desiredCfsPeriod) {
          FileOutputStream overwriteFileStream = new FileOutputStream(filename.toFile(), false);
          overwriteFileStream.write(Long.toString(desiredCfsPeriod).getBytes(StandardCharsets.US_ASCII));
          overwriteFileStream.close();
          LOG.info("Updated cfsPeriod from {} to {} for task {}", cfsPeriod, desiredCfsPeriod, taskId);
        }
      }
    } catch (Throwable t) {
      LOG.error("Unable to update cfs period/quota values for task {}",taskId, t);
    }
    return true;
  }
}
