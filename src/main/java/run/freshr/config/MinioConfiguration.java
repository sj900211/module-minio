package run.freshr.config;

import static org.springframework.util.StringUtils.hasLength;

import io.minio.MinioClient;
import io.minio.MinioClient.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.freshr.properties.MinioProperties;

/**
 * Minio configuration.
 *
 * @author FreshR
 * @apiNote 프로젝트의 application properties 설정
 * @since 2022. 12. 24. 오후 3:23:07
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {

  /**
   * Minio client.
   *
   * @param properties properties
   * @return minio client
   * @apiNote 프로젝트 설정 정보로 Client 생성
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:23:07
   */
  @Bean
  @ConditionalOnMissingBean
  public MinioClient minioClient(MinioProperties properties) {
    Builder builder = MinioClient
        .builder()
        .endpoint(properties.getHost())
        .credentials(properties.getAccess(), properties.getSecret());

    String region = properties.getRegion();

    if (hasLength(region)) {
      builder.region(region);
    }

    return builder.build();
  }

}
