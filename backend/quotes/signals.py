from .models import *


# todo: replace when auth get done
def create_profile_on_new_device_session(sender, instance, created, **kwargs):
    if not created:
        return
    session = instance
    profile = Profile.objects.create()
    profile.device_sessions.set([session,])
    profile.save()

def recharge_profile_on_purchase(sender, instance, created, **kwargs):
    if created:
        return
    if instance.status != PurchaseStatus.VALID:
        return
    purchase = instance
    session = purchase.device_session
    # app_product = Product.objects.filter(google_play_product__pk=purchase.product.pk)[0]
    app_product = Product.objects.get_by_store_product(purchase.product)

    profile_to_recharge = Profile.objects.get_by_session(session)
    profile_to_recharge.balance = profile_to_recharge.balance + app_product.balance_recharge
    profile_to_recharge.save()
