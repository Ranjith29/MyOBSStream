package org.obsplatform.cms.mediadetails.domain;

import org.obsplatform.cms.media.domain.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MediaAssetRepository  extends
JpaRepository<MediaAsset, Long>,
JpaSpecificationExecutor<MediaAsset>{

}
