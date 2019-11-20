from datetime import datetime

from googleapiclient import discovery
from googleapiclient.errors import HttpError
from google.oauth2.service_account import Credentials


class GoogleIAPStatus:
    def __init__(self, acknowledgement_state,
                       consumption_state,
                       purchase_state,
                       purchase_time_epoch,
                       kind,
                       purchase_type=None):
        self.acknowledgement_state = acknowledgement_state
        self.consumption_state = consumption_state
        self.purchase_state = purchase_state
        self.purchase_time_epoch = purchase_time_epoch
        self.kind = kind
        self.purchase_type = purchase_type

    def is_purchased_for_money(self):
        return self.purchase_type is None and self.is_purchased()

    def is_purchased_for_test(self):
        return self.purchase_type == PurchaseTypes.TEST and self.is_purchased()

    def is_purchased_for_rewarded(self):
        return self.purchase_type == PurchaseTypes.REWARDED and self.is_purchased()

    def is_purchased_for_promo(self):
        return self.purchase_type == PurchaseTypes.PROMO and self.is_purchased()

    def is_purchased(self):
        return self.purchase_state == GoogleIAPPurchaseState.PURCHASED

    def is_cancelled(self):
        return self.purchase_state == GoogleIAPPurchaseState.CANCELLED

    def is_ready(self):
        return self.acknowledgement_state == GoogleIAPAcknowledgementState.ACKNOWLEDGED

    def __str__(self):
        return f'Purchase State: {GoogleIAPPurchaseState.as_text[self.purchase_state]}\r\n' + \
               f'Acknowledgement: {GoogleIAPAcknowledgementState.as_text[self.acknowledgement_state]}\r\n' + \
               f'Consumption: {GoogleIAPConsumptionState.as_text[self.consumption_state]}\r\n' + \
               f'Purchase time: {self.purchase_time_epoch}\r\n' + \
               f'Purchase type: {GoogleIAPPurchaseType.as_text[self.purchase_type]}\r\n'

    @classmethod
    def from_response(cls, response):
        acknowledgement_state = response['acknowledgementState']
        consumption_state = response['consumptionState']

        purchase_state = response['purchaseState']
        purchase_time_epoch = datetime.fromtimestamp(int(response['purchaseTimeMillis']) / 1000.0)

        kind = response['kind']
        assert kind == 'androidpublisher#productPurchase'

        purchase_type = response.get('purchaseType', None)

        return GoogleIAPStatus(acknowledgement_state=acknowledgement_state,
                               consumption_state=consumption_state,
                               purchase_state=purchase_state,
                               purchase_time_epoch=purchase_time_epoch,
                               kind=kind,
                               purchase_type=purchase_type)



class PurchaseValidationError(Exception):
    pass


class GoogleIAPAcknowledgementState:
    YET_TO_BE_ACKNOWLEDGED = 0
    ACKNOWLEDGED = 1

    as_text = {
        0: 'yet to be acknowledged',
        1: 'acknowledged'
    }


class GoogleIAPConsumptionState:
    YET_TO_BE_CONSUMED = 0
    CONSUMED = 1

    as_text = {
        0: 'yet to be consumed',
        1: 'consumed'
    }


class GoogleIAPPurchaseState:
    PURCHASED = 0
    CANCELLED = 1
    PENDING = 2

    as_text = {
        0: 'purchased',
        1: 'cancelled',
        2: 'pending'
    }


class GoogleIAPPurchaseType:
    TEST = 0
    PROMO = 1
    REWARDED = 2

    as_text = {
        None: None,
        0: 'test',
        1: 'promo',
        2: 'rewarded'
    }


def make_credentials_from_file(file_path):
    AUTH_SCOPE = 'https://www.googleapis.com/auth/androidpublisher'

    credentials = Credentials.from_service_account_file(file_path, scopes=[AUTH_SCOPE])
    credentials.expiry = None
    return credentials


def create_request(credentials, package, product_sku, purchase_token):

    service = discovery.build('androidpublisher', 'v3', credentials=credentials)

    products = service.purchases().products()
    request = products.get(packageName=package, productId=product_sku, token=purchase_token)
    return request


def process_response(request):
    try:
        result = request.execute()
        return GoogleIAPStatus.from_response(result)
    except HttpError as e:
        raise PurchaseValidationError(e)
