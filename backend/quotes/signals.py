import logging
logger = logging.getLogger(__name__)

from .models import *

from django.db import transaction


# todo: replace when auth get done
def create_profile_on_new_device_session(sender, instance, created, **kwargs):
    if not created:
        return
    session = instance
    profile = Profile.objects.create()
    profile.device_sessions.set([session,])
    profile.settings = GameBalance.objects.get_actual()
    profile.save()

    logger.debug('Created profile with device session: %s', session)
    logger.debug('Profile: %s', profile)


def consume_product_on_purchase(sender, instance, created, raw, **kwargs):
    if created or raw:
        return
    if instance.status not in (PurchaseStatus.PURCHASED, PurchaseStatus.CANCELLED):
        return
    if instance.status == instance.previous_status:
        return

    purchase = instance



    try:
        d = PurchaseProductDiscovery()
        app_product = d.find_product_by_purchase(instance)
        # app_product = BalanceRechargeProduct.objects.get_by_store_product(purchase.product)

        # with transaction.atomic(): # already in transaction
        profile = Profile.objects.get(device_sessions__pk__contains=purchase.device_session.pk)

        if instance.status == PurchaseStatus.PURCHASED:
            app_product.consume_by_profile(profile, purchase=instance)
        elif instance.status == PurchaseStatus.CANCELLED:
            app_product.unconsume_by_profile(profile, purchase=instance)

        logger.debug('Profile %s consumed product: %s', profile, app_product)

    except Profile.DoesNotExist:
        logger.error('Profile matching purchase doesnt exist: %s', purchase)
    except BalanceRechargeProduct.DoesNotExist:
        # purchase related to another Product/Purchase model, so skipping
        logger.debug('Balance recharge doesnot exist by store product: %s', purchase.product)
    except DoubleUpProduct.DoesNotExist:
        # purchase related to another Product/Purchase model, so skipping
        logger.debug('DoubleUpProduct doesnot exist by store product: %s', purchase.product)
#
#
# def unlock_category_on_purchase(sender, instance, created, raw, **kwargs):
#     if created or raw:
#         return
#     if instance.status not in (PurchaseStatus.PURCHASED, PurchaseStatus.CANCELLED):
#         return
#     purchase = instance
#     logger.debug('New valid purchase: %s', purchase)
#
#     session = purchase.device_session
#
#     try:
#         unlock = CategoryUnlockPurchase.objects.get(google_play_purchase=purchase)
#         if instance.status == PurchaseStatus.PURCHASED and instance.previous_status != PurchaseStatus.PURCHASED:
#             unlock.do_unlock()
#         elif instance.status == PurchaseStatus.CANCELLED and instance.previous_status == PurchaseStatus.PURCHASED:
#             unlock.undo_unlock()
#     except CategoryUnlockPurchase.DoesNotExist:
#         logger.debug('CategoryUnlockPurchase.DoesNotExist: Google Play Purchase %s', purchase.pk)
#         pass
