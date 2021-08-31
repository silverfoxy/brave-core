// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

import 'chrome://resources/mojo/mojo/public/js/mojo_bindings_lite.js'
import 'chrome://resources/mojo/url/mojom/url.mojom-lite.js'
import 'chrome://resources/mojo/mojo/public/mojom/base/time.mojom-lite.js';
import 'chrome://resources/mojo/brave/components/brave_today/common/brave_news.mojom-lite.js'

import {addSingletonGetter} from 'chrome://resources/js/cr.m.js'

const braveNewsControllerInstance = braveNews.mojom.BraveNewsController.getRemote();

export default braveNewsControllerInstance
