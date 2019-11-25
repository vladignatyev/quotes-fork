from functools import lru_cache
from django.conf import settings

from .models import GooglePlayIAPPurchase, AppStoreIAPPurchase, Credentials, PurchaseStatus


from api import google
# import make_credentials_from_file, create_request, process_response, PurchaseValidationError

class PurchaseValidationError(Exception): pass


@lru_cache(maxsize=1)
def create_google_validator():
    credentials = google.make_credentials_from_file(settings.GOOGLE_IAP_CREDENTIALS)
    app_credentials = Credentials.objects.get()
    package_name = settings.GOOGLE_PLAY_BUNDLE_ID or app_credentials.google_play_bundle_id
    validator = GoogleIAPValidator(credentials=credentials, package_name=package_name)
    return validator


class BaseIAPValidator:
    def __init__(self, credentials, *args, **kwargs):
        self.credentials = credentials


class GoogleIAPValidator(BaseIAPValidator):
    def __init__(self, credentials, *args, **kwargs):
        super(GoogleIAPValidator, self).__init__(credentials, *args, **kwargs)
        self.package_name = kwargs['package_name']
        assert self.package_name != ''

    def validate(self, purchase):
        request = google.create_request(self.credentials,
                                        self.package_name,
                                        purchase.product.sku,
                                        purchase.purchase_token)
        try:
            status = google.process_response(request)

            return self._apply_status_to_purchase_state(status, purchase)
        except google.PurchaseValidationError as e:
            purchase.status = PurchaseStatus.INVALID
            # raise PurchaseValidationError(e)

    def _apply_status_to_purchase_state(self, status, purchase):
        if not status.is_ready():
            return
        if status.is_purchased():
            purchase.previous_status = purchase.status
            purchase.status = PurchaseStatus.PURCHASED
        elif status.is_cancelled():
            purchase.previous_status = purchase.status
            purchase.status = PurchaseStatus.CANCELLED
