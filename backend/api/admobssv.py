import base64
import urllib
import requests


from base64 import b64decode

import json

from urllib.parse import parse_qsl

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.serialization import load_der_parameters
from cryptography.hazmat.primitives.asymmetric.utils import decode_dss_signature

GOOGLE_ADMOB_KEY_SERVER = 'https://www.gstatic.com/admob/reward/verifier-keys.json'



class AdMobRewardVerificator:
    class QueryStringParsingError(Exception): pass
    class UnknownKeyId(Exception): pass

    SIGNATURE_PARAM_NAME = "signature="

    def __init__(self, key_server=GOOGLE_ADMOB_KEY_SERVER):
        self.key_server = key_server

    def verify_from_query_string(self, qs):
        content_to_verify, signature, key_id = self._parse_query_string(qs)
        key_pem = self._get_pub_key_by_id(key_id)
        public_key = default_backend().load_pem_public_key(key_pem)

        try:
            public_key.verify(signature, content_to_verify, ec.ECDSA(hashes.SHA256()))
        except:
            return False
        return True

    def get_data(self, qs):
        as_dict = dict(parse_qsl(qs))
        return {
            'ad_network': as_dict.get('ad_network'),
            'ad_unit': as_dict.get('ad_unit'),
            'custom_data': as_dict.get('custom_data'),
            'reward_amount': as_dict.get('reward_amount'),
            'reward_item': as_dict.get('reward_item'),
            'timestamp': as_dict.get('timestamp'),
            'transaction_id': as_dict.get('transaction_id'),
            'user_id': as_dict.get('user_id'),
        }

    def _load_keys_from_server(self):
        r = requests.get(self.key_server)
        as_json = json.loads(r.content)

        result = [(str(kv['keyId']), kv['pem'].replace("\\n", '\n').encode('ascii')) for kv in as_json['keys'] ]

        return dict(result)

    def _get_pub_key_by_id(self, key_id):
        # TODO: request the caching backend first, if unable to find, then upload _add_ key in cache
        # TODO: Extract into separate KeyStorage entity
        pub_keys = self._load_keys_from_server()
        found_pem = pub_keys.get(key_id)

        if found_pem is None:
            raise self.UnknownKeyId(f"The Key ID provided in request ({key_id}) doesn't correspond to any of known.")

        return found_pem

    def _parse_query_string(self, qs):
        """
        See reference: https://developers.google.com/admob/android/rewarded-video-ssv#manual_verification_of_rewarded_video_ssv
        """
        try:
            as_dict = dict(parse_qsl(qs))

            key_id = as_dict.get('key_id')
            signature = base64.urlsafe_b64decode(as_dict.get('signature') + '===')

            splitted = qs.split('?')[-1].split('&' + self.SIGNATURE_PARAM_NAME)

            content_to_verify = urllib.parse.unquote(splitted[0]).encode('utf-8')

            return content_to_verify, signature, key_id
        except:
            raise self.QueryStringParsingError(f"Unable to parse the provided query string: {qs}")


if __name__ == '__main__':
    qs =  '?ad_network=5450213213286189855&ad_unit=1234567890&timestamp=1580923772739&transaction_id=123456789&signature=MEQCIGM6J3YkXH26c9mmJuR7ipyyeuhcKmZs1eK7VMlzAWw1AiBelO_Bux6wl0zmIMNMyqbvKjhCCxPUbcf03aFvKAIiCw&key_id=3335741209'
    qs = '?ad_network=5450213213286189855&ad_unit=5032174875&custom_data=balance_recharge%3A2181a4c8-4522-4af2-bddc-ee86c4ca9a84&reward_amount=1&reward_item=Next%20word&timestamp=1581014198904&transaction_id=25c00f9eb183495519a50896dd1a1c9a&signature=MEQCIH2aQ0DZmhEDKUeqRQH0kRwIl0Z0Pm5_4mYkwK1XV_Z4AiBeI2fYPphEVhG4j1ETcLvyt0dU2Ypj1-pt-DeYyMdO8w&key_id=3335741209'
    a = AdMobRewardVerificator()
    print(a.verify_from_query_string(qs))
