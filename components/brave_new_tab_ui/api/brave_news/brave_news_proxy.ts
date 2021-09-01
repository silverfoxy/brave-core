// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

import 'gen/mojo/public/js/mojo_bindings_lite.js'
import 'gen/mojo/public/mojom/base/time.mojom-lite.js'
import 'gen/url/mojom/url.mojom-lite.js'
import 'gen/brave/components/brave_today/common/brave_news.mojom-lite.js'

// declare type Feed = {

// }

// declare type Publisher = {

// }

// declare interface BraveNewsController {
//   getFeed: () => Feed
//   getPublishers: () => Publisher[]
// }

const braveNewsControllerInstance =
    braveNews.mojom.BraveNewsController.getRemote();

export default braveNewsControllerInstance
