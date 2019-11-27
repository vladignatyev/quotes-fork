from datetime import datetime, timedelta
from django.apps import apps



class InMemoryCacheStorage:
    TTL = 2 * 60 # seconds

    def __init__(self, ttl=None):
        self.objects = {}
        self.objects_ttl = {}
        self.ttl = ttl or self.TTL

    def get_bucket(self, bucket):
        profile_bucket = self.objects.get(bucket, None)
        if profile_bucket is None:
            self.objects[bucket] = {}

        self.objects_ttl[bucket] = datetime.now()
        self.update_ttl()
        return self.objects[bucket]

    def clear_bucket(self, bucket):
        self.objects.pop(bucket, None)
        self.objects_ttl.pop(bucket, None)

    def update_ttl(self):
        items = list(self.objects_ttl.items())
        lt = datetime.now() - timedelta(seconds=self.TTL)
        for k, t in items:
            if t < lt:
                self.clear_bucket(k)


# todo: completely replace this entity by introducing a `lru_cache`-like decorator
class ProfilesDataStorage(InMemoryCacheStorage):
    def get_levels_complete_by_profile_in_category(self, profile_pk, category_pk):
        bucket = self.get_bucket(profile_pk)

        key = f'get_levels_complete_by_profile_in_category({profile_pk}, {category_pk})'

        # result = bucket.get(key, None)
        result = False
        if not result:
            Quote = apps.get_model('quotes.Quote')
            QuoteCompletion = apps.get_model('quotes.QuoteCompletion')

            # complete_by_users_relation_values = Quote.complete_by_users.through.objects.filter(profile=profile_pk).all()
            # quotes_pks = [o.quote_id for o in complete_by_users_relation_values]

            # result = list(Quote.objects.filter(category=category_pk).filter(pk__in=quotes_pks).all())
            quote_completions = QuoteCompletion.objects.filter(profile=profile_pk).values_list('pk', flat=True)
            result = list(Quote.objects.filter(category=category_pk).filter(complete_by_users2__in=quote_completions).all())
            bucket[key] = result

        return result

    def get_levels_complete_by_profile_in_section_count(self, profile_pk, section_pk):
        bucket = self.get_bucket(profile_pk)

        key = f'get_levels_complete_by_profile_in_section_count({profile_pk}, {section_pk})'

        # result = bucket.get(key, None)
        result = False
        if not result:
            Quote = apps.get_model('quotes.Quote')
            QuoteCompletion = apps.get_model('quotes.QuoteCompletion')

            quote_completions = QuoteCompletion.objects.filter(profile=profile_pk).values_list('pk', flat=True)

            result = Quote.objects.filter(complete_by_users2__in=quote_completions).filter(category__section=section_pk).count()
            bucket[key] = result

        return result

    def get_levels_complete_by_profile_in_topic_count(self, profile_pk, topic_pk):
        bucket = self.get_bucket(profile_pk)

        key = f'get_levels_complete_by_profile_in_topic_count({profile_pk}, {topic_pk})'

        # result = bucket.get(key, None)
        result = False
        if not result:
            Quote = apps.get_model('quotes.Quote')
            QuoteCompletion = apps.get_model('quotes.QuoteCompletion')
            
            quote_completions = QuoteCompletion.objects.filter(profile=profile_pk).values_list('pk', flat=True)
            result = Quote.objects.filter(complete_by_users2__in=quote_completions).filter(category__section__topic=topic_pk).count()
            bucket[key] = result

        return result


# PROFILE_DATA_STORAGE = ProfilesDataStorage()



def get_profiles_storage():
    if hasattr(get_profiles_storage, 'instance'):
        return get_profiles_storage.instance
    else:
        inst = get_profiles_storage.instance = ProfilesDataStorage()
        return inst
