# PYTHONPATH=./backend/ DJANGO_SETTINGS_MODULE=backend.settings python generate_auth_credentials.py

import json
import django
from django.utils import timezone

django.setup()

from api.models import *

device_token = generate_secret()
timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')

payload = {
    'device_token': device_token,
    'timestamp': timestamp,
    'signature': generate_signature(device_token, timestamp, shared_secret='0f1109c3b6e8acaa45988b1bd783f13fbcde16125067d7f8f2bab16ffb22c0db'),
    'nickname': f'тестировщик-{device_token[:4]}..{device_token[-4:]}'
}

print(json.dumps(payload, ensure_ascii=False))
