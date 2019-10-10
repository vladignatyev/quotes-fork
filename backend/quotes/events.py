

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

    @classmethod
    def new(cls, name, param):
        return (name, param)

    @classmethod
    def filter_by_name(cls, name, events=[]):
        for event in events:
            event_name, _ = event
            if event_name == name:
                yield event
