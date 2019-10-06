# PYTHONPATH=./backend/ DJANGO_SETTINGS_MODULE=backend.settings python generate_auth_credentials.py

import json
import django
from django.utils import timezone

django.setup()

from api.models import *

device_token = 'sometesttoken'
timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')

payload = {
    'device_token': device_token,
    'timestamp': timestamp,
    'signature': generate_signature(device_token, timestamp),
    'nickname': 'тестировщик'
}

print(json.dumps(payload, ensure_ascii=False))
