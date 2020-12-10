import pytest
from hamcrest import assert_that, is_, not_none
from skyeng_client import SkyengClient

user_admin = {
    "userName": "as",
    "firstName": "as",
    "lastName": "Systems",
    "birthDate": "14-10-404",
    "gender": "male",
    "email": "admin@skyeng.com",
    "password": "admin",
    "phone": "+79871234567",
    "role": "admin"
}

user_student_one = dict(user_admin, **{"userName": "studentOne", "role": "student", "password": "student"})
user_student_two = dict(user_admin, **{"userName": "studentTwo", "role": "student", "password": "student"})


def test_signup(skyeng_client):
    response = skyeng_client.signup_user(user_admin)

    assert_that(response.status_code, is_(200))
    new_user = response.json()
    assert_that(new_user['id'], not_none())


def test_login(skyeng_client):
    response = skyeng_client.login_user(user_admin)

    assert_that(response.status_code, is_(200))
    new_user = response.json()
    assert_that(new_user['id'], not_none())
    assert_that(new_user['userName'], is_(user_admin['userName']))

    incorrect_password = dict(user_admin, **{'password': 'incorrect_password'})
    bad_pass_resp = skyeng_client.login_user(incorrect_password)
    assert_that(bad_pass_resp.status_code, is_(400))


def test_signup_existing_user(skyeng_client):
    response = skyeng_client.signup_user(user_student_one)

    assert_that(response.status_code, is_(200))
    new_user = response.json()
    assert_that(new_user['id'], not_none())

    # Attempt to create a user with the same name results in a conflict
    response = skyeng_client.signup_user(user_student_one)
    assert_that(response.status_code, is_(409))


def test_user_by_username(skyeng_client):
    response = skyeng_client.find_user_by_name(user_student_one['userName'])

    resp_user = response.json()

    assert_that(resp_user['userName'], is_(user_student_one['userName']))


def test_list_users( skyeng_client):
    response = skyeng_client.list_users()

    users = response.json()

    assert_that(users[0]['firstName'], is_('Admin'))


def test_list_users_paginated( skyeng_client):
    response = skyeng_client.list_users(pageSize=10, offset=0)

    users = response.json()

    assert_that(users[0]['firstName'], is_('Admin'))


def test_update_user( skyeng_client):
    new_last_name = "Wicked"
    response_lookup = skyeng_client.find_user_by_name(user_student_one['userName'])

    resp_user = response_lookup.json()

    update_user = dict(resp_user, **{"lastName": new_last_name})

    response_update = skyeng_client.update_user(update_user)
    assert_that(response_update.status_code, is_(200))

    response_lookup2 = skyeng_client.find_user_by_name(user_student_one['userName'])
    post_update_user = response_lookup2.json()

    assert_that(post_update_user['lastName'], is_(new_last_name))


def test_invalid_update_user( skyeng_client):
    new_last_name = "ThisWon'tWork"

    lookup = skyeng_client.find_user_by_name(user_student_one['userName'])
    lookup_user = lookup.json()

    # Try to change the id in the user to an id that cannot exist in the database
    update_user = dict(lookup_user, **{"lastName": new_last_name, "id": -1})

    response_update = skyeng_client.update_user(update_user)
    assert_that(response_update.status_code, is_(404))

    lookup2 = skyeng_client.find_user_by_name(user_student_one['userName'])
    lookup2_user = lookup2.json()

    assert_that(lookup2_user['userName'], is_(user_student_one['userName']))


def test_delete_user_by_username(skyeng_client):
    response = skyeng_client.delete_user_by_username(user_admin['userName'])

    assert_that(response.status_code, is_(200))
