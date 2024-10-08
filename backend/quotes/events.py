

class UserEvents:
    COMPLETE = 'generic_complete'

    TOPIC_COMPLETE = 'topic_complete'
    SECTION_COMPLETE = 'section_complete'
    CATEGORY_COMPLETE = 'category_complete'
    LEVEL_COMPLETE = 'level_complete'

    RECEIVED_PER_LEVEL_REWARD = 'received_per_level_reward'
    RECEIVED_PER_CATEGORY_REWARD = 'received_per_category_reward'
    RECEIVED_PER_SECTION_REWARD = 'received_per_section_reward'
    RECEIVED_PER_TOPIC_REWARD = 'received_per_topic_reward'

    RECEIVED_LEVEL_ACHIEVEMENT = 'received_level_achievement'
    RECEIVED_CATEGORY_ACHIEVEMENT = 'received_category_achievement'
    RECEIVED_SECTION_ACHIEVEMENT = 'received_section_achievement'
    RECEIVED_TOPIC_ACHIEVEMENT = 'received_topic_achievement'

    RECEIVED_GENERIC_ACHIEVEMENT = 'received_generic_achievement'
    RECEIVED_GENERIC_REWARD = 'received_generic_reward'

    QUOTERANK_POSITION_IN_TOP_CHANGED = 'quoterank_position_in_top_changed'
    QUOTERANK_RANK_CHANGED = 'quoterank_rank_changed'

    HINT_USED_AUTHOR = 'hints_used_author'
    HINT_USED_NEXT_WORD = 'hints_used_next_word'
    HINT_USED_SKIPPED_LEVEL = 'hints_used_skipped_level'


    @classmethod
    def new(cls, name, param):
        return (name, param)

    @classmethod
    def filter_by_name(cls, name, events):
        return filter(lambda e: e[0] == name, events)
