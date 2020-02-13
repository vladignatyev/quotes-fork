"""
Django settings for backend project.

Generated by 'django-admin startproject' using Django 2.2.5.

For more information on this file, see
https://docs.djangoproject.com/en/2.2/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/2.2/ref/settings/
"""

import os

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/2.2/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'bpi+3j6g58*pnle#(q32y_#2prtpu$k%e6$8$3x_h!z7cha&ea'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = ['*']


# Application definition

INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles'
]

INSTALLED_APPS += [
    'longjob.apps.LongjobConfig'
]

INSTALLED_APPS += [
    'api.apps.ApiConfig',
    'quotes.apps.QuotesConfig'
]

INSTALLED_APPS += [
    'quoterank.apps.QuoterankConfig'
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    # 'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]


ROOT_URLCONF = 'backend.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': ['templates/'],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'backend.wsgi.application'


# Database
# https://docs.djangoproject.com/en/2.2/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2',
        'NAME': 'quotesdev',
        'USER':'quotes',
        'PASSWORD': 'quotes',
        'HOST': 'localhost',
        'PORT': '5432',
    }
}


# Password validation
# https://docs.djangoproject.com/en/2.2/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


# Internationalization
# https://docs.djangoproject.com/en/2.2/topics/i18n/

# LANGUAGE_CODE = 'en-us'
LANGUAGE_CODE = 'ru-RU'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/2.2/howto/static-files/
STATIC_URL = '/static/'

# API Version for API URLs
#
API_VERSION = 1

GOOGLE_APPLICATION_CREDENTIALS = '/Users/ignatev/Downloads/quotes-fcm.json'
GOOGLE_PLAY_API_KEY = 'AIzaSyD4tYv7eoQIbkFNMhIbNAgyWhdoPmzwCPU'
GOOGLE_PLAY_PROJECT_ID = 'quotepuzzle-904ce'
GOOGLE_PLAY_BUNDLE_ID = 'com.quote.mosaic'
GOOGLE_IAP_CREDENTIALS = '/Users/ignatev/Downloads/Quotes Game-3abe0b360f9f.json'

import os
import logging

LOGGING_FORMAT = '%(asctime)-15s [%(levelname)s] %(name)s:  %(message)s'
logging.basicConfig(format=LOGGING_FORMAT)

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler'
        },
    },
    'loggers': {
        # 'django': {
        #     'level': os.getenv('DJANGO_LOG_LEVEL', 'INFO'),
        #     'handlers': ['console']
        # },
        'quotes': {
            'handlers': ['console'],
            'level': os.getenv('QUOTES_LOG_LEVEL', 'INFO'),
            'propagate': True
        }
    },
}


DEFAULT_FILE_STORAGE = 'storages.backends.s3boto3.S3Boto3Storage'

import sys
TEST = 'test' in sys.argv

from .settings_aws import *
