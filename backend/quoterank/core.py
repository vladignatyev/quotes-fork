from datetime import datetime
from math import exp

from django.utils import timezone


def gompertz(days, c=1.1, a=-0.2):
    """
    Calculate Gompertz sigmoide-like function

    FooPlot: http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiJleHAoLTEuMSooZXhwKC0wLjIqeCktMSkpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiMTUuMDc5OTk5OTk5OTk5NjY4IiwiNDEuMDc5OTk5OTk5OTk5NzY0IiwiLTUuNTk5OTk5OTk5OTk5OTkyNSIsIjEwLjQwMDAwMDAwMDAwMDAxMyJdfV0-
    Wiki: https://en.wikipedia.org/wiki/Gompertz_function
    """
    t = days
    return exp(-c * (exp(a * t) - 1.0))


_reward_fields = {
    'total_quote_reward': 8.0,
    'total_category_reward': 13.0,
    'total_section_reward': 21.0,
    'total_topic_reward': 34.0
}

_ach_fields = {
    'total_quote_achievements_received': 89.0,
    'total_category_achievements_received': 144.0,
    'total_section_achievements_received': 233.0,
    'total_topic_achievements_received': 377.0
}

_time_field = 'last_update'


def get_days_from_last_update(last_update):
    dt = timezone.now() - last_update
    return int(dt.days)


class qrstub:
    def __init__(self, *args, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


class Stub(qrstub):
    def __init__(self):
        v = dict([ (k, 0) for k in _reward_fields.keys()])
        v.update(dict([ (k, 0) for k in _ach_fields.keys()]))
        v[_time_field] = datetime.now()
        super(Stub, self).__init__(**v)



def calculate_quote_rank_v1_0(o):
    """
    o -- object with fields:
        total_quote_reward
        total_category_reward
        total_section_reward
        total_topic_reward

        total_quote_achievements_received
        total_category_achievements_received
        total_section_achievements_received
        total_topic_achievements_received

        last_update
    """

    days = get_days_from_last_update(getattr(o, _time_field))

    points_for_rewards = sum([_reward_coeff * float(getattr(o, k)) for k, _reward_coeff in _reward_fields.items()])
    points_for_achievements = sum([_achievements_coeff * float(getattr(o, k)) for k, _achievements_coeff in _ach_fields.items()])

    rank = round(gompertz(days) * (points_for_rewards + points_for_achievements))

    return rank
