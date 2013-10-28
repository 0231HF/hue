#!/usr/bin/env python
# Licensed to Cloudera, Inc. under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  Cloudera, Inc. licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from djangosaml2.views import login, echo_attributes, metadata,\
							  assertion_consumer_service, logout_service

import libsaml.conf


__all__ = ['login', 'echo_attributes', 'assertion_consumer_service', 'metadata']


def acs(request, config_loader_path=None, attribute_mapping=None, create_unknown_user=None):
  username_source = libsaml.conf.USERNAME_SOURCE.get().lower()
  return assertion_consumer_service(request, config_loader_path, attribute_mapping, create_unknown_user, username_source)


setattr(logout_service, 'login_notrequired', True)
setattr(login, 'login_notrequired', True)
setattr(echo_attributes, 'login_notrequired', True)
setattr(acs, 'login_notrequired', True)
setattr(metadata, 'login_notrequired', True)
