import pytest
from hamcrest import assert_that, is_, not_none
from skyeng_client import SkyengClient


user_admin = {
    "userName": "admin",
    "firstName": "Admin",
    "lastName": "Systems",
    "birthDate": "14-10-404",
    "gender": "male",
    "email": "admin@skyeng.com",
    "password": "admin",
    "phone": "+79871234567",
    "role": "admin"
}

