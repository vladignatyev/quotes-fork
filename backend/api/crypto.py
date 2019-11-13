import os
import hashlib
import hmac
import codecs

# from django.conf import settings


# settings.SECRET_KEY

def crypto_generate_secret(secret_key):
    '''
    Here we generate random sequence of bytes by OS provided RNG.
    We need a plain valid a-z|0-9 string, so we use SHA256
    to compress the source sequence into 256-bit pseudorandom sequence.

    Since we don't actually know the quality of OS's RNG,
    we do add a padding by SECRET_KEY provided in web application settings.
    '''
    h = hashlib.sha256()
    h.update(secret_key.encode('utf-8'))
    h.update(os.urandom(32))
    return h.hexdigest()


def crypto_generate_signature(device_token, timestamp, shared_secret=None):
    '''
    Generate deterministic signature.
    The algorithm should be implemented on client too,
    to be able to sign requests to authentication API.
    '''
    h = hashlib.sha256()
    h.update(shared_secret.encode('utf-8'))
    masked_shared_secret = h.hexdigest()

    sequence = f'{device_token}|{timestamp}'

    h2 = hashlib.sha256()
    h2.update(masked_shared_secret.encode('utf-8'))
    h2.update(sequence.encode('utf-8'))

    signature = h2.hexdigest()
    return signature


def crypto_generate_auth_token(secret_key, server_secret):
    random_value = str(crypto_generate_secret(secret_key))
    server_sig = str(crypto_sign_auth_token(random_value, server_secret))
    return random_value + server_sig


def crypto_sign_auth_token(auth_token_payload, server_secret):
    return hmac.new(codecs.encode(server_secret), codecs.encode(auth_token_payload), digestmod=hashlib.sha256).hexdigest()[:16]


def crypto_check_signature(device_token, timestamp, signature, shared_secret):
    return crypto_generate_signature(device_token, timestamp, shared_secret) == signature


def crypto_check_auth_token(auth_token, server_secret):
    if auth_token is None or auth_token == '':
        return False

    auth_token_payload = auth_token[0:-16]
    server_sig = auth_token[-16:]

    return crypto_sign_auth_token(auth_token_payload, server_secret) == server_sig
