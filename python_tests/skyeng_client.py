import json
import requests

from urllib.parse import urljoin
from urllib.parse import urlparse
from urllib.parse import parse_qs
from urllib.parse import urlsplit


class SkyengClient(object):
    def __init__(self, url='http://localhost:8080'):
        self.index_url = url
        self.headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

        self.authHeader = None
        self.session = requests.Session()

    def url(self, path):
        return urljoin(self.index_url, path)

    def make_request(self, path, ob=None, method='GET', headers={}, **kw):
        hs = dict(dict(self.headers, **headers), **{"Authorization": self.authHeader})
        u = self.url(path)
        response = None
        if ob is None:
            response = self.session.request(method, u, headers=hs, **kw)
        else:
            response = self.session.request(method, u, headers=hs, data=json.dumps(ob), **kw)
        if "Authorization" in response.headers:
            self.authHeader = response.headers['Authorization']
        return response

    """ USER ENDPOINT TEST"""

    def signup_user(self, user):
        """
        Signs up a new user
        """
        return self.make_request('/users', user, 'POST')

    def login_user(self, user):
        """
        Logs in a new user
        """
        return self.make_request('/users/login', user, 'POST')

    def update_user(self, user):
        """
        Updates a user, returning the updated user response
        :param user:
        :return:
        """
        return self.make_request("/users/{0}".format(user['userName']), user, 'PUT')

    def list_users(self, **kwargs):
        """
        Returns a list of users
        :return:
        """
        return self.make_request("/users", params=kwargs)

    def find_user_by_name(self, userName):
        """
        Get user by userName
        """
        return self.make_request("/users/{0}".format(userName))

    def delete_user_by_username(self, userName):
        """
        Delete user by userName
        """
        return self.make_request("/users/{0}".format(userName), method='DELETE')

    def create_teacher(self, id):
        return self.make_request("/teachers/assign/{0}".format(id), method="POST")

    """ TeacherProfile """

    # Try put method
    def search_teacher(self, id):
        return self.make_request("/teachers/update/{0}".format(id), method="POST")

    def list_teachers(self, **kwargs):
        return self.make_request("/teachers", params=kwargs)

    def delete_teacher(self, id):
        return self.make_request("/teachers/{0}".format(id), method="DELETE")

    """ STUDENT PROFILE ENDPOINTS """

    def set_student(self, student):
        return self.make_request("/students/", student, method="POST")

    def search_student(self, id):
        return self.make_request("/students/{0}".format(id))

    def delete_student_profile(self, id):
        return self.make_request("/students/{0}".format(id), method="DELETE")
