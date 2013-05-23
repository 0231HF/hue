## Licensed to Cloudera, Inc. under one
## or more contributor license agreements.  See the NOTICE file
## distributed with this work for additional information
## regarding copyright ownership.  Cloudera, Inc. licenses this file
## to you under the Apache License, Version 2.0 (the
## "License"); you may not use this file except in compliance
## with the License.  You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.

<%!
  from desktop.views import commonheader, commonfooter
  from django.utils.translation import ugettext as _
%>

<%namespace name="layout" file="layout.mako" />
<%namespace name="macros" file="macros.mako" />
<%namespace name="utils" file="utils.inc.mako" />


${ commonheader(_('Search'), "search", user) | n,unicode }

<%def name="indexProperty(key)">
  %if key in solr_collection["status"][hue_collection.name]["index"]:
      ${ solr_collection["status"][hue_collection.name]["index"][key] }
    %endif
</%def>

<%def name="collectionProperty(key)">
  %if key in solr_collection["status"][hue_collection.name]:
      ${ solr_collection["status"][hue_collection.name][key] }
    %endif
</%def>

<%layout:skeleton>
  <%def name="title()">
    <h1>${_('Customize ')} ${ hue_collection.label }</h1>
  </%def>

  <%def name="navigation()">
    ${ layout.sidebar(hue_collection.name, 'properties') }
  </%def>

  <%def name="content()">
  <form method="POST">
    <ul class="nav nav-tabs">
      <li class="active">
        <a href="#index" data-toggle="tab">${_('Collection')}</a>
      </li>
      <li>
        <a href="#schema" data-toggle="tab">${_('Schema')}</a>
      </li>
      <li>
        <a href="#properties" data-toggle="tab">${_('Cores')}</a>
      </li>
    </ul>
    <div class="tab-content">
      <div class="tab-pane active" id="index">
        <div class="fieldWrapper">
          ${ utils.render_field(collection_form['enabled']) }
          ${ utils.render_field(collection_form['name']) }
          ${ utils.render_field(collection_form['label']) }
        </div>

	    <div class="form-actions">
	      <button type="submit" class="btn btn-primary" id="save-sorting">${_('Save')}</button>
	    </div>
      </div>

      <div class="tab-pane" id="schema">
        ${_('Loading...')} <img src="/static/art/spinner.gif">
      </div>

      <div class="tab-pane" id="properties">
        ${_('Loading...')} <img src="/static/art/spinner.gif">
      </div>
    </div>
  </form>
  </%def>

</%layout:skeleton>

<script type="text/javascript" charset="utf-8">
  $(document).ready(function(){
    $.get("${ url('search:admin_collection_schema', collection=hue_collection.name) }", function(data) {
        $("#schema").html(data.content); // Need to scroll to refresh
    });
    $.get("${ url('search:admin_collection_solr_properties', collection=hue_collection.name) }", function(data) {
        $("#properties").html(data.content);
    });
 });
</script>

${ commonfooter(messages) | n,unicode }
