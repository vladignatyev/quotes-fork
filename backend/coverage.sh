coverage erase
coverage run --source='.' manage.py test
coverage html
open htmlcov/index.html
