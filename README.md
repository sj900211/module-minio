# Minio
> Storage Service MinIO 관리
> - ## properties.MinioProperties
>> 프로젝트에 따라 변경되는 항목의 정보를 변경할 수 있는 Properties 정의  
>> 프로젝트에서 사용할 때에는 application.yml 에서 각 정보를 변경할 수 있다.
>> freshr.minio.path: 업로드될 ROOT 디렉토리. 생략 가능
>> freshr.minio.host: minio api 주소. port 까지 입력
>> freshr.minio.region: minio region 정보. 생략 가능
>> freshr.minio.access: access key
>> freshr.minio.secret: secret key
>> freshr.minio.bucket: bucket 이름
>> freshr.minio.defaultMinute: Presigned URL 의 만료 시간. 생략 가능
>> freshr.minio.except: minio 업로드 처리를 제외할 profile 목록
>> ``` yaml
>> freshr:
>>   minio:
>>       path: STRING
>>       host: STRING
>>       region: STRING
>>       access: STRING
>>       secret: STRING
>>       bucket: STRING
>>       defaultMinute: NUMBER
>>       except: LIST
>> ```
> - ## config.MinioConfiguration
>> properties.MinioProperties 에 설정한 값으로 response.MinioResponse 를 빌드한다.
> - ## response.MinioResponse
>> properties.MinioProperties 에 설정한 값으로 빌드되는 Class.  
>> 이 Class 를 프로젝트에서 가져다 사용한다.
> - ## response.PutResultResponse
>> 업로드를 실행한 뒤 반환할 정보를 설정한 Class.
> - ## service.MinioService
>> Storage Service 기능을 정의한 Class