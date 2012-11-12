// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.httpd.rpc.dashboard;

import com.google.common.base.Strings;
import com.google.gerrit.httpd.RestApiServlet;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.OutputFormat;
import com.google.gerrit.server.dashboard.ListDashboards;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class ListDashboardsServlet extends RestApiServlet {
  private static final long serialVersionUID = 1L;
  private static final String PROJECT_LEVEL_PREFIX = "project/";
  private final ParameterParser paramParser;
  private final Provider<ListDashboards> factory;

  @Inject
  ListDashboardsServlet(final Provider<CurrentUser> currentUser,
      ParameterParser paramParser, Provider<ListDashboards> ls) {
    super(currentUser);
    this.paramParser = paramParser;
    this.factory = ls;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    ListDashboards impl = factory.get();
    if (!Strings.isNullOrEmpty(req.getPathInfo())) {
      final String path = URLDecoder.decode(req.getPathInfo(), "UTF-8");
      if (path.startsWith(PROJECT_LEVEL_PREFIX)) {
        impl.setLevel(ListDashboards.Level.PROJECT);
        impl.setEntityName(path.substring(PROJECT_LEVEL_PREFIX.length()));
      }
    }
    if (acceptsJson(req)) {
      impl.setFormat(OutputFormat.JSON_COMPACT);
    }
    if (paramParser.parse(impl, req, res)) {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      if (impl.getFormat().isJson()) {
        res.setContentType(JSON_TYPE);
        buf.write(JSON_MAGIC);
      } else {
        res.setContentType("text/plain");
      }
      impl.display(buf);
      res.setCharacterEncoding("UTF-8");
      send(req, res, buf.toByteArray());
    }
  }
}
