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


def generate_signature(device_token, timestamp):
    shared_secret = get_shared_secret()

    h = hashlib.sha256()
    h.update(shared_secret)
    masked_shared_secret = h.hexdigest()

    sequence = [str(device_token), str(timestamp)].join('|')

    h2 = hashlib.sha256()
    h2.update(masked_shared_secret)
    h2.update(sequence)

    signature = h2.hexdigest()
    return signature


def check_signature(device_token, timestamp, signature):
    return generate_signature(device_token, timestamp) == signature
