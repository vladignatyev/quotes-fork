from .models import *


# todo: replace when auth get done
def create_profile_on_new_device_session(sender, instance, created, **kwargs):
    if not created:
        return
    session = instance
    profile = Profile.objects.create()
    profile.device_sessions.set([session,])
    profile.save()


# TODO: - extract core logic: find responsible for processing purchase and then call him to process a purchase
# TODO: - guarantee that every purchase has been processed
def recharge_profile_on_purchase(sender, instance, created, **kwargs):
    if created:
        return
    if instance.status != PurchaseStatus.VALID:
        return
    purchase = instance

    try:
        app_product = BalanceRechargeProduct.objects.get_by_store_product(purchase.product)

        profile_to_recharge = Profile.objects.get_by_session(purchase.device_session)
        profile_to_recharge.balance = profile_to_recharge.balance + app_product.balance_recharge
        profile_to_recharge.save()
    except BalanceRechargeProduct.DoesNotExist:
        pass  # purchase related to another Product/Purchase model, so skipping


def unlock_category_on_purchase(sender, instance, created, **kwargs):
    if created:
        return
    if instance.status != PurchaseStatus.VALID:
        return
    purchase = instance

    session = purchase.device_session

    try:
        unlock_category_purchase = CategoryUnlockPurchase.objects.get(google_play_purchase=purchase)

        profile_to_unlock_for = unlock_category_purchase.profile
        unlock_category_purchase.category_to_unlock.available_to_users.add(profile_to_unlock_for)
        unlock_category_purchase.save()
    except CategoryUnlockPurchase.DoesNotExist:
        pass
