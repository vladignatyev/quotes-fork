from django.conf import settings

from googleapiclient.errors import HttpError


class PurchaseValidationError(Exception):
    pass


def make_credentials_from_file(file_path):
    AUTH_SCOPE = 'https://www.googleapis.com/auth/androidpublisher'
    from google.oauth2.service_account import Credentials

    credentials = Credentials.from_service_account_file(file_path, scopes=[AUTH_SCOPE])
    return credentials


def create_request(credentials, package, product_sku, purchase_token):
    from googleapiclient import discovery
    service = discovery.build('androidpublisher', 'v3', credentials=credentials)

    products = service.purchases().products()
    request = products.get(packageName=package, productId=product_sku, token=purchase_token)
    return request


credentials = make_credentials_from_file(settings.GOOGLE_APPLICATION_CREDENTIALS)
request = create_request(credentials, 'com.quote.mosaic', 'get_20_coins', 'mlpnjomdpnhpbffhanohcknh.AO-J1OzLAejHvpx838DvYJ0qBKzI-a1vDM638cYyvTgMacMr0c6oElhEaBkwAONS3-EMeAC8BhJuZVH1XwwpEmArwuo16iV_wqWd3ef63YDtIzSscQQzOY8')

try:
    result = request.execute()
    print(result)
except HttpError as e:
    raise PurchaseValidationError(e)
