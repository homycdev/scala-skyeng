import pytest
from skyeng_client import SkyengClient

user_admin = {
    "userName": "ge",
    "firstName": "Admin",
    "lastName": "Systems",
    "birthDate": "14-10-404",
    "gender": "male",
    "email": "admin@skyeng.com",
    "password": "admin",
    "phone": "+79871234567",
    "role": "admin"
}


def dict_filter(u, *ks):
    return {k: v for k, v in list(u.items()) if k in ks}


def login_from_user(u):
    return dict_filter(u, "userName", "password")


@pytest.fixture(scope="session")
def skyeng_client(request):
    return SkyengClient()


def user_context(request, skyeng_client, userName, role):
    user1 = dict(user_admin, **{"userName": userName, "role": role})

    skyeng_client.signup_user(user1)
    skyeng_client.login_user(login_from_user(user1))

    request.addfinalizer(lambda: skyeng_client.delete_user_by_username(userName))

    # return here as a lambda, in case we need to reauthenticate a user, 
    # for example to switch between customer and admin roles
    return lambda: skyeng_client.login_user(login_from_user(user1))


@pytest.fixture(scope="function")
def teacher_context(request, skyeng_client):
    return user_context(request, skyeng_client, "teacher", "teacher")


@pytest.fixture(scope="function")
def admin_context(request, skyeng_client):
    return user_context(request, skyeng_client, "admin", "admin")


@pytest.fixture(scope="function")
def customer_context(request, skyeng_client):
    return user_context(request, skyeng_client, "studentOne", "student")
