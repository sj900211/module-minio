package run.freshr.service;

import static io.minio.http.Method.GET;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.isNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.tomcat.util.http.fileupload.FileUploadBase.ATTACHMENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.util.StringUtils.getFilenameExtension;
import static run.freshr.utils.CryptoUtil.encodeUrl;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.freshr.properties.MinioProperties;
import run.freshr.response.PutResultResponse;

/**
 * Minio service.
 *
 * @author FreshR
 * @apiNote 파일 비즈니스 로직 설정
 * @since 2022. 12. 24. 오후 3:26:37
 */
@Slf4j
@Service
public class MinioService {

  /**
   * Minio client
   *
   * @apiNote Minio Client
   * @since 2022. 12. 24. 오후 3:26:37
   */
  private final MinioClient minioClient;
  /**
   * Properties
   *
   * @apiNote 프로젝트 설정 정보
   * @since 2022. 12. 24. 오후 3:26:37
   */
  private final MinioProperties properties; // 커스텀 설정 값
  /**
   * Environment
   *
   * @apiNote 프로젝트 환경 정보
   * @since 2022. 12. 24. 오후 3:26:37
   */
  private final Environment environment;

  /**
   * Default path
   *
   * @apiNote 기본 업로드 경로
   * @since 2022. 12. 24. 오후 3:26:38
   */
  private final String DEFAULT_PATH = "temp";

  /**
   * Instantiates.
   *
   * @param minioClient the minio client
   * @param properties  the properties
   * @param environment the environment
   * @apiNote 의존성 주입
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  @Autowired
  public MinioService(MinioClient minioClient, MinioProperties properties,
      Environment environment) {
    this.minioClient = minioClient;
    this.properties = properties;
    this.environment = environment;
  }

  /**
   * Upload.
   *
   * @param file the file
   * @return the put result response
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws IOException               io exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 업로드
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public PutResultResponse upload(final MultipartFile file)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    log.info("MinioService.upload");

    return upload(DEFAULT_PATH, file);
  }

  /**
   * Upload.
   *
   * @param directory the directory
   * @param file      the file
   * @return the put result response
   * @throws IOException               io exception
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 업로드
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public PutResultResponse upload(final String directory, final MultipartFile file)
      throws IOException, ServerException, InsufficientDataException, ErrorResponseException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    log.info("MinioService.upload");

    String bucketName = properties.getBucket();
    String contentType = file.getContentType();
    String filename = file.getOriginalFilename();
    Long size = of(file.getSize()).orElse(null);

    if (!(!isNull(contentType) && !isNull(filename))) {
      return null;
    }

    String extension = getFilenameExtension(filename);
    UUID uuid = UUID.randomUUID();
    String save = uuid + "." + extension;
    String date = "/" + now().format(ofPattern("yyyyMMdd")) + "/";
    String path = "/" + directory + date + save;
    String physical = ofNullable(properties.getPath()).orElse("") + path;
    boolean except = checkExcept();

    if (!except) {
      InputStream inputStream = file.getInputStream();

      PutObjectArgs putObjectArgs = PutObjectArgs.builder()
          .bucket(bucketName)
          .object(physical)
          .stream(inputStream, inputStream.available(), -1)
          .contentType(contentType)
          .build();

      minioClient.putObject(putObjectArgs);
    }

    return PutResultResponse
        .builder()
        .filename(filename)
        .physical(physical)
        .contentType(contentType)
        .size(size)
        .build();
  }

  /**
   * Put.
   *
   * @param contentType the content type
   * @param physical    the physical
   * @param inputStream the input stream
   * @throws IOException               io exception
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 업로드<br>
   * 비즈니즈 로직 없음<br>
   * 사용할 때 주의가 필요
   * @author FreshR
   * @since 2022. 12. 26. 오후 4:42:37
   */
  public void put(final String contentType, final String physical,
      final InputStream inputStream) throws IOException, ServerException, InsufficientDataException,
      ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
      InvalidResponseException, XmlParserException, InternalException {
    log.info("MinioService.put");

    String bucketName = properties.getBucket();

    if (!(!isNull(contentType) && !isNull(physical))) {
      throw new IOException();
    }

    boolean except = checkExcept();

    if (!except) {
      PutObjectArgs putObjectArgs = PutObjectArgs.builder()
          .bucket(bucketName)
          .object(physical)
          .stream(inputStream, inputStream.available(), -1)
          .contentType(contentType)
          .build();

      minioClient.putObject(putObjectArgs);
    }
  }

  /**
   * Get input stream.
   *
   * @param path the path
   * @return the input stream
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws IOException               io exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 Stream 조회
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public InputStream get(String path)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    return minioClient.getObject(
        GetObjectArgs
            .builder()
            .bucket(properties.getBucket())
            .object(path)
            .build());
  }

  /**
   * Gets url.
   *
   * @param path the path
   * @return the url
   * @throws ServerException           the server exception
   * @throws InsufficientDataException the insufficient data exception
   * @throws ErrorResponseException    the error response exception
   * @throws IOException               the io exception
   * @throws NoSuchAlgorithmException  the no such algorithm exception
   * @throws InvalidKeyException       the invalid key exception
   * @throws InvalidResponseException  the invalid response exception
   * @throws XmlParserException        the xml parser exception
   * @throws InternalException         the internal exception
   * @apiNote 파일 접근 URL 조회
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public URL getUrl(String path)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    log.info("MinioService.get");

    return new URL(minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs
            .builder()
            .method(GET)
            .bucket(properties.getBucket())
            .object(path)
            .expiry(properties.getDefaultMinute().intValue(), MINUTES)
            .build()));
  }

  /**
   * Download.
   *
   * @param filename the filename
   * @param path     the path
   * @return the response entity
   * @throws IOException               io exception
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 다운로드
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public ResponseEntity<Resource> download(String filename, String path)
      throws IOException, ServerException, InsufficientDataException, ErrorResponseException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    URL resourceUrl = getUrl(path);
    Resource resource = new UrlResource(resourceUrl);
    HttpHeaders httpHeaders = new HttpHeaders();

    httpHeaders.setContentType(APPLICATION_OCTET_STREAM);
    httpHeaders.setContentLength(resource.contentLength());
    httpHeaders.setContentDispositionFormData(ATTACHMENT, encodeUrl(filename));

    return new ResponseEntity<>(resource, httpHeaders, OK);
  }

  /**
   * Delete.
   *
   * @param path the path
   * @throws ServerException           server exception
   * @throws InsufficientDataException insufficient data exception
   * @throws ErrorResponseException    error response exception
   * @throws IOException               io exception
   * @throws NoSuchAlgorithmException  no such algorithm exception
   * @throws InvalidKeyException       invalid key exception
   * @throws InvalidResponseException  invalid response exception
   * @throws XmlParserException        xml parser exception
   * @throws InternalException         internal exception
   * @apiNote 파일 삭제
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public void delete(String path)
      throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
      NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
      InternalException {
    log.info("MinioService.delete");

    minioClient.removeObject(
        RemoveObjectArgs
            .builder()
            .bucket(properties.getBucket())
            .object(path)
            .build());
  }

  /**
   * Check except boolean.
   *
   * @return the boolean
   * @apiNote 제외 profile 인지 체크
   * @author FreshR
   * @since 2022. 12. 24. 오후 3:26:37
   */
  public boolean checkExcept() {
    boolean except = false;
    List<String> exceptList = properties.getExcept();

    if (!isNull(exceptList)) {
      except = Arrays.stream(environment.getActiveProfiles())
          .anyMatch(active -> exceptList
              .stream()
              .anyMatch(active::equalsIgnoreCase));
    }

    return except;
  }

}
