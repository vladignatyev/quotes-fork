from django.http import JsonResponse

def _truncate(text, length=50, suffix='...'):
    '''
    >>> _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚')
    >>> Карл у Кла˚˚˚
    '''
    return f'{text}'[:length] + (suffix if len(text) > length else '')


def json_response(res_dict):
    return JsonResponse(res_dict, json_dumps_params={'indent': 4, 'ensure_ascii': False})
