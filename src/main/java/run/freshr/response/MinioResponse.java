package run.freshr.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Minio response.
 *
 * @author FreshR
 * @apiNote 설정 정보 반환 객체
 * @since 2022. 12. 24. 오후 3:25:05
 */
@Data
@Builder
public class MinioResponse {

  /**
   * Path
   *
   * @apiNote 업로드 경로
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String path;
  /**
   * Host
   *
   * @apiNote Minio Host 주소
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String host;
  /**
   * Region
   *
   * @apiNote Region 이름
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String region;
  /**
   * Access
   *
   * @apiNote Access Key
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String access;
  /**
   * Secret
   *
   * @apiNote Secret Key
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String secret;
  /**
   * Bucket
   *
   * @apiNote Bucket 이름
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private String bucket;
  /**
   * Default minute
   *
   * @apiNote PresignedURL 접근 가능 시간(분 단위)
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private Long defaultMinute;
  /**
   * Except
   *
   * @apiNote 제외할 profile 목록
   * @since 2022. 12. 24. 오후 3:18:18
   */
  private List<String> except;

}
