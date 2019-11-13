from .models import *


def handle_rank_update(profile, occured_events):
    profile_rank = ProfileRank.objects.create_if_not_exist(profile)
    new_events = profile_rank.update_by_events(occured_events)

    return new_events
