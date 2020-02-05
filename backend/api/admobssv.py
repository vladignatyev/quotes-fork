import requests
import json

from urllib.parse import parse_qsl

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import ec


GOOGLE_ADMOB_KEY_SERVER = 'https://www.gstatic.com/admob/reward/verifier-keys.json'


#
# pubkey = default_backend().load_pem_public_key(load_keys_from_server()[3335741209])

# print(load_keys_from_server())




class AdMobRewardVerificator:
    class QueryStringParsingError(Exception): pass
    class UnknownKeyId(Exception): pass

    SIGNATURE_PARAM_NAME = "signature="

    def __init__(self, key_server=GOOGLE_ADMOB_KEY_SERVER):
        self.key_server = key_server

    def verify_from_query_string(self, qs):
        content_to_verify, signature, key_id = self._parse_query_string(qs)
        key_pem = self._get_pub_key_by_id(key_id)

        pubkey = default_backend().load_pem_public_key(key_pem)
        # verify

        return False

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

        result = [(kv['keyId'], kv['pem'].replace("\\n", '\n')) for kv in as_json['keys'] ]
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
            signature = as_dict.get('signature')

            splitted = qs.split('?')[-1].split('&' + self.SIGNATURE_PARAM_NAME)

            content_to_verify = splitted[0]

            return content_to_verify, signature, key_id
        except:
            raise self.QueryStringParsingError(f"Unable to parse the provided query string: {qs}")


if __name__ == '__main__':
    a = AdMobRewardVerificator()
    a.verify_from_query_string('?ad_network=54...55&ad_unit=12345678&reward_amount=10&reward_item=coins&timestamp=150777823&transaction_id=12...DEF&user_id=1234567&signature=ME...Z1c&key_id=1268887')
