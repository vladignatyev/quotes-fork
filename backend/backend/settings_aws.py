# Todo: replace for production
AWS_STORAGE_BUCKET_NAME = 'globalnewsmedia'
AWS_S3_REGION_NAME = 'eu-central-1'  # Frankfurt

AWS_ACCESS_KEY_ID = 'AKIAZJ2JT4MG563OENGY'
AWS_SECRET_ACCESS_KEY = 'b3Xiw71iJVot1ydoqQ+q/JrVhteq5FmEP4TyeTR9'

AWS_S3_OBJECT_PARAMETERS = {
    # 'Expires': 'Thu, 15 Apr 2010 20:00:00 GMT',
    'CacheControl': 'max-age=86400',
}

AWS_QUERYSTRING_AUTH = False

AWS_S3_USE_SSL = True
AWS_S3_VERIFY = True
AWS_S3_ADDRESSING_STYLE = 'path'
