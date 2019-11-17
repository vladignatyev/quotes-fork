from .models import *


def handle_rank_update(profile, occured_events):
    profile_rank, created = ProfileRank.objects.get_or_create(profile=profile)
    new_events = profile_rank.update_by_events(occured_events)

    return new_events
