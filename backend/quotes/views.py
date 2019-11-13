import uuid
import json

from django.db import models
from django.apps import apps
from django.http import Http404, HttpResponse, JsonResponse
# from django.views.generic import ListView, View, DetailView
from django.forms.models import model_to_dict
from django.views.decorators.http import require_http_methods
from django.urls import reverse
from django import forms

from .models import *


from api.views import AuthenticationForm, AuthenticateView, SafeView, PushSubscriptionView


from .utils import json_response

class BaseView(SafeView):
    def mixin_authorized(self, request):
        request.user_profile = Profile.objects.get_by_session(request.device_session)
        request.user_profile.save()


class TopicDetailView(BaseView):
    def get(self, request, *args, **kwargs):
        try:
            topic = Topic.objects.get(pk=kwargs['pk'], hidden=False)
            res_obj = {
                'objects': [topic.get_flat(profile=self.request.user_profile)],
                'meta': {}
            }
            return json_response(res_obj)
        except Topic.DoesNotExist:
            raise Http404()


class TopicListView(BaseView):
    queryset = Topic.objects.filter(hidden=False)
    ordering = '-pk'
    detail_url = 'topic-detail'

    def flatten(self, topic):
        return {
            'id': topic.pk,
            'title': topic.title,
            'bonus_reward': topic.bonus_reward,
            'on_complete_achievement': topic.on_complete_achievement.pk if topic.on_complete_achievement else None,
            'uri': reverse(self.detail_url, kwargs={'pk': topic.pk})
        }

    def get(self, request, *args, **kwargs):
        objects = [ self.flatten(o) for o in self.queryset.order_by(self.ordering) ]

        res_dict = {
            "objects": objects,
            "meta": {
                # "limit": self.paginate_by,
                # # "next": null,
                # "offset": 0,
                # # "previous": null,
                # "total_count": p.count,
                # "num_pages": p.num_pages
            }
        }

        return json_response(res_dict)


class LevelsListView(BaseView):
    def get(self, request, *args, **kwargs):
        category_pk = kwargs.get('category_pk', None)

        try:
            query_result = get_levels(category_pk=category_pk,
                                      profile=self.request.user_profile)
        except QuoteCategory.DoesNotExist:
            return HttpResponse(status=404)

        if query_result is None:
            return HttpResponse(status=402)

        levels = list(query_result)

        if levels is None:
            return HttpResponse(status=404)

        res_dict = {
            "objects": levels,
            "meta": {}
        }
        return json_response(res_dict)


class LevelCompleteView(BaseView):
    def post(self, request, *args, **kwargs):
        level_pk = kwargs.get('level_pk', None)

        try:
            quote = Quote.objects.get(pk=level_pk)
            # todo: future: if not quote.solved(solution): return HttpResponse(status=422)
            events = quote.mark_complete(profile=self.request.user_profile)

            res_dict = {
                "objects": events,
                "meta": {}
            }

            return json_response(res_dict)
        except Quote.NoAccess:
            return HttpResponse(status=402)
        except Quote.DoesNotExist:
            return HttpResponse(status=404)


class ProfileView(BaseView):
    def get(self, request, *args, **kwargs):
        try:
            flat_profile = self.request.user_profile.get_flat()

            res_dict = {
                "objects": [flat_profile],
                "meta": {}
            }

            return json_response(res_dict)
        except ValueError:
            return HttpResponse(status=404)

class FormBasedView(BaseView):
    PAYLOAD_MAX_LENGTH = 512

    form_cls = None

    def make_form_from_request(self, request):
        data = request.body[:self.PAYLOAD_MAX_LENGTH]
        try:
            deserialized = json.loads(data)
        except json.decoder.JSONDecodeError:
            deserialized = None
        return self.form_cls(deserialized)


class GooglePlayIAPForm(forms.Form):
    order_id = forms.CharField(label='Google Play Billing Order ID', max_length=256)
    purchase_token = forms.CharField(label='Google Play Billing Purchase Token', max_length=256)

class RechargeForm(GooglePlayIAPForm):
    balance_recharge = forms.UUIDField(label='Related BalanceRechargeProduct ID')

class CategoryUnlockForm(GooglePlayIAPForm):
    category_id = forms.IntegerField(label='ID of the category to unlock by purchase', min_value=0)
    google_play_product_id = forms.UUIDField(label='Related Google Play Product ID')


class PurchaseCoinsView(FormBasedView):
    form_cls = RechargeForm

    def post(self, request, *args, **kwargs):
        form = self.make_form_from_request(request)
        if not form or not form.is_valid():
            return HttpResponse(status=400)

        Purchase = apps.get_model('api.GooglePlayIAPPurchase')

        cleaned_data = form.clean()

        try:
            existing_purchase = Purchase.objects.get(order_id=cleaned_data['order_id'],
                                                  purchase_token=cleaned_data['purchase_token'])
            res_dict = {
                "objects":[{
                    "purchase_id": existing_purchase.id
                }],
                "meta": {}
            }
            return json_response(res_dict)
        except Purchase.DoesNotExist:
            pass

        try:
            recharge = BalanceRechargeProduct.objects.get(id=cleaned_data['balance_recharge'])

            purchase = Purchase.objects.create(product=recharge.google_play_product,
                                               device_session=request.device_session,
                                               order_id=cleaned_data['order_id'],
                                               purchase_token=cleaned_data['purchase_token'])
            res_dict = {
                "objects":[{
                    "purchase_id": purchase.id
                }],
                "meta": {}
            }
            return json_response(res_dict)
        except BalanceRechargeProduct.DoesNotExist:
            return HttpResponse(status=404)

        return HttpResponse(status=501)


class PurchaseUnlockView(FormBasedView):
    form_cls = CategoryUnlockForm

    def post(self, request, *args, **kwargs):
        form = self.make_form_from_request(request)

        if not form or not form.is_valid():
            return HttpResponse(status=400)

        Purchase = apps.get_model('api.GooglePlayIAPPurchase')
        GooglePlayProduct = apps.get_model('api.GooglePlayProduct')

        cleaned_data = form.clean()

        try:
            category_to_unlock = QuoteCategory.objects.get(pk=cleaned_data['category_id'])

            if category_to_unlock.is_available_to_user(self.request.user_profile):
                return HttpResponse(status=422)
        except QuoteCategory.DoesNotExist:
            return HttpResponse(status=404)

        try:
            existing_purchase = Purchase.objects.get(order_id=cleaned_data['order_id'],
                                                     purchase_token=cleaned_data['purchase_token'])
            res_dict = {
                "objects":[{
                    "purchase_id": existing_purchase.id
                }],
                "meta": {}
            }
            return json_response(res_dict)
        except Purchase.DoesNotExist:
            pass

        try:
            google_play_product = GooglePlayProduct.objects.get(pk=cleaned_data['google_play_product_id'])
            purchase = Purchase.objects.create(product=google_play_product,
                                               device_session=request.device_session,
                                               order_id=cleaned_data['order_id'],
                                               purchase_token=cleaned_data['purchase_token'])

            # todo: redesign in conformance with #16.
            unlock = CategoryUnlockPurchase.objects.create(type=CategoryUnlockTypes.UNLOCK_BY_PURCHASE,
                                                           profile=self.request.user_profile,
                                                           category_to_unlock=category_to_unlock,
                                                           google_play_purchase=purchase)

            res_dict = {
                "objects":[{
                    "purchase_id": purchase.id
                }],
                "meta": {}
            }
            return json_response(res_dict)
        except GooglePlayProduct.DoesNotExist:
            return HttpResponse(status=404)

        return HttpResponse(status=501)


class PurchaseStatusView(BaseView):
    # todo: move to API
    def get(self, request, *args, **kwargs):
        purchase_id = kwargs['purchase_id']
        Purchase = apps.get_model('api.GooglePlayIAPPurchase')

        try:
            purchase = Purchase.objects.get(id=purchase_id)
            res_dict = {
                "objects":[{
                    "status": purchase.status
                }],
                "meta": {}
            }
            return json_response(res_dict)
        except Purchase.DoesNotExist:
            pass

        raise Http404()


class PurchaseableProductsListView(BaseView):
    def get(self, request, *args, **kwargs):
        balance_recharges = BalanceRechargeProduct.objects.all()
        balance_recharge_flat_list = [o.get_flat() for o in balance_recharges]

        balance_recharge_play_products_skus = [o['sku'] for o in balance_recharge_flat_list]

        all_play_products = apps.get_model('api.GooglePlayProduct').objects.all()
        google_play_products_flat_list = [{'id': o.id, 'sku': o.sku} for o in all_play_products]
        google_play_products_flat_list_filtered = list(filter(lambda o: o['sku'] not in balance_recharge_play_products_skus, google_play_products_flat_list))


        products = {
            'other_products': google_play_products_flat_list_filtered,
            'recharge_products': balance_recharge_flat_list
        }
        res_dict = {
            "objects": [products],
            "meta": {}
        }
        return json_response(res_dict)


class CategoryUnlockView(BaseView):
    def post(self, request, *args, **kwargs):
        try:
            category = QuoteCategory.objects.get(pk=kwargs['category_pk'])
            # avoid creating dupes, see issue #16 and related test
            unlock, created = CategoryUnlockPurchase.objects.get_or_create(type=CategoryUnlockTypes.UNLOCK_FOR_COINS,
                                                  profile=self.request.user_profile,
                                                  category_to_unlock=category)
            unlock.do_unlock()
        except CategoryUnlockPurchase.InsufficientFunds:
            return HttpResponse(status=402)
        except QuoteCategory.DoesNotExist:
            raise Http404()
        except CategoryUnlockPurchase.AlreadyAvailable:
            pass

        return HttpResponse(status=200)

class AchievementListView(BaseView):
    def flattened(self, iterable):
        result = [ None ] * len(iterable)

        for i, o in enumerate(iterable):
            result[i] = {
                'achievement_id': o.achievement.pk,
                'received_at': o.received_at.strftime('%Y-%m-%dT%H:%M:%S%z')
            }
        return result

    def filter(self):
        return AchievementReceiving.objects.filter(profile=self.request.user_profile)

    def get(self, request, *args, **kwargs):
        achievements = self.flattened(self.filter())

        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)


class AllAchievementListView(BaseView):
    fields = ('icon', 'title', 'received_text', 'description_text')

    def flattened(self, iterable):
        result = [ None ] * len(iterable)

        for i, o in enumerate(iterable):
            # result[i] = model_to_dict(o, fields=self.fields)
            result[i] = {
                'id': o.pk,
                'title': o.title,
                'icon': o.icon,
                'received_text': o.received_text,
                'description_text': o.description_text
            }
        return result

    def get(self, request, *args, **kwargs):
        achievements = self.flattened(Achievement.objects.all())

        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)


class QuotesAuthenticationForm(AuthenticationForm):
    nickname = forms.CharField(label='user\'s nickname', max_length=256, required=False)


class QuotesAuthenticateView(AuthenticateView):
    def __init__(self, *args, **kwargs):
        super(QuotesAuthenticateView, self).__init__(form_cls=QuotesAuthenticationForm, *args, **kwargs)

    def add_nickname_to_profile(self):
        profile = Profile.objects.get_by_session(self.device_session)
        profile.nickname = self.cleaned_data['nickname']
        profile.save()

    def respond_authenticated(self):
        self.add_nickname_to_profile()

        return JsonResponse({
            'auth_token': self.device_session.auth_token
        })


class PushNotificationSubscriptionView(PushSubscriptionView):
    pass
