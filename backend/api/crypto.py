import hashlib
import os

from django.conf import settings


def generate_secret():
    '''
    Here we generate random sequence of bytes by OS provided RNG.
    We need a plain valid a-z|0-9 string, so we use SHA256
    to compress the source sequence into 256-bit pseudorandom sequence.

    Since we don't actually know the quality of OS's RNG,
    we do add a padding by SECRET_KEY provided in web application settings.
    '''
    h = hashlib.sha256()
    h.update(settings.SECRET_KEY.encode('utf-8'))
    h.update(os.urandom(32))
    return h.hexdigest()
