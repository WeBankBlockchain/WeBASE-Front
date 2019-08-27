package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.precompiledapi.sysconf.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SystemConfigRepository extends CrudRepository<SystemConfig, Long>, JpaSpecificationExecutor<SystemConfig> {

    SystemConfig findByGroupIdAndConfigKey(int groupId, String configKey);

    List<SystemConfig> findByGroupId(int groupId);
}
