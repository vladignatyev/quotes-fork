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


def recharge_profile_on_purchase(sender, instance, created, raw, **kwargs):
    print('Пришел сигнал')
    print(f'{instance} - {instance.status} - {instance.previous_status}')
    if created or raw:
        return
    if instance.status not in (PurchaseStatus.PURCHASED, PurchaseStatus.CANCELLED):
        return
    if instance.status == instance.previous_status:
        return

    purchase = instance

    try:
        app_product = BalanceRechargeProduct.objects.get_by_store_product(purchase.product)

        with transaction.atomic(): # already in transaction
            profile = Profile.objects.select_for_update().get(device_sessions__pk__contains=purchase.device_session.pk)

            if instance.status == PurchaseStatus.PURCHASED:
                profile.balance = profile.balance + app_product.balance_recharge
                profile.save()
            elif instance.status == PurchaseStatus.CANCELLED:
                new_balance = profile.balance - app_product.balance_recharge
                if new_balance < 0:
                    profile.is_banned = True
                profile.balance = new_balance
                profile.save()

        logger.debug('Profile recharged: %s', profile)

    except Profile.DoesNotExist:
        logger.error('Profile matching purchase doesnt exist: %s', purchase)
    except BalanceRechargeProduct.DoesNotExist:
        # purchase related to another Product/Purchase model, so skipping
        logger.debug('Balance recharge doesnot exist by store product: %s', purchase.product)


def unlock_category_on_purchase(sender, instance, created, raw, **kwargs):
    if created or raw:
        return
    if instance.status not in (PurchaseStatus.PURCHASED, PurchaseStatus.CANCELLED):
        return
    purchase = instance
    logger.debug('New valid purchase: %s', purchase)

    session = purchase.device_session

    try:
        unlock = CategoryUnlockPurchase.objects.get(google_play_purchase=purchase)
        if instance.status == PurchaseStatus.PURCHASED and instance.previous_status != PurchaseStatus.PURCHASED:
            unlock.do_unlock()
        elif instance.status == PurchaseStatus.CANCELLED and instance.previous_status == PurchaseStatus.PURCHASED:
            unlock.undo_unlock()
    except CategoryUnlockPurchase.DoesNotExist:
        logger.debug('CategoryUnlockPurchase.DoesNotExist: Google Play Purchase %s', purchase.pk)
        pass
